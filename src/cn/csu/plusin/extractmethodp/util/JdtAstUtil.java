package cn.csu.plusin.extractmethodp.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

public class JdtAstUtil {
	private static ASTParser astParser = ASTParser.newParser(AST.JLS3);// 非常慢

	/**
	 * 获得java源文件的结构CompilationUnit
	 * 
	 * @param javaFilePath
	 *            java文件的绝对路径
	 * @return CompilationUnit
	 * @throws Exception
	 */
	public static CompilationUnit getCompilationUnit(String javaFilePath)
			throws Exception {

		BufferedInputStream bufferedInputStream = new BufferedInputStream(
				new FileInputStream(javaFilePath));

		byte[] input = new byte[bufferedInputStream.available()];
		bufferedInputStream.read(input);
		bufferedInputStream.close();
		astParser.setSource(new String(input).toCharArray());

		CompilationUnit result = (CompilationUnit) (astParser.createAST(null));// 很慢

		return result;
	}

	public static String getSource(String javaFilePath) throws Exception {

		BufferedInputStream bufferedInputStream = new BufferedInputStream(
				new FileInputStream(javaFilePath));

		byte[] input = new byte[bufferedInputStream.available()];
		bufferedInputStream.read(input);
		bufferedInputStream.close();
		return (new String(input)).trim();

		// FileReader fr = new FileReader(javaFilePath);
		// BufferedReader br = new BufferedReader(fr);
		// String s = "";
		// while (br.readLine() != null) {
		// s += br.readLine();
		// s +="\n";
		// }
		// br.close();
		// return s;

	}

	public static File getFile(String javaFilePat) {

		return new File(javaFilePat);

	}

	public static String getFileName(String javaFilePat) {

		return new File(javaFilePat).getName();

	}
	
	public static void getAstToFile(String path,CompilationUnit cu)throws IOException{
		
		
		
	         File file=new File(path);
	         if(!file.exists())
	             file.createNewFile();
	         FileOutputStream out=new FileOutputStream(file,true);        

	             
	             out.write(cu.toString().getBytes("utf-8"));
	               
	         out.close();
	     
	}
	
	public static CompilationUnit getCompilationUnit(ICompilationUnit icu){
		
		astParser.setSource(icu);

		CompilationUnit result = (CompilationUnit) (astParser.createAST(null));

		return result;
		
		
	}
}
