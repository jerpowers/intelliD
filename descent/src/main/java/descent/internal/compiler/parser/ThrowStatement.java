package descent.internal.compiler.parser;

import melnorme.utilbox.tree.TreeVisitor;
import descent.core.compiler.IProblem;
import descent.internal.compiler.parser.ast.IASTVisitor;


public class ThrowStatement extends Statement {

	public Expression exp, sourceExp;

	public ThrowStatement(char[] filename, int lineNumber, Expression exp) {
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
	public int getNodeType() {
		return THROW_STATEMENT;
	}

	@Override
	public Statement semantic(Scope sc, SemanticContext context) {
		FuncDeclaration fd = (FuncDeclaration) sc.parent.isFuncDeclaration();
		fd.hasReturnExp |= 2;

		if (sc.incontract != 0) {
			if (context.acceptsErrors()) {
				context.acceptProblem(Problem.newSemanticTypeError(
						IProblem.ThrowStatementsCannotBeInContracts, this));
			}
		}
		exp = exp.semantic(sc, context);
		exp = resolveProperties(sc, exp, context);
		
		// Descent: might happen if there are syntatic errors
		if (exp.type == null) {
			return this;
		}
		
		if (null == exp.type.toBasetype(context).isClassHandle()) {
			if (context.acceptsErrors()) {
				context.acceptProblem(Problem.newSemanticTypeError(
						IProblem.CanOnlyThrowClassObjects, exp, new String[] { exp.type.toChars(context) }));
			}
		}
		return this;
	}

	@Override
	public Statement syntaxCopy(SemanticContext context) {
		ThrowStatement s = context.newThrowStatement(filename, lineNumber, exp.syntaxCopy(context));
		s.copySourceRange(this);
		return s;
	}

	@Override
	public void toCBuffer(OutBuffer buf, HdrGenState hgs,
			SemanticContext context) {
		buf.writestring("throw ");
		exp.toCBuffer(buf, hgs, context);
		buf.writeByte(';');
		buf.writenl();
	}

}
