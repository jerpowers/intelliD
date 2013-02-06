package descent.internal.compiler.parser;

import static descent.internal.compiler.parser.BE.BEfallthru;
import static descent.internal.compiler.parser.TOK.TOKon_scope_success;
import melnorme.utilbox.tree.TreeVisitor;
import descent.internal.compiler.parser.ast.IASTVisitor;

public class OnScopeStatement extends Statement {

	private static int num;
	public TOK tok;

	public Statement statement;

	public OnScopeStatement(char[] filename, int lineNumber, TOK tok, Statement statement) {
		super(filename, lineNumber);
		this.tok = tok;
		this.statement = statement;
	}

	@Override
	public void accept0(IASTVisitor visitor) {
		boolean children = visitor.visit(this);
		if (children) {
			TreeVisitor.acceptChildren(visitor, statement);
		}
		visitor.endVisit(this);
	}
	
	@Override
	public int blockExit(SemanticContext context) {
		// At this point, this statement is just an empty placeholder
	    return BEfallthru;
	}

	@Override
	public int getNodeType() {
		return ON_SCOPE_STATEMENT;
	}

	@Override
	public void scopeCode(Scope sc, Statement[] sentry, Statement[] sexception,
			Statement[] sfinally, SemanticContext context) {
		sentry[0] = null;
		sexception[0] = null;
		sfinally[0] = null;
		switch (tok) {
		case TOKon_scope_exit:
			sfinally[0] = statement;
			break;

		case TOKon_scope_failure:
			sexception[0] = statement;
			break;

		case TOKon_scope_success: {
			/* Create:
			 *	sentry:   int x = 0;
			 *	sexception:    x = 1;
			 *	sfinally: if (!x) statement;
			 */
			IdentifierExp id = context.uniqueId("__osf", ++num);

			ExpInitializer ie = new ExpInitializer(filename, lineNumber, new IntegerExp(0));
			VarDeclaration v = new VarDeclaration(filename, lineNumber, Type.tint32, id, ie);
			sentry[0] = new DeclarationStatement(filename, lineNumber, v);

			Expression e = new IntegerExp(1);
			e = new AssignExp(null, 0, new VarExp(null, 0, v), e);
			sexception[0] = new ExpStatement(null, 0, e);

			e = new VarExp(null, 0, v);
			e = new NotExp(null, 0, e);
			sfinally[0] = new IfStatement(null, 0, null, e, statement, null);

			break;
		}

		default:
			throw new IllegalStateException("assert(0);");
		}
	}

	@Override
	public Statement semantic(Scope sc, SemanticContext context) {
		/* semantic is called on results of scopeCode() */
		return this;
	}

	@Override
	public Statement syntaxCopy(SemanticContext context) {
		OnScopeStatement s = context.newOnScopeStatement(filename, lineNumber, tok, statement
				.syntaxCopy(context));
		s.copySourceRange(this);
		return s;
	}

	@Override
	public void toCBuffer(OutBuffer buf, HdrGenState hgs,
			SemanticContext context) {
		buf.writestring(tok.toString());
		buf.writebyte(' ');
		statement.toCBuffer(buf, hgs, context);
	}

	@Override
	public boolean usesEH(SemanticContext context) {
		if (context.isD2()) {
			return true;
		} else {
			return (tok != TOKon_scope_success);
		}
	}

}
