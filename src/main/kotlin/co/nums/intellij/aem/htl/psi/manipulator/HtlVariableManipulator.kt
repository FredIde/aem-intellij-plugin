package co.nums.intellij.aem.htl.psi.manipulator

import co.nums.intellij.aem.htl.psi.HtlVariable
import com.intellij.openapi.util.TextRange
import com.intellij.psi.AbstractElementManipulator
import com.intellij.psi.PsiNamedElement

class HtlVariableManipulator : AbstractElementManipulator<HtlVariable>() {

    override fun handleContentChange(element: HtlVariable, range: TextRange, newContent: String?): HtlVariable {
        if (element is PsiNamedElement && newContent != null && newContent.isNotEmpty()) {
            element.setName(newContent)
        }
        return element
    }

}
