package descent.internal.compiler.parser;

import static descent.internal.compiler.parser.BE.BEfallthru;
import melnorme.utilbox.tree.TreeVisitor;
import descent.internal.compiler.parser.ast.IASTVisitor;

public class VolatileStatement extends Statement {

	public Statement statement;
	public Statement sourceStatement;

	public VolatileStatement(char[] filename, int lineNumber, Statement statement) {
		super(filename, lineNumber);
		this.sourceStatement = statement;
		this.statement = statement;
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
		return statement != null ? statement.blockExit(context) : BEfallthru;
	}

	@Override
	public Statements flatten(Scope sc, SemanticContext context) {
		Statements a;

		a = statement != null ? statement.flatten(sc, context) : null;
		if (a != null) {
			for (int i = 0; i < a.size(); i++) {
				Statement s = a.get(i);

				s = new VolatileStatement(filename, lineNumber, s);
				a.set(i, s);
			}
		}

		return a;
	}

	@Override
	public int getNodeType() {
		return VOLATILE_STATEMENT;
	}

	@Override
	public Statement semantic(Scope sc, SemanticContext context) {
		if (statement != null) {
			statement = statement.semantic(sc, context);
		}
		return this;
	}

	@Override
	public Statement syntaxCopy(SemanticContext context) {
		VolatileStatement s = context.newVolatileStatement(filename, lineNumber,
				statement != null ? statement.syntaxCopy(context) : null);
		s.copySourceRange(this);
		return s;
	}

	@Override
	public void toCBuffer(OutBuffer buf, HdrGenState hgs,
			SemanticContext context) {
		buf.writestring("volatile");
		if (statement != null) {
			if (statement.isScopeStatement() != null) {
				buf.writenl();
			} else {
				buf.writebyte(' ');
			}
			statement.toCBuffer(buf, hgs, context);
		}
	}

}
