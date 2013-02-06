package descent.internal.compiler.parser;

import static descent.internal.compiler.parser.BE.BEfallthru;
import static descent.internal.compiler.parser.Scope.CSXlabel;
import melnorme.utilbox.tree.TreeVisitor;
import descent.core.compiler.IProblem;
import descent.internal.compiler.parser.ast.IASTVisitor;

public class LabelStatement extends Statement {

	public IdentifierExp ident;
	public Statement statement, sourceStatement;
	public TryFinallyStatement tf;
	public boolean isReturnLabel;

	public LabelStatement(char[] filename, int lineNumber, IdentifierExp ident, Statement statement) {
		super(filename, lineNumber);
		this.ident = ident;
		this.statement = this.sourceStatement = statement;
	}

	@Override
	public void accept0(IASTVisitor visitor) {
		boolean children = visitor.visit(this);
		if (children) {
			TreeVisitor.acceptChildren(visitor, ident);
			TreeVisitor.acceptChildren(visitor, sourceStatement);
		}
		visitor.endVisit(this);
	}
	
	@Override
	public int blockExit(SemanticContext context) {
		return statement != null ? statement.blockExit(context) : BEfallthru;
	}

	@Override
	public boolean comeFrom() {
		return true;
	}

	@Override
	public Statements flatten(Scope sc, SemanticContext context) {
		Statements a = null;

		if (statement != null) {
			a = statement.flatten(sc, context);
			if (a != null) {
				if (0 == a.size()) {
					a.add(new ExpStatement(filename, lineNumber, null));
				}
				Statement s = a.get(0);

				s = new LabelStatement(filename, lineNumber, ident, s);
				a.set(0, s);
			}
		}

		return a;
	}

	@Override
	public int getNodeType() {
		return LABEL_STATEMENT;
	}

	@Override
	public Expression interpret(InterState istate, SemanticContext context) {
		if (istate.start == this) {
			istate.start = null;
		}
		return statement != null ? statement.interpret(istate, context) : null;
	}

	@Override
	public Statement semantic(Scope sc, SemanticContext context) {
		LabelDsymbol ls;
		FuncDeclaration fd = (FuncDeclaration) sc.parent.isFuncDeclaration(); // SEMANTIC

		ls = fd.searchLabel(ident);
		if (ls.statement != null) {
			if (context.acceptsErrors()) {
				context.acceptProblem(Problem.newSemanticTypeErrorLoc(
						IProblem.LabelIsAlreadyDefined, this, new String[] { ls.toChars(context) }));
			}
		} else {
			ls.statement = this;
		}
		tf = sc.tf;
		sc = sc.push();
		sc.scopesym = sc.enclosing.scopesym;
		sc.callSuper |= CSXlabel;
		sc.slabel = this;
		if (statement != null) {
			statement = statement.semantic(sc, context);
		}
		sc.pop();
		return this;
	}

	@Override
	public Statement syntaxCopy(SemanticContext context) {
		LabelStatement s = context.newLabelStatement(filename, lineNumber, ident, statement
				.syntaxCopy(context));
		s.copySourceRange(this);
		return s;
	}

	@Override
	public void toCBuffer(OutBuffer buf, HdrGenState hgs,
			SemanticContext context) {
		buf.writestring(ident.toChars());
		buf.writebyte(':');
		buf.writenl();
		if (statement != null) {
			statement.toCBuffer(buf, hgs, context);
		}
	}

	@Override
	public boolean usesEH(SemanticContext context) {
		return statement != null ? statement.usesEH(context) : false;
	}
	
	@Override
	public int getErrorStart() {
		return ident.start;
	}
	
	@Override
	public int getErrorLength() {
		return ident.length;
	}

}
