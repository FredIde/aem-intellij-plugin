package co.nums.intellij.aem.htl.psi

import co.nums.intellij.aem.htl.data.blocks.BlockIdentifierType
import co.nums.intellij.aem.htl.data.blocks.HtlBlockVariable
import co.nums.intellij.aem.htl.psi.search.HtlJavaSearch
import co.nums.intellij.aem.htl.psi.search.HtlSearch
import com.intellij.lang.StdLanguages
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase
import java.util.*

class HtlVariableReference(htlVariable: HtlVariable) : PsiReferenceBase<HtlVariable>(htlVariable) {

    override fun resolve(): PsiElement? {
        val variableName = element.identifier.text
        return HtlJavaSearch.getInstance()?.globalObjectJavaClass(element.project, variableName)
                ?: blockVariableAttribute(variableName)
    }

    private fun blockVariableAttribute(variableName: String?): PsiElement? {
        val htmlFile = element.containingFile.viewProvider.getPsi(StdLanguages.HTML)
        return HtlSearch.blockVariables(htmlFile)
                .filter { it.identifier.equals(variableName, ignoreCase = true) }
                .filter { it.hasMatchingScope() }
                .sortedWith(BestMatchingToElementComparator(element))
                .map { it.definer }
                .firstOrNull()
    }

    private fun HtlBlockVariable.hasMatchingScope() = when (identifierType) {
        BlockIdentifierType.BLOCK_VARIABLE -> coversElement()
        BlockIdentifierType.HOISTED_VARIABLE -> isDeclaredBeforeElement()
        BlockIdentifierType.TEMPLATE_NAME -> true
        else -> false
    }

    private fun HtlBlockVariable.coversElement() = definer.parent.textRange.contains(element.textRange)

    private fun HtlBlockVariable.isDeclaredBeforeElement() = definer.textOffset < element.textOffset

    override fun getVariants() = emptyArray<Any>()

}

private class BestMatchingToElementComparator(val element: HtlVariable) : Comparator<HtlBlockVariable> {

    override fun compare(o1: HtlBlockVariable, o2: HtlBlockVariable): Int {
        val o1Priority = o1.getPriority()
        val o2Priority = o2.getPriority()
        if (o1Priority == o2Priority) {
            val o1DistanceFromElement = element.textOffset - o1.definer.textOffset
            val o2DistanceFromElement = element.textOffset - o2.definer.textOffset
            return o1DistanceFromElement - o2DistanceFromElement
        }
        return o1Priority - o2Priority
    }

    private fun HtlBlockVariable.getPriority() = when (identifierType) {
        BlockIdentifierType.BLOCK_VARIABLE -> 0
        BlockIdentifierType.HOISTED_VARIABLE -> 1
        BlockIdentifierType.TEMPLATE_NAME -> 2
        else -> 3
    }

}
