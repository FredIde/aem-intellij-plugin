package co.nums.intellij.aem.htl.highlighter

import co.nums.intellij.aem.htl.psi.HtlVariable
import co.nums.intellij.aem.htl.psi.extensions.isHtlVariableDeclaration
import com.intellij.codeInsight.highlighting.ReadWriteAccessDetector
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.impl.source.xml.XmlAttributeReference
import com.intellij.psi.xml.XmlAttribute

class HtlReadWriteAccessDetector : ReadWriteAccessDetector() {

    override fun isReadWriteAccessible(element: PsiElement) = element is HtlVariable || isHtlVariableDeclaration(element)

    override fun isDeclarationWriteAccess(element: PsiElement) = isHtlVariableDeclaration(element)

    private fun isHtlVariableDeclaration(element: PsiElement) = element is XmlAttribute && element.isHtlVariableDeclaration()

    override fun getReferenceAccess(referencedElement: PsiElement, reference: PsiReference) =
            if (reference is XmlAttributeReference) Access.Write else Access.Read

    override fun getExpressionAccess(expression: PsiElement) = Access.Read

}
