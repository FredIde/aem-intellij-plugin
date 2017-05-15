package co.nums.intellij.aem.htl.refactoring

import co.nums.intellij.aem.htl.psi.extensions.isHtlVariableDeclaration
import com.intellij.psi.PsiElement
import com.intellij.psi.codeStyle.SuggestedNameInfo
import com.intellij.psi.xml.XmlAttribute
import com.intellij.refactoring.rename.PreferrableNameSuggestionProvider

class HtlVariableNameSuggestionProvider : PreferrableNameSuggestionProvider() {

    override fun getSuggestedNames(element: PsiElement?, nameSuggestionContext: PsiElement?, result: MutableSet<String>?): SuggestedNameInfo? {
        if (element is XmlAttribute && element.isHtlVariableDeclaration()) {
            result?.clear()
            val currentName = element.localName.substringAfter('.', "")
            result?.add(currentName)
        }
        return SuggestedNameInfo.NULL_INFO
    }

    override fun shouldCheckOthers() = false

}
