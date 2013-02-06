package descent.internal.compiler.parser;

import static descent.internal.compiler.parser.BE.BEreturn;
import static descent.internal.compiler.parser.BE.BEthrow;
import static descent.internal.compiler.parser.STC.STCout;
import static descent.internal.compiler.parser.STC.STCref;
import static descent.internal.compiler.parser.Scope.CSXany_ctor;
import static descent.internal.compiler.parser.Scope.CSXreturn;
import static descent.internal.compiler.parser.Scope.CSXsuper_ctor;
import static descent.internal.compiler.parser.Scope.CSXthis_ctor;
import static descent.internal.compiler.parser.TOK.TOKcomplex80;
import static descent.internal.compiler.parser.TOK.TOKfloat64;
import static descent.internal.compiler.parser.TOK.TOKimaginary80;
import static descent.internal.compiler.parser.TOK.TOKint64;
import static descent.internal.compiler.parser.TOK.TOKnull;
import static descent.internal.compiler.parser.TOK.TOKstring;
import static descent.internal.compiler.parser.TOK.TOKsuper;
import static descent.internal.compiler.parser.TOK.TOKthis;
import static descent.internal.compiler.parser.TOK.TOKvar;
import static descent.internal.compiler.parser.TY.Tstruct;
import static descent.internal.compiler.parser.TY.Tvoid;

import java.util.ArrayList;

import melnorme.utilbox.tree.TreeVisitor;

import melnorme.utilbox.core.Assert;

import descent.core.compiler.IProblem;
import descent.internal.compiler.parser.ast.IASTVisitor;


public class ReturnStatement extends Statement {

	public Expression exp, sourceExp;

	public ReturnStatement(char[] filename, int lineNumber, Expression exp) {
		super(filename, lineNumber);
		this.exp = exp;
		this.sourceExp = exp;
	}

	@Override
	public void accept0(IASTVisitor visitor) {
		boolean children = visitor.visit(this);
		if (children) {
			TreeVisitor.acceptChildren(visitor, sourceExp);
		}
		visitor.endVisit(this);
	}

	@Override
	public int blockExit(SemanticContext context) {
		int result = BEreturn;

	    if (exp != null && exp.canThrow(context)) {
	    	result |= BEthrow;
	    }
	    return result;
	}

	@Override
	public int getNodeType() {
		return RETURN_STATEMENT;
	}

	@Override
	public Expression interpret(InterState istate, SemanticContext context) {
		// START()
		if (istate.start != null) {
			if (istate.start != this) {
				return null;
			}
			istate.start = null;
		}
		// START()
		if (null == exp) {
			return EXP_VOID_INTERPRET;
		}
		return exp.interpret(istate, context);
	}

	@Override
	public ReturnStatement isReturnStatement() {
		return this;
	}

	@Override
	public Statement semantic(Scope sc, SemanticContext context) {
		FuncDeclaration fd = (FuncDeclaration) sc.parent.isFuncDeclaration();
		Scope scx = sc;
		int implicit0 = 0;

		if (sc.fes != null) {
			// Find scope of function foreach is in
			for (; true; scx = scx.enclosing) {
				Assert.isNotNull(scx);
				if (scx.func != fd) {
					fd = scx.func; // fd is now function enclosing foreach
					break;
				}
			}
		}

		Type tret = fd.type.nextOf();
		if (fd.tintro != null) {
			tret = fd.tintro.nextOf();
		}
		Type tbret = null;

		if (tret != null) {
			tbret = tret.toBasetype(context);
		}

		// main() returns 0, even if it returns void
		if (exp == null && (tbret == null || tbret.ty == Tvoid) && fd.isMain()) {
			implicit0 = 1;
			exp = new IntegerExp(filename, lineNumber, 0);
		}

		if (sc.incontract != 0 || scx.incontract != 0) {
			if (context.acceptsErrors()) {
				context.acceptProblem(Problem.newSemanticTypeError(
						IProblem.ReturnStatementsCannotBeInContracts, this));
			}
		}
		if (sc.tf != null || scx.tf != null) {
			if (context.acceptsErrors()) {
				context.acceptProblem(Problem.newSemanticTypeError(IProblem.ReturnStatementsCannotBeInFinallyScopeExitOrScopeSuccessBodies, this));
			}
		}

		if (fd.isCtorDeclaration() != null) {
			// Constructors implicitly do:
			//	return this;
			if (exp != null && exp.op != TOKthis) {
				if (context.acceptsErrors()) {
					context.acceptProblem(Problem.newSemanticTypeError(IProblem.CannotReturnExpressionFromConstructor, this));
				}
			}
			exp = new ThisExp(filename, lineNumber);
		}

		if (exp == null) {
			fd.nrvo_can = 0;
		}

		if (exp != null) {
			fd.hasReturnExp |= 1;

			exp = exp.semantic(sc, context);
			exp = resolveProperties(sc, exp, context);
			exp = exp.optimize(WANTvalue, context);

			if (fd.nrvo_can != 0 && exp.op == TOKvar) {
				VarExp ve = (VarExp) exp;
				VarDeclaration v = ve.var.isVarDeclaration();

				if (context.isD1()) {
					if (v == null || v.isOut() || v.isRef()) {
						fd.nrvo_can = 0;
					} else if (fd.nrvo_var == null) {
						if (!v.isDataseg(context) && !v.isParameter()
								&& v.toParent2() == fd) {
							fd.nrvo_var = v;
						} else {
							fd.nrvo_can = 0;
						}
					} else if (fd.nrvo_var != v) {
						fd.nrvo_can = 0;
					}
				} else {
					if (((TypeFunction) fd.type).isref)
						// Function returns a reference
						fd.nrvo_can = 0;
					else if (null == v || v.isOut() || v.isRef())
						fd.nrvo_can = 0;
					else if (tbret.ty == Tstruct && ((TypeStruct) tbret).sym.dtor != null)
						// Struct being returned has destructors
						fd.nrvo_can = 0;
					else if (fd.nrvo_var == null) {
						if (!v.isDataseg(context) && !v.isParameter() && v.toParent2() == fd) {
							fd.nrvo_var = v;
						} else
							fd.nrvo_can = 0;
					} else if (fd.nrvo_var != v)
						fd.nrvo_can = 0;
				}
			} else {
				fd.nrvo_can = 0;
			}

			if (fd.returnLabel != null && tbret.ty != Tvoid) {
			} else if (fd.inferRetType) {
				if (fd.type.nextOf() != null) {
					if (!exp.type.equals(fd.type.nextOf())) {
						if (context.acceptsErrors()) {
							context.acceptProblem(Problem.newSemanticTypeError(
									IProblem.MismatchedFunctionReturnTypeInference, sourceExp, new String[] { exp.type.toChars(context), fd.type.nextOf().toChars(context) }));
						}
					}
				} else {
					((TypeFunction)fd.type).next = exp.type;
					fd.type = fd.type.semantic(filename, lineNumber, sc, context);
					if (fd.tintro == null) {
						tret = fd.type.nextOf();
						tbret = tret.toBasetype(context);
					}
				}
			} else if (tbret.ty != Tvoid) {
				exp = exp.implicitCastTo(sc, tret, context);
				if (context.isD2()) {
				    exp = exp.optimize(WANTvalue, context);
				}
			}
		} else if (fd.inferRetType) {
			if (fd.type.nextOf() != null) {
				if (fd.type.nextOf().ty != Tvoid) {
					if (context.acceptsErrors()) {
						context.acceptProblem(Problem.newSemanticTypeError(
								IProblem.MismatchedFunctionReturnTypeInference, this, new String[] { "void", fd.type.nextOf().toChars(context) }));
					}
				}
			} else {
				((TypeFunction)fd.type).next = Type.tvoid;
				fd.type = fd.type.semantic(filename, lineNumber, sc, context);
				if (fd.tintro == null) {
					tret = Type.tvoid;
					tbret = tret;
				}
			}
		} else if (tbret.ty != Tvoid) { // if non-void return
			if (context.acceptsErrors()) {
				context.acceptProblem(Problem.newSemanticTypeError(
						IProblem.ReturnExpressionExpected, this));
			}
		}

		if (sc.fes != null) {
			Statement s;

			if (exp != null && implicit0 == 0) {
				exp = exp.implicitCastTo(sc, tret, context);
			}
			if (exp == null || exp.op == TOKint64 || exp.op == TOKfloat64
					|| exp.op == TOKimaginary80 || exp.op == TOKcomplex80
					|| exp.op == TOKthis || exp.op == TOKsuper
					|| exp.op == TOKnull || exp.op == TOKstring) {
				if (sc.fes.cases == null) {
					sc.fes.cases = new ArrayList(1);
				}
				sc.fes.cases.add(this);
				s = new ReturnStatement(filename, lineNumber, new IntegerExp(filename, lineNumber, sc.fes.cases
						.size() + 1));
			} else if (fd.type.nextOf().toBasetype(context) == Type.tvoid) {
				s = new ReturnStatement(filename, lineNumber, null);
				sc.fes.cases.add(s);

				// Construct: { exp; return cases.dim + 1; }
				Statement s1 = new ExpStatement(filename, lineNumber, exp);
				Statement s2 = new ReturnStatement(filename, lineNumber, new IntegerExp(filename, lineNumber, sc.fes.cases
						.size() + 1));
				s = new CompoundStatement(filename, lineNumber, s1, s2);
			} else {
				// Construct: return vresult;
				if (fd.vresult == null) {
					VarDeclaration v = new VarDeclaration(filename, lineNumber, tret, Id.result, null);
					v.noauto = true;
					v.semantic(scx, context);
					if (scx.insert(v) == null) {
						melnorme.utilbox.core.Assert.isTrue(false);
					}
					v.parent = fd;
					fd.vresult = v;
				}

				s = new ReturnStatement(filename, lineNumber, new VarExp(filename, lineNumber, fd.vresult));

				if (sc.fes.cases == null) {
					sc.fes.cases = new Objects(1);
				}
				sc.fes.cases.add(s);

				// Construct: { vresult = exp; return cases.dim + 1; }
				exp = new AssignExp(filename, lineNumber, new VarExp(filename, lineNumber, fd.vresult), exp);
				exp = exp.semantic(sc, context);
				Statement s1 = new ExpStatement(filename, lineNumber, exp);
				Statement s2 = new ReturnStatement(filename, lineNumber, new IntegerExp(filename, lineNumber, sc.fes.cases
						.size() + 1));
				s = new CompoundStatement(filename, lineNumber, s1, s2);
			}
			return s;
		}

		if (exp != null) {
			if (fd.returnLabel != null && tbret.ty != Tvoid) {
				Assert.isNotNull(fd.vresult);
				VarExp v = new VarExp(filename, lineNumber, fd.vresult);

				exp = new AssignExp(filename, lineNumber, v, exp);
				exp = exp.semantic(sc, context);
			}

			if (context.isD2()) {
				if (((TypeFunction) fd.type).isref && null == fd.isCtorDeclaration()) {
					// Function returns a reference
					if (tbret.isMutable())
						exp = exp.modifiableLvalue(sc, exp, context);
					else
						exp = exp.toLvalue(sc, exp, context);

					if (exp.op == TOKvar) {
						VarExp ve = (VarExp) exp;
						VarDeclaration v = ve.var.isVarDeclaration();
						if (v != null && !v.isDataseg(context) && 0 == (v.storage_class & (STCref | STCout))) {
							if (context.acceptsErrors()) {
								context.acceptProblem(Problem.newSemanticTypeError(IProblem.EscapingReferenceToLocalVariable, v, v.toChars(context)));
							}
						}
					}
				}
			}

			//exp.dump(0);
			//exp.print();
			exp.checkEscape(context);
		}

		/* BUG: need to issue an error on:
		 *	this
		 *	{   if (x) return;
		 *	    super();
		 *	}
		 */

		if ((sc.callSuper & CSXany_ctor) != 0
				&& (sc.callSuper & (CSXthis_ctor | CSXsuper_ctor)) == 0) {
			if (context.acceptsErrors()) {
				context.acceptProblem(Problem.newSemanticTypeError(
						IProblem.ReturnWithoutCallingConstructor, this, new String[] { toChars(context) }));
			}
		}

		sc.callSuper |= CSXreturn;

		// See if all returns are instead to be replaced with a goto returnLabel;
		if (fd.returnLabel != null) {
			GotoStatement gs = new GotoStatement(filename, lineNumber, new IdentifierExp(filename, lineNumber,
					Id.returnLabel));

			gs.label = fd.returnLabel;
			if (exp != null) {
				/* Replace: return exp;
				 * with:    exp; goto returnLabel;
				 */
				Statement s = new ExpStatement(filename, lineNumber, exp);
				return new CompoundStatement(filename, lineNumber, s, gs);
			}

			return gs;
		}

		if (exp != null && tbret.ty == Tvoid && !fd.isMain()) {
			Statement s;

			s = new ExpStatement(filename, lineNumber, exp);

			exp = null;
			return new CompoundStatement(filename, lineNumber, s, this);
		}

		return this;
	}

	@Override
	public Statement syntaxCopy(SemanticContext context) {
		Expression e = null;
		if (exp != null) {
			e = exp.syntaxCopy(context);
		}
		ReturnStatement s = context.newReturnStatement(filename, lineNumber, e);
		s.copySourceRange(this);
		return s;
	}

	@Override
	public void toCBuffer(OutBuffer buf, HdrGenState hgs,
			SemanticContext context) {
		buf.printf("return ");
		if (exp != null) {
			exp.toCBuffer(buf, hgs, context);
		}
		buf.writeByte(';');
		buf.writenl();
	}

}
