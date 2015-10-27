package cn.csu.plusin.extractmethodp.util;


import java.util.HashMap; 
import java.util.HashSet; 
import java.util.Map; 
import java.util.Set; 
  


import org.eclipse.jdt.core.dom.AST; 
import org.eclipse.jdt.core.dom.ASTNode; 
import org.eclipse.jdt.core.dom.ASTVisitor; 
import org.eclipse.jdt.core.dom.Assignment; 
import org.eclipse.jdt.core.dom.Block; 
import org.eclipse.jdt.core.dom.CompilationUnit; 
import org.eclipse.jdt.core.dom.FieldAccess; 
import org.eclipse.jdt.core.dom.FieldDeclaration; 
import org.eclipse.jdt.core.dom.MethodDeclaration; 
import org.eclipse.jdt.core.dom.Modifier.ModifierKeyword; 
import org.eclipse.jdt.core.dom.SimpleName; 
import org.eclipse.jdt.core.dom.SingleVariableDeclaration; 
import org.eclipse.jdt.core.dom.Statement; 
import org.eclipse.jdt.core.dom.Type; 
import org.eclipse.jdt.core.dom.TypeDeclaration; 
import org.eclipse.jdt.core.dom.VariableDeclarationFragment; 
import org.eclipse.jdt.core.dom.VariableDeclarationStatement; 
  


import cn.csu.plusin.extractmethodp.util.*; 

  
public class ExtractUtil extends ASTVisitor { 
      
    private CompilationUnit comp; 
    private int start; 
    private int end; 
    private String newMethodName; 
      
    //用来存储属性数据
    private Map<String, Type> fieldSet = new HashMap<String, Type>(); 
    //用来存储方法参数变量数据
    private Map<String, Type> paramSet = new HashMap<String, Type>(); 
    //用来存储变量数据
    private Map<String, Type> varSet = new HashMap<String, Type>(); 
      
    private Set<String> pieceVarSet = new HashSet<String>(); 
      
    private AST ast; 
    private MethodDeclaration newMethod; 
      
    public ExtractUtil(String path, int start, int end, String newMethodName) throws Exception { 
        super(); 
          
        this.comp = JdtAstUtil.getCompilationUnit(path); 
        this.start =start; 
        this.end = end; 
        this.newMethodName = newMethodName; 
          
        this.comp.accept(this); 
    } 
  
    @Override
    public boolean visit(TypeDeclaration node) { 
        MethodDeclaration method = null; 
          
        //find the method 
        for (int i = 0, size = node.getMethods().length; i < size; i++) { 
            method = node.getMethods()[i]; 
              
            //if this method is between start and end 
            if (comp.getLineNumber( 
                    method.getStartPosition()) < start) { 
                //not the last one 
                if (i + 1 < size) { 
                    if (comp.getLineNumber( 
                                node.getMethods()[i + 1].getStartPosition()) > end) { 
                        break; 
                    } 
                } 
            } 
        } 
          
        //get information of fields 
        for (FieldDeclaration field: node.getFields()) { 
            for (Object o: field.fragments()) { 
                VariableDeclarationFragment var = (VariableDeclarationFragment)o; 
                String name = var.getName().getIdentifier(); 
                  
                fieldSet.put(name, field.getType()); 
            } 
        } 
          
        //get information of parameters 
        for (Object o: method.parameters()) { 
            SingleVariableDeclaration var = (SingleVariableDeclaration)o; 
            String name = var.getName().getIdentifier(); 
              
            paramSet.put(name, var.getType()); 
        } 
          
        //new method 
        ast = node.getRoot().getAST(); 
        newMethod = ast.newMethodDeclaration(); 
        newMethod.modifiers().add( 
                ast.newModifier( 
                        ModifierKeyword.PRIVATE_KEYWORD)); 
        newMethod.setName( 
                ast.newSimpleName(newMethodName)); 
          
        //new method body 
        //TODO 
        Block block = ast.newBlock(); 
        for (Object o: method.getBody().statements()) { 
            Statement statement = (Statement)o; 
              
            int line = comp.getLineNumber(statement.getStartPosition()); 
              
            if (line < start) { 
                //get variables' information declared in the PRE part 
                statement.accept(new PreVisitor()); 
            } 
            else if (line >= start && line <= end) { 
                //set variables as parameters of new method  
                //which are parameters of original method or variables declared in the PRE part  
                statement.accept(new PiecePreVisitor()); 
                block.statements().add( 
                        ASTNode.copySubtree(ast, statement)); 
            } 
            else if (line > end) { 
                ; 
            } 
        } 
          
        newMethod.setBody(block); 
          
        //TODO 
        System.out.println(newMethod.toString()); 
          
        return false; 
    } 
      
    //variables declared in PRE part 
    class PreVisitor extends ASTVisitor { 
          
        @Override
        public boolean visit(VariableDeclarationStatement node) { 
            for (Object o: node.fragments()) { 
                VariableDeclarationFragment var = (VariableDeclarationFragment)o; 
                String name = var.getName().getIdentifier(); 
                  
                varSet.put(name, node.getType()); 
            } 
              
            return false; 
        } 
          
    } 
      
    //variables used in PIECE part and come from PRE part 
    class PiecePreVisitor extends ASTVisitor { 
          
        @Override
        public boolean visit(SimpleName node) { 
            ASTNode parent = node.getParent(); 
              
            if (InstanceOfUtil.isVariableDeclarationFragment(parent)) { 
                //TODO 
                return true; 
            } 
            if (InstanceOfUtil.isFieldAccess(parent)) { 
                FieldAccess fieldAccess = (FieldAccess)parent; 
                if (InstanceOfUtil.isThisExpression(fieldAccess.getExpression())) { 
                    return true; 
                } 
            } 
              
            String name = node.getIdentifier(); 
              
            //ensure that the parameter is not added 
            for (Object o:newMethod.parameters()) { 
                SingleVariableDeclaration param = (SingleVariableDeclaration)o; 
                if (param.getName().toString().equals(name)) { 
                    return false; 
                } 
            } 
              
            if (paramSet.containsKey(name)) { 
                addParam(name, paramSet.get(name)); 
            } 
            else if (varSet.containsKey(name)) { 
                addParam(name, varSet.get(name)); 
            } 
              
            return false; 
        } 
          
        private void addParam(String name, Type type) { 
            SingleVariableDeclaration param = ast.newSingleVariableDeclaration(); 
            param.setName(ast.newSimpleName(name)); 
            param.setType( 
                    (Type) ASTNode.copySubtree(ast, type)); 
            newMethod.parameters().add(param); 
        } 
          
    } 
      
    //variables modified in PIECE part 
    class PiecePostVisitor extends ASTVisitor { 
          
        @Override
        public boolean visit(VariableDeclarationStatement node) { 
            for (Object o: node.fragments()) { 
                VariableDeclarationFragment var = (VariableDeclarationFragment)o; 
                String name = var.getName().getIdentifier(); 
                  
                varSet.put(name, node.getType()); 
                pieceVarSet.add(name); 
            } 
              
            return false; 
        } 
  
        @Override
        public boolean visit(Assignment node) { 
            //TODO 
            if (InstanceOfUtil.isName(node.getLeftHandSide())) { 
                  
            } 
              
            return false; 
        } 
          
    } 
}