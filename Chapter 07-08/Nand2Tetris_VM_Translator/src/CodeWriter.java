import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class CodeWriter {
	//Debugging?
	private static final boolean DEBUG = true;
	//Allows us to create a unique label in assembly by attaching a value to the name
	private int labelCounter;
	//Writes to ASM file
	private OutputStreamWriter writer;
	//name of the VM file (mostly used for symbols)
	private String fileName; 
	
	public static enum CommandType {
		C_ARITHMETIC, C_PUSH, C_POP, C_LABEL, C_GOTO, C_IF, C_FUNCTION, C_RETURN, C_CALL
	}

	public CodeWriter(FileOutputStream out, String fileName) {
		writer = new OutputStreamWriter(out);
		labelCounter = 0;
		this.fileName = fileName;
	}

	public void close(){
		try {
			writer.flush();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void writeToFile(String asm, String originalCommand){
		try {
			if(DEBUG){
				writer.write("//" + originalCommand + "\r\n");
			}
			writer.write(asm);
			writer.flush();
			
			//System.out.println("Writing: " + originalCommand);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/*
	 * add sub neg eq gt lt and or not
	 */
	public void writeArithmetic(String command) {
		String commandFormatted = command.toLowerCase();
		String asm;
		
		switch (commandFormatted) {
		case "add":
			asm = 
			"@SP \n" + 
			"AM=M-1 \n" + 
			"D=M \n" +
			"A=A-1 \n" +
			"M=D+M \n";
			
			writeToFile(asm, commandFormatted);
			return;
			
		case "sub":
			asm = 
			"@SP \n" +
			"AM=M-1 \n" + 
			"D=M \n" +
			"A=A-1 \n" +
			"M=M-D \n";
			
			writeToFile(asm, commandFormatted);
			return;
			
		case "neg":
			asm = 
			"@SP \n" +
			"A=M-1 \n" +
			"M=-M \n";
			
			writeToFile(asm, commandFormatted);
			return;
			
		case "eq":
			asm = 
			"@SP \n" +
			"AM=M-1 \n" +
			"D=M \n" +
			"A=A-1 \n" +
			"D=D-M \n" +
			"@EQUALS" + labelCounter + "\n" +
			"D;JEQ \n" +
			"@NOTEQUALS" + labelCounter + "\n" +
			"0;JMP \n" + 
			"(EQUALS"+ labelCounter + ")\n"+
			"@SP \n"+
			"A=M-1 \n" +
			"M=-1 \n" +
			"@BREAKOUT" + labelCounter + " \n"+
			"0;JMP \n" +
			"(NOTEQUALS" + labelCounter + ") \n" +
			"@SP \n" +
			"A=M-1 \n" +
			"M=0 \n" + 
			"(BREAKOUT" + labelCounter + ") \n";
			
			writeToFile(asm, commandFormatted);
			labelCounter++;
			return;
			
		case "gt":
			asm = 
			"@SP \n"+
			"AM=M-1 \n"+
			"D=M \n" +
			"A=A-1 \n" +
			"D=D-M \n"+
			"@GREATER" + labelCounter + "\n"+
			"D;JLT\n"+
			"@NOTGREATER" + labelCounter + "\n"+
			"0;JMP\n" +
			"(GREATER" + labelCounter + ")\n"+
			"@SP \n"+
			"A=M-1 \n"+
			"M=-1 \n"+
			"@BREAKOUT" + labelCounter + " \n" +
			"0;JMP \n" +
			"(NOTGREATER" + labelCounter + ")\n" +
			"@SP \n" +
			"A=M-1 \n" +
			"M=0 \n" +
			"(BREAKOUT" + labelCounter + ") \n";
			
			writeToFile(asm, commandFormatted);
			labelCounter++;
			return;
			
		case "lt":
			asm = 
			"@SP \n"+
			"AM=M-1 \n"+
			"D=M \n" +
			"A=A-1 \n" +
			"D=D-M \n"+
			"@LESSER" + labelCounter + "\n"+
			"D;JGT\n"+
			"@NOTLESSER" + labelCounter + "\n"+
			"0;JMP\n" +
			"(LESSER" + labelCounter + ")\n"+
			"@SP \n"+
			"A=M-1 \n"+
			"M=-1 \n" +
			"@BREAKOUT" + labelCounter + " \n" +
			"0;JMP \n" + 
			"(NOTLESSER" + labelCounter + ") \n" +
			"@SP \n" +
			"A=M-1 \n" +
			"M=0 \n" +
			"(BREAKOUT" + labelCounter + ") \n";
			
			writeToFile(asm, commandFormatted);
			labelCounter++;
			return;
			
		case "and":
			asm =
			"@SP \n" + 
			"AM=M-1 \n" + 
			"D=M \n" + 
			"A=A-1 \n" + 
			"M=D&M \n";
			
			writeToFile(asm,commandFormatted);
			return;
			
		case "or":
			asm =
			"@SP \n" + 
			"AM=M-1 \n" + 
			"D=M \n" + 
			"A=A-1 \n" + 
			"M=D|M \n";
			
			writeToFile(asm, commandFormatted);
			return;
		
		case "not":
			asm =
			"@SP \n" + 
			"A=M-1 \n" + 
			"M=!M \n";
			
			writeToFile(asm, commandFormatted);
			return;
		
		default:
			return;

		}

	}

	public void writePushPop(String command, String segment, int index) {
		String asm = "";
		String originalCommand = command + " " + segment + " " + index;
		
		if(CommandType.valueOf(command) == CommandType.C_POP){ 
			switch(segment){
			case "constant":
				System.out.println("Parse Error. Invalid command at: \n" + command + " " + segment + " " + index + "\n");
				System.exit(0);
				return;
			case "local":
				asm = 
				"@" + index + " \n" +
				"D=A \n" +
				"@LCL \n" +
				"D=D+M \n" +
				"@R13 \n" +
				"M=D \n" +
				"@SP \n" +
				"AM=M-1 \n" +
				"D=M \n" +
				"@R13 \n" +
				"A=M \n"+
				"M=D \n";
				
				writeToFile(asm, originalCommand);
				return;
			case "argument":
				asm = 
				"@" + index + " \n" +
				"D=A \n" +
				"@ARG \n" +
				"D=D+M \n" +
				"@R13 \n" +
				"M=D \n" +
				"@SP \n" +
				"AM=M-1 \n" +
				"D=M \n" +
				"@R13 \n" +
				"A=M \n"+
				"M=D \n";
				
				writeToFile(asm, originalCommand);
				return;
			case "this":
				asm = 
				"@" + index + " \n" +
				"D=A \n" +
				"@THIS \n" +
				"D=D+M \n" +
				"@R13 \n" +
				"M=D \n" +
				"@SP \n" +
				"AM=M-1 \n" +
				"D=M \n" +
				"@R13 \n" +
				"A=M \n"+
				"M=D \n";
				
				writeToFile(asm, originalCommand);
				return;
			case "that":
				asm = 
				"@" + index + " \n" +
				"D=A \n" +
				"@THAT \n" +
				"D=D+M \n" +
				"@R13 \n" +
				"M=D \n" +
				"@SP \n" +
				"AM=M-1 \n" +
				"D=M \n" +
				"@R13 \n" +
				"A=M \n"+
				"M=D \n";

				writeToFile(asm, originalCommand);
				return;
			case "pointer":
				asm = 
				"@" + index + " \n" +
				"D=A \n" +
				"@3 \n" +
				"D=D+A \n" +
				"@R13 \n" +
				"M=D \n" +
				"@SP \n" +
				"AM=M-1 \n" +
				"D=M \n" +
				"@R13 \n" +
				"A=M \n"+
				"M=D \n";
				
				writeToFile(asm, originalCommand);
				return;
			case "temp":
				asm = 
				"@" + index + " \n" +
				"D=A \n" +
				"@5 \n" +
				"D=D+A \n" +
				"@R13 \n" +
				"M=D \n" +
				"@SP \n" +
				"AM=M-1 \n" +
				"D=M \n" +
				"@R13 \n" +
				"A=M \n"+
				"M=D \n";
				
				writeToFile(asm, originalCommand);
				return;
			case "static":
				asm = 				
				"@SP \n" +
				"AM=M-1 \n" +
				"D=M \n" +
				"@" + fileName + "." + index + " \n" +
				"M=D \n";
				
				writeToFile(asm, originalCommand);
				return;
			default:
				return;
			
			}
			
		}else if(CommandType.valueOf(command) == CommandType.C_PUSH){
			switch(segment){
			case "constant":
				asm = 
				"@" + index + " \n" + 
				"D=A \n" +
				"@SP \n" +
				"A=M \n" +
				"M=D \n" + 
				"@SP \n" +
				"M=M+1 \n";
				
				writeToFile(asm, originalCommand);
				return;
			case "local":
				asm = 
				"@" + index + " \n" +
				"D=A \n" +
				"@LCL \n" +
				"A=D+M \n" +
				"D=M \n" +
				"@SP \n" +
				"A=M \n" +
				"M=D \n" +
				"@SP \n" +  
				"M=M+1 \n";
				
				writeToFile(asm, originalCommand);
				return;
			case "argument":
				asm = 
				"@" + index + " \n" +
				"D=A \n" +
				"@ARG \n" +
				"A=D+M \n" +
				"D=M \n" +
				"@SP \n" +
				"A=M \n" +
				"M=D \n" +
				"@SP \n" +  
				"M=M+1 \n";
				
				writeToFile(asm, originalCommand);
				return;
			case "this":
				asm = 
				"@" + index + " \n" +
				"D=A \n" +
				"@THIS \n" +
				"A=D+M \n" +
				"D=M \n" +
				"@SP \n" +
				"A=M \n" +
				"M=D \n" +
				"@SP \n" +  
				"M=M+1 \n";
				
				writeToFile(asm, originalCommand);
				return;
			case "that":
				asm = 
				"@" + index + " \n" +
				"D=A \n" +
				"@THAT \n" +
				"A=D+M \n" +
				"D=M \n" +
				"@SP \n" +
				"A=M \n" +
				"M=D \n" +
				"@SP \n" +  
				"M=M+1 \n";
				
				writeToFile(asm, originalCommand);
				return;
			case "pointer":
				asm = 
				"@" + index + " \n" +
				"D=A \n" +
				"@3 \n" +
				"A=D+A \n" +
				"D=M \n" +
				"@SP \n" +
				"A=M \n" +
				"M=D \n" +
				"@SP \n" +  
				"M=M+1 \n";
				
				writeToFile(asm, originalCommand);
				return;
			case "temp":
				asm = 
				"@" + index + " \n" +
				"D=A \n" +
				"@5 \n" +
				"A=D+A \n" +
				"D=M \n" +
				"@SP \n" +
				"A=M \n" +
				"M=D \n" +
				"@SP \n" +  
				"M=M+1 \n";
				
				writeToFile(asm, originalCommand);
				return;
			case "static":
				asm = 
				"@" + fileName + "." + index + " \n" + 
				"D=M \n" +
				"@SP \n" +
				"A=M \n" +
				"M=D \n" + 
				"@SP \n" +
				"M=M+1 \n";
				
				writeToFile(asm, originalCommand);
				return;
			default:
				return;
			}
		}
	}
}
