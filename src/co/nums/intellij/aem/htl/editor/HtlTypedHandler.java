package co.nums.intellij.aem.htl.editor;

import co.nums.intellij.aem.htl.HtlLanguage;
import com.intellij.codeInsight.editorActions.TypedHandlerDelegate;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

public class HtlTypedHandler extends TypedHandlerDelegate {

	@Override
	public Result charTyped(char charTyped, Project project, @NotNull Editor editor, @NotNull PsiFile file) {
		if (isNotInHtlFile(file)) {
			return Result.CONTINUE;
		}

		int offset = editor.getCaretModel().getOffset();
		if (offset < 2 || offset > editor.getDocument().getTextLength()) {
			return Result.CONTINUE;
		}

		String previousChar = editor.getDocument().getText(new TextRange(offset - 2, offset - 1));
		if (charTyped == '{' && "$".equals(previousChar)) {
			editor.getDocument().insertString(offset, "}");
		}
		return Result.CONTINUE;
	}

	private boolean isNotInHtlFile(@NotNull PsiFile file) {
		FileViewProvider provider = file.getViewProvider();
		return !provider.getBaseLanguage().isKindOf(HtlLanguage.INSTANCE);
	}

}
