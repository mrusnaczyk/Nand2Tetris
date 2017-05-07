import java.io.*;
//import java.nio.*;
//import java.nio.file.*;
import java.util.ArrayList;

import util.TokenType;

public class JackAnalyser {

	public static void main(String[] args) {
		String filePath = args[0];
		ArrayList<File> jackFiles = new ArrayList<File>();
		File inputFile = new File(filePath);

		if (inputFile.isDirectory()) {
			// Recursively look for .jack files
			File[] dirChildren = inputFile.listFiles();

			if (dirChildren.length > 0) {
				for (File f : dirChildren) {
					if (f.getName().endsWith(".jack"))
						jackFiles.add(f);
				}
			} else {
				System.out.println("This directory is empty!");
				System.exit(0);
			}

		} else if (inputFile.isFile()) {
			jackFiles.add(inputFile);
		}

		CompilationEngine compiler;

		for (File f : jackFiles) {
			try (JackTokenizer tokenizer = new JackTokenizer(f.toPath())) {
				
				String filename = f.getName().replaceAll("\\..*", "");
				String dir = f.getParent().toString();
				System.out.println(dir + "/" + filename);
				
				FileOutputStream fileOut = new FileOutputStream(dir + "/" + filename + "B.xml");
				FileOutputStream fileTokens = new FileOutputStream(dir + "/" + filename + "TokensB.xml");
				
				compiler = new CompilationEngine(tokenizer, fileOut, fileTokens);
				compiler.compileClass();
				
			} catch (Exception e) {
				e.printStackTrace();
			}

		}

	}
}
