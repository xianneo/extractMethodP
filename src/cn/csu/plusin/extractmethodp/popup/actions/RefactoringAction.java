package cn.csu.plusin.extractmethodp.popup.actions;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import cn.csu.plusin.extractmethodp.util.JdtAstUtil;
import cn.csu.plusin.extractmethodp.visitor.InOneMethodVisitor;
import cn.csu.plusin.extractmethodp.wizard.RefactoringWizard;

public class RefactoringAction implements IEditorActionDelegate {
	private ISelection selection = null;
	private Shell shell;
	private CompilationUnit CU;

	public RefactoringAction() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void run(IAction action) {

		try {
			IWorkbenchWindow windoow = PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow();
			RefactoringWizard wizard = new RefactoringWizard();

			ITextSelection textSelection = getITextSelection();

			int startLine = textSelection.getStartLine() + 1;
			int endLine = textSelection.getEndLine() + 1;
			InOneMethodVisitor iomv = new InOneMethodVisitor(
					getICompilationUnit(), startLine, endLine);
			if (iomv.isInOne()) {
				wizard.init(windoow.getWorkbench(), getICompilationUnit(),
						getITextSelection());
				WizardDialog dialog = new WizardDialog(shell, wizard);
				dialog.open();
			} else {
				throw new Exception("Selected Code Block Illegal!");
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			alert(e);
			e.printStackTrace();
		}

	}

	private ITextSelection getITextSelection() {
		ISelection selection = getEditor().getEditorSite()
				.getSelectionProvider().getSelection();

		ITextSelection textSelection = (ITextSelection) selection;
		return textSelection;
	}

	public ICompilationUnit getICompilationUnit() {
		IEditorPart part = getEditor();
		if (part != null) {
			Object ifile = part.getEditorInput().getAdapter(IFile.class);

			return JavaCore.createCompilationUnitFrom((IFile) ifile);
		}
		return null;
	}

	public String getEditFilePath() {
		IProject project = null;

		// 1.根据当前编辑器获取工程
		IEditorPart part = getEditor();

		if (part != null) {
			Object object = part.getEditorInput().getAdapter(IFile.class);
			if (object != null) {
				project = ((IFile) object).getProject();
				return (project.getLocation() + "/" + ((IFile) object)
						.getProjectRelativePath().toOSString());
			}
		}
		return null;

	}

	public static IEditorPart getEditor() {
		return PlatformUI.getWorkbench().getActiveWorkbenchWindow()
				.getActivePage().getActiveEditor();
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setActiveEditor(IAction action, IEditorPart targetEditor) {
		// TODO Auto-generated method stub

	}

	public void getCU(String filePath) throws Exception {
		CU = JdtAstUtil.getCompilationUnit(filePath);
	}

	public void alert(Object content) {
		MessageDialog.openInformation(shell, "提示", content + "");
	}
}