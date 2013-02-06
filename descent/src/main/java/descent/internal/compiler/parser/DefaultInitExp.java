package descent.internal.compiler.parser;

import descent.internal.compiler.parser.ast.IASTVisitor;

public abstract class DefaultInitExp extends Expression {

	private final TOK subop;

	public DefaultInitExp(char[] filename, int lineNumber, TOK subop) {
		super(filename, lineNumber, TOK.TOKdefault);
		this.subop = subop;
	}

	@Override
	public int getNodeType() {
		return DEFAULT_INIT_EXP;
	}

	@Override
	protected void accept0(IASTVisitor visitor) {
		visitor.visit(this);
		visitor.endVisit(this);
	}
	
	public abstract Expression resolve(char[] filename, int lineNumber, Scope sc, SemanticContext context);
	
	@Override
	public void toCBuffer(OutBuffer buf, HdrGenState hgs, SemanticContext context) {
		buf.writestring(subop.toString());
	}

}
