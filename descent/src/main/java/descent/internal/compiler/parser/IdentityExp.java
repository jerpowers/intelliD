package descent.internal.compiler.parser;

import static descent.internal.compiler.parser.Constfold.Identity;
import melnorme.utilbox.tree.TreeVisitor;
import descent.internal.compiler.parser.ast.IASTVisitor;


public class IdentityExp extends BinExp {

	public IdentityExp(char[] filename, int lineNumber, TOK op, Expression e1, Expression e2) {
		super(filename, lineNumber, op, e1, e2);
	}

	@Override
	public void accept0(IASTVisitor visitor) {
		boolean children = visitor.visit(this);
		if (children) {
			TreeVisitor.acceptChildren(visitor, sourceE1);
			TreeVisitor.acceptChildren(visitor, sourceE2);
		}
		visitor.endVisit(this);
	}

	@Override
	public int getNodeType() {
		return IDENTITY_EXP;
	}

	@Override
	public Expression interpret(InterState istate, SemanticContext context) {
		return interpretCommon2(istate, Identity, context);
	}

	@Override
	public boolean isBit() {
		return true;
	}

	@Override
	public Expression optimize(int result, SemanticContext context) {
		Expression e;

		e1 = e1.optimize(WANTvalue | (result & WANTinterpret), context);
		e2 = e2.optimize(WANTvalue | (result & WANTinterpret), context);
		e = this;

		if (this.e1.isConst() && this.e2.isConst()) {
			e = Identity.call(op, type, this.e1, this.e2, context);
		}
		return e;
	}

	@Override
	public Expression semantic(Scope sc, SemanticContext context) {
		if (null != type) {
			return this;
		}

		super.semanticp(sc, context);
		type = Type.tboolean;
		typeCombine(sc, context);
		if (!same(e1.type, e2.type, context) && e1.type.isfloating() && e2.type.isfloating()) {
			// Cast both to complex
			e1 = e1.castTo(sc, Type.tcomplex80, context);
			e2 = e2.castTo(sc, Type.tcomplex80, context);
		}
		
		return this;
	}

}
