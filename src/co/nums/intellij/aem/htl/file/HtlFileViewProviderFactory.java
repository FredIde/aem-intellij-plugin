package co.nums.intellij.aem.htl.file;

import co.nums.intellij.aem.htl.HtlLanguage;
import com.intellij.lang.Language;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.FileViewProviderFactory;
import com.intellij.psi.PsiManager;
import org.jetbrains.annotations.NotNull;

public class HtlFileViewProviderFactory implements FileViewProviderFactory {

	@NotNull
	@Override
	public FileViewProvider createFileViewProvider(@NotNull VirtualFile virtualFile, Language language,
			@NotNull PsiManager psiManager, boolean eventSystemEnabled) {
		if (!language.isKindOf(HtlLanguage.INSTANCE)) {
			throw new AssertionError();
		}
		return new HtlFileViewProvider(psiManager, virtualFile, eventSystemEnabled, language);
	}

}
