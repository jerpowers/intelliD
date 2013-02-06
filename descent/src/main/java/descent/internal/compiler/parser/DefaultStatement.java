package descent.internal.compiler.parser;

import static descent.internal.compiler.parser.BE.BEany;
import melnorme.utilbox.tree.TreeVisitor;
import descent.core.compiler.IProblem;
import descent.internal.compiler.parser.ast.IASTVisitor;

public class DefaultStatement extends Statement {

	public Statement statement;
	public Statement sourceStatement;

	public DefaultStatement(char[] filename, int lineNumber, Statement s) {
		super(filename, lineNumber);
		this.statement = s;
		this.sourceStatement = s;
	}

	@Override
	public void accept0(IASTVisitor visitor) {
		boolean children = visitor.visit(this);
		if (children) {
			TreeVisitor.acceptChildren(visitor, sourceStatement);
		}
		visitor.endVisit(this);
	}
	
	@Override
	public int blockExit(SemanticContext context) {
		// Assume the worst
	    return BEany;
	}

	@Override
	public boolean comeFrom() {
		return true;
	}

	@Override
	public int getNodeType() {
		return DEFAULT_STATEMENT;
	}

	@Override
	public Expression interpret(InterState istate, SemanticContext context) {
		if (istate.start == this) {
			istate.start = null;
		}
		if (statement != null) {
			return statement.interpret(istate, context);
		} else {
			return null;
		}
	}

	@Override
	public Statement semantic(Scope sc, SemanticContext context) {
		if (sc.sw != null) {
			if (sc.sw.sdefault != null) {
				if (context.acceptsErrors()) {
					context.acceptProblem(Problem.newSemanticTypeError(IProblem.SwitchAlreadyHasDefault, this));
				}
			}
			sc.sw.sdefault = this;
			
			if (context.isD2()) {
				if (sc.sw.tf != sc.tf) {
					if (context.acceptsErrors()) {
						context.acceptProblem(Problem.newSemanticTypeError(IProblem.SwitchAndDefaultAreInDifferentFinallyBlocks, this));	
					}
				}
			}
		} else {
			if (context.acceptsErrors()) {
				context.acceptProblem(Problem.newSemanticTypeError(IProblem.DefaultNotInSwitch, this));
			}
		}
		
		statement = statement.semantic(sc, context);
		return this;
	}

	@Override
	public Statement syntaxCopy(SemanticContext context) {
		DefaultStatement s = context.newDefaultStatement(filename, lineNumber, statement.syntaxCopy(context));
		s.copySourceRange(this);
		return s;
	}

	@Override
	public void toCBuffer(OutBuffer buf, HdrGenState hgs,
			SemanticContext context) {
		buf.writestring("default:\n");
		statement.toCBuffer(buf, hgs, context);
	}

	@Override
	public boolean usesEH(SemanticContext context) {
		return statement.usesEH(context);
	}

}
