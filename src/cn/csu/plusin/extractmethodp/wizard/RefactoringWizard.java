package cn.csu.plusin.extractmethodp.wizard;

import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.operation.*;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.CompilationUnit;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;

import java.io.*;

import org.eclipse.ui.*;
import org.eclipse.ui.ide.IDE;

import cn.csu.plusin.extractmethodp.dto.ExtractMethodInfo;
import cn.csu.plusin.extractmethodp.visitor.ExtractMethodVisitor;



/**
 * This is a sample new wizard. Its role is to create a new file 
 * resource in the provided container. If the container resource
 * (a folder or a project) is selected in the workspace 
 * when the wizard is opened, it will accept it as the target
 * container. The wizard creates one file with the extension
 * "mpe". If a sample multi-page editor (also available
 * as a template) is registered for the same extension, it will
 * be able to open it.
 */

public class RefactoringWizard extends Wizard {
	private RefactoringWizardPage page;
	private ParameterEidtPage page2;
	private ISelection selection;
	private ExtractMethodInfo emi;
	private ExtractMethodVisitor emu;
	/**
	 * Constructor for SampleNewWizard.
	 * @throws Exception 
	 */
	public RefactoringWizard() throws Exception {
		super();
		setNeedsProgressMonitor(true);
		emi = new ExtractMethodInfo();
		
	}
	
	/**
	 * Adding the page to the wizard.
	 */

	public void addPages() {
		page = new RefactoringWizardPage(selection,emi);
		page2 = new ParameterEidtPage(selection,emi);
		addPage(page);
		addPage (page2);
	}

	/**
	 * This method is called when 'Finish' button is pressed in
	 * the wizard. We will create an operation and run it
	 * using wizard as execution context.
	 */
	public boolean performFinish() {
//		final String containerName = page.getContainerName();
//		final String fileName = page.getFileName();
//		IRunnableWithProgress op = new IRunnableWithProgress() {
//			public void run(IProgressMonitor monitor) throws InvocationTargetException {
//				try {
					try {
						doFinish();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
//				} catch (Exception e) {
//					throw new InvocationTargetException(e);
//				} finally {
//					monitor.done();
//				}
//			}
//
//			
//		};
//		try {
//			getContainer().run(true, false, op);
//		} catch (InterruptedException e) {
//			return false;
//		} catch (InvocationTargetException e) {
//			Throwable realException = e.getTargetException();
//			MessageDialog.openError(getShell(), "Error", realException.getMessage());
//			return false;
//		}
		return true;
	}
	
	/**
	 * The worker method. It will find the container, create the
	 * file if missing or just replace its contents, and open
	 * the editor on the newly created file.
	 * @throws Exception 
	 */
	
	private void doFinish() throws Exception {

		emu.doExtractMethod();
		
	
		
	}

	
	
	/**
	 * We will initialize file contents with a sample text.
	 */

	private InputStream openContentStream() {
		String contents =
			"This is the initial file contents for *.mpe file that should be word-sorted in the Preview page of the multi-page editor";
		return new ByteArrayInputStream(contents.getBytes());
	}

	private void throwCoreException(String message) throws CoreException {
		IStatus status =
			new Status(IStatus.ERROR, "RefactoringWizard", IStatus.OK, message, null);
		throw new CoreException(status);
	}

	/**
	 * We will accept the selection in the workbench to see if
	 * we can initialize from it.
	 * @see IWorkbenchWizard#init(IWorkbench, IStructuredSelection)
	 */
//	public void init(IWorkbench workbench,String path ,ITextSelection textSelection) {
//		this.selection = textSelection;
//		emi.setPath(path);
//		emi.setStart(textSelection.getStartLine() + 1);
//		emi.setEnd(textSelection.getEndLine() + 1);
//		
//	}
	
	public void init(IWorkbench workbench,ICompilationUnit icu ,ITextSelection textSelection) throws Exception {
		this.selection = textSelection;
		emi.setICompilationUnit(icu);
		emi.setStart(textSelection.getStartLine() + 1);
		emi.setEnd(textSelection.getEndLine() + 1);
		emu = new ExtractMethodVisitor(emi);
		
		
	}

	public void init(IWorkbench workbench, IStructuredSelection selection) {
		// TODO Auto-generated method stub
		
	}
	
	
}