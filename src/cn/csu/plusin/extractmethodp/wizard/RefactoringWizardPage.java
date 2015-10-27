package cn.csu.plusin.extractmethodp.wizard;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ContainerSelectionDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.jdt.core.dom.CompilationUnit;

import cn.csu.plusin.extractmethodp.dto.ExtractMethodInfo;



/**
 * The "New" wizard page allows setting the container for the new file as well
 * as the file name. The page will only accept file name without the extension
 * OR with the extension that matches the expected one (mpe).
 */

public class RefactoringWizardPage extends WizardPage {

	private ISelection selection;

	private Text textMethodName;
	private Text textMethodSignaturePreview;
	private ExtractMethodInfo emi;

	/**
	 * Constructor for SampleNewWizardPage.
	 * 
	 * @param pageName
	 */
	public RefactoringWizardPage(ISelection selection, ExtractMethodInfo emi) {
		super("wizardPage");
		setTitle("Extract Method Wizard");
		setDescription("This wizard extract a method from original method that has Lond Method code bad smell.");
		this.selection = selection;
		this.emi = emi;
	}

	/**
	 * @see IDialogPage#createControl(Composite)
	 */
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		container.setLayout(layout);
		layout.verticalSpacing = 9;
		Label label;
		

		Composite composite = new Composite(container, SWT.NONE);
		GridData gd_composite = new GridData(SWT.LEFT, SWT.CENTER, false,
				false, 1, 1);
		gd_composite.heightHint = 29;
		gd_composite.widthHint = 567;
		composite.setLayoutData(gd_composite);

		Label lblMethodName = new Label(composite, SWT.NONE);
		lblMethodName.setBounds(10, 10, 96, 15);
		lblMethodName.setText("Method Name");

		textMethodName = new Text(composite, SWT.BORDER);
		textMethodName.setBounds(112, 7, 445, 21);
		textMethodName.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				dialogChanged();
			}
		});

		Group grpAccessModifier = new Group(container, SWT.NONE);
		GridData gd_grpAccessModifier = new GridData(SWT.LEFT, SWT.CENTER,
				false, false, 1, 1);
		gd_grpAccessModifier.heightHint = 28;
		gd_grpAccessModifier.widthHint = 559;
		grpAccessModifier.setLayoutData(gd_grpAccessModifier);
		grpAccessModifier.setText("Access Modifier");

		final Button btnPublic = new Button(grpAccessModifier, SWT.RADIO);
		btnPublic.setSelection(true);
		btnPublic.setBounds(10, 20, 97, 16);
		btnPublic.setText("public");
		btnPublic.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				emi.setModifier(btnPublic.getText());
				System.out.println(emi.getModifier());
			}

		});

		final Button btnProteced = new Button(grpAccessModifier, SWT.RADIO);
		btnProteced.setBounds(113, 20, 97, 16);
		btnProteced.setText("protected");
		btnProteced.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				emi.setModifier(btnProteced.getText());

			}

		});
		final Button btnPrivate = new Button(grpAccessModifier, SWT.RADIO);
		btnPrivate.setBounds(216, 20, 97, 16);
		btnPrivate.setText("private");
		btnPrivate.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				emi.setModifier(btnPrivate.getText());

			}

		});
		final Button btnDefault = new Button(grpAccessModifier, SWT.RADIO);
		btnDefault.setBounds(319, 20, 97, 16);
		btnDefault.setText("default");
		btnDefault.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				emi.setModifier(btnDefault.getText());

			}

		});
		Button btnCheckButton = new Button(container, SWT.CHECK);
		GridData gd_btnCheckButton = new GridData(SWT.LEFT, SWT.CENTER, false,
				false, 1, 1);
		gd_btnCheckButton.widthHint = 476;
		btnCheckButton.setLayoutData(gd_btnCheckButton);
		btnCheckButton.setText("Declared thrown runtime exceptions");

		Button btnCheckButton_1 = new Button(container, SWT.CHECK);
		btnCheckButton_1.setText("Generate method comment");

		Button btnCheckButton_2 = new Button(container, SWT.CHECK);
		btnCheckButton_2.setEnabled(false);
		btnCheckButton_2
				.setText("Replace additional of occurences of with method ");

		Label label_1 = new Label(container, SWT.SEPARATOR | SWT.HORIZONTAL);

		Label lblMethodCodePreview = new Label(container, SWT.NONE);
		lblMethodCodePreview.setText("Method Signature Preview");

		ScrolledComposite scrolledComposite = new ScrolledComposite(container,
				SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		GridData gd_scrolledComposite = new GridData(SWT.LEFT, SWT.CENTER,
				false, false, 1, 1);
		gd_scrolledComposite.widthHint = 544;
		gd_scrolledComposite.heightHint = 37;
		scrolledComposite.setLayoutData(gd_scrolledComposite);
		scrolledComposite.setExpandHorizontal(true);
		scrolledComposite.setExpandVertical(true);

		textMethodSignaturePreview = new Text(scrolledComposite, SWT.BORDER);
		scrolledComposite.setContent(textMethodSignaturePreview);
		scrolledComposite.setMinSize(textMethodSignaturePreview.computeSize(
				SWT.DEFAULT, SWT.DEFAULT));
		initialize();
		dialogChanged();
		setControl(container);
		
	}

	/**
	 * Tests if the current workbench selection is a suitable container to use.
	 */

	private void initialize() {

	}

	/**
	 * Ensures that both text fields are set.
	 */

	private void dialogChanged() {
		if (textMethodName.getText() != "") {
			emi.setNewMethodName(textMethodName.getText());
			updateStatus(null);
		} else
			updateStatus("MehthodName Cannot Be Empty!!");
		
	}

	private void updateStatus(String message) {
		setErrorMessage(message);
		setPageComplete(message == null);
	}

	public String getContainerName() {

		return null;
	}

	public String getFileName() {
		return null;
	}

}