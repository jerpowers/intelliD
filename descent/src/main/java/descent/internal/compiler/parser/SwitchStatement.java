package descent.internal.compiler.parser;

import static descent.internal.compiler.parser.BE.BEbreak;
import static descent.internal.compiler.parser.BE.BEfallthru;
import static descent.internal.compiler.parser.BE.BEnone;
import static descent.internal.compiler.parser.BE.BEthrow;
import static descent.internal.compiler.parser.Constfold.Equal;
import static descent.internal.compiler.parser.TOK.TOKequal;
import static descent.internal.compiler.parser.TY.Tarray;

import java.util.ArrayList;
import java.util.List;

import melnorme.utilbox.tree.TreeVisitor;
import descent.core.compiler.IProblem;
import descent.internal.compiler.parser.ast.IASTVisitor;

public class SwitchStatement extends Statement {

	public Expression condition, sourceCondition;
	public Statement body, sourceBody;
	public boolean isFinal;
	public DefaultStatement sdefault;
	public List gotoCases; // array of unresolved GotoCaseStatement's
	public List cases; // array of CaseStatement's
	public int hasNoDefault; // !=0 if no default statement
	public TryFinallyStatement tf;
	public int hasVars;

	public SwitchStatement(char[] filename, int lineNumber, Expression c, Statement b, boolean isFinal) {
		super(filename, lineNumber);
		this.condition = this.sourceCondition = c;
		this.body = this.sourceBody = b;
		this.isFinal = isFinal;
	}

	@Override
	public void accept0(IASTVisitor visitor) {
		boolean children = visitor.visit(this);
		if (children) {
			TreeVisitor.acceptChildren(visitor, sourceCondition);
			TreeVisitor.acceptChildren(visitor, sourceBody);
		}
		visitor.endVisit(this);
	}
	
	@Override
	public int blockExit(SemanticContext context) {
		int result = BEnone;
		if (condition.canThrow(context)) {
			result |= BEthrow;
		}

		if (body != null) {
			result |= body.blockExit(context);
			if ((result & BEbreak) != 0) {
				result |= BEfallthru;
				result &= ~BEbreak;
			}
		} else {
			result |= BEfallthru;
		}

		return result;
	}

	@Override
	public int getNodeType() {
		return SWITCH_STATEMENT;
	}

	@Override
	public boolean hasBreak() {
		return true;
	}

	@Override
	public Expression interpret(InterState istate, SemanticContext context) {
		if (istate.start == this) {
			istate.start = null;
		}
		Expression e = null;

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
			return e;
		}

		Expression econdition = condition.interpret(istate, context);
		if (econdition == EXP_CANT_INTERPRET) {
			return EXP_CANT_INTERPRET;
		}

		Statement s = null;
		if (cases != null) {
			for (int i = 0; i < cases.size(); i++) {
				CaseStatement cs = (CaseStatement) cases.get(i);
				e = Equal.call(TOKequal, Type.tint32, econdition, cs.exp,
						context);
				if (e == EXP_CANT_INTERPRET) {
					return EXP_CANT_INTERPRET;
				}
				if (e.isBool(true)) {
					s = cs;
					break;
				}
			}
		}
		if (null == s) {
			if (hasNoDefault != 0) {
				if (context.acceptsErrors()) {
					context.acceptProblem(Problem.newSemanticTypeErrorLoc(
							IProblem.NoDefaultOrCaseInSwitchStatement, this, new String[] { econdition.toChars(context) }));
				}
			}
			s = sdefault;
		}

		if (s == null) {
			throw new IllegalStateException("assert(s);");
		}
		istate.start = s;
		e = body != null ? body.interpret(istate, context) : null;
		if (istate.start != null) {
			throw new IllegalStateException("assert(!istate.start);");
		}
		if (e == EXP_BREAK_INTERPRET) {
			return null;
		}
		return e;
	}

	@Override
	public Statement semantic(Scope sc, SemanticContext context) {
		if (context.isD2()) {
			tf = sc.tf;
		}
		
		if (cases != null) {
			throw new IllegalStateException("assert(!cases);"); // ensure semantic() is only run once
		}
		condition = condition.semantic(sc, context);
		condition = resolveProperties(sc, condition, context);
		if (condition.type.isString(context)) {
			// If it's not an array, cast it to one
			if (condition.type.ty != Tarray) {
				condition = condition.implicitCastTo(sc, condition.type.nextOf()
						.arrayOf(context), context);
			}
			if (context.isD2()) {
				condition.type = condition.type.constOf(context);
			}
		} else {
			condition = condition.integralPromotions(sc, context);
			condition.checkIntegral(context);
		}
	    condition = condition.optimize(WANTvalue, context);

		sc = sc.push();
		sc.sbreak = this;
		sc.sw = this;

		cases = new ArrayList(size(gotoCases));
		sc.noctor++; // BUG: should use Scope::mergeCallSuper() for each case instead
		body = body.semantic(sc, context);
		sc.noctor--;

		// Resolve any goto case's with exp
		

		if (gotoCases != null) {
			Lfoundcase:
			for (int i = 0; i < gotoCases.size(); i++) {
				GotoCaseStatement gcs = (GotoCaseStatement) gotoCases.get(i);
	
				if (null == gcs.exp) {
					if (context.acceptsErrors()) {
						context.acceptProblem(Problem.newSemanticTypeError(
								IProblem.NoCaseStatementFollowingGoto, this));
					}
					break;
				}
	
				for (Scope scx = sc; scx != null; scx = scx.enclosing) {
					if (null == scx.sw) {
						continue;
					}
					for (int j = 0; j < scx.sw.cases.size(); j++) {
						CaseStatement cs = (CaseStatement) scx.sw.cases.get(j);
	
						if (cs.exp.equals(gcs.exp, context)) {
							gcs.cs = cs;
							// goto Lfoundcase;
							continue Lfoundcase;
						}
					}
				}
				if (context.acceptsErrors()) {
					context.acceptProblem(Problem.newSemanticTypeError(IProblem.CaseNotFound, gcs.exp, new String[] { gcs.exp.toChars(context) }));
				}
				// Lfoundcase: ;
			}
		}

		if (null == sc.sw.sdefault) {
			hasNoDefault = 1;

			if (context.global.params.warnings) {
				if (context.acceptsWarnings()) {
					context.acceptProblem(Problem.newSemanticTypeWarningLoc(
							IProblem.SwitchStatementHasNoDefault, this));
				}
			}

			// Generate runtime error if the default is hit
			Statements a = new Statements(3);
			CompoundStatement cs;
			Statement s;

			if (context.global.params.useSwitchError) {
				s = new SwitchErrorStatement(filename, lineNumber);
			} else {
				Expression e = new HaltExp(filename, lineNumber);
				s = new ExpStatement(filename, lineNumber, e);
			}

			a.add(body);
			a.add(new BreakStatement(filename, lineNumber, null));
			sc.sw.sdefault = new DefaultStatement(filename, lineNumber, s);
			a.add(sc.sw.sdefault);
			cs = new CompoundStatement(filename, lineNumber, a);
			body = cs;
		}

		sc.pop();
		return this;
	}

	@Override
	public Statement syntaxCopy(SemanticContext context) {
		SwitchStatement s = context.newSwitchStatement(filename, lineNumber, condition.syntaxCopy(context),
				body.syntaxCopy(context), isFinal);
		s.copySourceRange(this);
		return s;
	}

	@Override
	public void toCBuffer(OutBuffer buf, HdrGenState hgs,
			SemanticContext context) {
		buf.writestring("switch (");
		condition.toCBuffer(buf, hgs, context);
		buf.writebyte(')');
		buf.writenl();
		if (body != null) {
			if (null == body.isScopeStatement()) {
				buf.writebyte('{');
				buf.writenl();
				body.toCBuffer(buf, hgs, context);
				buf.writebyte('}');
				buf.writenl();
			} else {
				body.toCBuffer(buf, hgs, context);
			}
		}
	}

	@Override
	public boolean usesEH(SemanticContext context) {
		return body != null ? body.usesEH(context) : false;
	}
	
	@Override
	public int getErrorStart() {
		return start;
	}
	
	@Override
	public int getErrorLength() {
		return 6; // "switch".length()
	}

}
