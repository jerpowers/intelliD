package descent.internal.compiler.parser;

import static descent.internal.compiler.parser.Constfold.Cmp;
import static descent.internal.compiler.parser.MATCH.MATCHconst;
import static descent.internal.compiler.parser.TOK.TOKnull;
import static descent.internal.compiler.parser.TY.Tclass;
import static descent.internal.compiler.parser.TY.Tvoid;
import melnorme.utilbox.tree.TreeVisitor;
import descent.core.compiler.IProblem;
import descent.internal.compiler.parser.ast.IASTVisitor;


public class CmpExp extends BinExp {

	public CmpExp(char[] filename, int lineNumber, TOK op, Expression e1, Expression e2) {
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
		return CMP_EXP;
	}

	@Override
	public Expression interpret(InterState istate, SemanticContext context) {
		return interpretCommon2(istate, Cmp, context);
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
		return Id.cmp;
	}

	@Override
	public Expression optimize(int result, SemanticContext context) {
		Expression e;

		if (context.isD1()) {
			e1 = e1.optimize(result, context);
			e2 = e2.optimize(result, context);
			if (e1.isConst() && e2.isConst()) {
				e = Cmp.call(op, type, this.e1, this.e2, context);
			} else {
				e = this;
			}
		} else {
			e1 = e1.optimize(WANTvalue | (result & WANTinterpret), context);
			e2 = e2.optimize(WANTvalue | (result & WANTinterpret), context);

			Expression e1 = fromConstInitializer(result, this.e1, context);
			Expression e2 = fromConstInitializer(result, this.e2, context);

			e = Cmp.call(op, type, e1, e2, context);
			if (e == EXP_CANT_INTERPRET)
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
		
	    if (e1.type.toBasetype(context).ty == Tclass && e2.op == TOKnull ||
	    		e2.type.toBasetype(context).ty == Tclass && e1.op == TOKnull)
	    {
	    	if (context.acceptsErrors()) {
	    		context.acceptProblem(Problem.newSemanticTypeError(IProblem.DoNotUseNullWhenComparingClassTypes, this));
	    	}
	    }
		
		e = op_overload(sc, context);
		if (null != e) {
			if (!e.type.isscalar(context) && e.type.equals(e1.type)) {
				if (context.acceptsErrors()) {
		    		context.acceptProblem(Problem.newSemanticTypeError(IProblem.RecursiveOpCmpExpansion, this));
		    	}
				e = new ErrorExp();
			} else {
				e = new CmpExp(filename, lineNumber, op, e, new IntegerExp(filename, lineNumber, 0, Type.tint32));
				e.copySourceRange(this);
				e = e.semantic(sc, context);
			}
			return e;
		}

		typeCombine(sc, context);
		type = Type.tboolean;

		// Special handling for array comparisons
		t1 = e1.type.toBasetype(context);
		t2 = e2.type.toBasetype(context);
		
		boolean condition;
		if (context.isD1()) {
			condition = (t1.ty == TY.Tarray || t1.ty == TY.Tsarray)
				&& (t2.ty == TY.Tarray || t2.ty == TY.Tsarray);
		} else {
			condition = (t1.ty == TY.Tarray || t1.ty == TY.Tsarray || t1.ty == TY.Tpointer)
				&& (t2.ty == TY.Tarray || t2.ty == TY.Tsarray || t2.ty == TY.Tpointer);
		}
		
		if (condition) {
			if (context.isD1()) {
				if (!t1.nextOf().equals(t2.nextOf())) {
					if (context.acceptsErrors()) {
						context.acceptProblem(Problem.newSemanticTypeError(
								IProblem.ArrayComparisonTypeMismatch, this, t1.nextOf().toChars(context), t2.nextOf().toChars(context)));
					}
				}
			} else {
				if (t1.nextOf().implicitConvTo(t2.nextOf(), context).ordinal() < MATCHconst.ordinal() &&
					    t2.nextOf().implicitConvTo(t1.nextOf(), context).ordinal() < MATCHconst.ordinal() &&
					    (t1.nextOf().ty != Tvoid && t2.nextOf().ty != Tvoid)) {
					if (context.acceptsErrors()) {
						context.acceptProblem(Problem.newSemanticTypeError(
								IProblem.ArrayComparisonTypeMismatch, this, t1.nextOf().toChars(context), t2.nextOf().toChars(context)));
					}
				}
			}
			e = this;
		} else if (t1.ty == TY.Tstruct || t2.ty == TY.Tstruct
				|| (t1.ty == TY.Tclass && t2.ty == TY.Tclass)) {
			if (t2.ty == TY.Tstruct) {
				if (context.acceptsErrors()) {
					context.acceptProblem(Problem.newSemanticTypeError(IProblem.NeedMemberFunctionOpCmpForSymbolToCompare, this, t2
							.toDsymbol(sc, context).kind(), t2.toChars(context)));
				}
			} else {
				if (context.acceptsErrors()) {
					context.acceptProblem(Problem.newSemanticTypeError(IProblem.NeedMemberFunctionOpCmpForSymbolToCompare, this, t1
							.toDsymbol(sc, context).kind(), t1.toChars(context)));
				}
			}
			e = this;
		} else if (t1.iscomplex() || t2.iscomplex()) {
			if (context.acceptsErrors()) {
				context.acceptProblem(Problem.newSemanticTypeError(IProblem.CompareNotDefinedForComplexOperands, this));
			}
			if (context.isD1()) {
				e = new IntegerExp(filename, lineNumber, 0);
			} else {
				e = new ErrorExp();
			}
		} else {
			e = this;
		}

		return e;
	}

}
