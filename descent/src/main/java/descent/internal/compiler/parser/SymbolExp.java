package descent.internal.compiler.parser;

public abstract class SymbolExp extends Expression {
	
	public Declaration var;
	public boolean hasOverloads;

	public SymbolExp(char[] filename, int lineNumber, TOK op, Declaration var, boolean hasOverloads) {
		super(filename, lineNumber, op);
		
		this.var = var;
		this.hasOverloads = hasOverloads;
	}

}
