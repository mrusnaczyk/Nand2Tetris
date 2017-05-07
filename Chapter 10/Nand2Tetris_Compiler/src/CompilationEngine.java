import java.io.FileOutputStream;
import java.io.PrintWriter;

import util.Keyword;
import util.TokenType;

public class CompilationEngine {
	private PrintWriter out;
	private PrintWriter outTokens;
	private JackTokenizer tokenizer;
	// private ArrayList<String> classes;

	public CompilationEngine(JackTokenizer t, FileOutputStream o, FileOutputStream ot) {
		tokenizer = t;
		out = new PrintWriter(o);
		outTokens = new PrintWriter(ot);

		// Generate tokens file
		outTokens.println("<tokens>");
		while (tokenizer.hasMoreTokens()) {
			tokenizer.advance();

			String value;

			switch (tokenizer.tokenType()) {
				case TokenType.KEYWORD:
					value = tokenizer.keyword();
					break;
				case TokenType.SYMBOL:
					value = String.valueOf(tokenizer.symbol());
					break;
				case TokenType.IDENTIFIER:
					value = tokenizer.identifier();
					break;
				case TokenType.INT_CONST:
					value = String.valueOf(tokenizer.intVal());
					break;
				case TokenType.STRING_CONST:
					value = tokenizer.stringVal();
				default:
					value = "";
			}

			outTokens.println("\t<" + tokenizer.tokenType() + "> " + value + " </" + tokenizer.tokenType() + ">");

		}

		outTokens.println("</tokens>\n");
		outTokens.flush();
		outTokens.close();
		
		tokenizer.reset();
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
		output += "<class>\n";
		output += "<keyword> " + tokenizer.keyword() + " </keyword>\n";

		// IDENTIFIER::classname
		tokenizer.advance();
		if (!nextIs(TokenType.IDENTIFIER, "")) {
			output += writeError("IDENTIFIER");
		}
		output += "<identifier> " + tokenizer.identifier() + " </identifier>\n";

		// SYMBOL::{
		tokenizer.advance();
		if (!nextIs(TokenType.SYMBOL, "{")) {
			output += writeError("SYMBOL");
		}
		output += "<symbol> " + tokenizer.symbol() + " </symbol>\n";

		// classVarDec*
		output += compileClassVarDec();

		// subroutineDec*
		output += compileSubroutine();

		// SYMBOL::}
		tokenizer.advance();
		if (!nextIs(TokenType.SYMBOL, "}")) {
			output += writeError("SYMBOL");
		}
		output += "<symbol> " + tokenizer.symbol() + " </symbol>\n";

		output += "</class>";
		// System.out.println(output);
		out.println(output);
		out.flush();
		out.close();
	}

	// ('static'|'field') type varName (',' varName)* ';'
	public String compileClassVarDec() {
		String output = "";

		tokenizer.advance();
		while (nextIs(TokenType.KEYWORD, Keyword.STATIC) || nextIs(TokenType.KEYWORD, Keyword.FIELD)) {
			output += "<classVarDec>\n";
			output += "<keyword> " + tokenizer.keyword() + " </keyword>\n";

			output += compileVarDec();

			output += "</classVarDec>\n";

			tokenizer.advance();
		}

		tokenizer.previousToken();

		return output;
	}

	/*
	 * ('constructor'|'function'|'method') ('void' | type) subroutineName '('
	 * parameterList ')' subroutineBody
	 */
	public String compileSubroutine() {
		String output = "";

		tokenizer.advance();
		while (nextIs(TokenType.KEYWORD, Keyword.CONSTRUCTOR) || nextIs(TokenType.KEYWORD, Keyword.FUNCTION) || nextIs(TokenType.KEYWORD,
				Keyword.METHOD)) {
			output += "<subroutineDec> \n" + "<keyword> " + tokenizer.keyword() + " </keyword>\n";

			tokenizer.advance();
			if (nextIs(TokenType.KEYWORD, Keyword.VOID) || nextIs(TokenType.KEYWORD, Keyword.INT) || nextIs(TokenType.KEYWORD, Keyword.CHAR)
					|| nextIs(TokenType.KEYWORD, Keyword.BOOLEAN)) {
				output += "<keyword> " + tokenizer.keyword() + " </keyword>\n";

			} else if (nextIs(TokenType.IDENTIFIER, "")) {
				output += "<identifier> " + tokenizer.identifier() + " </identifier>\n";
			} else {
				output += writeError("KEYWORD' || 'IDENTIFIER");
				// break;
			}

			tokenizer.advance();
			if (!nextIs(TokenType.IDENTIFIER, "")) {
				output += writeError("IDENTIFIER");

				// break;
			}
			output += "<identifier> " + tokenizer.identifier() + " </identifier>\n";

			tokenizer.advance();
			if (!nextIs(TokenType.SYMBOL, "(")) {
				output += writeError("SYMBOL");
				// break;
			}
			output += "<symbol> " + tokenizer.symbol() + " </symbol>\n";

			output += "<parameterList>\n";
			output += compileParameterList();
			output += "</parameterList>\n";

			tokenizer.advance();
			if (!nextIs(TokenType.SYMBOL, ")")) {
				output += writeError("SYMBOL");
				// break;
			}
			output += "<symbol> " + tokenizer.symbol() + " </symbol>\n"
					+ "<subroutineBody>\n";

			// Subroutine body
			tokenizer.advance();
			if (!nextIs(TokenType.SYMBOL, "{")) {
				output += writeError("SYMBOL");
				// break;
			}
			output += "<symbol> { </symbol>\n";

			tokenizer.advance();
			while (nextIs(TokenType.KEYWORD, Keyword.VAR)) {
				output += "<varDec>\n";
				output += "<keyword> var </keyword>\n";

				output += compileVarDec();

				output += "</varDec>\n";

				tokenizer.advance();
			}
			tokenizer.previousToken();

//			output += "<statements>\n";
			output += compileStatements();
//			output += "</statements>\n";

			tokenizer.advance();
			if (!nextIs(TokenType.SYMBOL, "}")) {
				output += writeError("SYMBOL");
			}
			output += "<symbol> } </symbol>\n"
					+ "</subroutineBody>\n"
					+ "</subroutineDec>\n";

			tokenizer.advance();
		}

		return output;
	}

	public String compileParameterList() {
		String output = "";

		tokenizer.advance();
		if (nextIs(TokenType.KEYWORD, Keyword.VOID) || nextIs(TokenType.KEYWORD, Keyword.INT) || nextIs(TokenType.KEYWORD, Keyword.CHAR) || nextIs(
				TokenType.KEYWORD, Keyword.BOOLEAN)) {
			output += "<keyword> " + tokenizer.keyword() + " </keyword> \n";

		} else if (nextIs(TokenType.IDENTIFIER, "")) {
			output += "<identifier> " + tokenizer.identifier() + " </identifier> \n";
		} else {
			tokenizer.previousToken();
			return "";
		}

		tokenizer.advance();
		if (!nextIs(TokenType.IDENTIFIER, "")) {
			output += writeError("IDENTIFIER");
			return "";
		}
		output += "<identifier> " + tokenizer.identifier() + " </identifier>\n";

		tokenizer.advance();
		if (nextIs(TokenType.SYMBOL, ",")) {
			output += "<symbol> , </symbol>\n";
			output += compileParameterList();
		} else {
			tokenizer.previousToken();
		}

		return output;

	}

	// ' type varName (',' varName)* ';'
	public String compileVarDec() {
		String output = "";

		tokenizer.advance();
		if (nextIs(TokenType.KEYWORD, Keyword.VOID) || nextIs(TokenType.KEYWORD, Keyword.INT) || nextIs(TokenType.KEYWORD, Keyword.CHAR) || nextIs(
				TokenType.KEYWORD, Keyword.BOOLEAN)) {
			output += "<keyword> " + tokenizer.keyword() + " </keyword> \n";

		} else if (nextIs(TokenType.IDENTIFIER, "")) {
			output += "<identifier> " + tokenizer.identifier() + " </identifier> \n";
		} else {
			output += writeError("KEYWORD' || 'IDENTIFIER");
			// return;
		}

		tokenizer.advance();
		if (!nextIs(TokenType.IDENTIFIER, "")) {
			output += writeError("IDENTIFIER");
			// return;
		}
		output += "<identifier> " + tokenizer.identifier() + " </identifier>\n";

		tokenizer.advance();
		while (nextIs(TokenType.SYMBOL, ",")) {
			output += "<symbol> " + tokenizer.symbol() + " </symbol>\n";

			tokenizer.advance();
			if (!nextIs(TokenType.IDENTIFIER, "")) {
				output += writeError("IDENTIFIER");
				// return;
			}

			output += "<identifier> " + tokenizer.identifier() + " </identifier>\n";
			tokenizer.advance();

		}

		if (!nextIs(TokenType.SYMBOL, ";")) {
			output += writeError("SYMBOL");
			// return;
		}
		output += "<symbol> ; </symbol>\n";

		// System.out.println(output);
		return output;
	}

	// let,if,while,do,return
	public String compileStatements() {
		String output = "";

		// System.out.println(tokenizer.keyword() + "::" +
		// tokenizer.tokenType());
		output += "<statements>\n";
		tokenizer.advance();
		while (nextIs(TokenType.KEYWORD, "")) {
			System.out.println("KEY:" + tokenizer.keyword() + "; TN: " + tokenizer.tokenIndex());
			switch (tokenizer.keyword()) {
				case Keyword.LET:
					output += "<letStatement>\n"
							+ "<keyword> " + tokenizer.keyword() + " </keyword>\n" 
							+ compileLet() 
							+ "</letStatement>\n";
					
					break;
					
				case Keyword.IF:
					output += "<ifStatement>\n"
							+ "<keyword> " + tokenizer.keyword() + " </keyword>\n"
							+ compileIf()
							+ "</ifStatement>\n";

					break;
					
				case Keyword.WHILE:
					output += "<whileStatement>\n"
							+ "<keyword> " + tokenizer.keyword() + " </keyword>\n"
							+ compileWhile()
							+ "</whileStatement>\n";
					break;
					
				case Keyword.DO:
					output += "<doStatement>\n"
							+ "<keyword> " + tokenizer.keyword() + " </keyword>\n" 
							+ compileDo()
							+ "</doStatement>\n";
					break;
					
				case Keyword.RETURN:
					output += "<returnStatement>\n"
							+ "<keyword> " + tokenizer.keyword() + " </keyword>\n" 
							+ compileReturn()
							+ "</returnStatement>\n";
					break;
					
				default:
					// throw error
					break;
			}

			tokenizer.advance();
		}

		tokenizer.previousToken();
		output += "</statements>\n";
		
		return output;
	}

	// 'do' subroutineCall ';'
	public String compileDo() {
		String output = "";

		tokenizer.advance();
		if (!nextIs(TokenType.IDENTIFIER, "")) {
			output += writeError("IDENTIFIER");
		}
		output += "<identifier> " + tokenizer.identifier() + " </identifier>\n";

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
			output += "<identifier> " + tokenizer.identifier() + " </identifier>\n";

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

		output += compileExpressionList();

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

		return output;
	}

	// 'let' varName ('[' expression ']')? '=' expression ';'
	public String compileLet() {
		String output = "";

		tokenizer.advance();
		if (!nextIs(TokenType.IDENTIFIER, "")) {
			output += writeError("IDENTIFIER");
			// return;
		}
		output += "<identifier> " + tokenizer.identifier() + " </identifier>\n";

		tokenizer.advance();
		if (nextIs(TokenType.SYMBOL, "[")) {
			output += "<symbol> " + tokenizer.symbol() + " </symbol>\n" + compileExpression();

			tokenizer.advance();
			if (!nextIs(TokenType.SYMBOL, "]")) {
				output += writeError("SYMBOL");
			}
			output += "<symbol> " + tokenizer.symbol() + " </symbol>\n";

			tokenizer.advance();

		}

		// tokenizer.advance();
		if (!nextIs(TokenType.SYMBOL, "=")) {
			output += writeError("SYMBOL");
		}
		output += "<symbol> " + tokenizer.symbol() + " </symbol>\n";

		output += compileExpression();

		tokenizer.advance();
		if (!nextIs(TokenType.SYMBOL, ";")) {
			output += writeError("SYMBOL");
		}

		output += "<symbol> " + tokenizer.symbol() + " </symbol>\n";

		return output;
	}

	// 'while' '(' expression ')' '{' statements '}'
	public String compileWhile() {
		String output = "";

		tokenizer.advance();
		if (!nextIs(TokenType.SYMBOL, "(")) {
			output += writeError("SYMBOL");
		}
		output += "<symbol> " + tokenizer.symbol() + " </symbol>\n";

		output += compileExpression();

		tokenizer.advance();
		if (!nextIs(TokenType.SYMBOL, ")")) {
			output += writeError("SYMBOL");
		}
		output += "<symbol> " + tokenizer.symbol() + " </symbol>\n";

		tokenizer.advance();
		if (!nextIs(TokenType.SYMBOL, "{")) {
			output += writeError("SYMBOL");
		}
		output += "<symbol> " + tokenizer.symbol() + " </symbol>\n";

		output += compileStatements();

		tokenizer.advance();
		if (!nextIs(TokenType.SYMBOL, "}")) {
			output += writeError("SYMBOL");
		}
		output += "<symbol> " + tokenizer.symbol() + " </symbol>\n";

		return output;
	}

	// 'return' expression? ';'
	public String compileReturn() {
		String output = "";

		tokenizer.advance();
		if (nextIs(TokenType.SYMBOL, ";")) {
			output += "<symbol> " + tokenizer.symbol() + " </symbol>\n";
			return output;
		}
		tokenizer.previousToken();

		output += compileExpression();

		tokenizer.advance();
		if (!nextIs(TokenType.SYMBOL, ";")) {
			output += writeError("SYMBOL");
		}
		output += "<symbol> " + tokenizer.symbol() + " </symbol>\n";

		return output;

	}

	public String compileIf() {
		String output = "";

		tokenizer.advance();
		if (!nextIs(TokenType.SYMBOL, "(")) {
			output += writeError("SYMBOL");
		}
		output += "<symbol> " + tokenizer.symbol() + " </symbol>\n";

		output += compileExpression();

		tokenizer.advance();
		if (!nextIs(TokenType.SYMBOL, ")")) {
			output += writeError("SYMBOL");
		}
		output += "<symbol> " + tokenizer.symbol() + " </symbol>\n";

		tokenizer.advance();
		if (!nextIs(TokenType.SYMBOL, "{")) {
			output += writeError("SYMBOL");
		}
		output += "<symbol> " + tokenizer.symbol() + " </symbol>\n";

		output += compileStatements();

		tokenizer.advance();
		if (!nextIs(TokenType.SYMBOL, "}")) {
			output += writeError("SYMBOL");
		}
		output += "<symbol> " + tokenizer.symbol() + " </symbol>\n";

		tokenizer.advance();
		if (nextIs(TokenType.KEYWORD, Keyword.ELSE)) {
			output += "<keyword> " + tokenizer.keyword() + " </keyword>\n";

			tokenizer.advance();
			if (!nextIs(TokenType.SYMBOL, "{")) {
				output += writeError("SYMBOL");
			}
			output += "<symbol> " + tokenizer.symbol() + " </symbol>\n";

			output += compileStatements();

			tokenizer.advance();
			if (!nextIs(TokenType.SYMBOL, "}")) {
				output += writeError("SYMBOL");
			}
			output += "<symbol> " + tokenizer.symbol() + "</symbol>\n";
		} else {
			tokenizer.previousToken();
		}

		return output;

	}

	public String compileExpression() {
		String output = "";

		//output += "<expression>\n";

		output += compileTerm();

		tokenizer.advance();
		while (nextIs(TokenType.SYMBOL, "") && (tokenizer.symbol().matches("[\\+|\\-|\\*|\\/|\\&|\\||\\<|\\>|\\= ]"))) {
			output += "<symbol> ";

			switch (tokenizer.symbol()) {
				case "<":
					output += "&lt;";
					break;
				case ">":
					output += "&gt;";
					break;
				case "&":
					output += "&amp'";
					break;
				default:
					output += tokenizer.symbol();
			}

			output += " </symbol>\n";

			output += compileTerm();
			tokenizer.advance();
		}
		tokenizer.previousToken();

		if(output.isEmpty()){
			return "";
		}
		
		return "<expression>\n" + output + "</expression>\n";

	}

	/*
	 *
	 * stringConstant keywordConstant '(' expression ')' varName varName '['
	 * expression ']' subroutineCall unaryOp term
	 */
	public String compileTerm() {
		String output = "";

//		output += "<term> \n";

		tokenizer.advance();
		// integerConstant
		if (nextIs(TokenType.INT_CONST, "")) {
			output += "<integer_constant> " + tokenizer.intVal() + " </integer_constant>\n";
		}
		//
		else if (nextIs(TokenType.STRING_CONST, "")) {
			output += "<string_constant> " + tokenizer.stringVal() + " </string_constant>\n";
		}
		//
		else if (nextIs(TokenType.KEYWORD, "true") || nextIs(TokenType.KEYWORD, "false") || nextIs(TokenType.KEYWORD, "null") || nextIs(
				TokenType.KEYWORD, "this")) {
			output += "<keyword> " + tokenizer.keyword() + " </keyword>\n";
		}
		//
		else if (nextIs(TokenType.SYMBOL, "(")) {
			output += "<symbol> " + tokenizer.symbol() + " </symbol>\n";
			output += compileExpression();

			tokenizer.advance();
			if (!nextIs(TokenType.SYMBOL, ")")) {
				output += writeError("SYMBOL");
			}
			output += "<symbol> " + tokenizer.symbol() + " </symbol>\n";
		}
		//
		else if (nextIs(TokenType.IDENTIFIER, "")) {
			output += "<identifier> " + tokenizer.identifier() + " </identifier>\n";

			tokenizer.advance();
			if (nextIs(TokenType.SYMBOL, "[")) {

				output += "<symbol> " + tokenizer.symbol() + " </symbol>\n" + compileExpression();

				tokenizer.advance();
				if (!nextIs(TokenType.SYMBOL, "]")) {
					output += writeError("SYMBOL");
				}
				output += "<symbol> " + tokenizer.symbol() + " </symbol>\n";
			}
			//
			else if (nextIs(TokenType.SYMBOL, "(") || nextIs(TokenType.SYMBOL, ".")) {

				output += "<symbol> " + tokenizer.symbol() + " </symbol>\n";

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

				output += compileExpressionList();

				tokenizer.advance();
				if (!nextIs(TokenType.SYMBOL, ")")) {
					output += writeError("SYMBOL");
				}
				output += "<symbol> " + tokenizer.symbol() + " </symbol>\n";

			} else {
				tokenizer.previousToken();
			}

		} else if (nextIs(TokenType.SYMBOL, "-") || nextIs(TokenType.SYMBOL, "~")) {
			output += "<symbol> " + tokenizer.symbol() + " </symbol>\n";
			output += compileTerm();

		} else {
			tokenizer.previousToken();
		}
		
		if(output.isEmpty()){
			return "";
		}

		return "<term>\n" + output + "</term>\n";
	}

	public String compileExpressionList() {
		String output = "";

		output += compileExpression();

		tokenizer.advance();
		while (nextIs(TokenType.SYMBOL, ",")) {
			output += "<symbol> " + tokenizer.symbol() + " </symbol>\n";

			output += compileExpression();
			tokenizer.advance();
		}
		tokenizer.previousToken();

		return "<expressionList>\n" + output + "</expressionList>\n";

	}
}
