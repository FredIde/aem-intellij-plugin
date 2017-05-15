package co.nums.intellij.aem.htl.highlighter

import co.nums.intellij.aem.htl.psi.HtlVariable
import co.nums.intellij.aem.htl.service.HtlDefinitions
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.psi.PsiElement

class HtlVariablesHighlighter : Annotator {

    private val globalVariableNames = HtlDefinitions.globalObjects.map { it.name }

    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        if (element is HtlVariable) {
            when {
                element.isGlobalObject() -> holder.highlightText(element, HtlHighlighterColors.GLOBAL_OBJECT)
                element.isBlockVariable() -> holder.highlightText(element, HtlHighlighterColors.BLOCK_VARIABLE)
                else -> holder.createReferenceErrorAnnotation(element, "Unresolved reference: ${element.text}")
            }
        }
    }

    private fun HtlVariable.isGlobalObject() = globalVariableNames.contains(text)

    private fun HtlVariable.isBlockVariable() = reference?.resolve() != null

}
