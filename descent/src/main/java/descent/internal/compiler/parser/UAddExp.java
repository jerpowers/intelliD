package descent.internal.compiler.parser;

import melnorme.utilbox.tree.TreeVisitor;

import melnorme.utilbox.core.Assert;

import descent.internal.compiler.parser.ast.IASTVisitor;


public class UAddExp extends UnaExp {

	public UAddExp(char[] filename, int lineNumber, Expression e1) {
		super(filename, lineNumber, TOK.TOKuadd, e1);
	}

	@Override
	public void accept0(IASTVisitor visitor) {
		boolean children = visitor.visit(this);
		if (children) {
			TreeVisitor.acceptChildren(visitor, e1);
		}
		visitor.endVisit(this);
	}

	@Override
	public int getNodeType() {
		return UADD_EXP;
	}

	@Override
	public char[] opId(SemanticContext context) {
		return Id.uadd;
	}

	@Override
	public Expression semantic(Scope sc, SemanticContext context) {
		Expression e;

		Assert.isTrue(type == null);
		super.semantic(sc, context);
		e1 = resolveProperties(sc, e1, context);
		e = op_overload(sc, context);
		if (e != null) {
			return e;
		}
		e1.checkNoBool(context);
		e1.checkArithmetic(context);
		return e1;
	}

}
