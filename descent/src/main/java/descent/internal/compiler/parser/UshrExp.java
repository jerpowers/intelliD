package descent.internal.compiler.parser;

import static descent.internal.compiler.parser.Constfold.Ushr;
import melnorme.utilbox.tree.TreeVisitor;
import descent.internal.compiler.parser.ast.IASTVisitor;


public class UshrExp extends BinExp {

	public UshrExp(char[] filename, int lineNumber, Expression e1, Expression e2) {
		super(filename, lineNumber, TOK.TOKushr, e1, e2);
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
		return USHR_EXP;
	}
	
	@Override
	public Expression interpret(InterState istate, SemanticContext context) {
		return interpretCommon(istate, Ushr, context);
	}

	@Override
	public char[] opId(SemanticContext context) {
		return Id.ushr;
	}

	@Override
	public char[] opId_r() {
		return Id.ushr_r;
	}

	@Override
	public Expression optimize(int result, SemanticContext context) {
		return shift_optimize(result, this, Ushr, context);
	}

	@Override
	public Expression semantic(Scope sc, SemanticContext context) {
		Expression e;

		if (null == type) {
			super.semanticp(sc, context);

			e = op_overload(sc, context);
			if (null != e) {
				return e;
			}

			e1 = e1.checkIntegral(context);
			e2 = e2.checkIntegral(context);
			e1 = e1.integralPromotions(sc, context);
			e2 = e2.castTo(sc, Type.tshiftcnt, context);
			type = e1.type;
		}
		
		return this;
	}

}
