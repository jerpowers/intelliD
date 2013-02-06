package descent.internal.compiler.parser;

import static descent.internal.compiler.parser.Constfold.And;
import static descent.internal.compiler.parser.TOK.TOKslice;
import melnorme.utilbox.tree.TreeVisitor;
import descent.internal.compiler.parser.ast.IASTVisitor;

public class AndExp extends BinExp {

	public AndExp(char[] filename, int lineNumber, Expression e1, Expression e2) {
		super(filename, lineNumber, TOK.TOKand, e1, e2);
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
		return AND_EXP;
	}

	@Override
	public Expression interpret(InterState istate, SemanticContext context) {
		return interpretCommon(istate, And, context);
	}

	@Override
	public boolean isCommutative() {
		return true;
	}

	@Override
	public char[] opId(SemanticContext context) {
		return Id.iand;
	}

	@Override
	public char[] opId_r() {
		return Id.iand_r;
	}

	@Override
	public Expression optimize(int result, SemanticContext context) {
		Expression e;

		e1 = e1.optimize(result, context);
		e2 = e2.optimize(result, context);
		if (e1.isConst() && e2.isConst()) {
			e = And.call(type, e1, e2, context);
		} else {
			e = this;
		}
		return e;
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

			if ((e1.type.toBasetype(context).ty == TY.Tbool)
					&& (e2.type.toBasetype(context).ty == TY.Tbool)) {
				type = e1.type;
				e = this;
			} else {
				typeCombine(sc, context);
				
			    if (e1.op != TOKslice && e2.op != TOKslice) {
					e1.checkIntegral(context);
					e2.checkIntegral(context);
			    }
			}
		}
		
		return this;
	}
	
	@Override
	public void buildArrayIdent(OutBuffer buf, Expressions arguments) {
		/* Evaluate assign expressions left to right
	     */
	    e1.buildArrayIdent(buf, arguments);
	    e2.buildArrayIdent(buf, arguments);
	    buf.writestring("And");
	}
	
	@Override
	public Expression buildArrayLoop(Arguments fparams, SemanticContext context) {
		/* Evaluate assign expressions left to right
	     */
	    Expression ex1 = e1.buildArrayLoop(fparams, context);
	    Expression ex2 = e2.buildArrayLoop(fparams, context);
	    Expression e = new AndExp(null, 0, ex1, ex2);
	    return e;
	}

}
