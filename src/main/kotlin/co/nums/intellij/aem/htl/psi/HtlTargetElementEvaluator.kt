package co.nums.intellij.aem.htl.psi

import com.intellij.codeInsight.TargetElementEvaluator
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.impl.source.resolve.reference.impl.PsiMultiReference
import com.intellij.psi.impl.source.xml.XmlAttributeReference

class HtlTargetElementEvaluator : TargetElementEvaluator {

    override fun getElementByReference(reference: PsiReference, flags: Int): PsiElement? {
        if (reference is PsiMultiReference) {
            return reference.references
                    .find { it is XmlAttributeReference }
                    ?.element
        }
        return null
    }

    override fun includeSelfInGotoImplementation(element: PsiElement) = true

}
