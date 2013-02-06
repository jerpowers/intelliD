package descent.internal.compiler.parser;

import descent.internal.compiler.parser.ast.IASTVisitor;


public class HaltExp extends Expression {

	public HaltExp(char[] filename, int lineNumber) {
		super(filename, lineNumber, TOK.TOKhalt);
	}

	@Override
	public void accept0(IASTVisitor visitor) {
		visitor.visit(this);
		visitor.endVisit(this);
	}

	@Override
	public int checkSideEffect(int flag, SemanticContext context) {
		return 1;
	}

	@Override
	public int getNodeType() {
		return HALT_EXP;
	}

	@Override
	public Expression semantic(Scope sc, SemanticContext context) {
		type = Type.tvoid;
		return this;
	}

	@Override
	public void toCBuffer(OutBuffer buf, HdrGenState hgs,
			SemanticContext context) {
		buf.writestring("halt");
	}

}
