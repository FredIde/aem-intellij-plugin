package co.nums.intellij.aem.htl.file

import co.nums.intellij.aem.htl.HtlLanguage
import co.nums.intellij.aem.htl.psi.HtlElementType
import co.nums.intellij.aem.htl.psi.HtlTypes
import com.intellij.lang.Language
import com.intellij.lang.LanguageParserDefinitions
import com.intellij.lang.ParserDefinition
import com.intellij.lang.PerFileMappingsBase
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.LanguageSubstitutors
import com.intellij.psi.MultiplePsiFilesPerDocumentFileViewProvider
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.psi.impl.source.PsiFileImpl
import com.intellij.psi.templateLanguages.ConfigurableTemplateLanguageFileViewProvider
import com.intellij.psi.templateLanguages.TemplateDataElementType
import com.intellij.psi.templateLanguages.TemplateDataLanguageMappings
import com.intellij.util.containers.ContainerUtil

class HtlFileViewProvider @JvmOverloads constructor(manager: PsiManager,
                                                    file: VirtualFile,
                                                    physical: Boolean,
                                                    private val myBaseLanguage: Language,
                                                    private val myTemplateDataLanguage: Language = getTemplateDataLanguage(manager, file))
    : MultiplePsiFilesPerDocumentFileViewProvider(manager, file, physical), ConfigurableTemplateLanguageFileViewProvider {

    private val TEMPLATE_DATA_TO_LANG = ContainerUtil.newConcurrentMap<String, TemplateDataElementType>()

    override fun createFile(language: Language): PsiFile? {
        val parserDefinition = getParserDefinition(language)
        val file = parserDefinition.createFile(this)
        when {
            language.`is`(templateDataLanguage) -> {
                (file as PsiFileImpl).contentElementType = getTemplateDataElementType(baseLanguage)
                return file
            }
            language.isKindOf(baseLanguage) -> return file
            else -> return null
        }
    }

    private fun getParserDefinition(language: Language): ParserDefinition {
        val parserDefinitionLanguage = if (language.isKindOf(baseLanguage)) baseLanguage else language
        return LanguageParserDefinitions.INSTANCE.forLanguage(parserDefinitionLanguage)
    }

    private fun getTemplateDataElementType(language: Language): TemplateDataElementType {
        TEMPLATE_DATA_TO_LANG[language.id]?.let {
            return it
        }
        val created = TemplateDataElementType("HTL_TEMPLATE_DATA", language, HtlTypes.OUTER_TEXT, HtlElementType("HTL_FRAGMENT"))
        val prevValue = TEMPLATE_DATA_TO_LANG.putIfAbsent(language.id, created)
        return prevValue ?: created
    }

    override fun getBaseLanguage() = myBaseLanguage

    override fun getTemplateDataLanguage() = myTemplateDataLanguage

    override fun getLanguages(): Set<Language> = setOf(baseLanguage, templateDataLanguage)

    override fun supportsIncrementalReparse(rootLanguage: Language) = false

    override fun cloneInner(virtualFile: VirtualFile) = HtlFileViewProvider(manager, virtualFile, false, baseLanguage, templateDataLanguage)

}

private fun getTemplateDataLanguage(manager: PsiManager, file: VirtualFile): Language {
    // PerFileMappingsBase<Language> to avoid NoSuchMethodError in IntelliJ IDEA 15
    val mappings: PerFileMappingsBase<Language> = TemplateDataLanguageMappings.getInstance(manager.project)
    val dataLanguage = mappings.getMapping(file) ?: HtlLanguage.getTemplateLanguageFileType().language
    val substituteLanguage = LanguageSubstitutors.INSTANCE.substituteLanguage(dataLanguage, file, manager.project)
    return if (TemplateDataLanguageMappings.getTemplateableLanguages().contains(substituteLanguage)) substituteLanguage else dataLanguage
}
