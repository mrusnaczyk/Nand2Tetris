package util;

public class Keyword {
	public static final String CLASS = "class";
	public static final String METHOD = "method";
	public static final String FUNCTION = "function";
	public static final String CONSTRUCTOR = "constructor";
	public static final String INT = "int";
	public static final String BOOLEAN = "boolean";
	public static final String CHAR = "char";
	public static final String VOID = "void";
	public static final String VAR = "var";
	public static final String STATIC = "static";
	public static final String FIELD = "field";
	public static final String LET = "let";
	public static final String DO = "do";
	public static final String IF = "if";
	public static final String ELSE = "else";
	public static final String WHILE = "while";
	public static final String RETURN = "return";
	public static final String TRUE = "true";
	public static final String FALSE = "false";
	public static final String NULL = "null";
	public static final String THIS = "this";

	private static final String[] values = { CLASS, METHOD, FUNCTION, CONSTRUCTOR, INT, BOOLEAN, CHAR, VOID, VAR, STATIC, FIELD, LET, DO, IF, ELSE, WHILE,
			RETURN, TRUE, FALSE, NULL, THIS };

	public static String[] values() {
		return values;
	}
}
