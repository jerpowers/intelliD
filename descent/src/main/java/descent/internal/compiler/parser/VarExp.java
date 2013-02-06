package descent.internal.compiler.parser;

import static descent.internal.compiler.parser.STC.STCfield;
import static descent.internal.compiler.parser.STC.STClazy;
import static descent.internal.compiler.parser.STC.STCmanifest;
import static descent.internal.compiler.parser.STC.STCvariadic;
import static descent.internal.compiler.parser.TOK.TOKvar;
import static descent.internal.compiler.parser.TY.Tarray;
import static descent.internal.compiler.parser.TY.Tclass;
import static descent.internal.compiler.parser.TY.Tsarray;

import melnorme.utilbox.core.Assert;

import descent.core.compiler.IProblem;
import descent.internal.compiler.parser.ast.IASTVisitor;


public class VarExp extends SymbolExp {

	public VarExp(char[] filename, int lineNumber, Declaration var) {
		this(filename, lineNumber, var, false);
	}

	public VarExp(char[] filename, int lineNumber, Declaration var, boolean hasOverloads) {
		super(filename, lineNumber, TOK.TOKvar, var, hasOverloads);
		this.type = var.type;
	}

	@Override
	public void accept0(IASTVisitor visitor) {
		visitor.visit(this);
		visitor.endVisit(this);
	}

	@Override
	public void checkEscape(SemanticContext context) {
		VarDeclaration v = var.isVarDeclaration();
		if (v != null) {
			Type tb = v.type.toBasetype(context);
			// if reference type
			if (tb.ty == Tarray || tb.ty == Tsarray || tb.ty == Tclass) {
				if ((v.isAuto() || v.isScope()) && !v.noauto()) {
					if (context.acceptsErrors()) {
						context.acceptProblem(Problem.newSemanticTypeError(
								context.isD1() ? IProblem.EscapingReferenceToAutoLocal : IProblem.EscapingReferenceToScopeLocal, this, new String[] { v.toChars(context) }));
					}
				} else if ((v.storage_class & STCvariadic) != 0) {
					if (context.acceptsErrors()) {
						context.acceptProblem(Problem.newSemanticTypeError(
								IProblem.EscapingReferenceToVariadicParameter, this, new String[] { v.toChars(context) }));
					}
				}
			}
		}
	}

	@Override
	public boolean equals(Object o, SemanticContext context) {
		if (this == o) {
			return true;
		}

		if (o instanceof Expression) {
			if (((Expression) o).op == TOKvar) {
				VarExp ne = (VarExp) o;
				if (context.isD2()) {
					return (type.toHeadMutable(context).equals(ne.type.toHeadMutable(context)))
						&& var.equals(ne.var);
				} else {
					return type.equals(ne.type) && var.equals(ne.var);
				}
			}
		}

		return false;
	}

	@Override
	public int getNodeType() {
		return VAR_EXP;
	}

	@Override
	public Expression interpret(InterState istate, SemanticContext context) {
		return getVarExp(filename, lineNumber, istate, var, context);
	}

	@Override
	public boolean isLvalue(SemanticContext context) {
	    if ((var.storage_class & STClazy) != 0)
			return false;
		return true;
	}

	@Override
	public Expression modifiableLvalue(Scope sc, Expression e,
			SemanticContext context) {
		if (context.isD1()) {
			if (sc.incontract != 0 && var.isParameter()) {
				// TODO the start and length of the problem should be different (should be passed by parameter to this function)
				if (context.acceptsErrors()) {
					context.acceptProblem(Problem.newSemanticTypeError(IProblem.CannotModifyParameterInContract, this, new String[] { var.toChars(context) }));
				}
			}
		}

		if (type != null && type.toBasetype(context).ty == Tsarray) {
			// TODO the start and length of the problem should be different (should be passed by parameter to this function)
			if (context.acceptsErrors()) {
				context.acceptProblem(Problem.newSemanticTypeError(IProblem.CannotChangeReferenceToStaticArray, this, new String[] { var.toChars(context) }));
			}
		}

		if (context.isD2()) {
			var.checkModify(filename, lineNumber, sc, type, context);
		} else {
			VarDeclaration v = var.isVarDeclaration();
			if (v != null
					&& v.canassign() == 0
					&& (var.isConst() || (context.global.params.Dversion > 1 && var
							.isFinal()))) {
				if (context.acceptsErrors()) {
					context.acceptProblem(Problem.newSemanticTypeError(
							IProblem.CannotModifyFinalVariable, this, new String[] { var.toChars(context) }));
				}
			}

			if (var.isCtorinit()) { // It's only modifiable if inside the right constructor
				Dsymbol s = sc.func;
				while (true) {
					FuncDeclaration fd = null;
					if (s != null) {
						fd = s.isFuncDeclaration();
					}
					if (fd != null
							&& ((fd.isCtorDeclaration() != null && (var.storage_class & STCfield) != 0) || (fd
									.isStaticCtorDeclaration() != null && (var.storage_class & STCfield) == 0))
							&& fd.toParent() == var.toParent()) {
						VarDeclaration v2 = var.isVarDeclaration();
						Assert.isNotNull(v2);
						v2.ctorinit(true);
					} else {
						if (s != null) {
							s = s.toParent2();
							continue;
						} else {
							/* TODO semantic
							 const char *p = var.isStatic() ? "static " : "";
							 error("can only initialize %sconst %s inside %sconstructor",
							 p, var.toChars(), p);
							 */
						}
					}
					break;
				}
			}
		}

		// See if this expression is a modifiable lvalue (i.e. not const)
		return toLvalue(sc, e, context);
	}

	@Override
	public Expression optimize(int result, SemanticContext context) {
		if (context.isD2()) {
			return fromConstInitializer(result, this, context);
		} else {
			if ((result & WANTinterpret) > 0) {
				return fromConstInitializer(this, context);
			}
			return this;
		}
	}

	@Override
	public void scanForNestedRef(Scope sc, SemanticContext context) {
		VarDeclaration v = var.isVarDeclaration();
		if (null != v)
			v.checkNestedReference(sc, null, 0, context);
	}

	@Override
	public Expression semantic(Scope sc, SemanticContext context) {
		if (type == null) {
			type = var.type;
		}

		VarDeclaration v = var.isVarDeclaration();
		if (v != null) {
			if (context.isD1()) {
				if (v.isConst() && type.toBasetype(context).ty != TY.Tsarray
						&& v.init() != null) {
					ExpInitializer ei = v.init().isExpInitializer();
					if (ei != null) {
						return ei.exp.implicitCastTo(sc, type, context);
					}
				}
			}
			v.checkNestedReference(sc, filename, lineNumber, context);
			if (context.isD2()) {
				if (sc.func != null) {
					/*
					 * Determine if sc.func is pure or if any function that
					 * encloses it is also pure.
					 */
					boolean hasPureParent = false;
					for (FuncDeclaration outerfunc = sc.func; outerfunc != null;) {
						if (outerfunc.isPure()) {
							hasPureParent = true;
							break;
						}
						Dsymbol parent = outerfunc.toParent2();
						if (null == parent)
							break;
						outerfunc = parent.isFuncDeclaration();
					}

					/*
					 * If ANY of its enclosing functions are pure, it cannot do
					 * anything impure. If it is pure, it cannot access any
					 * mutable variables other than those inside itself
					 */
					if (hasPureParent && 0 == sc.intypeof
							&& v.isDataseg(context) && !v.isInvariant(context)) {
						if (context.acceptsErrors()) {
				    		context.acceptProblem(Problem.newSemanticTypeError(IProblem.PureFunctionCannotAccessMutableStaticData, sc.func, sc.func.toChars(context), v.toChars(context)));
				    	}
					} else if (sc.func.isPure() && sc.parent != v.parent
							&& 0 == sc.intypeof && !v.isInvariant(context)
							&& 0 == (v.storage_class & STCmanifest)) {
						if (context.acceptsErrors()) {
				    		context.acceptProblem(Problem.newSemanticTypeError(IProblem.PureNestedFunctionCannotAccessMutableData, sc.func, sc.func.toChars(context), v.toChars(context)));
				    	}
					}
				}
			}
		}
		return this;
	}

	@Override
	public void toCBuffer(OutBuffer buf, HdrGenState hgs,
			SemanticContext context) {
		buf.writestring(var.toChars(context));
	}

	@Override
	public String toChars(SemanticContext context) {
		return var.toChars(context);
	}

	@Override
	public Expression toLvalue(Scope sc, Expression e, SemanticContext context) {
		if ((var.storage_class & STClazy) != 0) {
			if (context.acceptsErrors()) {
				context.acceptProblem(Problem.newSemanticTypeError(IProblem.LazyVariablesCannotBeLvalues, this));
			}
		}
		return this;
	}
}
