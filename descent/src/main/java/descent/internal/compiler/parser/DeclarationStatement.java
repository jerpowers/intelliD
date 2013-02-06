package descent.internal.compiler.parser;

import static descent.internal.compiler.parser.TOK.TOKdeclaration;
import melnorme.utilbox.tree.TreeVisitor;
import descent.internal.compiler.parser.ast.IASTVisitor;


public class DeclarationStatement extends ExpStatement {

	public DeclarationStatement(char[] filename, int lineNumber, Dsymbol s) {
		super(filename, lineNumber, new DeclarationExp(filename, lineNumber, s));
	}

	public DeclarationStatement(char[] filename, int lineNumber, Expression exp) {
		super(filename, lineNumber, exp);
	}

	@Override
	public void accept0(IASTVisitor visitor) {
		boolean children = visitor.visit(this);
		if (children) {
			TreeVisitor.acceptChildren(visitor,
					((DeclarationExp) exp).declaration);
		}
		visitor.endVisit(this);
	}

	@Override
	public int getNodeType() {
		return DECLARATION_STATEMENT;
	}

	@Override
	public void scopeCode(Scope sc, Statement[] sentry, Statement[] sexception,
			Statement[] sfinally, SemanticContext context) {
		sentry[0] = null;
		sexception[0] = null;
		sfinally[0] = null;

		if (exp != null) {
			if (exp.op == TOKdeclaration) {
				DeclarationExp de = (DeclarationExp) (exp);
				VarDeclaration v = de.declaration.isVarDeclaration();
				if (v != null) {
					Expression e;

					e = v.callAutoDtor(sc, context);
					if (e != null) {
						sfinally[0] = new ExpStatement(filename, lineNumber, e);
					}
				}
			}
		}
	}

	@Override
	public Statement syntaxCopy(SemanticContext context) {
		DeclarationStatement ds = context.newDeclarationStatement(filename, lineNumber, exp
				.syntaxCopy(context));
		ds.copySourceRange(this);
		return ds;
	}

	@Override
	public void toCBuffer(OutBuffer buf, HdrGenState hgs,
			SemanticContext context) {
		exp.toCBuffer(buf, hgs, context);
	}

}
