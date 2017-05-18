import java.io.FileOutputStream;

import util.Command;
import util.Keyword;
import util.Kind;
import util.Segment;
import util.TokenType;

public class CompilationEngine {
	// private PrintWriter out;
	// private PrintWriter outTokens;
	private VMWriter writer;
	private JackTokenizer tokenizer;
	private SymbolTable symbols;
	private String currClass, currSubroutine;
	private int labelIndex;
	// private ArrayList<String> classes;

	public CompilationEngine(JackTokenizer t, FileOutputStream o, FileOutputStream ot) {
		tokenizer = t;
		writer = new VMWriter(o);
		symbols = new SymbolTable();
		labelIndex = 0;
		// out = new PrintWriter(o);
		// outTokens = new PrintWriter(ot);
		currClass = "";
		currSubroutine = "";
	}

	private String writeError(String parameter) {
		return "ERROR: Token '" + parameter + "' expected. Current token index: " + tokenizer.tokenIndex();
	}

	private boolean nextIs(String tokenType, String tokenValue) {
		// tokenizer.advance();
		// output += "ERROR: Token 'CLASS'
		// expected.");
		String token;
		String type = tokenizer.tokenType();
		switch (tokenType) {
			case TokenType.KEYWORD:
				if (!type.equals(TokenType.KEYWORD)) {
					return false;
				}

				if (tokenValue.isEmpty() && tokenType.equals(type)) {
					return true;
				}

				token = tokenizer.keyword();
				if (!token.isEmpty() && !token.equals(tokenValue)) {
					return false;
				}

				return true;
			case TokenType.SYMBOL:

				if (!type.equals(TokenType.SYMBOL)) {
					return false;
				}

				token = tokenizer.symbol();
				if (!tokenValue.isEmpty() && !token.equals(tokenValue)) {
					return false;
				}

				return true;

			case TokenType.IDENTIFIER:
				if (type.equals(TokenType.IDENTIFIER)) {
					return true;
				}

				else
					return false;
			case TokenType.STRING_CONST:
				if (type.equals(TokenType.STRING_CONST)) {
					return true;
				}

				return false;
			case TokenType.INT_CONST:
				if (type.equals(TokenType.INT_CONST)) {
					return true;
				}

				return false;
			default:
				return false;
		}

	}

	private String mapSegment(String kind) {
		if (kind.equals(Kind.FIELD))
			return Segment.THIS;

		if (kind.equals(Kind.STATIC))
			return Segment.STATIC;

		if (kind.equals(Kind.VAR))
			return Segment.LOCAL;

		if (kind.equals(Kind.ARG))
			return Segment.ARG;

		return null;
	}

	/*
	 * KEYWORD::class IDENTIFIER::classname SYMBOL::{ classVarDec*
	 * subRoutineDec* SYMBOL::}
	 */
	public void compileClass() {
		String output = "";

		// KEYWORD::class
		tokenizer.advance();
		if (!nextIs(TokenType.KEYWORD, Keyword.CLASS)) {
			output += writeError("CLASS");
		}

		// IDENTIFIER::classname
		tokenizer.advance();
		if (!nextIs(TokenType.IDENTIFIER, "")) {
			output += writeError("IDENTIFIER");
		}

		currClass = tokenizer.identifier();

		// SYMBOL::{
		tokenizer.advance();
		if (!nextIs(TokenType.SYMBOL, "{")) {
			output += writeError("SYMBOL");
		}
		// output += "<symbol> " + tokenizer.symbol() + " </symbol>\n";

		// classVarDec*
		compileClassVarDec();
		// subroutineDec*
		compileSubroutine();

		// SYMBOL::}
		tokenizer.advance();
		if (!nextIs(TokenType.SYMBOL, "}")) {
			output += writeError("SYMBOL");
		}

		writer.close();
		// out.println(output);
		// out.flush();
		// out.close();
	}

	// ('static'|'field') type varName (',' varName)* ';'
	public void compileClassVarDec() {
		String output = "";

		tokenizer.advance();
		while (nextIs(TokenType.KEYWORD, Keyword.STATIC) || nextIs(TokenType.KEYWORD, Keyword.FIELD)) {
			compileVarDec();
			tokenizer.advance();
		}

		tokenizer.previousToken();

		return;
	}

	/*
	 * ('constructor'|'function'|'method') ('void' | type) subroutineName '('
	 * parameterList ')' subroutineBody
	 */
	public void compileSubroutine() {
		String output = "";
		String type = "";
		String keyword = "";

		tokenizer.advance();
		while (nextIs(TokenType.KEYWORD, Keyword.CONSTRUCTOR) || nextIs(TokenType.KEYWORD, Keyword.FUNCTION) || nextIs(TokenType.KEYWORD,
				Keyword.METHOD)) {
			keyword = tokenizer.keyword();
			symbols.startSubroutine();

			if (nextIs(TokenType.KEYWORD, Keyword.METHOD)) {
				symbols.define("this", currClass, Kind.ARG);
			}

			tokenizer.advance();
			if (nextIs(TokenType.KEYWORD, "")) {
				type = tokenizer.keyword();
			} else if (nextIs(TokenType.IDENTIFIER, "")) {
				// output += "<identifier> " + tokenizer.identifier() + "
				// </identifier>\n";
				type = tokenizer.identifier();
			} else {
				output += writeError("KEYWORD' || 'IDENTIFIER");
			}

			tokenizer.advance();
			if (!nextIs(TokenType.IDENTIFIER, ""))
				output += writeError("IDENTIFIER");

			currSubroutine = tokenizer.identifier();

			tokenizer.advance();
			if (!nextIs(TokenType.SYMBOL, "("))
				output += writeError("SYMBOL");

			compileParameterList();

			tokenizer.advance();
			if (!nextIs(TokenType.SYMBOL, ")")) {
				output += writeError("SYMBOL");
			}

			// Subroutine body
			tokenizer.advance();
			if (!nextIs(TokenType.SYMBOL, "{")) {
				output += writeError("SYMBOL");
			}

			tokenizer.advance();
			while (nextIs(TokenType.KEYWORD, Keyword.VAR)) {
				compileVarDec();
				tokenizer.advance();
			}
			tokenizer.previousToken();

			writer.writeFunction(currClass + "." + currSubroutine, symbols.varCount(Kind.VAR));

			if (keyword.equals(Keyword.METHOD)) {
				writer.writePush(Segment.ARG, 0);
				writer.writePop(Segment.POINTER, 0);

			} else if (keyword.equals(Keyword.CONSTRUCTOR)) {
				writer.writePush(Segment.CONST, symbols.varCount(Kind.FIELD));
				writer.writeCall("Memory.alloc", 1);
				writer.writePop(Segment.POINTER, 0);
			}

			// output += "<statements>\n";
			compileStatements();
			// output += "</statements>\n";

			tokenizer.advance();
			if (!nextIs(TokenType.SYMBOL, "}")) {
				output += writeError("SYMBOL");
			}
			// output += "<symbol> } </symbol>\n"
			// + "</subroutineBody>\n"
			// + "</subroutineDec>\n";

			tokenizer.advance();
		}

		return;
	}

	public void compileParameterList() {
		String output = "";
		String type = "";

		tokenizer.advance();
		if (nextIs(TokenType.KEYWORD, Keyword.VOID) || nextIs(TokenType.KEYWORD, Keyword.INT) || nextIs(TokenType.KEYWORD, Keyword.CHAR) || nextIs(
				TokenType.KEYWORD, Keyword.BOOLEAN)) {
			type = tokenizer.keyword();

		} else if (nextIs(TokenType.IDENTIFIER, "")) {
			// output += "<identifier> " + tokenizer.identifier() + "
			// </identifier> \n";
			type = tokenizer.identifier();
		} else {
			tokenizer.previousToken();
			return;
		}

		tokenizer.advance();
		if (!nextIs(TokenType.IDENTIFIER, "")) {
			output += writeError("IDENTIFIER");
			return;
		}

		symbols.define(tokenizer.identifier(), type, Kind.ARG);
		// output += "<identifier> " + tokenizer.identifier() + "
		// </identifier>\n";

		tokenizer.advance();
		if (nextIs(TokenType.SYMBOL, ",")) {
			// output += "<symbol> , </symbol>\n";
			compileParameterList();
		} else {
			tokenizer.previousToken();
		}

		return;

	}

	// ' type varName (',' varName)* ';'
	public void compileVarDec() {
		// String output = "";
		String varType, varName;

		/** Determine type **/
		tokenizer.advance();
		// Primitive type
		if (nextIs(TokenType.KEYWORD, Keyword.VOID) || nextIs(TokenType.KEYWORD, Keyword.INT) || nextIs(TokenType.KEYWORD, Keyword.CHAR) || nextIs(
				TokenType.KEYWORD, Keyword.BOOLEAN)) {
			varType = tokenizer.keyword();
		}
		// Class type
		else if (nextIs(TokenType.IDENTIFIER, "")) {
			varType = tokenizer.identifier();
		}
		// None of the above
		else {
			return;
		}

		/** Determine name **/
		tokenizer.advance();
		if (!nextIs(TokenType.IDENTIFIER, "")) {
			// output += writeError("IDENTIFIER");
			return;
		}
		varName = tokenizer.identifier();

		symbols.define(varName, varType, Kind.VAR);

		// output += "<identifier> " + tokenizer.identifier() + "
		// </identifier>\n";

		tokenizer.advance();
		while (nextIs(TokenType.SYMBOL, ",")) {
			// output += "<symbol> " + tokenizer.symbol() + " </symbol>\n";

			tokenizer.advance();
			if (!nextIs(TokenType.IDENTIFIER, "")) {
				// output += writeError("IDENTIFIER");
				// return;
			}

			// output += "<identifier> " + tokenizer.identifier() + "
			// </identifier>\n";
			tokenizer.advance();

		}

		if (!nextIs(TokenType.SYMBOL, ";")) {
			// output += writeError("SYMBOL");
			return;
		}
		// output += "<symbol> ; </symbol>\n";

		// System.out.println(output);
		return;
	}

	// let,if,while,do,return
	public void compileStatements() {
		String output = "";

		// System.out.println(tokenizer.keyword() + "::" +
		// tokenizer.tokenType());
		output += "<statements>\n";
		tokenizer.advance();
		while (nextIs(TokenType.KEYWORD, "")) {
			// System.out.println("KEY:" + tokenizer.keyword() + "; TN: " +
			// tokenizer.tokenIndex());
			switch (tokenizer.keyword()) {
				case Keyword.LET:
					// output += "<letStatement>\n" + "<keyword> " +
					// tokenizer.keyword() + " </keyword>\n" + compileLet() +
					// "</letStatement>\n";
					compileLet();
					break;

				case Keyword.IF:
					// output += "<ifStatement>\n" + "<keyword> " +
					// tokenizer.keyword() + " </keyword>\n" + compileIf() +
					// "</ifStatement>\n";
					compileIf();
					break;

				case Keyword.WHILE:
					// output += "<whileStatement>\n" + "<keyword> " +
					// tokenizer.keyword() + " </keyword>\n" + compileWhile() +
					// "</whileStatement>\n";
					compileWhile();
					break;

				case Keyword.DO:
					// output += "<doStatement>\n" + "<keyword> " +
					// tokenizer.keyword() + " </keyword>\n" + compileDo() +
					// "</doStatement>\n";
					compileDo();
					break;

				case Keyword.RETURN:
					// output += "<returnStatement>\n" + "<keyword> " +
					// tokenizer.keyword() + " </keyword>\n" + compileReturn() +
					// "</returnStatement>\n";
					compileReturn();
					break;

				default:
					// throw error
					break;
			}

			tokenizer.advance();
		}

		tokenizer.previousToken();
		output += "</statements>\n";

		return;
	}

	// 'do' subroutineCall ';'
	//TODO: FINISH
	public void compileDo() {
		String output = "";
		String object = "";
		String name = "";

		tokenizer.advance();
		if (!nextIs(TokenType.IDENTIFIER, "")) {
			output += writeError("IDENTIFIER");
		}
		// output += "<identifier> " + tokenizer.identifier() + "
		// </identifier>\n";
		name = tokenizer.identifier();

		tokenizer.advance();
		if (!nextIs(TokenType.SYMBOL, ".") && !nextIs(TokenType.SYMBOL, "(")) {
			output += writeError("SYMBOL");
		}

		output += "<symbol> " + tokenizer.symbol() + " </symbol>\n";

		if (nextIs(TokenType.SYMBOL, ".")) {
			tokenizer.advance();
			if (!nextIs(TokenType.IDENTIFIER, "")) {
				output += writeError("IDENTIFIER");
			}

			// when calling object.method(params);
			object = name;
			name = symbols.typeOf(object);

			tokenizer.advance();
			if (!nextIs(TokenType.SYMBOL, "(")) {
				output += writeError("SYMBOL");
			}
			output += "<symbol> " + tokenizer.symbol() + " </symbol>\n";
		}

		// if(!nextIs(TokenType.SYMBOL, "(")){
		// output += writeError("SYMBOL");
		// }
		// output += "<symbol> " + tokenizer.symbol() + " </symbol>\n";

		compileExpressionList();

		tokenizer.advance();
		if (!nextIs(TokenType.SYMBOL, ")")) {
			output += writeError("SYMBOL");
		}
		output += "<symbol> " + tokenizer.symbol() + " </symbol>\n";

		tokenizer.advance();
		if (!nextIs(TokenType.SYMBOL, ";")) {
			output += writeError("SYMBOL");
		}
		output += "<symbol> " + tokenizer.symbol() + " </symbol>\n";

		writer.writePop(Segment.TEMP, 0);
		return;
	}

	// 'let' varName ('[' expression ']')? '=' expression ';'
	public void compileLet() {
		String output = "";
		String name = "";
		String arrayElement = "";
		Boolean isArray = false;

		tokenizer.advance();
		if (!nextIs(TokenType.IDENTIFIER, "")) {
			output += writeError("IDENTIFIER");
		}

		name = tokenizer.identifier();

		tokenizer.advance();
		if (nextIs(TokenType.SYMBOL, "[")) {
			// output += "<symbol> " + tokenizer.symbol() + " </symbol>\n" +
			// compileExpression();
			String kind = symbols.kindOf(name);
			int segIndex = symbols.indexOf(name);
			isArray = true;

			writer.writePush(mapSegment(kind), segIndex);

			compileExpression();

			tokenizer.advance();
			if (!nextIs(TokenType.SYMBOL, "]")) {
				output += writeError("SYMBOL");
			}

			writer.writeArithmetic(Command.ADD);
			// output += "<symbol> " + tokenizer.symbol() + " </symbol>\n";

			tokenizer.advance();

		}

		// tokenizer.advance();
		if (!nextIs(TokenType.SYMBOL, "=")) {
			output += writeError("SYMBOL");
		}
		// output += "<symbol> " + tokenizer.symbol() + " </symbol>\n";

		compileExpression();

		tokenizer.advance();
		if (!nextIs(TokenType.SYMBOL, ";")) {
			output += writeError("SYMBOL");
		}

		// output += "<symbol> " + tokenizer.symbol() + " </symbol>\n";
		if (isArray) {
			writer.writePop(Segment.TEMP, 0);
			writer.writePop(Segment.POINTER, 1);
			writer.writePush(Segment.TEMP, 0);
			writer.writePop(Segment.THAT, 0);
		} else {
			String kind = symbols.kindOf(name);
			int segIndex = symbols.indexOf(name);

			writer.writePop(mapSegment(kind), segIndex);
		}

		return;
	}

	// 'while' '(' expression ')' '{' statements '}'
	public void compileWhile() {
		String output = "";

		int label1Index = labelIndex;
		writer.writeLabel("LBL_" + (labelIndex++));

		tokenizer.advance();
		if (!nextIs(TokenType.SYMBOL, "(")) {
			output += writeError("SYMBOL");
		}
		// output += "<symbol> " + tokenizer.symbol() + " </symbol>\n";

		compileExpression();

		tokenizer.advance();
		if (!nextIs(TokenType.SYMBOL, ")")) {
			output += writeError("SYMBOL");
		}
		// output += "<symbol> " + tokenizer.symbol() + " </symbol>\n";

		int label2Index = labelIndex;
		writer.writeArithmetic(Command.NOT);
		writer.writeIf("LBL_" + (labelIndex++));

		tokenizer.advance();
		if (!nextIs(TokenType.SYMBOL, "{")) {
			output += writeError("SYMBOL");
		}
		// output += "<symbol> " + tokenizer.symbol() + " </symbol>\n";

		compileStatements();

		tokenizer.advance();
		if (!nextIs(TokenType.SYMBOL, "}")) {
			output += writeError("SYMBOL");
		}
		// output += "<symbol> " + tokenizer.symbol() + " </symbol>\n";
		writer.writeGoto("LBL_" + label1Index);
		writer.writeLabel("LBL_" + label2Index);

		return;
	}

	// 'return' expression? ';'
	public void compileReturn() {
		String output = "";

		tokenizer.advance();
		if (nextIs(TokenType.SYMBOL, ";")) {
			// output += "<symbol> " + tokenizer.symbol() + " </symbol>\n";
			// VMWriter.writePush(0);
			// VMWriter.writeReturn();
			writer.writePush(Segment.CONST, 0);
		} else {
			tokenizer.previousToken();

			compileExpression();

			tokenizer.advance();
			if (!nextIs(TokenType.SYMBOL, ";")) {
				output += writeError("SYMBOL");
			}
			// output += "<symbol> " + tokenizer.symbol() + " </symbol>\n";
		}

		writer.writeReturn();

	}

	public void compileIf() {
		String output = "";

		tokenizer.advance();
		if (!nextIs(TokenType.SYMBOL, "(")) {
			output += writeError("SYMBOL");
		}
		// output += "<symbol> " + tokenizer.symbol() + " </symbol>\n";

		compileExpression();

		tokenizer.advance();
		if (!nextIs(TokenType.SYMBOL, ")")) {
			output += writeError("SYMBOL");
		}
		// output += "<symbol> " + tokenizer.symbol() + " </symbol>\n";

		writer.writeArithmetic(Command.NOT);
		int label1Index = labelIndex;
		writer.writeIf("LBL_" + (labelIndex++));

		tokenizer.advance();
		if (!nextIs(TokenType.SYMBOL, "{")) {
			output += writeError("SYMBOL");
		}
		// output += "<symbol> " + tokenizer.symbol() + " </symbol>\n";

		compileStatements();

		tokenizer.advance();
		if (!nextIs(TokenType.SYMBOL, "}")) {
			output += writeError("SYMBOL");
		}
		// output += "<symbol> " + tokenizer.symbol() + " </symbol>\n";

		int label2Index = labelIndex;
		writer.writeGoto("LBL_" + (labelIndex++));

		writer.writeLabel("LBL_" + label1Index);

		tokenizer.advance();
		if (nextIs(TokenType.KEYWORD, Keyword.ELSE)) {
			// output += "<keyword> " + tokenizer.keyword() + " </keyword>\n";

			tokenizer.advance();
			if (!nextIs(TokenType.SYMBOL, "{")) {
				output += writeError("SYMBOL");
			}
			// output += "<symbol> " + tokenizer.symbol() + " </symbol>\n";

			compileStatements();

			tokenizer.advance();
			if (!nextIs(TokenType.SYMBOL, "}")) {
				output += writeError("SYMBOL");
			}
			// output += "<symbol> " + tokenizer.symbol() + "</symbol>\n";
		} else {
			tokenizer.previousToken();
		}

		writer.writeLabel("LBL_" + label2Index);

		return;

	}

	public void compileExpression() {
		String output = "";

		// output += "<expression>\n";

		compileTerm();

		tokenizer.advance();
		while (nextIs(TokenType.SYMBOL, "") && (tokenizer.symbol().matches("[\\+|\\-|\\*|\\/|\\&|\\||\\<|\\>|\\= ]"))) {
			// output += "<symbol> ";

			String symbol = tokenizer.symbol();
			// output += " </symbol>\n";

			compileTerm();
			switch (symbol) {
				case "<":
					// output += "&lt;";
					writer.writeArithmetic(Command.LT);
					break;
				case ">":
					// output += "&gt;";
					writer.writeArithmetic(Command.GT);
					break;
				case "&":
					writer.writeArithmetic(Command.AND);
					break;
				case "+":
					writer.writeArithmetic(Command.ADD);
					break;
				case "-":
					writer.writeArithmetic(Command.SUB);
					break;
				case "*":
					writer.writeCall("Math.multiply", 2);
					break;
				case "/":
					writer.writeCall("Math.divide", 2);
					break;
				case "=":
					writer.writeArithmetic(Command.EQ);
					break;
				case "|":
					writer.writeArithmetic(Command.OR);
					break;
				// default:
				// output += tokenizer.symbol();
			}
			tokenizer.advance();	
		}
		
		tokenizer.previousToken();

		//if (output.isEmpty()) {
		// return;
		//}

		// return "<expression>\n" + output + "</expression>\n";
		return;
	}

	/*
	 * stringConstant keywordConstant '(' expression ')' varName varName '['
	 * expression ']' subroutineCall unaryOp term
	 */
	public void compileTerm() {
		String output = "";

		// output += "<term> \n";

		tokenizer.advance();
		// integerConstant
		if (nextIs(TokenType.INT_CONST, "")) {
			//output += "<integer_constant> " + tokenizer.intVal() + " </integer_constant>\n";
			writer.writePush(Segment.CONST, tokenizer.intVal());
		}
		//
		else if (nextIs(TokenType.STRING_CONST, "")) {
			//output += "<string_constant> " + tokenizer.stringVal() + " </string_constant>\n";
            String stringVal = tokenizer.stringVal();

            writer.writePush(Segment.CONST, stringVal.length());
            writer.writeCall("String.new", 1);
            
            for (int i = 0; i < stringVal.length(); i++){
            	int charVal = (int) stringVal.charAt(i);
            	
                writer.writePush(Segment.CONST, charVal);
                writer.writeCall("String.appendChar",2);
            }
		
		}
		//
		else if (nextIs(TokenType.KEYWORD, "true")){
			//output += "<keyword> " + tokenizer.keyword() + " </keyword>\n";
            writer.writePush(Segment.CONST, 0);
            writer.writeArithmetic(Command.NOT);
		}
		else if(nextIs(TokenType.KEYWORD, "false") || nextIs(TokenType.KEYWORD, "null")){ 
			writer.writePush(Segment.CONST,0);
		}else if(nextIs(TokenType.KEYWORD, "this")){
			
			writer.writePush(Segment.POINTER,0);
		}
		//
		else if (nextIs(TokenType.SYMBOL, "(")) {
			//output += "<symbol> " + tokenizer.symbol() + " </symbol>\n";
			compileExpression();

			tokenizer.advance();
			if (!nextIs(TokenType.SYMBOL, ")")) {
				output += writeError("SYMBOL");
			}
			//output += "<symbol> " + tokenizer.symbol() + " </symbol>\n";
		}
		//
		else if (nextIs(TokenType.IDENTIFIER, "")) {
			//output += "<identifier> " + tokenizer.identifier() + " </identifier>\n";
			String identifier = tokenizer.identifier();
			//TODO: FINISH
			tokenizer.advance();				
			String kind = symbols.kindOf(identifier);
			int index = symbols.indexOf(identifier);
			
			if (nextIs(TokenType.SYMBOL, "[")) {

				//output += "<symbol> " + tokenizer.symbol() + " </symbol>\n" + 

				
				writer.writePush(mapSegment(kind), index);
				
				compileExpression();

				tokenizer.advance();
				if (!nextIs(TokenType.SYMBOL, "]")) {
					output += writeError("SYMBOL");
				}
				//output += "<symbol> " + tokenizer.symbol() + " </symbol>\n";
				writer.writeArithmetic(Command.ADD);
				
		        writer.writePop(Segment.POINTER, 1);
                writer.writePush(Segment.THAT,0);
			}
			//
			else if (nextIs(TokenType.SYMBOL, "(") || nextIs(TokenType.SYMBOL, ".")) {
				//output += "<symbol> " + tokenizer.symbol() + " </symbol>\n";

				if (nextIs(TokenType.SYMBOL, ".")) {
					tokenizer.advance();
					if (!nextIs(TokenType.IDENTIFIER, "")) {
						output += writeError("IDENTIFIER");
					}
					output += "<identifier> " + tokenizer.identifier() + " </identifier>\n";

					tokenizer.advance();
					if (!nextIs(TokenType.SYMBOL, "(")) {
						output += writeError("SYMBOL");
					}
					output += "<symbol> " + tokenizer.symbol() + " </symbol>\n";
				}

				compileExpressionList();

				tokenizer.advance();
				if (!nextIs(TokenType.SYMBOL, ")")) {
					output += writeError("SYMBOL");
				}
				output += "<symbol> " + tokenizer.symbol() + " </symbol>\n";

			} else {
				tokenizer.previousToken();
			}

		} else if (nextIs(TokenType.SYMBOL, "-") || nextIs(TokenType.SYMBOL, "~")) {
			String s = tokenizer.symbol();
			//output += "<symbol> " + tokenizer.symbol() + " </symbol>\n";
			
			compileTerm();
			
			if(s.equals("-")){
				writer.writeArithmetic(Command.NEG);
			}else{
				writer.writeArithmetic(Command.NOT);
			}
				

		} else {
			tokenizer.previousToken();
		}

		//if (output.isEmpty()) {
		//	return;
		//}

		// return "<term>\n" + output + "</term>\n";
		return;
	}

	public int  compileExpressionList() {
		String output = "";
		int numArgs = 0;

		compileExpression();
		numArgs++;
		
		tokenizer.advance();
		while (nextIs(TokenType.SYMBOL, ",")) {
			output += "<symbol> " + tokenizer.symbol() + " </symbol>\n";

			compileExpression();
			numArgs++;
			
			tokenizer.advance();
		}
		tokenizer.previousToken();

		return numArgs;//"<expressionList>\n" + output + "</expressionList>\n";

	}

}
