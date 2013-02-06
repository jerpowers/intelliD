package descent.internal.compiler.parser;

import descent.internal.compiler.parser.ast.IASTVisitor;


public class DotTemplateExp extends UnaExp {

	public TemplateDeclaration td;

	public DotTemplateExp(char[] filename, int lineNumber, Expression e1, TemplateDeclaration td) {
		super(filename, lineNumber, TOK.TOKdottd, e1);
		this.td = td;
	}

	@Override
	public void accept0(IASTVisitor visitor) {
		visitor.visit(this);
		visitor.endVisit(this);
	}

	@Override
	public int getNodeType() {
		return DOT_TEMPLATE_EXP;
	}

	@Override
	public void toCBuffer(OutBuffer buf, HdrGenState hgs,
			SemanticContext context) {
		expToCBuffer(buf, hgs, e1, PREC.PREC_primary, context);
		buf.writeByte('.');
		buf.writestring(td.toChars(context));
	}

}
