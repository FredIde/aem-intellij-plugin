package co.nums.intellij.aem.htl.psi

import co.nums.intellij.aem.htl.HtlBlocks
import co.nums.intellij.aem.htl.HtlLanguage
import com.intellij.lang.html.HTMLLanguage
import com.intellij.openapi.util.TextRange
import com.intellij.patterns.StandardPatterns.or
import com.intellij.patterns.StandardPatterns.string
import com.intellij.patterns.XmlPatterns.xmlAttribute
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceContributor
import com.intellij.psi.PsiReferenceProvider
import com.intellij.psi.PsiReferenceRegistrar
import com.intellij.psi.impl.source.xml.XmlAttributeImpl
import com.intellij.psi.impl.source.xml.XmlAttributeReference
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.xml.XmlAttribute
import com.intellij.util.ProcessingContext

class HtlVariablesReferenceContributor : PsiReferenceContributor() {

    override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
        val pattern = xmlAttribute().withLocalName(or(
                string().equalTo(HtlBlocks.LIST),
                string().equalTo(HtlBlocks.REPEAT),
                string().startsWith("${HtlBlocks.LIST}."),
                string().startsWith("${HtlBlocks.REPEAT}."),
                string().startsWith("${HtlBlocks.USE}."),
                string().startsWith("${HtlBlocks.TEST}."),
                string().startsWith("${HtlBlocks.TEMPLATE}.")))
        registrar.registerReferenceProvider(pattern, HtlPsiReferenceProvider())
    }

}

class HtlPsiReferenceProvider : PsiReferenceProvider() {

    override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<PsiReference> {
        val attribute = element as? XmlAttribute ?: return emptyArray()
        val variableReferences: MutableList<PsiReference> = htlVariablesDeclaredBy(attribute)
                .map(::HtlVariableReference)
                .toMutableList()
        variableReferences.add(HtlVariableBlockReference(element as XmlAttributeImpl))
        return variableReferences.toTypedArray()
    }

    fun htlVariablesDeclaredBy(attribute: XmlAttribute): Collection<HtlVariable> {
        val htlFile = attribute.containingFile.viewProvider.getPsi(HtlLanguage) ?: return emptyList()
        val variableIdentifier = attribute.getVariableIdentifier()
        val blockName = attribute.getBlockName()
        val htlVariables = htlVariables(htlFile, blockName, variableIdentifier)
        return when {
            HtlBlocks.isScopedVariableBlock(blockName) -> referencesInScope(attribute, variableIdentifier, htlVariables)
            HtlBlocks.isHoistedVariableBlock(blockName) -> referencesAfterDeclaration(attribute, variableIdentifier, htlVariables)
            HtlBlocks.isTemplateVariableBlock(blockName) -> emptyList()
            else -> throw IllegalStateException("Invalid block attribute: $blockName")
        }
    }

    private fun htlVariables(htlFile: PsiFile, blockName: String, variableIdentifier: String): List<HtlVariable> {
        val iterableBlock = HtlBlocks.isIterableBlock(blockName)
        return PsiTreeUtil.findChildrenOfType(htlFile, HtlVariable::class.java)
                .filter {
                    it.text.equals(variableIdentifier, ignoreCase = true)
                            || (iterableBlock && it.text.equals("${variableIdentifier}List", ignoreCase = true))
                }
    }

    private fun XmlAttribute.getBlockName() =
            if (localName.contains('.')) localName.substringBefore('.') else localName

    private fun XmlAttribute.getVariableIdentifier() =
            if (localName.contains('.')) localName.substringAfter('.') else "item"

    private fun referencesInScope(attribute: XmlAttribute, variableIdentifier: String, htlVariables: List<HtlVariable>): Collection<HtlVariable> {
        val scopeRange = attribute.parent.textRange
        val overridingScopes = PsiTreeUtil.findChildrenOfType(attribute.parent, XmlAttribute::class.java)
                .filter { it.getVariableIdentifier().equals(variableIdentifier, ignoreCase = true) }
                .filter { HtlBlocks.isScopedVariableBlock(it.getBlockName()) }
                .map { it.parent }
                .filter { it != attribute.parent }
                .map { it.textRange }
        return htlVariables
                .filter { scopeRange.contains(it.textOffset) }
                .filter { isNotOverridden(it, overridingScopes) }
    }

    private fun isNotOverridden(htlVariable: HtlVariable, redeclarationScopes: List<TextRange>) =
            redeclarationScopes.none { it.contains(htlVariable.textOffset) }

    private fun referencesAfterDeclaration(attribute: XmlAttribute, variableIdentifier: String, htlVariables: List<HtlVariable>): Collection<HtlVariable> {
        val overridingScopes = PsiTreeUtil.findChildrenOfType(attribute.containingFile.viewProvider.getPsi(HTMLLanguage.INSTANCE), XmlAttribute::class.java)
                .filter { it.getVariableIdentifier().equals(variableIdentifier, ignoreCase = true) }
                .filter { HtlBlocks.isScopedVariableBlock(it.getBlockName()) }
                .map { it.parent }
                .map { it.textRange }
        return htlVariables
                .filter { it.textOffset > attribute.textOffset }
                .filter { isNotOverridden(it, overridingScopes) }
    }

}

class HtlVariableBlockReference(val xmlAttribute: XmlAttributeImpl) : XmlAttributeReference(xmlAttribute) {

    override fun resolve() = xmlAttribute

}
