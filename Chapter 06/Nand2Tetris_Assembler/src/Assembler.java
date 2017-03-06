import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

public class Assembler {
    public static void main(String[] args) {
	try {
	    String assemblyFileName = args[0];
	    String hackFileName = assemblyFileName.substring(0, assemblyFileName.indexOf(".")) + ".hack";

	    FileInputStream fileStream = new FileInputStream(assemblyFileName);
	    OutputStreamWriter outputFile = new OutputStreamWriter(new FileOutputStream(new File(hackFileName)));
	    Parser parser = new Parser(fileStream);
	    SymbolTable symbolTable = new SymbolTable();

	    // First pass
	    int currentROMAddr = 0;
	    while (parser.hasMoreCommands()) {
		parser.advance();

		switch (parser.commandType()) {
		case L_COMMAND:
		    if (!symbolTable.contains(parser.symbol().trim()))
			symbolTable.addEntry(parser.symbol().trim(), currentROMAddr);

		    break;
		default:
		    currentROMAddr++;
		}
	    }

	    parser.restart();

	    // Second pass
	    while (parser.hasMoreCommands()) {
		parser.advance();

		switch (parser.commandType()) {
		case A_COMMAND:
		    String symbol = parser.symbol().trim();
		    int addrBin;
		    if (symbol.matches("[0-9]+")) {
			addrBin = Integer.parseInt(symbol);
			// System.out.println("AddressNum: " + symbol + " :: " +
			// addrBin);
		    } else {
			if (!symbolTable.contains(symbol))
			    symbolTable.addEntry(symbol);

			addrBin = symbolTable.GetAddress(symbol);
			// System.out.println("SymNum: " + symbol + " :: " +
			// addrBin);
		    }

		    String unformatted = Integer.toBinaryString(addrBin);
		    String formatted = ("000000000000000" + unformatted).substring(unformatted.length());

		    outputFile.write("0" + formatted + "\r\n");
		    outputFile.flush();
		    break;

		case C_COMMAND:
		    // Prepare the different parts of the command
		    String comp = Code.comp(parser.comp());
		    String dest = Code.dest(parser.dest());
		    String jump = Code.jump(parser.jump());

		    // Concatinate and write.
		    outputFile.write("111" + comp + dest + jump + "\r\n");
		    outputFile.flush();
		    // System.out.println(comp + " :: " + dest + " :: " + jump);
		    break;

		default:
		    break;

		}
	    }

	    outputFile.close();

	} catch (Exception e) {
	    e.printStackTrace();
	}

    }

}
