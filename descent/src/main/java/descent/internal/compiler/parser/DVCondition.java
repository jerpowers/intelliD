package descent.internal.compiler.parser;


public abstract class DVCondition extends Condition {
	
	public Module mod;
	public char[] ident;
	public long level;
	public int startPosition;
	public int length;
	
	public DVCondition(Module mod, char[] filename, int lineNumber, long level, char[] id) {
		super(filename, lineNumber);
		this.mod = mod;
		this.level = level;
		this.ident = id;
	}
	
	@Override
	public Condition syntaxCopy(SemanticContext context) {
		return this; // don't need to copy
	}

}
