package descent.internal.compiler.parser;

import static descent.internal.compiler.parser.Constfold.Equal;
import static descent.internal.compiler.parser.TOK.TOKequal;
import static descent.internal.compiler.parser.TOK.TOKnull;
import static descent.internal.compiler.parser.TY.Tclass;
import melnorme.utilbox.tree.TreeVisitor;
import descent.core.compiler.IProblem;
import descent.internal.compiler.parser.ast.IASTVisitor;


public class EqualExp extends BinExp {

	public EqualExp(char[] filename, int lineNumber, TOK op, Expression e1, Expression e2) {
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
		return EQUAL_EXP;
	}

	@Override
	public Expression interpret(InterState istate, SemanticContext context) {
		return interpretCommon2(istate, Equal, context);
	}

	@Override
	public boolean isBit() {
		return true;
	}

	@Override
	public boolean isCommutative() {
		return true;
	}

	@Override
	public char[] opId(SemanticContext context) {
		return Id.eq;
	}

	@Override
	public Expression optimize(int result, SemanticContext context) {
		Expression e;

		e1 = e1.optimize(WANTvalue | (result & WANTinterpret), context);
		e2 = e2.optimize(WANTvalue | (result & WANTinterpret), context);
		e = this;

		Expression e1 = fromConstInitializer(context.isD1() ? 0 : result, this.e1, context);
		Expression e2 = fromConstInitializer(context.isD1() ? 0 : result, this.e2, context);

		e = Equal.call(op, type, e1, e2, context);
		if (e == EXP_CANT_INTERPRET) {
			e = this;
		}
		return e;
	}

	@Override
	public Expression semantic(Scope sc, SemanticContext context) {
		Expression e;
		Type t1;
		Type t2;

		if (null != type) {
			return this;
		}

		super.semanticp(sc, context);

		/* Before checking for operator overloading, check to see if we're
		 * comparing the addresses of two statics. If so, we can just see
		 * if they are the same symbol.
		 */
		if (e1.op == TOK.TOKaddress && e2.op == TOK.TOKaddress) {
			AddrExp ae1 = (AddrExp) e1;
			AddrExp ae2 = (AddrExp) e2;

			if (ae1.e1.op == TOK.TOKvar && ae2.e1.op == TOK.TOKvar) {
				VarExp ve1 = (VarExp) ae1.e1;
				VarExp ve2 = (VarExp) ae2.e1;

				if (ve1.var == ve2.var) {
					// They are the same, result is 'true' for ==, 'false' for !=
					e = new IntegerExp(filename, lineNumber, (op == TOK.TOKequal) ? 1 : 0,
							Type.tboolean);
					return e;
				}
			}
		}
		
	    if ((e1.type.toBasetype(context).ty == Tclass && e2.op == TOKnull ||
	    		e2.type.toBasetype(context).ty == Tclass && e1.op == TOKnull)) {
	    	if (context.acceptsErrors()) {
	    		context.acceptProblem(Problem.newSemanticTypeError(IProblem.UseTokenInsteadOfTokenWhenComparingWithNull, this, op == TOKequal ? "is" : "!is", op.toString()));
	    	}
	    }

		e = op_overload(sc, context);
		if (null != e) {
			if (op == TOK.TOKnotequal) {
				e = new NotExp(e.filename, e.lineNumber,  e);
				e = e.semantic(sc, context);
			}
			return e;
		}

		e = typeCombine(sc, context);
		type = Type.tboolean;

		// Special handling for array comparisons
		if (context.isD1()) {
			t1 = e1.type.toBasetype(context);
			t2 = e2.type.toBasetype(context);
			if ((t1.ty == TY.Tarray || t1.ty == TY.Tsarray)
					&& (t2.ty == TY.Tarray || t2.ty == TY.Tsarray)) {
				if (!t1.nextOf().equals(t2.nextOf())) {
					if (context.acceptsErrors()) {
						context.acceptProblem(Problem.newSemanticTypeError(
								IProblem.ArrayComparisonTypeMismatch, this, t1.nextOf().toChars(context), t2.nextOf().toChars(context)));
					}
				}
			} else {
				if (!same(e1.type, e2.type, context) && e1.type.isfloating()
						&& e2.type.isfloating()) {
					// Cast both to complex
					e1 = e1.castTo(sc, Type.tcomplex80, context);
					e2 = e2.castTo(sc, Type.tcomplex80, context);
				}
			}
		} else {
		    if (!arrayTypeCompatible(filename, lineNumber, e1.type, e2.type, context)) {
				if (e1.type != e2.type && e1.type.isfloating() && e2.type.isfloating()) {
					// Cast both to complex
					e1 = e1.castTo(sc, Type.tcomplex80, context);
					e2 = e2.castTo(sc, Type.tcomplex80, context);
				}
			}
		}
		
		return e;
	}

}
