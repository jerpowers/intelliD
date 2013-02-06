package descent.internal.compiler.parser;

import static descent.internal.compiler.parser.Constfold.Cat;
import static descent.internal.compiler.parser.MATCH.MATCHconst;
import static descent.internal.compiler.parser.TOK.TOKstring;
import static descent.internal.compiler.parser.TY.Tarray;
import static descent.internal.compiler.parser.TY.Tsarray;
import melnorme.utilbox.tree.TreeVisitor;
import descent.core.compiler.IProblem;
import descent.internal.compiler.parser.ast.IASTVisitor;


public class CatExp extends BinExp {

	public CatExp(char[] filename, int lineNumber, Expression e1, Expression e2) {
		super(filename, lineNumber, TOK.TOKtilde, e1, e2);
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
		return CAT_EXP;
	}

	@Override
	public Expression interpret(InterState istate, SemanticContext context) {
		//Expression e;
		Expression e1;
		Expression e2;

		e1 = this.e1.interpret(istate, context);
		if (e1 == EXP_CANT_INTERPRET) {
			return EXP_CANT_INTERPRET; //goto Lcant;
		}
		e2 = this.e2.interpret(istate, context);
		if (e2 == EXP_CANT_INTERPRET) {
			return EXP_CANT_INTERPRET; //goto Lcant;
		}
		return Cat.call(type, e1, e2, context);

		//Lcant:
		//	return EXP_CANT_INTERPRET;
	}

	@Override
	public char[] opId(SemanticContext context) {
		return Id.cat;
	}

	@Override
	public char[] opId_r() {
		return Id.cat_r;
	}

	@Override
	public Expression optimize(int result, SemanticContext context) {
		Expression e;

		e1 = e1.optimize(result, context);
		e2 = e2.optimize(result, context);
		e = Cat.call(type, e1, e2, context);
		if (e == EXP_CANT_INTERPRET) {
			e = this;
		}
		return e;
	}

	@Override
	public Expression semantic(Scope sc, SemanticContext context) {
		if (null != type) {
			return this;
		}

		Expression e;

		super.semanticp(sc, context);
		e = op_overload(sc, context);
		if (null != e) {
			return e;
		}

		Type tb1 = e1.type.toBasetype(context);
		Type tb2 = e2.type.toBasetype(context);

		/* BUG: Should handle things like:
		 *	char c;
		 *	c ~ ' '
		 *	' ' ~ c;
		 */

		if (context.isD1()) {
			if ((tb1.ty == TY.Tsarray || tb1.ty == TY.Tarray)
					&& e2.type.equals(tb1.nextOf())) {
				type = tb1.nextOf().arrayOf(context);
				if (tb2.ty == TY.Tarray) {
					// Make e2 into [e2]
					Expressions elements = new Expressions(1);
					elements.add(e2);
					e2 = new ArrayLiteralExp(e2.filename, e2.lineNumber, elements);
					e2.type = type;
				}
				return this;
			}
	
			else if ((tb2.ty == TY.Tsarray || tb2.ty == TY.Tarray)
					&& e1.type.equals(tb2.nextOf())) {
				type = tb2.nextOf().arrayOf(context);
				if (tb1.ty == TY.Tarray) {
					// Make e1 into [e1]
					Expressions elements = new Expressions(1);
					elements.add(e1);
					e1 = new ArrayLiteralExp(e1.filename, e1.lineNumber, elements);
					e1.type = type;
				}
				return this;
			}
	
			typeCombine(sc, context);
	
			if (type.toBasetype(context).ty == TY.Tsarray) {
				type = type.toBasetype(context).nextOf().arrayOf(context);
			}
	
			if (e1.op == TOK.TOKstring && e2.op == TOK.TOKstring) {
				e = optimize(WANTvalue, context);
			} else if (e1.type.equals(e2.type)
					&& (e1.type.toBasetype(context).ty == TY.Tarray || e1.type
							.toBasetype(context).ty == TY.Tsarray)) {
				e = this;
			} else {
				if (context.acceptsErrors()) {
					context.acceptProblem(Problem.newSemanticTypeError(
							IProblem.CanOnlyConcatenateArrays, this,
							e1.type.toChars(context), e2.type.toChars(context)));
				}
				type = Type.tint32;
				e = this;
			}
			e.type = e.type.semantic(filename, lineNumber, sc, context);
			return e;
		} else {
			if ((tb1.ty == Tsarray || tb1.ty == Tarray) && e2.type.implicitConvTo(tb1.nextOf(), context).ordinal() >= MATCHconst.ordinal()) {
				type = tb1.nextOf().arrayOf(context);
				if (tb2.ty == Tarray) { // Make e2 into [e2]
					e2 = new ArrayLiteralExp(e2.filename, e2.lineNumber, e2);
					e2.type = type;
				}
				return this;
			} else if ((tb2.ty == Tsarray || tb2.ty == Tarray) && e1.type.implicitConvTo(tb2.nextOf(), context).ordinal() >= MATCHconst.ordinal()) {
				type = tb2.nextOf().arrayOf(context);
				if (tb1.ty == Tarray) { // Make e1 into [e1]
					e1 = new ArrayLiteralExp(e1.filename, e1.lineNumber, e1);
					e1.type = type;
				}
				return this;
			}

			if ((tb1.ty == Tsarray || tb1.ty == Tarray) && (tb2.ty == Tsarray || tb2.ty == Tarray) && (tb1.nextOf().mod != 0 || tb2.nextOf().mod != 0)
					&& (tb1.nextOf().mod != tb2.nextOf().mod)) {
				Type t1 = tb1.nextOf().mutableOf(context).constOf(context).arrayOf(context);
				Type t2 = tb2.nextOf().mutableOf(context).constOf(context).arrayOf(context);
				if (e1.op == TOKstring && !((StringExp) e1).committed)
					e1.type = t1;
				else
					e1 = e1.castTo(sc, t1, context);
				if (e2.op == TOKstring && !((StringExp) e2).committed)
					e2.type = t2;
				else
					e2 = e2.castTo(sc, t2, context);
			}

			typeCombine(sc, context);
			type = type.toHeadMutable(context);

			Type tb = type.toBasetype(context);
			if (tb.ty == Tsarray)
				type = tb.nextOf().arrayOf(context);
			if (type.ty == Tarray && tb1.nextOf() != null && tb2.nextOf() != null && tb1.nextOf().mod != tb2.nextOf().mod) {
				type = type.nextOf().toHeadMutable(context).arrayOf(context);
			}
			Type t1 = e1.type.toBasetype(context);
			Type t2 = e2.type.toBasetype(context);
			if (e1.op == TOKstring && e2.op == TOKstring)
				e = optimize(WANTvalue, context);
			else if ((t1.ty == Tarray || t1.ty == Tsarray) && (t2.ty == Tarray || t2.ty == Tsarray))

			{
				e = this;
			} else {
				if (context.acceptsErrors()) {
					context.acceptProblem(Problem.newSemanticTypeError(IProblem.CanOnlyConcatenateArrays, this, e1.type.toChars(context), e2.type
							.toChars(context)));
				}
				type = Type.tint32;
				e = this;
			}
			e.type = e.type.semantic(filename, lineNumber, sc, context);
			return e;
		}
	}
}
