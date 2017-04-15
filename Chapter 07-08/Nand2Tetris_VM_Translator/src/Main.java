import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Main {

	public static void main(String[] args) {
		try {
			String VMFileName = args[0].substring(args[0].indexOf("/")+1);
			String VMPath = args[0].substring(0,args[0].lastIndexOf("/"));
			String asmFileName = VMFileName.substring(0, VMFileName.lastIndexOf(".")) + ".asm";

			Parser parser = new Parser(new FileInputStream(VMFileName));
			CodeWriter codeWriter = new CodeWriter(new FileOutputStream(new File(asmFileName)), VMPath + "/" + asmFileName);
			
			File[] files = new File(VMPath).listFiles(new FilenameFilter() {
				public boolean accept(File dir, String name) {
					return name.endsWith(".vm");
				}
			});
			
			if(files.length > 1){
				codeWriter.writeBootstrap();
			}else if(!Files.exists(Paths.get(VMPath + "/Sys.vm"))){
				System.out.println("Missing Sys.vm");
			}
			
			//codeWriter = new CodeWriter(new FileOutputStream(new File(asmFileName)), asmFileName);
			for(File f :files){
				codeWriter.setFileName(f.getName().substring(0, f.getName().indexOf(".")));
				parser = new Parser(new FileInputStream(VMPath + "/" + f.getName()));
				processCommands(parser, codeWriter);
			}
			

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	// C_ARITHMETIC, C_PUSH, C_POP, C_LABEL, C_GOTO, C_IF, C_FUNCTION, C_RETURN,
	// C_CALL, C_NULL
	private static void processCommands(Parser parser, CodeWriter codeWriter) {
		while (parser.hasMoreCommands()) {
			parser.advance();
			
			switch (parser.commandType()) {
			case C_ARITHMETIC:
				codeWriter.writeArithmetic(parser.arg1());
				break;
			case C_POP:
			case C_PUSH:
				codeWriter.writePushPop(parser.commandType().toString(), parser.arg1(), parser.arg2());
				break;
			case C_LABEL:
				codeWriter.writeLabel(parser.arg1());
				break;
			case C_GOTO:
				codeWriter.writeGoto(parser.arg1());
				break;
			case C_IF:
				codeWriter.writeIf(parser.arg1());
				break;
			case C_FUNCTION:
				codeWriter.writeFunction(parser.arg1(), parser.arg2());
				break;
			case C_CALL:
				codeWriter.writeCall(parser.arg1(), parser.arg2());
				break;
			case C_RETURN:
				codeWriter.writeReturn();
				break;
			default:
				return;
			}
			//System.out.println(parser.commandType().toString() + " " + parser.arg1().toString());
		}
	}

}
