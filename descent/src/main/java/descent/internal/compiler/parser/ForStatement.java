package descent.internal.compiler.parser;

import static descent.internal.compiler.parser.BE.BEbreak;
import static descent.internal.compiler.parser.BE.BEcontinue;
import static descent.internal.compiler.parser.BE.BEfallthru;
import static descent.internal.compiler.parser.BE.BEgoto;
import static descent.internal.compiler.parser.BE.BEthrow;
import melnorme.utilbox.tree.TreeVisitor;
import descent.internal.compiler.parser.ast.IASTVisitor;

public class ForStatement extends Statement {

	public Statement init;
	public Expression condition;
	public Expression increment;
	public Statement body;

	public ForStatement(char[] filename, int lineNumber, Statement init, Expression condition,
			Expression increment, Statement body) {
		super(filename, lineNumber);
		this.init = init;
		this.condition = condition;
		this.increment = increment;
		this.body = body;
	}

	@Override
	public void accept0(IASTVisitor visitor) {
		boolean children = visitor.visit(this);
		if (children) {
			TreeVisitor.acceptChildren(visitor, init);
			TreeVisitor.acceptChildren(visitor, condition);
			TreeVisitor.acceptChildren(visitor, increment);
			TreeVisitor.acceptChildren(visitor, body);
		}
		visitor.endVisit(this);
	}
	
	@Override
	public int blockExit(SemanticContext context) {
		int result = BEfallthru;

		if (init != null) {
			result = init.blockExit(context);
			if (0 == (result & BEfallthru)) {
				return result;
			}
		}
		if (condition != null) {
			if (condition.canThrow(context)) {
				result |= BEthrow;
			}
		} else
			result &= ~BEfallthru; // the body must do the exiting
		if (body != null) {
			int r = body.blockExit(context);
			if ((r & BEbreak | BEgoto) != 0) {
				result |= BEfallthru;
			}
			result |= r & ~(BEfallthru | BEbreak | BEcontinue);
		}
		if (increment != null && increment.canThrow(context)) {
			result |= BEthrow;
		}
		return result;
	}

	@Override
	public boolean comeFrom() {
		if (body != null) {
			boolean result = body.comeFrom();
			return result;
		}
		return false;
	}

	@Override
	public int getNodeType() {
		return FOR_STATEMENT;
	}

	@Override
	public boolean hasBreak() {
		return true;
	}

	@Override
	public boolean hasContinue() {
		return true;
	}

	@Override
	public Expression interpret(InterState istate, SemanticContext context) {
		if (istate.start == this) {
			istate.start = null;
		}
		Expression e;

		if (init != null) {
			e = init.interpret(istate, context);
			if (e == EXP_CANT_INTERPRET) {
				return e;
			}
			assert (null == e);
		}

		if (istate.start != null) {
			e = body != null ? body.interpret(istate, context) : null;
			if (istate.start != null) {
				return null;
			}
			if (e == EXP_CANT_INTERPRET) {
				return e;
			}
			if (e == EXP_BREAK_INTERPRET) {
				return null;
			}
			boolean gotoLcontinue = false;
			if (e == EXP_CONTINUE_INTERPRET) {
				// goto Lcontinue;
				gotoLcontinue = true;
				e = increment.interpret(istate, context);
				if (e == EXP_CANT_INTERPRET) {
					return e;
				}
			}
			if (!gotoLcontinue && e != null) {
				return e;
			}
		}

		while (true) {
			e = condition.interpret(istate, context);
			if (e == EXP_CANT_INTERPRET) {
				break;
			}
			if (!e.isConst()) {
				e = EXP_CANT_INTERPRET;
				break;
			}
			if (e.isBool(true)) {
				e = body != null ? body.interpret(istate, context) : null;
				if (e == EXP_CANT_INTERPRET) {
					break;
				}
				if (e == EXP_BREAK_INTERPRET) {
					e = null;
					break;
				}
				if (e != null && e != EXP_CONTINUE_INTERPRET) {
					break;
				}
				// Lcontinue:
			    if (increment != null) {
					e = increment.interpret(istate, context);
					if (e == EXP_CANT_INTERPRET) {
						break;
					}
				}
			} else if (e.isBool(false)) {
				e = null;
				break;
			} else {
				throw new IllegalStateException("assert (0);");
			}
		}
		return e;
	}

	@Override
	public void scopeCode(Scope sc, Statement[] sentry, Statement[] sexception,
			Statement[] sfinally, SemanticContext context) {
		if (init != null) {
			init.scopeCode(sc, sentry, sexception, sfinally, context);
		} else {
			super.scopeCode(sc, sentry, sexception, sfinally, context);
		}
	}

	@Override
	public Statement semantic(Scope sc, SemanticContext context) {
		ScopeDsymbol sym = new ScopeDsymbol();
		sym.parent = sc.scopesym;
		sc = sc.push(sym);
		if (init != null) {
			init = init.semantic(sc, context);
		}
		sc.noctor++;
		
	    if (condition != null) {
			condition = condition.semantic(sc, context);
			condition = resolveProperties(sc, condition, context);
			condition = condition.optimize(WANTvalue, context);
			condition = condition.checkToBoolean(context);
		}

		if (increment != null) {
			increment = increment.semantic(sc, context);
			increment = resolveProperties(sc, increment, context);
		}

		sc.sbreak = this;
		sc.scontinue = this;
		if (body != null) {
			body = body.semantic(sc, context);
		}
		sc.noctor--;

		sc.pop();
		return this;
	}

	@Override
	public Statement syntaxCopy(SemanticContext context) {
		Statement i = null;
		if (init != null) {
			i = init.syntaxCopy(context);
		}
		Expression c = null;
		if (condition != null) {
			c = condition.syntaxCopy(context);
		}
		Expression inc = null;
		if (increment != null) {
			inc = increment.syntaxCopy(context);
		}
		ForStatement s = context.newForStatement(filename, lineNumber, i, c, inc, body.syntaxCopy(context));
		s.copySourceRange(this);
		return s;
	}

	@Override
	public void toCBuffer(OutBuffer buf, HdrGenState hgs,
			SemanticContext context) {
		buf.writestring("for (");
		if (init != null) {
			hgs.FLinit.init++;
			init.toCBuffer(buf, hgs, context);
			hgs.FLinit.init--;
		} else {
			buf.writebyte(';');
		}
		if (condition != null) {
			buf.writebyte(' ');
			condition.toCBuffer(buf, hgs, context);
		}
		buf.writebyte(';');
		if (increment != null) {
			buf.writebyte(' ');
			increment.toCBuffer(buf, hgs, context);
		}
		buf.writebyte(')');
		buf.writenl();
		buf.writebyte('{');
		buf.writenl();
		body.toCBuffer(buf, hgs, context);
		buf.writebyte('}');
		buf.writenl();
	}

	@Override
	public boolean usesEH(SemanticContext context) {
		return (init != null && init.usesEH(context)) || body.usesEH(context);
	}

}
