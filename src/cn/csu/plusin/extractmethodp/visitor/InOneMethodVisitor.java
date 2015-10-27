package cn.csu.plusin.extractmethodp.visitor;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import cn.csu.plusin.extractmethodp.util.JdtAstUtil;
import cn.csu.plusin.extractmethodp.visitor.ExtractMethodVisitor.BlockVisitor;
import cn.csu.plusin.extractmethodp.visitor.ExtractMethodVisitor.PiecePreVisitor;
import cn.csu.plusin.extractmethodp.visitor.ExtractMethodVisitor.PostVisitor;
import cn.csu.plusin.extractmethodp.visitor.ExtractMethodVisitor.PreVisitor;

public class InOneMethodVisitor extends ASTVisitor {

	private CompilationUnit cu;
	private int start;
	private int end;
	private boolean isInOne = false;
	private MethodDeclaration method;

	public InOneMethodVisitor(ICompilationUnit cu, int start, int end) {
		this.cu = JdtAstUtil.getCompilationUnit(cu);
		this.start = start;
		this.end = end;
		this.cu.accept(this);

	}

	public boolean visit(TypeDeclaration node) {

		MethodDeclaration method = null;

		// find the method
		for (int i = 0, size = node.getMethods().length; i < size; i++) {

			method = node.getMethods()[i];

			// if this method is between start and end
			int startPosition = method.getStartPosition();
			int startLineNumber = cu.getLineNumber(startPosition);
			if (startLineNumber < start) {
				// store the methodDeclaration

				// not the last one
				int endLine = cu.getLineNumber(node.getMethods()[i]
						.getStartPosition() + node.getMethods()[i].getLength());
				if (end <= endLine) {

					this.method = method;
					break;
				}
			}
		}
		if (this.method != null)
			this.method.accept(new BlockVisitor());
		return true;
	}

	class BlockVisitor extends ASTVisitor {
		public boolean visit(Block block) {

			// for (Object o : block.statements()) {
			// Statement statement = (Statement) o;
			// boolean visited = false;
			//
			// int line = cu.getLineNumber(statement.getStartPosition());
			//
			// if (line < start) {
			//
			// statement.accept(new BlockVisitor());
			// } else if (line >= start && line <= end) {
			// isInOne = true;
			// } else if(line >= end){
			// isInOne = false;
			// }
			// }
			int blockStart, blockEnd;
			blockStart = cu.getLineNumber(block.getStartPosition());
			blockEnd = cu.getLineNumber(block.getStartPosition()
					+ block.getLength());
			if (blockStart < start) {
				if (blockEnd >= end) {
					isInOne = true;
				} else if (blockEnd < end) {
					isInOne = false;
					return false;
				}
			}
			return true;

		}
	}

	public boolean isInOne() {
		return isInOne;
	}

	public void setInOne(boolean isInOne) {
		this.isInOne = isInOne;
	}
}
