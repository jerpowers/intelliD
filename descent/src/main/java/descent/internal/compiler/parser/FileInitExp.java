package descent.internal.compiler.parser;

import descent.internal.compiler.parser.ast.IASTVisitor;

public class FileInitExp extends DefaultInitExp {

	public FileInitExp(char[] filename, int lineNumber) {
		super(filename, lineNumber, TOK.TOKfile);
	}

	@Override
	public int getNodeType() {
		return FILE_INIT_EXP;
	}

	@Override
	protected void accept0(IASTVisitor visitor) {
		visitor.visit(this);
		visitor.endVisit(this);
	}

	@Override
	public Expression semantic(Scope sc, SemanticContext context) {
		type = Type.tchar.invariantOf(context).arrayOf(context);
		return this;
	}

	@Override
	public Expression resolve(char[] filename, int lineNumber, Scope sc, SemanticContext context) {
		char[] s = filename != null ? filename
				: sc.module.ident.toChars().toCharArray();
		Expression e = new StringExp(filename, lineNumber, s);
		e = e.semantic(sc, context);
		e = e.castTo(sc, type, context);
		return e;
	}

}
