package co.nums.intellij.aem.htl.inspections

import co.nums.intellij.aem.htl.HtlBlocks
import co.nums.intellij.aem.htl.highlighter.createReferenceErrorAnnotation
import co.nums.intellij.aem.htl.psi.extensions.isHtl
import co.nums.intellij.aem.htl.psi.extensions.isHtlBlock
import co.nums.intellij.aem.htl.service.HtlDefinitions
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.psi.PsiElement
import com.intellij.psi.xml.XmlAttribute
import com.intellij.util.text.EditDistance

class HtlIncorrectBlockTypeAnnotator : Annotator {

    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        if (element.containingFile.isHtl() && element is XmlAttribute && element.name.toLowerCase().startsWith(HtlBlocks.PREFIX)) {
            if (!element.isHtlBlock()) {
                highlightUnknownHtlBlockError(holder, element)
            }
        }
    }

    private fun highlightUnknownHtlBlockError(holder: AnnotationHolder, element: XmlAttribute) {
        val blockType = element.localName.substringBefore('.')
        val suggestedBlockType = blockStartingWith(blockType) ?: closestLevenshteinDistance(blockType)
        val errorMessage = "Unknown HTL block: $blockType. Did you mean $suggestedBlockType?"
        holder.createReferenceErrorAnnotation(element.nameElement.textRange, errorMessage)
                .registerFix(HtlIncorrectBlockTypeFix(suggestedBlockType))
    }

    private fun blockStartingWith(blockType: String) = HtlDefinitions.blocks
            .map { it.type }
            .firstOrNull { it.startsWith(blockType) }

    private fun closestLevenshteinDistance(blockType: String) = HtlDefinitions.blocks
            .sortedBy { EditDistance.levenshtein(it.type, blockType, false) }
            .map { it.type }
            .first()

}
