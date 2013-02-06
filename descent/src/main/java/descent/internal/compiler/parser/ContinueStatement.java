package descent.internal.compiler.parser;

import static descent.internal.compiler.parser.BE.BEcontinue;
import static descent.internal.compiler.parser.BE.BEgoto;
import melnorme.utilbox.tree.TreeVisitor;
import descent.core.compiler.IProblem;
import descent.internal.compiler.parser.ast.IASTVisitor;

public class ContinueStatement extends Statement {

	public IdentifierExp ident;

	public ContinueStatement(char[] filename, int lineNumber, IdentifierExp ident) {
		super(filename, lineNumber);
		this.ident = ident;
	}

	@Override
	public void accept0(IASTVisitor visitor) {
		boolean children = visitor.visit(this);
		if (children) {
			TreeVisitor.acceptChildren(visitor, ident);
		}
		visitor.endVisit(this);
	}
	
	@Override
	public int blockExit(SemanticContext context) {
		return ident != null ? BEgoto : BEcontinue;
	}

	@Override
	public int getNodeType() {
		return CONTINUE_STATEMENT;
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
		if (ident != null) {
			return EXP_CANT_INTERPRET;
		} else {
			return EXP_CONTINUE_INTERPRET;
		}
	}

	@Override
	public Statement semantic(Scope sc, SemanticContext context) {
		if (ident != null) {
			Scope scx;
			FuncDeclaration thisfunc = sc.func;

			for (scx = sc; scx != null; scx = scx.enclosing) {
				LabelStatement ls;

				if (scx.func != thisfunc) // if in enclosing function
				{
					if (sc.fes != null) // if this is the body of a foreach
					{
						for (; scx != null; scx = scx.enclosing) {
							ls = scx.slabel;
							if (ls != null && equals(ls.ident, ident)
									&& ls.statement == sc.fes) {
								// Replace continue ident; with return 0;
								return new ReturnStatement(null, 0, new IntegerExp(0));
							}
						}

						/* Post this statement to the fes, and replace
						 * it with a return value that caller will put into
						 * a switch. Caller will figure out where the break
						 * label actually is.
						 * Case numbers start with 2, not 0, as 0 is continue
						 * and 1 is break.
						 */
						Statement s;
						sc.fes.cases.add(this);
						s = new ReturnStatement(null, 0, new IntegerExp(sc.fes.cases
								.size() + 1));
						return s;
					}
					break; // can't continue to it
				}

				ls = scx.slabel;
				if (ls != null && equals(ls.ident, ident)) {
					Statement s = ls.statement;

					if (!s.hasContinue()) {
						if (context.acceptsErrors()) {
							context.acceptProblem(Problem.newSemanticTypeError(
									IProblem.LabelHasNoContinue, this, ident.toChars()));
						}
					}
					if (ls.tf != sc.tf) {
						if (context.acceptsErrors()) {
							context.acceptProblem(Problem.newSemanticTypeError(
									IProblem.CannotContinueOutOfFinallyBlock, this));
						}
					}
					return this;
				}
			}
			if (context.acceptsErrors()) {
				context.acceptProblem(Problem.newSemanticTypeError(IProblem.EnclosingLabelForContinueNotFound, ident, ident.toChars()));
			}
		} else if (null == sc.scontinue) {
			if (sc.fes != null) {
				Statement s;

				// Replace continue; with return 0;
				s = new ReturnStatement(null, 0, new IntegerExp(0));
				return s;
			}
			if (context.acceptsErrors()) {
				context.acceptProblem(Problem.newSemanticTypeError(IProblem.ContinueNotInLoop, this));
			}
		}
		return this;
	}

	@Override
	public Statement syntaxCopy(SemanticContext context) {
		ContinueStatement s = context.newContinueStatement(filename, lineNumber, ident);
		s.copySourceRange(this);
		return s;
	}

	@Override
	public void toCBuffer(OutBuffer buf, HdrGenState hgs,
			SemanticContext context) {
		buf.writestring("continue");
		if (ident != null) {
			buf.writebyte(' ');
			buf.writestring(ident.toChars());
		}
		buf.writebyte(';');
		buf.writenl();
	}

}
