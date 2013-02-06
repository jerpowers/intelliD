package descent.internal.compiler.parser;

import static descent.internal.compiler.parser.BE.BEgoto;
import melnorme.utilbox.tree.TreeVisitor;
import descent.core.compiler.IProblem;
import descent.internal.compiler.parser.ast.IASTVisitor;


public class GotoCaseStatement extends Statement {

	public Expression exp, sourceExp;
	public CaseStatement cs; // case statement it resolves to

	public GotoCaseStatement(char[] filename, int lineNumber, Expression exp) {
		super(filename, lineNumber);
		this.exp = this.sourceExp = exp;
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
		return BEgoto;
	}

	@Override
	public int getNodeType() {
		return GOTO_CASE_STATEMENT;
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
		if (cs == null) {
			throw new IllegalStateException("assert(cs);");
		}
		istate.gotoTarget = cs;
		return EXP_GOTO_INTERPRET;
	}

	@Override
	public Statement semantic(Scope sc, SemanticContext context) {
		if (exp != null) {
			exp = exp.semantic(sc, context);
		}

		if (null == sc.sw) {
			if (context.acceptsErrors()) {
				context.acceptProblem(Problem.newSemanticTypeError(IProblem.GotoCaseNotInSwitch, this));
			}
		} else {
			if (sc.sw.gotoCases == null) {
				sc.sw.gotoCases = new Identifiers(4);
			}
			sc.sw.gotoCases.add(this);
			if (exp != null) {
				exp = exp.implicitCastTo(sc, sc.sw.condition.type, context);
				exp = exp.optimize(WANTvalue, context);
			}
		}
		return this;
	}

	@Override
	public Statement syntaxCopy(SemanticContext context) {
		Expression e = exp != null ? exp.syntaxCopy(context) : null;
		GotoCaseStatement s = context.newGotoCaseStatement(filename, lineNumber, e);
		s.copySourceRange(this);
		return s;
	}

	@Override
	public void toCBuffer(OutBuffer buf, HdrGenState hgs,
			SemanticContext context) {
		buf.writestring("goto case");
		if (exp != null) {
			buf.writebyte(' ');
			exp.toCBuffer(buf, hgs, context);
		}
		buf.writebyte(';');
		buf.writenl();
	}

}
