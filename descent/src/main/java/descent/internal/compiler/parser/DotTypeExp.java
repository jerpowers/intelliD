package descent.internal.compiler.parser;

import descent.internal.compiler.parser.ast.IASTVisitor;


public class DotTypeExp extends UnaExp {

	public Dsymbol sym;

	public DotTypeExp(char[] filename, int lineNumber, Expression e, Dsymbol s, SemanticContext context) {
		super(filename, lineNumber, TOK.TOKdottype, e);
		this.sym = s;
		this.type = s.getType(context);
	}

	@Override
	public void accept0(IASTVisitor visitor) {
		visitor.visit(this);
		visitor.endVisit(this);
	}

	@Override
	public int getNodeType() {
		return 0;
	}

	@Override
	public Expression semantic(Scope sc, SemanticContext context) {
		super.semantic(sc, context);
		return this;
	}

	@Override
	public void toCBuffer(OutBuffer buf, HdrGenState hgs,
			SemanticContext context) {
		expToCBuffer(buf, hgs, e1, PREC.PREC_primary, context);
		buf.writeByte('.');
		buf.writestring(sym.toChars(context));
	}

}
