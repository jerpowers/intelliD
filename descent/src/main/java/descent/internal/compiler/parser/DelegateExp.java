package descent.internal.compiler.parser;

import static descent.internal.compiler.parser.TY.Tdelegate;
import static descent.internal.compiler.parser.TY.Tfunction;
import descent.core.compiler.IProblem;
import descent.internal.compiler.parser.ast.IASTVisitor;


public class DelegateExp extends UnaExp {

	public FuncDeclaration func;
	public boolean hasOverloads;

	public DelegateExp(char[] filename, int lineNumber, Expression e, FuncDeclaration f) {
		this(filename, lineNumber, e, f, false);
	}
	
	public DelegateExp(char[] filename, int lineNumber, Expression e, FuncDeclaration f, boolean hasOverloads) {
		super(filename, lineNumber, TOK.TOKdelegate, e);
		this.func = f;
		this.hasOverloads = hasOverloads;
	}

	@Override
	public void accept0(IASTVisitor visitor) {
		visitor.visit(this);
		visitor.endVisit(this);
	}

	@Override
	public Expression castTo(Scope sc, Type t, SemanticContext context) {
		Type tb;
		Type typeb = null;
		Expression e = this;

		tb = t.toBasetype(context);
		
		if (context.isD1()) {
			type = type.toBasetype(context);
		} else {
			typeb = type.toBasetype(context);
		}
		if (!same(tb, type, context)) {
			// Look for delegates to functions where the functions are
			// overloaded.
			FuncDeclaration f;
			
			boolean condition = context.isD1() ?
					type.ty == Tdelegate && type.nextOf().ty == Tfunction && tb.ty == Tdelegate && tb.nextOf().ty == Tfunction
				:
					typeb.ty == Tdelegate && typeb.nextOf().ty == Tfunction && tb.ty == Tdelegate && tb.nextOf().ty == Tfunction;

			if (condition) {
				if (func != null) {
					f = func.overloadExactMatch(context.isD1() ? tb.nextOf() : tb.nextOf(), context);
					if (f != null) {
						int[] offset = { 0 };
						
						condition = context.isD1() ?
								f.tintro() != null && f.tintro().nextOf().isBaseOf(f.type.nextOf(), offset, context) && offset[0] != 0
							:
								f.tintro() != null && f.tintro().nextOf().isBaseOf(f.type.nextOf(), offset, context) && offset[0] != 0;
						
						if (condition) {
							if (context.acceptsErrors()) {
								context.acceptProblem(Problem.newSemanticTypeError(
										IProblem.CannotFormDelegateDueToCovariantReturnType, this));
							}
						}
						if (!context.isD1()) {
							f.tookAddressOf++;
						}
						e = new DelegateExp(filename, lineNumber, e1, f);
						e.type = t;
						return e;
					}
					if (func.tintro() != null) {
						if (context.acceptsErrors()) {
							context.acceptProblem(Problem.newSemanticTypeError(
									IProblem.CannotFormDelegateDueToCovariantReturnType, this));
						}
					}
				}
			}
			e = super.castTo(sc, t, context);
		} else {
			int[] offset = { 0 };
			
			if (!context.isD1()) {
				func.tookAddressOf++;
			}
			
			boolean condition = context.isD1() ?
					func.tintro() != null && func.tintro().nextOf().isBaseOf(func.type.nextOf(), offset, context) && offset[0] != 0
				:
					func.tintro() != null && func.tintro().nextOf().isBaseOf(func.type.nextOf(), offset, context) && offset[0] != 0;

			if (condition) {
				if (context.acceptsErrors()) {
					context.acceptProblem(Problem.newSemanticTypeError(
							IProblem.CannotFormDelegateDueToCovariantReturnType, this));
				}
			}
			
			e = copy();
			e.type = t;
		}
		
		if (context.isD1()) {
			e.type = t;
		}
		return e;
	}

	@Override
	public int getNodeType() {
		return DELEGATE_EXP;
	}

	@Override
	public MATCH implicitConvTo(Type t, SemanticContext context) {
		MATCH result;

		result = type.implicitConvTo(t, context);

		if (result.ordinal() == 0) {
			// Look for pointers to functions where the functions are
			// overloaded.

			t = t.toBasetype(context);
			if (type.ty == Tdelegate && type.nextOf().ty == Tfunction
					&& t.ty == Tdelegate && t.nextOf().ty == Tfunction) {
				if (func != null
						&& func.overloadExactMatch(t.nextOf(), context) != null) {
					result = MATCH.MATCHexact;
				}
			}
		}
		return result;
	}

	@Override
	public Expression semantic(Scope sc, SemanticContext context) {
		if (type == null) {
			e1 = e1.semantic(sc, context);
			type = new TypeDelegate(func.type);
			type = type.semantic(filename, lineNumber, sc, context);
			AggregateDeclaration ad = func.toParent().isAggregateDeclaration();
			if (context.isD2()) {
				// L10:
				boolean repeat = true;
				while (repeat) {
					repeat = false;

					Type t = e1.type;
					if (func.needThis()
							&& ad != null
							&& !(t.ty == TY.Tpointer
									&& ((TypePointer) t).next.ty == TY.Tstruct && ((TypeStruct) ((TypePointer) t).next).sym == ad)
							&& !(t.ty == TY.Tstruct && ((TypeStruct) t).sym == ad)) {
						ClassDeclaration cd = ad.isClassDeclaration();
						ClassDeclaration tcd = t.isClassHandle();

						if (null == cd
								|| null == tcd
								|| !(tcd == cd || cd.isBaseOf(tcd, null,
										context))) {
							if (tcd != null && tcd.isNested()) { 
								// Try again with outer scope
								e1 = new DotVarExp(filename, lineNumber, e1, tcd.vthis);
								e1 = e1.semantic(sc, context);
								// goto L10;
								repeat = true;
								continue;
							}
							if (context.acceptsErrors()) {
								context
										.acceptProblem(Problem
												.newSemanticTypeError(
														IProblem.ThisForSymbolNeedsToBeType,
														this,
														func.toChars(context),
														ad.toChars(context),
														t.toChars(context)));
							}
						}
					}
				}
			} else {
				if (func.needThis()) {
				    e1 = getRightThis(filename, lineNumber, sc, ad, e1, func, context);
				}
			}
		}
		return this;
	}

	@Override
	public void toCBuffer(OutBuffer buf, HdrGenState hgs,
			SemanticContext context) {
		buf.writeByte('&');
		if (!func.isNested()) {
			expToCBuffer(buf, hgs, e1, PREC.PREC_primary, context);
			buf.writeByte('.');
		}
		buf.writestring(func.toChars(context));
	}

}
