package descent.internal.compiler.parser;

import static descent.internal.compiler.parser.BE.BEany;
import static descent.internal.compiler.parser.TOK.TOKint64;
import static descent.internal.compiler.parser.TOK.TOKstring;
import static descent.internal.compiler.parser.TOK.TOKvar;
import static descent.internal.compiler.parser.TY.Tclass;
import melnorme.utilbox.tree.TreeVisitor;
import descent.core.compiler.IProblem;
import descent.internal.compiler.parser.ast.IASTVisitor;

public class CaseStatement extends Statement {

	public Expression exp, sourceExp;
	public Statement statement, sourceStatement;
	
	public SwitchStatement sw; // descent

	public CaseStatement(char[] filename, int lineNumber, Expression exp, Statement s) {
		super(filename, lineNumber);
		this.exp = sourceExp = exp;
		this.statement = sourceStatement = s;
	}

	@Override
	public void accept0(IASTVisitor visitor) {
		boolean children = visitor.visit(this);
		if (children) {
			TreeVisitor.acceptChildren(visitor, sourceExp);
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

	public boolean compare(ASTDmdNode obj) {
		if (!(obj instanceof CaseStatement)) {
			return false;
		}
		
		// Sort cases so we can do an efficient lookup
	    CaseStatement cs2 = (CaseStatement) obj;
	    return exp.compare(cs2.exp);
	}

	@Override
	public int getNodeType() {
		return CASE_STATEMENT;
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
		SwitchStatement sw = sc.sw;
		this.sw = sw;

		exp = exp.semantic(sc, context);
		if (sw != null) {
			int i;

			exp = exp.implicitCastTo(sc, sw.condition.type, context);
			exp = exp.optimize(WANTvalue | WANTinterpret, context);
			
			boolean gotoL1 = false;
			
			if (context.isD2()) {
				/* This is where variables are allowed as case expressions.
				 */
				if (exp.op == TOKvar)
				{   VarExp ve = (VarExp) exp;
				    VarDeclaration v = ve.var.isVarDeclaration();
				    Type t = exp.type.toBasetype(context);
				    if (v != null && (t.isintegral() || t.ty == Tclass))
				    {	/* Flag that we need to do special code generation
					 * for this, i.e. generate a sequence of if-then-else
					 */
					sw.hasVars = 1;
					// goto L1;
					gotoL1 = true;
				    }
				}
			}
			
			if (!gotoL1) {
				if (exp.op != TOKstring && exp.op != TOKint64) {
					if (context.acceptsErrors()) {
						context.acceptProblem(Problem.newSemanticTypeError(IProblem.CaseMustBeAnIntegralOrStringConstant, sourceExp, exp.toChars(context)));
					}
					exp = new IntegerExp(0);
				}
			}

			// L1:
			for (i = 0; i < sw.cases.size(); i++) {
				CaseStatement cs = (CaseStatement) sw.cases.get(i);

				if (cs.exp.equals(exp, context)) {
					if (context.acceptsErrors()) {
						context.acceptProblem(Problem.newSemanticTypeErrorLoc(IProblem.DuplicateCaseInSwitchStatement, this, exp.toChars(context)));
					}
					break;
				}
			}

			sw.cases.add(this);

			// Resolve any goto case's with no exp to this case statement
			if (sw.gotoCases != null) {
				for (i = 0; i < sw.gotoCases.size(); i++) {
					GotoCaseStatement gcs = (GotoCaseStatement) sw.gotoCases.get(i);
	
					if (gcs.exp == null) {
						gcs.cs = this;
						sw.gotoCases.remove(i); // remove from array
					}
				}
			}
			
			if (context.isD2()) {
				if (sc.sw.tf != sc.tf) {
					if (context.acceptsErrors()) {
						context.acceptProblem(Problem.newSemanticTypeErrorLoc(IProblem.SwitchAndCaseAreInDifferentFinallyBlocks, this));
					}
				}
			}
		} else {
			if (context.acceptsErrors()) {
				context.acceptProblem(Problem.newSemanticTypeError(IProblem.CaseIsNotInSwitch, this));
			}
		}
		statement = statement.semantic(sc, context);
		return this;
	}
	
	public void setStatement(Statement s) {
		this.statement = this.sourceStatement = s;
	}
	
	@Override
	public Statement syntaxCopy(SemanticContext context) {
		CaseStatement s = context.newCaseStatement(filename, lineNumber, exp.syntaxCopy(context), statement.syntaxCopy(context));
		s.copySourceRange(this);
	    return s;
	}
	
	@Override
	public void toCBuffer(OutBuffer buf, HdrGenState hgs, SemanticContext context) {
		buf.writestring("case ");
	    exp.toCBuffer(buf, hgs, context);
	    buf.writebyte(':');
	    buf.writenl();
	    statement.toCBuffer(buf, hgs, context);
	}
	
	@Override
	public boolean usesEH(SemanticContext context) {
		return statement.usesEH(context);
	}
	
	@Override
	public int getErrorStart() {
		return start;
	}
	
	@Override
	public int getErrorLength() {
		return sourceExp.start + sourceExp.length - start;
	}

	

}
