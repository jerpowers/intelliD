package descent.internal.compiler.parser;

import static descent.internal.compiler.parser.LINK.LINKd;
import static descent.internal.compiler.parser.MATCH.MATCHconst;
import static descent.internal.compiler.parser.MATCH.MATCHconvert;
import static descent.internal.compiler.parser.MATCH.MATCHexact;
import static descent.internal.compiler.parser.MATCH.MATCHnomatch;
import static descent.internal.compiler.parser.TOK.TOKadd;
import static descent.internal.compiler.parser.TOK.TOKand;
import static descent.internal.compiler.parser.TOK.TOKdelegate;
import static descent.internal.compiler.parser.TOK.TOKint64;
import static descent.internal.compiler.parser.TOK.TOKmin;
import static descent.internal.compiler.parser.TOK.TOKor;
import static descent.internal.compiler.parser.TOK.TOKxor;
import static descent.internal.compiler.parser.TY.Tbit;
import static descent.internal.compiler.parser.TY.Tbool;
import static descent.internal.compiler.parser.TY.Tclass;
import static descent.internal.compiler.parser.TY.Tint32;
import static descent.internal.compiler.parser.TY.Tpointer;
import static descent.internal.compiler.parser.TY.Treference;
import static descent.internal.compiler.parser.TY.Tsarray;
import static descent.internal.compiler.parser.TY.Tstruct;
import static descent.internal.compiler.parser.TY.Tvoid;

import java.util.ArrayList;
import java.util.List;

import descent.core.compiler.IProblem;
import descent.internal.compiler.parser.Constfold.BinExp_fp;


public abstract class Expression extends ASTDmdNode implements Cloneable {

	public class Parenthesis {
		public int startPosition;
		public int length;

		public Parenthesis(int startPosition, int length) {
			this.startPosition = startPosition;
			this.length = length;
		}
	}

	public static Expressions arraySyntaxCopy(Expressions exps,
			SemanticContext context) {
		Expressions a = null;

		if (exps != null) {
			a = new Expressions(exps.size());
			a.setDim(exps.size());
			for (int i = 0; i < exps.size(); i++) {
				Expression e = exps.get(i);
				e = e.syntaxCopy(context);
				a.set(i, e);
			}
		}
		return a;
	}

	public static Expression build_overload(char[] filename, int lineNumber, Scope sc,
			Expression ethis, Expression earg, IdentifierExp id,
			SemanticContext context) {
		Expression e;

		e = new DotIdExp(filename, lineNumber, ethis, id);

		if (earg != null) {
			e = new CallExp(filename, lineNumber, e, earg);
		} else {
			e = new CallExp(filename, lineNumber, e);
		}

		e = e.semantic(sc, context);
		return e;
	}

	public static Expression build_overload(char[] filename, int lineNumber, Scope sc,
			Expression ethis, Expression earg, char[] id,
			SemanticContext context) {
		return build_overload(filename, lineNumber, sc, ethis, earg, new IdentifierExp(id),
				context);
	}

	public static Expression combine(Expression e1, Expression e2) {
		if (e1 != null) {
			if (e2 != null) {
				e1 = new CommaExp(e1.filename, e1.lineNumber, e1, e2);
				e1.type = e2.type;
			}
		} else {
			e1 = e2;
		}
		return e1;
	}

	public int lineNumber;
	public char[] filename;
	public TOK op;
	public Type type, sourceType;
	public List<Parenthesis> parenthesis;

	public Expression(char[] filename, int lineNumber, TOK op) {
		this.lineNumber = lineNumber;
		this.filename = filename;
		this.op = op;
		this.type = null;
	}

	public void addParenthesis(int startPosition, int length) {
		if (parenthesis == null) {
			parenthesis = new ArrayList<Parenthesis>();
		}
		parenthesis.add(new Parenthesis(startPosition, length));
	}

	public Expression addressOf(Scope sc, SemanticContext context) {
		Expression e;

		e = toLvalue(sc, null, context);
		e = new AddrExp(filename, lineNumber, e);
		e.type = type.pointerTo(context);
		return e;
	}
	
	public boolean canThrow(SemanticContext context) {
		if (context.isD2()) {
			return false;
		} else {
			return true;
		}
	}

	public Expression castTo(Scope sc, Type t, SemanticContext context) {
		return Expression_castTo(sc, t, context);
	}
	
	public final Expression Expression_castTo(Scope sc, Type t, SemanticContext context) {
		if (same(type, t, context)) {
			return this;
		}

		Expression e = this;
		Type tb = t.toBasetype(context);
		Type typeb = type.toBasetype(context);
		if (!same(tb, typeb, context)) {
			// Do (type *) cast of (type [dim])
			if (tb.ty == Tpointer && typeb.ty == Tsarray) {
				if (typeb.size(filename, lineNumber, context) == 0) {
					e = new NullExp(filename, lineNumber);
				} else {
					e = new AddrExp(filename, lineNumber, e);
				}
			} else {
				if (!context.isD1()) {
					if (typeb.ty == Tstruct) {
						TypeStruct ts = (TypeStruct) typeb;
						if (!(tb.ty == Tstruct && ts.sym == ((TypeStruct) tb).sym)
								&& ts.sym.aliasthis != null) {
							/*
							 * Forward the cast to our alias this member,
							 * rewrite to: cast(to)e1.aliasthis
							 */
							Expression e1 = new DotIdExp(filename, lineNumber, this,
									ts.sym.aliasthis.ident);
							e = new CastExp(filename, lineNumber, e1, tb);
							e = e.semantic(sc, context);
							return e;
						}
					} else if (typeb.ty == Tclass) {
						TypeClass ts = (TypeClass) typeb;
						if (tb.ty != Tclass && ts.sym.aliasthis != null) {
							/*
							 * Forward the cast to our alias this member,
							 * rewrite to: cast(to)e1.aliasthis
							 */
							Expression e1 = new DotIdExp(filename, lineNumber, this,
									ts.sym.aliasthis.ident);
							e = new CastExp(filename, lineNumber, e1, tb);
							e = e.semantic(sc, context);
							return e;
						}
					}
				}
				e = new CastExp(filename, lineNumber, e, tb);
			}
		} else {
			e = e.copy(); // because of COW for assignment to e.type
		}
		assert (e != this);
		e.type = t;

		// Descent
		e.copySourceRange(this);

		return e;
	}

	public Expression checkArithmetic(SemanticContext context) {
		if (!type.isintegral() && !type.isfloating()) {
			if (context.acceptsErrors()) {
				context.acceptProblem(Problem.newSemanticTypeError(
						IProblem.SymbolIsNotAnArithmeticTypeItIs, this,
						new String[] { toChars(context), type.toChars(context) }));
				return new IntegerExp(0);
			}
		}
		return this;
	}

	public void checkDeprecated(Scope sc, Dsymbol s, SemanticContext context) {
		s.checkDeprecated(sc, context, this);
	}

	public void checkEscape(SemanticContext context) {
		// empty
	}

	public Expression checkIntegral(SemanticContext context) {
		if (!type.isintegral()) {
			if (context.acceptsErrors()) {
				context.acceptProblem(Problem.newSemanticTypeError(
						IProblem.SymbolIsNotOfIntegralType, this, new String[] {
								toChars(context), type.toChars(context) }));
			}
			if (context.isD1()) {
				return new IntegerExp(filename, lineNumber, 0);
			} else {
				return new ErrorExp();
			}
		}
		return this;
	}

	public void checkNoBool(SemanticContext context) {
		if (type.toBasetype(context).ty == Tbool) {
			if (context.acceptsErrors()) {
				context.acceptProblem(Problem.newSemanticTypeError(
						IProblem.OperationNotAllowedOnBool, this,
						new String[] { toChars(context) }));
			}
		}
	}
	
	public void checkPurity(Scope sc, FuncDeclaration f, SemanticContext context) {
		if (sc.func != null) {
			FuncDeclaration outerfunc = sc.func;
			while (outerfunc.toParent2() != null
					&& outerfunc.toParent2().isFuncDeclaration() != null) {
				outerfunc = outerfunc.toParent2().isFuncDeclaration();
			}
			if (outerfunc.isPure() && 0 == sc.intypeof
					&& (!f.isNested() && !f.isPure()))
				if (context.acceptsErrors()) {
					context
							.acceptProblem(Problem
									.newSemanticTypeError(
											IProblem.PureFunctionCannotCallImpure,
											this, sc.func.toChars(context), "function", f.toChars(context)));
				}
		}
	}

	public void checkScalar(SemanticContext context) {
		if (!type.isscalar(context)) {
			if (context.acceptsErrors()) {
				context.acceptProblem(Problem.newSemanticTypeError(
						IProblem.SymbolIsNotAScalar, this, new String[] {
								toChars(context), type.toChars(context) }));
			}
		}
	}

	public int checkSideEffect(int flag, SemanticContext context) {
		if (flag == 0) {
			if (context.acceptsErrors()) {
				context.acceptProblem(Problem.newSemanticTypeError(
						IProblem.ExpressionHasNoEffect, this));
			}
		}
		return 0;
	}

	public Expression checkToBoolean(SemanticContext context) {
		if (!type.checkBoolean(context)) {
			if (context.acceptsErrors()) {
				context.acceptProblem(Problem.newSemanticTypeError(
						IProblem.ExpressionOfTypeDoesNotHaveABooleanValue, this,
						new String[] { toChars(context), type.toChars(context) }));
			}
		}
		return this;
	}

	public Expression checkToPointer(SemanticContext context) {
		Expression e;
		Type tb;

		e = this;

		// If C static array, convert to pointer
		tb = type.toBasetype(context);
		if (tb.ty == Tsarray) {
			TypeSArray ts = (TypeSArray) tb;
			if (ts.size(filename, lineNumber, context) == 0) {
				e = new NullExp(filename, lineNumber);
			} else {
				e = new AddrExp(filename, lineNumber, this);
			}
			e.type = ts.next.pointerTo(context);
		}
		return e;
	}

	public boolean compare(Expression exp) {
		// TODO semantic
		return false;
	}

	public Expression copy() {
		try {
			Expression exp = (Expression) clone();
			exp.copySourceRange(this);
			return exp;
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}

	public Expression deref() {
		if (type.ty == Treference) {
			Expression e;

			e = new PtrExp(filename, lineNumber, this);
			e.type = type.nextOf();
			return e;
		}
		return this;
	}

	@Override
	public DYNCAST dyncast() {
		return DYNCAST.DYNCAST_EXPRESSION;
	}

	public Expression implicitCastTo(Scope sc, Type t, SemanticContext context) {
		MATCH match = implicitConvTo(t, context);
		if (match != MATCHnomatch) {
			TY tyfrom = type.toBasetype(context).ty;
			TY tyto = t.toBasetype(context).ty;
			if (context.global.params.warnings
					&& Type.impcnvWarn[tyfrom.ordinal()][tyto.ordinal()]
					&& op != TOKint64) {
				Expression e = optimize(WANTflags | WANTvalue, context);

				if (e.op == TOKint64) {
					return e.implicitCastTo(sc, t, context);
				}

				if (tyfrom == Tint32 &&
			    		(op == TOKadd || op == TOKmin ||
			    		 op == TOKand || op == TOKor || op == TOKxor)
			    	       ) {
			    		/* This is really only a semi-kludge fix,
			    		 * we really should look at the operands of op
			    		 * and see if they are narrower types.
			    		 * For example, b=b|b and b=b|7 and s=b+b should be allowed,
			    		 * but b=b|i should be an error.
			    		 */
			    		;
	    	    }
	    	    else
	    	    {
					if (context.acceptsWarnings()) {
						context.acceptProblem(Problem.newSemanticTypeWarning(
								IProblem.ImplicitConversionCanCauseLossOfData, 0,
								start, length, new String[] { toChars(context),
										type.toChars(context), t.toChars(context) }));
					}
			    }
			}
			if (context.isD2()) {
				if (match == MATCHconst && t == type.constOf(context)) {
					Expression e = copy();
					e.type = t;
					return e;
				}
			}
			return castTo(sc, t, context);
		}

		Expression e = optimize(WANTflags | WANTvalue, context);
		if (e != this) {
			return e.implicitCastTo(sc, t, context);
		}
		
		// Added for Descent
		if (t == null && context.global.errors > 0) {
			return new IntegerExp(0);
		}

		if (t.deco == null) { /*
		 * Can happen with: enum E { One } class A {
		 * static void fork(EDG dg) { dg(E.One); } alias
		 * void delegate(E) EDG; } Should eventually
		 * make it work.
		 */
			if (context.acceptsErrors()) {
				context.acceptProblem(Problem.newSemanticTypeError(
						IProblem.ForwardReferenceToType, this, new String[] { t
								.toChars(context) }));
			}
		} else if (t.reliesOnTident() != null) {
			if (context.acceptsErrors()) {
				context.acceptProblem(Problem.newSemanticTypeError(
						IProblem.ForwardReferenceToType, this, new String[] { t
								.reliesOnTident().toChars(context) }));
			}
		}

		if (context.acceptsErrors()) {
			context.acceptProblem(Problem.newSemanticTypeError(
					IProblem.CannotImplicitlyConvert, this, new String[] {
							toChars(context), type.toChars(context),
							t.toChars(context) }));
		}

		return castTo(sc, t, context);
	}

	public MATCH implicitConvTo(Type t, SemanticContext context) {
		if (type == null) {
			if (context.acceptsWarnings()) {
				context.acceptProblem(Problem.newSemanticTypeWarning(
						IProblem.SymbolNotAnExpression, 0, start, length,
						new String[] { toChars(context) }));
			}
			type = Type.terror;
		}

		if (context.isD1()) {
			// Added for Descent
			if (t == null && context.global.errors > 0) {
				return MATCHnomatch;
			}
			if (t.ty == Tbit && isBit()) {
				return MATCHconvert;
			}
		}
		Expression e = optimize(WANTvalue | WANTflags, context);
	    if (context.isD2()) {
	    	if (e.type == t) {
	    		return MATCHexact;
	    	}
	    }
		
		if (e != this) {
			return e.implicitConvTo(t, context);
		}
		MATCH match = type.implicitConvTo(t, context);
		if (match != MATCHnomatch) {
			return match;
		}
		return MATCHnomatch;
	}

	public Expression integralPromotions(Scope sc, SemanticContext context) {
		Expression e = this;
		switch (type.toBasetype(context).ty) {
		case Tvoid:
			if (context.acceptsErrors()) {
				context.acceptProblem(Problem.newSemanticTypeError(
						IProblem.SymbolHasNoValue, this, new String[] { "void" }));
			}
			break;

		case Tint8:
		case Tuns8:
		case Tint16:
		case Tuns16:
		case Tbit:
		case Tbool:
		case Tchar:
		case Twchar:
			e = e.castTo(sc, Type.tint32, context);
			break;

		case Tdchar:
			e = e.castTo(sc, Type.tuns32, context);
			break;
		}
		return e;
	}

	public Expression interpret(InterState istate, SemanticContext context) {
		return EXP_CANT_INTERPRET;
	}

	public boolean isBit() {
		return false;
	}

	public boolean isBool(boolean result) {
		return false;
	}

	public boolean isCommutative() {
		return false; // default is no reverse
	}

	public boolean isConst() {
		return false;
	}
	
	public boolean isLvalue(SemanticContext context) {
		return false;
	}

	public Expression modifiableLvalue(Scope sc, Expression e,
			SemanticContext context) {
		// See if this expression is a modifiable lvalue (i.e. not const)
		if (context.isD2()) {
			if (type != null && (!type.isMutable() || !type.isAssignable(context))) {
				if (context.acceptsErrors()) {
					context.acceptProblem(Problem.newSemanticTypeError(IProblem.SymbolIsNotMutable, e, e.toChars(context)));
				}
			}
		}
		
		return toLvalue(sc, e, context);
	}

	public char[] opId(SemanticContext context) {
		throw new IllegalStateException("assert(0);");
	}

	public char[] opId_r() {
		return null;
	}

	public Expression optimize(int result, SemanticContext context) {
		return this;
	}

	public void rvalue(SemanticContext context) {
		if (type != null && type.toBasetype(context).ty == Tvoid) {
			if (context.acceptsErrors()) {
				context.acceptProblem(Problem.newSemanticTypeError(
						IProblem.ExpressionIsVoidAndHasNoValue, this,
						new String[] { toChars(context) }));
			}
			type = Type.tint32;
		}
	}

	public void scanForNestedRef(Scope sc, SemanticContext context) {
		// empty
	}

	public Expression semantic(Scope sc, SemanticContext context) {
		if (type != null) {
			type = type.semantic(filename, lineNumber, sc, context);
		} else {
			type = Type.tvoid;
		}
		return this;
	}

	public Expression syntaxCopy(SemanticContext context) {
		return copy();
	}

	public void toCBuffer(OutBuffer buf, HdrGenState hgs,
			SemanticContext context) {
		buf.writestring(op.toString());
	}

	@Override
	public String toChars(SemanticContext context) {
		OutBuffer buf = new OutBuffer();
		HdrGenState hgs = new HdrGenState();
		toCBuffer(buf, hgs, context);
		return buf.toChars();
	}

	public complex_t toComplex(SemanticContext context) {
		if (context.acceptsErrors()) {
			context.acceptProblem(Problem.newSemanticTypeError(
					IProblem.FloatingPointConstantExpressionExpected, this,
					new String[] { toChars(context) }));
		}
		return complex_t.ZERO;
	}

	public Expression toDelegate(Scope sc, Type t, SemanticContext context) {
		TypeFunction tf = new TypeFunction(null, t, 0, LINKd);
		FuncLiteralDeclaration fld = new FuncLiteralDeclaration(filename, lineNumber, tf,
				TOKdelegate, null);
		Expression e;
		sc = sc.push();
		sc.parent = fld; // set current function to be the delegate
		e = this;
		e.scanForNestedRef(sc, context);
		sc = sc.pop();
		Statement s = new ReturnStatement(filename, lineNumber, e);
		fld.fbody = s;
		e = new FuncExp(filename, lineNumber, fld);
		e = e.semantic(sc, context);
		return e;
	}

	public real_t toImaginary(SemanticContext context) {
		if (context.acceptsErrors()) {
			context.acceptProblem(Problem.newSemanticTypeError(
					IProblem.FloatingPointConstantExpressionExpected, this,
					new String[] { toChars(context) }));
		}
		return real_t.ZERO;
	}

	public integer_t toInteger(SemanticContext context) {
		if (context.acceptsErrors()) {
			context.acceptProblem(Problem.newSemanticTypeError(
					IProblem.IntegerConstantExpressionExpected, this));
		}
		return integer_t.ZERO;
	}

	public Expression toLvalue(Scope sc, Expression e, SemanticContext context) {
		if (e == null) {
			e = this;
		} else if (filename == null) {
			filename = e.filename;
			lineNumber = e.lineNumber;
		}
		if (context.acceptsErrors()) {
			context.acceptProblem(Problem.newSemanticTypeError(
					IProblem.NotAnLvalue, e, new String[] { e.toChars(context) }));
		}
		return this;
	}

	public void toMangleBuffer(OutBuffer buf, SemanticContext context) {
		if (context.acceptsErrors()) {
			context.acceptProblem(Problem.newSemanticTypeError(
					IProblem.ExpressionIsNotAValidTemplateValueArgument, this,
					new String[] { toChars(context) }));
		}
	}

	public real_t toReal(SemanticContext context) {
		if (context.acceptsErrors()) {
			context.acceptProblem(Problem.newSemanticTypeError(
					IProblem.FloatingPointConstantExpressionExpected, this,
					new String[] { toChars(context) }));
		}
		return real_t.ZERO;
	}

	public integer_t toUInteger(SemanticContext context) {
		return toInteger(context).castToUns64();
	}
	
	public Expression trySemantic(Scope sc, SemanticContext context) {
		int errors = context.global.errors;
		context.global.gag++;
		Expression e = semantic(sc, context);
		context.global.gag--;
		if (errors != context.global.errors) {
			context.global.errors = errors;
			e = null;
		}
		return e;
	}
	
	public static Expression shift_optimize(int result, BinExp e, BinExp_fp fp,
			SemanticContext context) {
		Expression ex = e;

		e.e1 = e.e1.optimize(result, context);
		e.e2 = e.e2.optimize(result, context);
		if (e.e2.isConst()) {
			integer_t i2 = e.e2.toInteger(context);
			integer_t sz = new integer_t(e.e1.type.size(context)).multiply(8);
			if (i2.compareTo(0) < 0 || i2.compareTo(sz) > 0) {
				if (context.acceptsErrors()) {
					context.acceptProblem(Problem.newSemanticTypeError(
							IProblem.ShiftAssignIsOutsideTheRange, e,
							new String[] { i2.toString(), sz.toString() }));
				}
				e.e2 = new IntegerExp(0);
			}
			if (e.e1.isConst())
				ex = fp.call(e.type, e.e1, e.e2, context);
		}
		return ex;
	}
	
	/******************************************
	 * Construct the identifier for the array operation function,
	 * and build the argument list to pass to it.
	 */
	public void buildArrayIdent(OutBuffer buf, Expressions arguments) {
		buf.writestring("Exp");
		arguments.shift(this);
	}
	
	/******************************************
	 * Construct the inner loop for the array operation function,
	 * and build the parameter list.
	 */
	public Expression buildArrayLoop(Arguments fparams, SemanticContext context) {
		IdentifierExp id = context.generateId("c", size(fparams));
		Argument param = new Argument(0, type, id, null);
		fparams.shift(param);
		Expression e = new IdentifierExp(null, 0, id);
		return e;
	}

	@Override
	public int getLineNumber() {
		return lineNumber;
	}
	@Override
	public void setLineNumber(int lineNumber) {
		this.lineNumber = lineNumber;
	}
	
	@Override
	public final boolean equals(Object arg0) {
		return super.equals(arg0);
	}
	
	public boolean equals(Object o, SemanticContext context) {
		return super.equals(o);
	}

	public void setResolvedSymbol(Dsymbol symbol, SemanticContext context) {

	}

	public void setEvaluatedExpression(Expression exp, SemanticContext context) {

	}

	public void setResolvedExpression(Expression exp, SemanticContext context) {

	}

}
