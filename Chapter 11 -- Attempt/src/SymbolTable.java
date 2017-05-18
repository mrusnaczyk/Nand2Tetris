import java.util.ArrayList;

import util.Kind;

public class SymbolTable {

	private ArrayList<String[]> subroutineSymbols, classSymbols;
	private int staticIndex, fieldIndex, argIndex, varIndex;

	public SymbolTable() {
		// Class-wide scope
		classSymbols = new ArrayList<String[]>();
		staticIndex = 0;
		fieldIndex = 0;

		// Subroutine scope
		subroutineSymbols = new ArrayList<String[]>();
		argIndex = 0;
		varIndex = 0;

	}

	public void startSubroutine() {
		subroutineSymbols.clear();
		argIndex = 0;
		varIndex = 0;

	}

	public void define(String name, String type, String kind) {
		// if static or field, class scope
		// arg or var, subroutine scope

		// class scope
		if (kind.equals(Kind.STATIC) || kind.equals(Kind.FIELD)) {
			//name type kind #
			int index = 0;
			if(kind.equals(Kind.STATIC)){
				index = staticIndex;
				staticIndex++;
			}else{
				index = fieldIndex;
				fieldIndex++;
			}
				
			String[] symbol = {name, type, kind, String.valueOf(index)};
			classSymbols.add(symbol);
			
		}
		else if (kind.equals(Kind.ARG) || kind.equals(Kind.VAR)) {
			int index = 0;
			if(kind.equals(Kind.ARG)){
				index = argIndex;
				argIndex++;
			}else{
				index = varIndex;
				varIndex++;
			}
				
			String[] symbol = {name, type, kind, String.valueOf(index)};
			subroutineSymbols.add(symbol);
		}
	}

	public int varCount(String var) {
		switch(var.toString()){
			case Kind.STATIC:
				return staticIndex + 1;
			case Kind.FIELD:
				return fieldIndex + 1;
			case Kind.ARG:
				return argIndex + 1;
			case Kind.VAR:
				return varIndex + 1;
			default:
				return -1;
		}
	}

	public String kindOf(String name) {
		
		//check within subroutine
		for(String[] s: subroutineSymbols){
			String sName = s[0];
			String sKind = s[2];
			
			if(sName.equals(name)){
				return sKind;
			}
		}
		
		//check within class
		for(String[] s: classSymbols){
			String sName = s[0];
			String sKind = s[2];
			
			if(sName.equals(name)){
				return sKind;
			}
		}
		
		//if a symbol with the given name is not found, return NONE
		return Kind.NONE;
	}

	public String typeOf(String name) {
		//check within subroutine
		for(String[] s: subroutineSymbols){
			String sName = s[0];
			String sType = s[1];
			
			if(sName.equals(name)){
				return sType;
			}
		}
		
		//check within class
		for(String[] s: classSymbols){
			String sName = s[0];
			String sType = s[1];
			
			if(sName.equals(name)){
				return sType;
			}
		}
		
		//if a symbol with the given name is not found, return NONE
		return null;
	}

	public int indexOf(String name) {
		return 0;
	}

}
