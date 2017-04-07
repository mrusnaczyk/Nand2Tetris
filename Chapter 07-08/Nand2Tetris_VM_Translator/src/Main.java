import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class Main {

	public static void main(String[] args) {
		try {
			String VMFileName = args[0];
			String asmFileName = VMFileName.substring(0, VMFileName.indexOf(".")) + ".asm";

			Parser parser = new Parser(new FileInputStream(VMFileName));
			CodeWriter codeWriter = new CodeWriter(new FileOutputStream(new File(asmFileName)), asmFileName);

			while (parser.hasMoreCommands()) {
				parser.advance();
				switch (parser.commandType()) {
				case C_ARITHMETIC:
					codeWriter.writeArithmetic(parser.arg1());
					break;
				case C_POP:
					codeWriter.writePushPop(parser.commandType().toString(), parser.arg1(), Integer.parseInt(parser.arg2()));
					break;
				case C_PUSH:
					codeWriter.writePushPop(parser.commandType().toString(), parser.arg1(), Integer.parseInt(parser.arg2()));
					break;
				default:
					return;
				}
			}
			
			System.out.println("Done");

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
