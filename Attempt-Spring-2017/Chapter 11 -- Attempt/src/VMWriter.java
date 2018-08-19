import util.Segment;

import java.io.FileOutputStream;
import java.io.PrintWriter;

import util.Command;

public class VMWriter {
	PrintWriter writer;
	
	public VMWriter(FileOutputStream fileOut){
		writer = new PrintWriter(fileOut);
		return;
	}
	
	public void writePush(String arg, int segIndex){
		writer.println("push " + arg.toString() + " " + segIndex);
		return;
	}
	
	public void writePop(String seg, int index){
		writer.println("pop " + seg.toString() + " " + index);
		return;
	}
	
	public void writeArithmetic(String add){
		writer.println(add);
		return;
	}
	
	public void writeLabel(String label){
		writer.println("label " + label);
		return;
	}
	
	public void writeGoto(String label){
		writer.println("goto" + label);
		return;
	}
	
	public void writeIf(String label){
		writer.println("if-goto" + label);
		return;
	}
	
	public void writeCall(String label, int nArgs){
		writer.println("call " + label + " " + nArgs);
		return;
	}
	
	public void writeFunction(String label, int nArgs){
		writer.println("function " + label + " " + nArgs);
		return;
	}
	
	public void writeReturn(){
		writer.println("return");
		return;
	}
	
	public void close(){
		writer.flush();
		writer.close();
	}
	
}
