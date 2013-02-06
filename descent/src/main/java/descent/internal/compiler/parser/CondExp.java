package descent.internal.compiler.parser;

import static descent.internal.compiler.parser.TOK.TOKstring;
import static descent.internal.compiler.parser.TY.Tarray;
import melnorme.utilbox.tree.TreeVisitor;
import descent.core.compiler.IProblem;
import descent.internal.compiler.parser.ast.IASTVisitor;


public class CondExp extends BinExp {

	public Expression econd, sourceEcond;

	public CondExp(char[] filename, int lineNumber, Expression econd, Expression e1, Expression e2) {
		super(filename, lineNumber, TOK.TOKquestion, e1, e2);
		this.econd = econd;
		this.sourceEcond = econd;
	}

	@Override
	public void accept0(IASTVisitor visitor) {
		boolean children = visitor.visit(this);
		if (children) {
			TreeVisitor.acceptChildren(visitor, sourceEcond);
			TreeVisitor.acceptChildren(visitor, sourceE1);
			TreeVisitor.acceptChildren(visitor, sourceE2);			
		}
		visitor.endVisit(this);
	}
	
	@Override
	public boolean canThrow(SemanticContext context) {
	    return econd.canThrow(context) || e1.canThrow(context) || e2.canThrow(context);
	}

	@Override
	public Expression castTo(Scope sc, Type t, SemanticContext context) {
		Expression e = this;

		if (!same(type, t, context)) {
			if (true || e1.op == TOKstring || e2.op == TOKstring) {
				e = new CondExp(filename, lineNumber, econd, e1.castTo(sc, t, context), e2
						.castTo(sc, t, context));
				e.type = t;
			} else {
				e = super.castTo(sc, t, context);
			}
		}
		return e;
	}

	@Override
	public void checkEscape(SemanticContext context) {
		e1.checkEscape(context);
		e2.checkEscape(context);
	}

	@Override
	public int checkSideEffect(int flag, SemanticContext context) {
		if (flag == 2) {
			int result = econd.checkSideEffect(2, context);
			if (result > 0) {
				return 0;
			}
			result = e1.checkSideEffect(2, context);
			return result > 0 ? result : e2.checkSideEffect(2, context);
		} else {
			econd.checkSideEffect(1, context);
			e1.checkSideEffect(flag, context);
			return e2.checkSideEffect(flag, context);
		}
	}

	@Override
	public Expression checkToBoolean(SemanticContext context) {
		e1 = e1.checkToBoolean(context);
		e2 = e2.checkToBoolean(context);
		return this;
	}

	@Override
	public int getNodeType() {
		return COND_EXP;
	}

	@Override
	public MATCH implicitConvTo(Type t, SemanticContext context) {
		MATCH m1;
		MATCH m2;

		m1 = e1.implicitConvTo(t, context);
		m2 = e2.implicitConvTo(t, context);

		// Pick the worst match
		return (m1.ordinal() < m2.ordinal()) ? m1 : m2;
	}

	@Override
	public Expression interpret(InterState istate, SemanticContext context) {
		Expression e = econd.interpret(istate, context);
		if (e != EXP_CANT_INTERPRET) {
			if (e.isBool(true)) {
				e = e1.interpret(istate, context);
			} else if (e.isBool(false)) {
				e = e2.interpret(istate, context);
			} else {
				e = EXP_CANT_INTERPRET;
			}
		}
		return e;
	}

	@Override
	public Expression modifiableLvalue(Scope sc, Expression e,
			SemanticContext context) {
		if (context.acceptsErrors()) {
			context.acceptProblem(Problem.newSemanticTypeError(
					IProblem.ConditionalExpressionIsNotAModifiableLvalue, this, toChars(context)));
		}
		return this;
	}

	@Override
	public Expression optimize(int result, SemanticContext context) {
		Expression e;

		econd = econd.optimize(WANTflags | (result & WANTinterpret), context);
		if (econd.isBool(true)) {
			e = e1.optimize(result, context);
		} else if (econd.isBool(false)) {
			e = e2.optimize(result, context);
		} else {
			e1 = e1.optimize(result, context);
			e2 = e2.optimize(result, context);
			e = this;
		}
		return e;
	}

	@Override
	public void scanForNestedRef(Scope sc, SemanticContext context) {
		econd.scanForNestedRef(sc, context);
		e1.scanForNestedRef(sc, context);
		e2.scanForNestedRef(sc, context);
	}

	@Override
	public Expression semantic(Scope sc, SemanticContext context) {
		Type t1;
		Type t2;
		int cs0;
		int cs1;

		if (null != type) {
			return this;
		}

		econd = econd.semantic(sc, context);
		econd = resolveProperties(sc, econd, context);
		econd = econd.checkToPointer(context);
		econd = econd.checkToBoolean(context);

		cs0 = sc.callSuper;
		e1 = e1.semantic(sc, context);
		e1 = resolveProperties(sc, e1, context);
		cs1 = sc.callSuper;
		sc.callSuper = cs0;
		e2 = e2.semantic(sc, context);
		e2 = resolveProperties(sc, e2, context);
		sc.mergeCallSuper(filename, lineNumber, cs1, this, context);

		t1 = e1.type;
		t2 = e2.type;

		// If either operand is void, the result is void
		if (t1.ty == TY.Tvoid || t2.ty == TY.Tvoid) {
			type = Type.tvoid;
		}

		else if (same(t1, t2, context)) {
			type = t1;
		}

		else {
			typeCombine(sc, context);

			switch (e1.type.toBasetype(context).ty) {
			case Tcomplex32:
			case Tcomplex64:
			case Tcomplex80:
				e2 = e2.castTo(sc, e1.type, context);
				break;
			}

			switch (e2.type.toBasetype(context).ty) {
			case Tcomplex32:
			case Tcomplex64:
			case Tcomplex80:
				e1 = e1.castTo(sc, e2.type, context);
				break;
			}
			
			if (!context.isD1()) {
				if (type.toBasetype(context).ty == Tarray)
				{
				    e1 = e1.castTo(sc, type, context);
				    e2 = e2.castTo(sc, type, context);
				}
			}
		}

		return this;
	}

	@Override
	public Expression syntaxCopy(SemanticContext context) {
		return new CondExp(filename, lineNumber, econd.syntaxCopy(context), e1.syntaxCopy(context), e2
				.syntaxCopy(context));
	}

	@Override
	public void toCBuffer(OutBuffer buf, HdrGenState hgs,
			SemanticContext context) {
		expToCBuffer(buf, hgs, econd, PREC.PREC_oror, context);
		buf.writestring(" ? ");
		expToCBuffer(buf, hgs, e1, PREC.PREC_expr, context);
		buf.writestring(" : ");
		expToCBuffer(buf, hgs, e2, PREC.PREC_cond, context);
	}

	@Override
	public Expression toLvalue(Scope sc, Expression e, SemanticContext context) {
		PtrExp pe;

		// convert (econd ? e1 : e2) to *(econd ? &e1 : &e2)
		pe = new PtrExp(filename, lineNumber, this, type);

		e1 = e1.addressOf(sc, context);
		e2 = e2.addressOf(sc, context);

		typeCombine(sc, context);
		type = e2.type;

		return pe;
	}

}
