package descent.internal.compiler.parser;

import melnorme.utilbox.tree.TreeVisitor;
import descent.core.compiler.IProblem;
import descent.internal.compiler.parser.ast.IASTVisitor;

public class CaseRangeStatement extends Statement {

	public Expression first, sourceFirst;
	public Expression last, sourceLast;
	public Statement statement, sourceStatement;

	public CaseRangeStatement(char[] filename, int lineNumber, Expression first,
			Expression last, Statement statement) {
		super(filename, lineNumber);
		this.first = this.sourceFirst = first;
		this.last = this.sourceLast = last;
		this.statement = this.sourceStatement = statement;
	}
	
	@Override
	public Statement syntaxCopy(SemanticContext context) {
		CaseRangeStatement s = context.newCaseRangeStatement(filename, lineNumber,
				first.syntaxCopy(context), last.syntaxCopy(context), statement.syntaxCopy(context));
			    return s;
	}
	
	@Override
	public Statement semantic(Scope sc, SemanticContext context) {
		SwitchStatement sw = sc.sw;

		if (sw.isFinal) {
			if (context.acceptsErrors()) {
				context.acceptProblem(Problem.newSemanticTypeError(IProblem.CaseRangesNotAllowedInFinalSwitch, this));
			}
		}

		first = first.semantic(sc, context);
		first = first.implicitCastTo(sc, sw.condition.type, context);
		first = first.optimize(WANTvalue | WANTinterpret, context);
		integer_t fval = first.toInteger(context);

		last = last.semantic(sc, context);
		last = last.implicitCastTo(sc, sw.condition.type, context);
		last = last.optimize(WANTvalue | WANTinterpret, context);
		integer_t lval = last.toInteger(context);

		if (lval.subtract(fval).compareTo(256) > 0) {
			if (context.acceptsErrors()) {
				context.acceptProblem(Problem.newSemanticTypeError(IProblem.MoreThan256CasesInCaseRange, this));
			}
			lval = fval.add(256);
		}

		/*
		 * This works by replacing the CaseRange with an array of Case's.
		 * 
		 * case a: .. case b: s; => case a: [...] case b: s;
		 */

		Statements statements = new Statements();
		for (integer_t i = fval; i.compareTo(lval) <= 0; i = i.add(1)) {
			Statement s = statement;
			if (i != lval) {
				s = new ExpStatement(filename, lineNumber, null);
			}
			Expression e = new IntegerExp(filename, lineNumber, i, first.type);
			Statement cs = new CaseStatement(filename, lineNumber, e, s);
			statements.add(cs);
		}
		Statement s = new CompoundStatement(filename, lineNumber, statements);
		s = s.semantic(sc, context);
		return s;
	}
	
	@Override
	public void toCBuffer(OutBuffer buf, HdrGenState hgs,
			SemanticContext context) {
		buf.writestring("case ");
	    first.toCBuffer(buf, hgs, context);
	    buf.writestring(": .. case ");
	    last.toCBuffer(buf, hgs, context);
	    buf.writenl();
	    statement.toCBuffer(buf, hgs, context);
	}

	@Override
	public int getNodeType() {
		return CASE_RANGE_STATEMENT;
	}

	@Override
	protected void accept0(IASTVisitor visitor) {
		boolean children = visitor.visit(this);
		if (children) {
			TreeVisitor.acceptChildren(visitor, sourceFirst);
			TreeVisitor.acceptChildren(visitor, sourceLast);
			TreeVisitor.acceptChildren(visitor, sourceStatement);
		}
		visitor.endVisit(this);
	}

}
