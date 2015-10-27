package cn.csu.plusin.extractmethodp.util;

import java.util.List;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.Modifier.ModifierKeyword;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

public class AstUtil {

	public static SingleVariableDeclaration newSingleVariableDeclaration(
			AST ast, String name, Type type) {
		SingleVariableDeclaration param = ast.newSingleVariableDeclaration();
		param.setName(ast.newSimpleName(name));
		param.setType((Type) ASTNode.copySubtree(ast, type));
		return param;
	}

	public static FieldDeclaration newFieldDeclaration(AST ast, String name,
			Type type, ModifierKeyword modifierKeyword) {
		VariableDeclarationFragment vdf = ast

		.newVariableDeclarationFragment();
		vdf.setName(ast.newSimpleName(name));
		FieldDeclaration newField = ast.newFieldDeclaration(vdf);
		newField.setType((Type) ASTNode.copySubtree(ast, type));
		newField.modifiers().add(ast.newModifier(modifierKeyword));
		return newField;
	}

	public static MethodDeclaration newSetter(AST ast, String var, Type type) {
		MethodDeclaration setMd = ast.newMethodDeclaration();
		String varName = var.toUpperCase().charAt(0) + var.substring(1);
		setMd.setName(ast.newSimpleName("set" + varName));
		setMd.modifiers().add(ast.newModifier(ModifierKeyword.PUBLIC_KEYWORD));

		setMd.setReturnType2(ast.newPrimitiveType(PrimitiveType.VOID));
		SingleVariableDeclaration temp = ast.newSingleVariableDeclaration();
		temp.setName(ast.newSimpleName(var));
		temp.setType((Type) ASTNode.copySubtree(ast, type));
		setMd.parameters().add(temp);
		setMd.setBody(ast.newBlock());

		Assignment a = ast.newAssignment();
		FieldAccess fa = ast.newFieldAccess();
		fa.setName(ast.newSimpleName(var));
		fa.setExpression(ast.newThisExpression());
		a.setLeftHandSide(fa);
		a.setRightHandSide(ast.newSimpleName(var));
		ExpressionStatement es = ast.newExpressionStatement(a);
		setMd.getBody().statements().add(es);
		return setMd;
	}

	public static MethodDeclaration newGetter(AST ast, String var, Type type) {
		MethodDeclaration getMd = ast.newMethodDeclaration();
		String varName = var.toUpperCase().charAt(0) + var.substring(1);
		getMd.setName(ast.newSimpleName("get" + varName));
		getMd.modifiers().add(ast.newModifier(ModifierKeyword.PUBLIC_KEYWORD));
		getMd.setReturnType2((Type) ASTNode.copySubtree(ast, type));
		ReturnStatement rs2 = ast.newReturnStatement();
		rs2.setExpression(ast.newSimpleName(var));
		getMd.setBody(ast.newBlock());
		getMd.getBody().statements().add(rs2);
		return getMd;
	}

	public static MethodInvocation newMethodInvocation(AST ast,
			String methodName, String instanceName, List arguments) {
		MethodInvocation mi = ast.newMethodInvocation();
		mi.setName(ast.newSimpleName(methodName));
		if (instanceName != null)
			mi.setExpression(ast.newSimpleName(instanceName));
		if (arguments != null) {
			mi.arguments().addAll(arguments);
		}

		return mi;
	}

	public static VariableDeclarationStatement newVariableDeclarationStatement(
			AST ast, String varName, Type type,Expression initializer) {
		VariableDeclarationFragment vdf = ast.newVariableDeclarationFragment();
		VariableDeclarationStatement vds = ast
				.newVariableDeclarationStatement(vdf);
		vdf.setName(ast.newSimpleName(varName));

		vdf.setInitializer(initializer);
		vds.setType((Type) ASTNode.copySubtree(ast, type));
		return vds;

	}

	public static VariableDeclarationStatement newVariableDeclarationStatement(
			AST ast, String varName, Type type) {
		VariableDeclarationFragment vdf = ast.newVariableDeclarationFragment();
		VariableDeclarationStatement vds = ast
				.newVariableDeclarationStatement(vdf);
		vdf.setName(ast.newSimpleName(varName));
		vds.setType((Type) ASTNode.copySubtree(ast, type));
				return vds;
	}

	public static ExpressionStatement newAssignmentStatment(AST ast,Expression leftH,Expression rightH ){
		Assignment a = ast.newAssignment();
		a = ast.newAssignment();
		a.setLeftHandSide(leftH);
		a.setRightHandSide(rightH);
		return ast.newExpressionStatement(a);
	}
}