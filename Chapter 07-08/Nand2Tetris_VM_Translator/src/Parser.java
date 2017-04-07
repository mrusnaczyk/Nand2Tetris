import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;


public class Parser {
	private BufferedReader input;
	private ArrayList<String> vmFileCommands;
	private int currentElement;

	public static enum CommandType {
		C_ARITHMETIC, C_PUSH, C_POP, C_LABEL, C_GOTO, C_IF, C_FUNCTION, C_RETURN, C_CALL, C_NULL
	}

	public Parser(FileInputStream in) {
		vmFileCommands = new ArrayList<String>();
		input = new BufferedReader(new InputStreamReader(in));
		currentElement = -1;
		String next = "";

		try {
			while ((next = input.readLine()) != null) {
				String commandFormatted = next.replaceAll("//.*", "");
				if(!commandFormatted.equals(""))
					vmFileCommands.add(next.trim());
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public boolean hasMoreCommands() {
		return (currentElement < vmFileCommands.size() - 1);
	}

	public void advance() {
		if (hasMoreCommands())
			currentElement++;
	}

	public CommandType commandType() {
		String[] commandTokens = vmFileCommands.get(currentElement).split(" ");

		if(commandTokens[0].toLowerCase().matches("(add|sub|neg|eq|gt|lt|and|or|not).*"))
			return CommandType.C_ARITHMETIC;
		else if(commandTokens[0].equals("push"))
			return CommandType.C_PUSH;
		else if(commandTokens[0].equals("pop"))
			return CommandType.C_POP;
		return CommandType.C_NULL;
	}

	public String arg1() {
		String[] tokens = vmFileCommands.get(currentElement).split(" ");
		
		switch(commandType()){
		case C_ARITHMETIC:
			//For arithmetic commands, just return the command
			return tokens[0].trim();
		case C_POP:
			return tokens[1].trim();
		case C_PUSH:
			return tokens[1].trim();
		default:
			return null;
		}
	}

	public String arg2() {
		String[] tokens = vmFileCommands.get(currentElement).split(" ");
		
		switch(commandType()){
		case C_ARITHMETIC:
			return "";
		default:
			return tokens[2].trim();
		}
	}
}
