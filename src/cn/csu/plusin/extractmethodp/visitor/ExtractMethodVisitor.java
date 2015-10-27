package cn.csu.plusin.extractmethodp.visitor;

import java.util.List;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.rewrite.*;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.LineComment;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.Modifier.ModifierKeyword;
import org.eclipse.jdt.internal.corext.refactoring.typeconstraints.typesets.ArraySuperTypeSet;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.TextEdit;

import cn.csu.plusin.extractmethodp.dto.ExtractMethodInfo;
import cn.csu.plusin.extractmethodp.util.AstUtil;
import cn.csu.plusin.extractmethodp.util.InstanceOfUtil;
import cn.csu.plusin.extractmethodp.util.JdtAstUtil;

public class ExtractMethodVisitor extends ASTVisitor {

	private CompilationUnit cu;
	private int start;
	private int end;
	private String source, nICInstName, nOCInstName;
	private MethodDeclaration methodToEdit;

	private ExtractMethodInfo emi;
	// private Boolean isNewInClass = false,isNewOutClass=false;

	// CASet: Variable Set of CA CA: Class Attributes
	private Map<String, Type> cASet = new HashMap<String, Type>();
	// MPSet: Variable Set of MP MP: Method Parameters
	private Map<String, Type> mPSet = new HashMap<String, Type>();
	// PrSDefSet: Variables declared in PrS PrS: Pre-Segment
	private Map<String, Type> prSDefSet = new HashMap<String, Type>();
	// ESDefSet: Variables declared in ES ES: Extraction Segment
	private Map<String, Type> eSDefSet = new HashMap<String, Type>();
	// ESDefSet: Variables declared in ES
	private Map<String, Type> eSUseSet = new HashMap<String, Type>();
	// PoSUseSet: Variables used in PoS PoS: Post-Segment
	private Map<String, Type> poSUseSet = new HashMap<String, Type>();
	// ESUseSet ∩ (CASet∪MPSet ∪PrSDefSet) = InSet
	private Map<String, Type> inSet = new HashMap<String, Type>();
	// PoSUseSet ∩ ESDefSet = OutSet
	private Map<String, Type> outSet = new HashMap<String, Type>();
	// EM: Extract Method
	private MethodDeclaration eM;
	// nIClass : New Input Class nOClass: new output Class
	private TypeDeclaration typeToEdit, nIClass, nOClass;

	private Set<Statement> statementToRemove = new HashSet<Statement>();
	private List<Statement> statementToAdd = new ArrayList<Statement>();
	private Map<String, Type> newClassField;
	private AST ast;
	private ASTRewrite rewriter;

	private Statement preStatement;
	private ListRewrite methodListRewrite, statementListRewrite,
			eMStatementRewrite;
	private Block blockToAdd, blockToEdit;

	public ExtractMethodVisitor(ExtractMethodInfo emi) throws Exception {
		super();

		this.cu = JdtAstUtil.getCompilationUnit(emi.getICompilationUnit());
		source = emi.getICompilationUnit().getBuffer().getContents();
		// document = new Document(source);
		this.start = emi.getStart();
		this.end = emi.getEnd();

		this.emi = emi;
		// newClassField = emi.getNci().getNewClassField();
		ast = cu.getAST();
		emi.setNewMethodDeclaration(ast.newMethodDeclaration());
		eM = emi.getNewMethodDeclaration();
		nICInstName = emi.getnICInfo().getInstanceName();
		nICInstName = "inClass";
		nOCInstName = emi.getnOCInfo().getInstanceName();
		nOCInstName = "outClass";
		blockToAdd = ast.newBlock();

		doVisitMethod();
	}

	public boolean visit(TypeDeclaration node) {
		boolean isRightMethod = false;
		MethodDeclaration method = null;
		// find the method
		for (int i = 0, size = node.getMethods().length; i < size; i++) {

			method = node.getMethods()[i];

			// if this method is between start and end
			int methodStartLine = cu.getLineNumber(method.getStartPosition());
			int endLine = cu.getLineNumber(node.getMethods()[i]
					.getStartPosition() + node.getMethods()[i].getLength());
			if (methodStartLine < start) {
				if (end <= endLine) {
					methodToEdit = method;
					typeToEdit = node;
					isRightMethod = true;
					break;
				}
			}
		}
		if (isRightMethod) {
			// get information of fields
			for (FieldDeclaration field : node.getFields()) {
				for (Object o : field.fragments()) {
					VariableDeclarationFragment var = (VariableDeclarationFragment) o;
					String name = var.getName().getIdentifier();

					cASet.put(name, field.getType());
				}
			}

			// get information of parameters
			for (Object o : method.parameters()) {
				SingleVariableDeclaration var = (SingleVariableDeclaration) o;
				String name = var.getName().getIdentifier();

				mPSet.put(name, (Type) ASTNode.copySubtree(ast, var.getType()));
			}

			method.accept(new BlockVisitor());
		}

		return true;
	}

	class BlockVisitor extends ASTVisitor {

		public boolean visit(Block block) {
			boolean visited = false;
			for (Object o : block.statements()) {
				Statement statement = (Statement) o;

				int line = cu.getLineNumber(statement.getStartPosition());

				if (line < start) {
					// get variables' information declared in the PRE part
					statement.accept(new PreVisitor());
					visited = true;
					preStatement = statement;
					statement.accept(new BlockVisitor());
				} else if (line >= start && line <= end) {
					// set variables as parameters of new method
					// which are parameters of original method or variables
					// declared
					// in the PRE part
					if (!visited) {
						preStatement = null;
					}
					blockToEdit = block;
					statement.accept(new PiecePreVisitor());
					blockToAdd.statements().add(
							ASTNode.copySubtree(ast, statement));
					statementToRemove.add(statement);

				} else if (line > end) {

					statement.accept(new PostVisitor());

				}
			}

			return false;

		}
	}

	// variables declared in PRE part
	class PreVisitor extends ASTVisitor {

		@Override
		public boolean visit(VariableDeclarationStatement node) {
			for (Object o : node.fragments()) {
				VariableDeclarationFragment var = (VariableDeclarationFragment) o;
				String name = var.getName().getIdentifier();

				prSDefSet.put(name, node.getType());
			}

			return false;
		}
		
		public boolean visit(VariableDeclarationExpression node) {
			for (Object o : node.fragments()) {
				VariableDeclarationFragment var = (VariableDeclarationFragment) o;
				String name = var.getName().getIdentifier();

				prSDefSet.put(name, node.getType());
			}

			return false;
		}

	}

	// variables used in PIECE part and come from PRE part
	class PiecePreVisitor extends ASTVisitor {

		@Override
		public boolean visit(SimpleName node) {
			ASTNode parent = node.getParent();
			String name = node.getIdentifier();
			if (!eSUseSet.containsKey(name)) {
				if (InstanceOfUtil.isVariableDeclarationFragment(parent)) {
					VariableDeclarationFragment newVDF = null;
					Type type = null;
					if (InstanceOfUtil.isVariableDeclarationExpression(parent
							.getParent())) {
						VariableDeclarationExpression VDS = (VariableDeclarationExpression) parent
								.getParent();
						newVDF = (VariableDeclarationFragment) parent;
						type = VDS.getType();
					} else if (InstanceOfUtil
							.isVariableDeclarationStatement(parent.getParent())) {
						VariableDeclarationStatement VDS = (VariableDeclarationStatement) parent
								.getParent();
						newVDF = (VariableDeclarationFragment) parent;
						type = VDS.getType();

					}
					if (!eSDefSet.containsKey(newVDF.getName().getIdentifier())) {

						eSDefSet.put(newVDF.getName().getIdentifier(),
								(Type) ASTNode.copySubtree(ast, type));
					}

					return true;
				}
				if (InstanceOfUtil.isFieldAccess(parent)) {
					FieldAccess fieldAccess = (FieldAccess) parent;
					if (InstanceOfUtil.isThisExpression(fieldAccess
							.getExpression())) {
						return true;
					}
				}

				if (InstanceOfUtil.isSimpleType(parent)) {
					return true;
				}

				if (InstanceOfUtil.isMethodInvocation(parent)) {
					MethodInvocation mi = (MethodInvocation) parent;
					if (mi.getName() == node)
						return true;
				}

				eSUseSet.put(name, null);
			}

			return false;

		}

	}

	class PostVisitor extends ASTVisitor {
		public boolean visit(SimpleName node) {
			ASTNode parent = node.getParent();
			String name = node.getIdentifier();
			if (!poSUseSet.containsKey(name)) {
				if (InstanceOfUtil.isVariableDeclarationFragment(parent)) {

					return true;
				}
				if (InstanceOfUtil.isFieldAccess(parent)) {
					FieldAccess fieldAccess = (FieldAccess) parent;
					if (InstanceOfUtil.isThisExpression(fieldAccess
							.getExpression())) {
						return true;
					}
				}

				if (InstanceOfUtil.isSimpleType(parent)) {
					return true;
				}

				if (InstanceOfUtil.isMethodInvocation(parent)) {
					MethodInvocation mi = (MethodInvocation) parent;
					if (mi.getName() == node)
						return true;
				}

				poSUseSet.put(name, null);
			}

			return false;
		}
	}

	class postVarReplaceVisitor extends ASTVisitor {
		public boolean visit(SimpleName node) {
			ASTNode parent = node.getParent();

			if (InstanceOfUtil.isVariableDeclarationFragment(parent)) {

				return true;
			}
			if (InstanceOfUtil.isFieldAccess(parent)) {
				FieldAccess fieldAccess = (FieldAccess) parent;
				if (InstanceOfUtil
						.isThisExpression(fieldAccess.getExpression())) {
					return true;
				}
			}

			String name = node.getIdentifier();

			if (inSet.containsKey(name)) {
				MethodInvocation mi = AstUtil.newMethodInvocation(ast, name,
						nICInstName, null);
				rewriter.replace(node, mi, null);
			}

			return false;
		}
	}

	class commentsVisitor extends ASTVisitor {
		public boolean visit(LineComment node) {
			System.out.println(node.toString());
			return false;

		}
	}

	private void getInSet() {
		HashSet<String> inSetTemp = new HashSet<String>();

		inSetTemp.addAll(cASet.keySet());
		inSetTemp.addAll(mPSet.keySet());
		inSetTemp.addAll(prSDefSet.keySet());
		inSetTemp.retainAll(eSUseSet.keySet());
		for (String name : inSetTemp) {
			if (prSDefSet.containsKey(name)) {
				inSet.put(name, prSDefSet.get(name));
			} else if (mPSet.containsKey(name)) {
				inSet.put(name, mPSet.get(name));
			} else if (cASet.containsKey(name)) {
				inSet.put(name, cASet.get(name));
			}
		}
		emi.getnICInfo().setNewClassField(inSet);

	}

	private void getOutSet() {
		Set<String> outSetTemp = new HashSet<String>();
		outSetTemp.addAll(eSDefSet.keySet());
		outSetTemp.addAll(eSUseSet.keySet());
		outSetTemp.retainAll(poSUseSet.keySet());
		for (String name : outSetTemp) {
			if (eSDefSet.containsKey(name)) {
				outSet.put(name, eSDefSet.get(name));
			} else if (inSet.containsKey(name)) {
				outSet.put(name, inSet.get(name));

			}

		}
		// Set<String> inSetTemp = inSet.keySet();
		// for (String name : inSetTemp) {
		// if (inSet.get(name).isPrimitiveType())
		// outSet.put(name, inSet.get(name));
		// }

	}

	public void doVisitMethod() {
		cu.accept(this);
		List cl = cu.getCommentList();
		cu.accept(new commentsVisitor());
		getInSet();
		getOutSet();
	}

	public TypeDeclaration doNewClass(String className, Map fieldSet) {

		TypeDeclaration newClass = ast.newTypeDeclaration();
		newClass.setName(ast.newSimpleName(className));

		// Add Fields to the Class

		Set<String> keySet = fieldSet.keySet();
		for (String s : keySet) {

			newClass.bodyDeclarations().add(
					AstUtil.newFieldDeclaration(ast, s, (Type) fieldSet.get(s),
							ModifierKeyword.PRIVATE_KEYWORD));
		}

		// Generators of Setter and Getter
		for (String s : keySet) {

			newClass.bodyDeclarations().add(
					AstUtil.newSetter(ast, s, (Type) fieldSet.get(s)));

			newClass.bodyDeclarations().add(
					AstUtil.newGetter(ast, s, (Type) fieldSet.get(s)));
			int i = 0;
		}
		return newClass;

	}

	public void doExtractMethod() {
		// new Rewriter to modify AST
		rewriter = ASTRewrite.create(ast);
		String ss = new String();
		// Type's Method Rewriter
		methodListRewrite = rewriter.getListRewrite(typeToEdit,
				typeToEdit.getBodyDeclarationsProperty());
		// Method's Statement Rewriter
		statementListRewrite = rewriter.getListRewrite(blockToEdit,
				blockToEdit.STATEMENTS_PROPERTY);

		Set<String> inKeySet = inSet.keySet();
		Set<String> outKeySet = outSet.keySet();
		//

		// if (inSet.size() != 0) {
		if (emi.getnICInfo().isNeedNewClass()) {
			nIClass = doNewClass(emi.getnICInfo().getClassName(), emi
					.getnICInfo().getNewClassField());
			methodListRewrite.insertLast(nIClass, null);
			// }
		}
		if (outSet.size() > 1) {
			nOClass = doNewClass("OutPutClass", outSet);
			methodListRewrite.insertLast(nOClass, null);
		}

		// methodToEdit.accept(new postVarReplaceVisitor());
		//

		// Remove Original Method's extract statements
		for (Object o : statementToRemove) {
			statementListRewrite.remove((Statement) o, null);
		}

		// Set newMethod's modifier and parameters
		if (emi.getModifierKeyWords() != null)
			eM.modifiers().add(ast.newModifier(emi.getModifierKeyWords()));
		eM.setName(ast.newSimpleName(emi.getNewMethodName()));

		// Set returnType of newMethod
		if (nOClass != null) {
			eM.setReturnType2(ast.newSimpleType(ast.newName(nOClass.getName()
					.getIdentifier())));
		} else if (outSet.size() == 1) {
			for (String s : outKeySet) {

				eM.setReturnType2((Type) ASTNode.copySubtree(ast, outSet.get(s)));
			}
		}
		// If nIClass not null ,EM's paramater will be nIClass,and
		// ClassIstanceCreation will add to Extract Position. While nIClass
		// is
		// null, parameters are inSet
		if (emi.getnICInfo().isNeedNewClass()) {

			SingleVariableDeclaration svd = AstUtil
					.newSingleVariableDeclaration(ast, nICInstName, ast
							.newSimpleType(ast.newName(nIClass.getName()
									.getIdentifier())));
			eM.parameters().add(svd);
			ClassInstanceCreation cic = ast.newClassInstanceCreation();
			cic.setType(ast.newSimpleType(ast.newName(nIClass.getName()
					.getIdentifier())));
			VariableDeclarationStatement vds = AstUtil
					.newVariableDeclarationStatement(ast, nICInstName,
							cic.getType(), cic);

			statementToAdd.add(vds);
			for (String s : inKeySet) {
				List arguments = new ArrayList();
				arguments.add(ast.newName(s));
				MethodInvocation mi = AstUtil.newMethodInvocation(ast, "set"
						+ s.toUpperCase().charAt(0) + s.substring(1),
						nICInstName, arguments);
				ast.newMethodInvocation();
				Statement newStatement = ast.newExpressionStatement(mi);

				statementToAdd.add(newStatement);

			}

		} else {

			Set<String> keySet = inSet.keySet();
			for (String s : keySet) {

				eM.parameters().add(
						AstUtil.newSingleVariableDeclaration(ast, s,
								inSet.get(s)));
			}
		}

		for (String s : outKeySet) {
			if (!inSet.containsKey(s))
				statementToAdd.add(AstUtil.newVariableDeclarationStatement(ast,
						s, outSet.get(s)));
		}

		// 生成提取方法调用语句
		List<SimpleName> argumentList = new ArrayList<SimpleName>();
		if (emi.getnICInfo().isNeedNewClass()) {
			argumentList.add(ast.newSimpleName(nICInstName));

		} else {
			for (String s : inKeySet) {
				argumentList.add(ast.newSimpleName(s));
			}
		}
		MethodInvocation newMethodInvocation = AstUtil.newMethodInvocation(ast,
				emi.getNewMethodName(), null, argumentList);

		Statement newStatement = null;

		// 判断需不需要增加输出类，如果要则将方法调用返回值传入返回类的实例
		if (nOClass == null) {

			if (outSet.size() == 1) {
				for (String s : outKeySet) {
					newStatement = AstUtil.newAssignmentStatment(ast,
							ast.newSimpleName(s), newMethodInvocation);
				}

			} else {
				newStatement = ast.newExpressionStatement(newMethodInvocation);
			}
		} else {
			newStatement = AstUtil.newVariableDeclarationStatement(ast,
					nOCInstName, ast.newSimpleType(ast.newName(nOClass
							.getName().getIdentifier())), newMethodInvocation);
		}
		statementToAdd.add(newStatement);

		if (outKeySet.size() == 1) {
			// for (String s : outKeySet) {
			// Assignment a = ast.newAssignment();
			// a = ast.newAssignment();
			// a.setLeftHandSide(ast.newSimpleName(s));
			// a.setRightHandSide(AstAssistance.newMethodInvocation(ast,
			// "get" + s.toUpperCase().charAt(0) + s.substring(1),
			// nOCInstName, null));
			// newStatement = ast.newExpressionStatement(a);
			// statementToAdd.add(newStatement);
			// }

		} else {
			for (String s : outKeySet) {
				Assignment a = ast.newAssignment();
				a = ast.newAssignment();
				a.setLeftHandSide(ast.newSimpleName(s));
				a.setRightHandSide(AstUtil.newMethodInvocation(ast, "get"
						+ s.toUpperCase().charAt(0) + s.substring(1),
						nOCInstName, null));
				newStatement = ast.newExpressionStatement(a);
				statementToAdd.add(newStatement);
			}
		}

		for (Statement s : statementToAdd) {
			if (preStatement != null) {
				statementListRewrite.insertAfter(s, preStatement, null);
				preStatement = s;
			} else
				statementListRewrite.insertFirst(s, null);
			preStatement = s;
		}

		// blockToAdd.accept(new ExtractPartVarReplaceVisitor());
		eM.setBody(blockToAdd);
		eMStatementRewrite = rewriter.getListRewrite(eM.getBody(),
				eM.getBody().STATEMENTS_PROPERTY);

		if (emi.getnICInfo().isNeedNewClass()) {
			for (String s : inKeySet) {
				VariableDeclarationStatement vds = AstUtil
						.newVariableDeclarationStatement(ast, s, inSet.get(s),
								AstUtil.newMethodInvocation(
										ast,
										"get" + s.toUpperCase().charAt(0)
												+ s.substring(1), nICInstName,
										null));

				eMStatementRewrite.insertFirst(vds, null);
			}

		}

		if (nOClass != null) {

			ClassInstanceCreation cic = ast.newClassInstanceCreation();
			cic.setType(ast.newSimpleType(ast.newName(nOClass.getName()
					.getIdentifier())));
			VariableDeclarationStatement vds = AstUtil
					.newVariableDeclarationStatement(ast, nOCInstName,
							cic.getType(), cic);
			eMStatementRewrite.insertFirst(vds, null);

			for (String s : outKeySet) {
				List arguments = new ArrayList();
				arguments.add(ast.newSimpleName(s));
				MethodInvocation mi = AstUtil.newMethodInvocation(ast, "set"
						+ s.toUpperCase().charAt(0) + s.substring(1),
						nOCInstName, arguments);
				ast.newMethodInvocation();
				newStatement = ast.newExpressionStatement(mi);
				eMStatementRewrite.insertLast(newStatement, null);
			}

			ReturnStatement rs = ast.newReturnStatement();
			rs.setExpression(ast.newSimpleName(nOCInstName));
			eMStatementRewrite.insertLast(rs, null);

		} else if (outKeySet.size() == 1) {
			ReturnStatement rs = ast.newReturnStatement();
			for (String s : outKeySet) {
				rs.setExpression(ast.newSimpleName(s));
			}

			eMStatementRewrite.insertLast(rs, null);
		}

		methodListRewrite.insertAfter(eM, methodToEdit, null);

		// Accept AST modify into File and ICompiliationUnit
		TextEdit edits;
		try {
			edits = rewriter.rewriteAST();
			Document document = new Document(emi.getICompilationUnit()
					.getSource());

			edits.apply(document);

			// this is the code for adding statements
			emi.getICompilationUnit().getBuffer().setContents(document.get());
		} catch (JavaModelException | IllegalArgumentException e1) {

			e1.printStackTrace();
		} catch (MalformedTreeException e) {

			e.printStackTrace();
		} catch (BadLocationException e) {

			e.printStackTrace();
		}
	}
}
