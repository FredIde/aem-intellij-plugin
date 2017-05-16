package co.nums.intellij.aem.htl.psi.mixins

import co.nums.intellij.aem.htl.file.HtlFileType
import co.nums.intellij.aem.htl.psi.HtlTypes
import co.nums.intellij.aem.htl.psi.HtlVariable
import co.nums.intellij.aem.htl.psi.HtlVariableReference
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.PsiNameIdentifierOwner
import com.intellij.psi.util.PsiTreeUtil

abstract class HtlVariableMixin(node: ASTNode) : ASTWrapperPsiElement(node), PsiNameIdentifierOwner {

    override fun getReference() = HtlVariableReference(this as HtlVariable)

    override fun setName(name: String): PsiElement? {
        createIdentifier(name)?.let {
            node.replaceChild(node.firstChildNode, it)
        }
        return this
    }

    private fun createIdentifier(name: String): ASTNode? {
        val dummyFileContent = "${'$'}{$name}"
        val dummyFile = PsiFileFactory.getInstance(project)
                .createFileFromText("dummy.html", HtlFileType, dummyFileContent)
        return PsiTreeUtil.findChildOfType(dummyFile, HtlVariable::class.java)
                ?.node
                ?.findChildByType(HtlTypes.IDENTIFIER)
    }

    override fun getName(): String? = nameIdentifier?.text

    override fun getNameIdentifier(): PsiElement? = findChildByType(HtlTypes.IDENTIFIER)

}
