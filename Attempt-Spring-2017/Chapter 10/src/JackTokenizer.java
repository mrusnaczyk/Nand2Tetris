import java.io.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.regex.*;

import util.Keyword;
import util.TokenType;

public class JackTokenizer implements AutoCloseable {

	private String keywordRegex = "class|method|function|constructor|int|boolean|char|void|var|static|field|let|do|"
			+ "if|else|while|return|true|false|null|this";
	private String symbolRegex = "[\\{|\\}|\\(|\\)|\\[|\\]|\\.|\\,|\\.|\\;|\\+|\\-|\\*|\\/|\\&|\\||\\<|\\>|\\=|\\~]";
	private String intRegex = "[0-9]+";
	private String stringRegex = "\"[^\"\n]*\"";
	private String identifierRegex = "[\\w_]+";

	private BufferedReader input;
	private ArrayList<String> tokens = new ArrayList<String>();

	private String tokenType;
	private int currToken;

	public JackTokenizer(Path path) {
		currToken = -1;

		String fileRaw = "";
		String line = "";

		try {
			input = new BufferedReader(new FileReader(path.toString()));

			while ((line = input.readLine()) != null) {
				// Remove single-line comments
				line = line.replaceAll("//.*", "");
				fileRaw += line.trim();
			}
			fileRaw += String.valueOf((char) input.read());
		} catch (IOException e) {
			e.printStackTrace();
		}

		String fileSansComments = removeBlockComments(fileRaw);

		// System.out.println(fileSansComments);

		// Every possible token we are looking for
		Pattern tokenPattern = Pattern.compile(keywordRegex + "|" + symbolRegex + "|" + intRegex + "|" + stringRegex + "|" + identifierRegex);
		Matcher tokenSpotter = (tokenPattern.matcher(fileSansComments));

		// Find all of the tokens
		while (tokenSpotter.find()) {
			tokens.add(tokenSpotter.group());
		}

	}

	public void reset() {
		currToken = -1;

	}

	public int tokenIndex() {
		return currToken;
	}

	public boolean hasMoreTokens() {
		return currToken < tokens.size() - 1;
	}

	public void advance() {
		if (hasMoreTokens())
			currToken++;

		String token = tokens.get(currToken);

		if (token.matches(keywordRegex)) {
			tokenType = TokenType.KEYWORD;
			return;
		}

		if (token.matches(symbolRegex)) {
			tokenType = TokenType.SYMBOL;
			return;
		}

		if (token.matches(stringRegex)) {
			tokenType = TokenType.STRING_CONST;
			return;
		}

		if (token.matches(intRegex)) {
			tokenType = TokenType.INT_CONST;
			return;
		}

		if (token.matches(identifierRegex)) {
			tokenType = TokenType.IDENTIFIER;
			return;
		}

	}

	public void previousToken() {
		if (currToken > 0)
			currToken--;
		
		String token = tokens.get(currToken);

		if (token.matches(keywordRegex)) {
			tokenType = TokenType.KEYWORD;
			return;
		}

		if (token.matches(symbolRegex)) {
			tokenType = TokenType.SYMBOL;
			return;
		}

		if (token.matches(stringRegex)) {
			tokenType = TokenType.STRING_CONST;
			return;
		}

		if (token.matches(intRegex)) {
			tokenType = TokenType.INT_CONST;
			return;
		}

		if (token.matches(identifierRegex)) {
			tokenType = TokenType.IDENTIFIER;
			return;
		}
	}

	public String tokenType() {
		return tokenType;
	}

	public String keyword() {
		String token = tokens.get(currToken);

		if (tokenType == TokenType.KEYWORD) {
			for (String k : Keyword.values()) {
				if (token.equalsIgnoreCase(k.toString())) {
					return k;
				}
			}
		}

		return "NOTKEYWORD";

	}

	public String symbol() {
		String token = tokens.get(currToken);

		if (tokenType == TokenType.SYMBOL) {
			return token;
		}

		return "NOTSYMBOL";
	}

	public String identifier() {
		String token = tokens.get(currToken);

		if (tokenType == TokenType.IDENTIFIER) {
			return token;
		}

		return "NOTID";
	}

	public int intVal() {
		String token = tokens.get(currToken);

		if (tokenType == TokenType.INT_CONST) {
			return Integer.parseInt(token);
		}

		return -1;
	}

	public String stringVal() {
		String token = tokens.get(currToken);

		if (tokenType == TokenType.STRING_CONST) {
			return token;
		}

		return "NOTSTRINGVAL";
	}

	private String removeBlockComments(String s) {
		if (!s.contains("/**"))
			return s;

		String out = "";
		boolean commentMode = false;
		
		for (int i = 0; i < s.length() - 1; i++) {
			String commentCandidate = s.substring(i, i + 2);
			if (!commentMode) {
				if (commentCandidate.equals("/*")) {
					commentMode = true;
				} else {
					out += s.substring(i, i + 1);
				}
			} else if (commentMode && commentCandidate.equals("*/")) {
				commentMode = false;
				// Skip over the comment notation
				i++;
			}

		}

		return out;

	}

	@Override
	public void close() throws Exception {
	}

}
