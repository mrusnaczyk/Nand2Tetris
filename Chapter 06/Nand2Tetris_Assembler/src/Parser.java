import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Vector;

public class Parser {
    private String currentCommand;
    private Vector<String> assemblyFile;
    private int currentElement;
    private BufferedReader fileInput;

    public static enum CommandType {
	A_COMMAND, C_COMMAND, L_COMMAND, ERROR
    }

    public Parser(FileInputStream inStr) {
	fileInput = new BufferedReader(new InputStreamReader(inStr));
	currentCommand = null;
	currentElement = 0;
	assemblyFile = new Vector<String>();

	String next = "";

	try {
	    while ((next = fileInput.readLine()) != null) {
		String sanitizedCommand = "";
		for (char c : next.toCharArray()) {
		    if (c != '/')
			sanitizedCommand += c;
		    else
			break;
		}

		if (!sanitizedCommand.equals(""))
		    assemblyFile.add(sanitizedCommand.trim());
	    }
	} catch (IOException e) {
	    e.printStackTrace();
	}

//	for (String a : assemblyFile) {
//	    System.out.println(a);
//	}
    }

    public void restart() {
	currentElement = 0;
	currentCommand = null;
    }

    public boolean hasMoreCommands() {
	return (currentElement < assemblyFile.size());
    }

    public void advance() {
	currentCommand = assemblyFile.get(currentElement++);
    }

    public CommandType commandType() {
	if (currentCommand.contains("@"))
	    return CommandType.A_COMMAND;
	else if (currentCommand.contains("=") || currentCommand.contains(";"))
	    return CommandType.C_COMMAND;
	else if (currentCommand.contains("("))
	    return CommandType.L_COMMAND;

	return CommandType.ERROR;
    }

    /*
     * Returns the symbol without any additional syntax markings such as "@",
     * "(",or ")"
     */
    public String symbol() {
	if (commandType() == CommandType.A_COMMAND || commandType() == CommandType.L_COMMAND)
	    return currentCommand.replaceAll("[@()]", "");
	
	return "";
    }

    // Returns the destination bits of the C-Instruction
    public String dest() {
	// Only look for a destination if the instruction isn't a variant of jump
	if (commandType() == CommandType.C_COMMAND && currentCommand.contains("="))
	    return currentCommand.substring(0, currentCommand.indexOf("="));

	return "";
    }

    // Returns the type of computation being performed
    public String comp() {
	if (commandType() == CommandType.C_COMMAND) {
	    /*
	     * If the instruction isn't a variant of jump, we want everything
	     * follows '='. If it is instead a jump instruction, we want
	     * everything before the semicolon (;)
	     */
	    if (currentCommand.contains("="))
		return currentCommand.substring(currentCommand.indexOf("=") + 1);
	    else
		return currentCommand.substring(0, currentCommand.indexOf(";"));
	}

	return "";
    }

    public String jump() {
	if (commandType() == CommandType.C_COMMAND && currentCommand.contains(";"))
	    return currentCommand.substring(currentCommand.indexOf(";") + 1);

	return "";
    }

}
