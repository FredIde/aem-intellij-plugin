package co.nums.intellij.aem.htl.highlighter

import co.nums.intellij.aem.htl.HtlBlocks
import co.nums.intellij.aem.htl.psi.extensions.isHtl
import co.nums.intellij.aem.htl.psi.extensions.isHtlVariableDeclaration
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.psi.PsiElement
import com.intellij.psi.xml.XmlAttribute

class HtlBlocksHighlighter : Annotator {

    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        if (element.containingFile.isHtl() && element is XmlAttribute && element.name.toLowerCase().startsWith(HtlBlocks.PREFIX)) {
            highlightHtlBlock(holder, element)
        }
    }

    private fun highlightHtlBlock(holder: AnnotationHolder, element: XmlAttribute) {
        val blockStart = element.textOffset
        val blockName = element.localName
        val dotOffset = blockName.indexOf('.')
        val blockTypeEnd = if (dotOffset != -1) (blockStart + dotOffset) else (blockStart + blockName.length)
        holder.highlightText(blockStart, blockTypeEnd, HtlHighlighterColors.BLOCK_TYPE)
        if (element.isHtlVariableDeclaration()) {
            val variableIdentifier = blockName.substringAfter('.', missingDelimiterValue = "")
            if (variableIdentifier.isNotEmpty()) {
                val blockNameEnd = blockStart + blockName.length
                holder.highlightText(blockStart + dotOffset + 1, blockNameEnd, HtlHighlighterColors.BLOCK_VARIABLE)
            }
        }
    }

}
