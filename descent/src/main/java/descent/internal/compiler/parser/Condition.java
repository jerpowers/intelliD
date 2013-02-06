package descent.internal.compiler.parser;

import descent.internal.compiler.parser.ast.ASTRangeLessNode;


public abstract class Condition extends ASTRangeLessNode {

	public final static int DEBUG = 1;
	public final static int IFTYPE = 2;
	public final static int STATIC_IF = 3;
	public final static int VERSION = 4;

	public int lineNumber;
	public char[] filename;
	public int inc;

	public Condition(char[] filename, int lineNumber) {
		this.filename = filename;
		this.lineNumber = lineNumber;
	}

	public abstract int getConditionType();
	
	@Override
	public int getLineNumber() {
		return lineNumber;
	}
	@Override
	public void setLineNumber(int lineNumber) {
		this.lineNumber = lineNumber;
	}

	@Override
	public int getNodeType() {
		return getConditionType();
	}

	public abstract boolean include(Scope sc, ScopeDsymbol s, SemanticContext context);

	public abstract Condition syntaxCopy(SemanticContext context);

	public abstract void toCBuffer(OutBuffer buf, HdrGenState hgs,
			SemanticContext context);

}
