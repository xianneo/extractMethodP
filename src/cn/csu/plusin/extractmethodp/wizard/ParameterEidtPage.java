package cn.csu.plusin.extractmethodp.wizard;

import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ContainerSelectionDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Table;

import cn.csu.plusin.extractmethodp.dto.ExtractMethodInfo;



/**
 * The "New" wizard page allows setting the container for the new file as well
 * as the file name. The page will only accept file name without the extension
 * OR with the extension that matches the expected one (mpe).
 */

public class ParameterEidtPage extends WizardPage {

	private ISelection selection;
	private Table tableParameters;
	private ExtractMethodInfo emi;
	private Text txtInputclassname;
	private Button btnNewInputClass;

	/**
	 * Constructor for SampleNewWizardPage.
	 * 
	 * @param emi
	 * 
	 * @param pageName
	 */
	public ParameterEidtPage(ISelection selection, ExtractMethodInfo emi) {
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
		layout.numColumns = 2;
		container.setLayout(layout);
		layout.verticalSpacing = 9;
		Label label;
		initialize();
		
		setControl(container);

		btnNewInputClass = new Button(container, SWT.CHECK);
		btnNewInputClass.setText("New Input Class");
		btnNewInputClass.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				dialogChanged();
			}

			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		new Label(container, SWT.NONE);

		txtInputclassname = new Text(container, SWT.BORDER);
		txtInputclassname.setText("InputClassName");
		txtInputclassname.setLayoutData(new GridData(SWT.FILL, SWT.CENTER,
				true, false, 1, 1));
		txtInputclassname.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				dialogChanged();
			}
		});
		new Label(container, SWT.NONE);

		Group groupParameter = new Group(container, SWT.NONE);
		groupParameter.setText("Parameters");
		GridData gd_groupParameter = new GridData(SWT.LEFT, SWT.CENTER, false,
				false, 1, 1);
		gd_groupParameter.heightHint = 146;
		gd_groupParameter.widthHint = 452;
		groupParameter.setLayoutData(gd_groupParameter);

		ScrolledComposite scrolledComposite = new ScrolledComposite(
				groupParameter, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		scrolledComposite.setBounds(0, 10, 448, 139);
		scrolledComposite.setExpandHorizontal(true);
		scrolledComposite.setExpandVertical(true);

		tableParameters = new Table(scrolledComposite,  SWT.BORDER
				| SWT.FULL_SELECTION);

		tableParameters.setHeaderVisible(true);
		tableParameters.setLinesVisible(true);

		setTableParameterContext();

		scrolledComposite.setContent(tableParameters);
		scrolledComposite.setMinSize(tableParameters.computeSize(SWT.DEFAULT,
				SWT.DEFAULT));

		Group group_1 = new Group(container, SWT.NONE);
		GridData gd_group_1 = new GridData(SWT.LEFT, SWT.CENTER, false, false,
				1, 1);
		gd_group_1.heightHint = 147;
		gd_group_1.widthHint = 110;
		group_1.setLayoutData(gd_group_1);

		Button btnEidt = new Button(group_1, SWT.NONE);
		btnEidt.setBounds(20, 40, 72, 22);
		btnEidt.setText("Eidt");

		Button btnUp = new Button(group_1, SWT.NONE);
		btnUp.setBounds(20, 68, 72, 22);
		btnUp.setText("Up");

		Button btnDown = new Button(group_1, SWT.NONE);
		btnDown.setBounds(20, 96, 72, 22);
		btnDown.setText("Down");
		
		dialogChanged();
	}

	private void setTableParameterContext() {
		TableColumn tc1 = new TableColumn(tableParameters, SWT.CENTER);
		TableColumn tc2 = new TableColumn(tableParameters, SWT.CENTER);

		tc1.setText("Type");
		tc2.setText("Name");

		tc1.setWidth(170);
		tc2.setWidth(170);

		Map<String, Type> newClassField = emi.getnICInfo().getNewClassField();
		Set<String> keySet = newClassField.keySet();
		if(keySet.size()>3){
			btnNewInputClass.setSelection(true);
			emi.getnICInfo().setIsNeedNewClass(true);
		}
		for (String s : keySet) {
			TableItem item = new TableItem(tableParameters, SWT.NONE);
			item.setText(new String[] { newClassField.get(s).toString(),s });
		}

	}

	/**
	 * Tests if the current workbench selection is a suitable container to use.
	 */

	private void initialize() {
		// if (selection != null && selection.isEmpty() == false
		// && selection instanceof IStructuredSelection) {
		// IStructuredSelection ssel = (IStructuredSelection) selection;
		// if (ssel.size() > 1)
		// return;
		// Object obj = ssel.getFirstElement();
		// if (obj instanceof IResource) {
		// IContainer container;
		// if (obj instanceof IContainer)
		// container = (IContainer) obj;
		// else
		// container = ((IResource) obj).getParent();
		// // textMethodName.setText(container.getFullPath().toString());
		// }
		// }
	}

	/**
	 * Ensures that both text fields are set.
	 */

	private void dialogChanged() {
		// IResource container = ResourcesPlugin.getWorkspace().getRoot()
		// .findMember(new Path(getContainerName()));
		// String fileName = getFileName();

		
	
			emi.getnICInfo().setIsNeedNewClass(btnNewInputClass.getSelection());
			tableParameters.setEnabled(btnNewInputClass.getSelection());
			txtInputclassname.setEnabled(btnNewInputClass.getSelection());
		
		if (getInputClassName().length() == 0) {
			updateStatus("Inputclassname must be specified");
			return;
		} else {
			emi.getnICInfo().setClassName(txtInputclassname.getText());
		}
		
		

		// if (container == null
		// || (container.getType() & (IResource.PROJECT | IResource.FOLDER)) ==
		// 0) {
		// updateStatus("File container must exist");
		// return;
		// }
		// if (!container.isAccessible()) {
		// updateStatus("Project must be writable");
		// return;
		// }
		// if (fileName.length() == 0) {
		// updateStatus("File name must be specified");
		// return;
		// }
		// if (fileName.replace('\\', '/').indexOf('/', 1) > 0) {
		// updateStatus("File name must be valid");
		// return;
		// }
		// int dotLoc = fileName.lastIndexOf('.');
		// if (dotLoc != -1) {
		// String ext = fileName.substring(dotLoc + 1);
		// if (ext.equalsIgnoreCase("mpe") == false) {
		// updateStatus("File extension must be \"mpe\"");
		// return;
		// }
		// }
		updateStatus(null);
	}

	private void updateStatus(String message) {
		setErrorMessage(message);
		setPageComplete(message == null);
	}

	public String getInputClassName() {
		return txtInputclassname.getText();
	}

	public String getFileName() {
		return null;
	}
}
