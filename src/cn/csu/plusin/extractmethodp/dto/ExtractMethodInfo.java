package cn.csu.plusin.extractmethodp.dto;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier.ModifierKeyword;
import org.eclipse.jdt.core.dom.Type;

public class ExtractMethodInfo {
	private String path;
	private int start;
	private int end;
	private String newMethodName;
	private String Modifier = "public";
	private Type returnType;
	private ICompilationUnit icu;
	private List parameters;
	private List<String> MethodInvocationarguments = new ArrayList<String>();
	private MethodDeclaration newMethodDeclaration;
	private NewClassInfo nICInfo,nOCInfo;
	
	public ExtractMethodInfo(){
		nICInfo = new NewClassInfo() ;
		nOCInfo = new NewClassInfo();
	}
	
	public ICompilationUnit getIcu() {
		return icu;
	}
	public void setIcu(ICompilationUnit icu) {
		this.icu = icu; 
	}
	public List getParameters() {
		return parameters;
	}
	public void setParameters(List parameters) {
		this.parameters = parameters;
	}
	public List<String> getMethodInvocationarguments() {
		return MethodInvocationarguments;
	}
	public void addMethodInvocationarguments(String name){
		MethodInvocationarguments.add(name);
	}
	
	public void setMethodInvocationarguments(List<String> methodInvocationarguments) {
		MethodInvocationarguments = methodInvocationarguments;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public int getStart() {
		return start;
	}
	public void setStart(int start) {
		this.start = start;
	}
	public int getEnd() {
		return end;
	}
	public void setEnd(int end) {
		this.end = end;
	}
	public String getNewMethodName() {
		return newMethodName;
	}
	public void setNewMethodName(String newMethodName) {
		this.newMethodName = newMethodName;
	}
	public String getModifier() {
		return Modifier;
	}
	public ModifierKeyword getModifierKeyWords(){
		switch(Modifier){
		case "public" : return ModifierKeyword.PUBLIC_KEYWORD;
		case "protected" : return ModifierKeyword.PROTECTED_KEYWORD;
		case "private" : return ModifierKeyword.PRIVATE_KEYWORD;
		case "default" : return null;
		}
		return null;
		
	}
	public void setModifier(String modifier) {
		Modifier = modifier;
	}
	public void setICompilationUnit(ICompilationUnit icu) {
		this.icu = icu;
		
	}
	
	public ICompilationUnit getICompilationUnit(){
		return icu;
	}
	public MethodDeclaration getNewMethodDeclaration() {
		return newMethodDeclaration;
	}
	public void setNewMethodDeclaration(MethodDeclaration newMethodDeclaration) {
		this.newMethodDeclaration = newMethodDeclaration;
	}
	public Type getReturnType() {
		return returnType;
	}
	public void setReturnType(Type returnType) {
		this.returnType = returnType;
	}


	public NewClassInfo getnICInfo() {
		return nICInfo;
	}

	public void setnICInfo(NewClassInfo nICInfo) {
		this.nICInfo = nICInfo;
	}

	public NewClassInfo getnOCInfo() {
		return nOCInfo;
	}

	public void setnOCInfo(NewClassInfo nOCInfo) {
		this.nOCInfo = nOCInfo;
	}
	
	
}
