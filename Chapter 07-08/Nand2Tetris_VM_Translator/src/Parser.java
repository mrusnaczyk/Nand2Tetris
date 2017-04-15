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
				if (!commandFormatted.equals(""))
					vmFileCommands.add(commandFormatted.trim());
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
		
		System.out.println(commandType().toString() + " " + arg1() + " " + arg2());
	}

	public CommandType commandType() {
		String[] commandTokens = vmFileCommands.get(currentElement).split(" ");

		if (commandTokens[0].toLowerCase().matches("(add|sub|neg|eq|gt|lt|and|or|not).*"))
			return CommandType.C_ARITHMETIC;
		else if (commandTokens[0].equals("push"))
			return CommandType.C_PUSH;
		else if (commandTokens[0].equals("pop"))
			return CommandType.C_POP;
		else if (commandTokens[0].equals("goto"))
			return CommandType.C_GOTO;
		else if (commandTokens[0].equals("if-goto"))
			return CommandType.C_IF;
		else if (commandTokens[0].equals("label"))
			return CommandType.C_LABEL;
		else if (commandTokens[0].equals("function"))
			return CommandType.C_FUNCTION;
		else if (commandTokens[0].equals("return"))
			return CommandType.C_RETURN;
		else if (commandTokens[0].equals("call"))
			return CommandType.C_CALL;
		return CommandType.C_NULL;
	}

	public String arg1() {
		String[] tokens = vmFileCommands.get(currentElement).split(" ");

		switch (commandType()) {
		case C_ARITHMETIC:
		case C_RETURN:
			return tokens[0].trim();
		default:
			return tokens[1].trim();
		}
	}

	public int arg2() {
		String[] tokens = vmFileCommands.get(currentElement).split(" ");

		switch (commandType()) {
		case C_PUSH:
		case C_POP:
		case C_FUNCTION:
		case C_CALL:
			return Integer.parseInt(tokens[2].trim());
		default:
			return -1;
		}
	}
}
