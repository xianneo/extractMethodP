package cn.csu.plusin.extractmethodp.dto;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;

public class NewClassInfo {
	private String className = "class",instanceName = "p";
	private TypeDeclaration newClass;
	private Map<String, Type> newClassField = new HashMap<String, Type>();
	private boolean isNeedNewClass = false;
	public TypeDeclaration getNewClass() {
		return newClass;
	}
	public void setNewClass(TypeDeclaration newClass) {
		this.newClass = newClass;
	}
	public Map<String, Type> getNewClassField() {
		return newClassField;
	}
	public void setNewClassField(
			Map<String, Type> newClassField) {
		this.newClassField = newClassField;
	}
	public String getInstanceName() {
		return instanceName;
	}
	public void setInstanceName(String instanceName) {
		this.instanceName = instanceName;
	
	}

	public boolean isNeedNewClass() {
		return isNeedNewClass;
	}
	public void setIsNeedNewClass(boolean isNeedNewClass) {
		this.isNeedNewClass = isNeedNewClass;
	}
	public String getClassName() {
		return className;
	}
	public void setClassName(String className) {
		this.className = className;
	}

}
