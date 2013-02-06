package descent.internal.compiler.parser;

import static descent.internal.compiler.parser.BE.BEbreak;
import static descent.internal.compiler.parser.BE.BEcontinue;
import static descent.internal.compiler.parser.BE.BEfallthru;
import melnorme.utilbox.core.Assert;
import descent.internal.compiler.parser.ast.IASTVisitor;

public class UnrolledLoopStatement extends Statement {

	public Statements statements;

	public UnrolledLoopStatement(char[] filename, int lineNumber, Statements statements) {
		super(filename, lineNumber);
		this.statements = statements;
	}

	@Override
	public void accept0(IASTVisitor visitor) {
		Assert.fail("Accept0 on a fake node");
	}
	
	@Override
	public int blockExit(SemanticContext context) {
		int result = BEfallthru;
		for (int i = 0; i < size(statements); i++) {
			Statement s = (Statement) statements.get(i);
			if (s != null) {
				int r = s.blockExit(context);
				result |= r & ~(BEbreak | BEcontinue);
			}
		}
		return result;
	}

	@Override
	public boolean comeFrom() {
		boolean comefrom = false;

		for (int i = 0; i < statements.size(); i++) {
			Statement s = statements.get(i);

			if (null == s) {
				continue;
			}

			comefrom |= s.comeFrom();
		}
		return comefrom;
	}

	@Override
	public int getNodeType() {
		return UNROLLED_LOOP_STATEMENT;
	}

	@Override
	public boolean hasBreak() {
		return true;
	}

	@Override
	public boolean hasContinue() {
		return true;
	}

	@Override
	public Expression interpret(InterState istate, SemanticContext context) {
		Expression e = null;

		if (istate.start == this) {
			istate.start = null;
		}
		if (statements != null) {
			for (int i = 0; i < statements.size(); i++) {
				Statement s = statements.get(i);

				e = s.interpret(istate, context);
				if (e == EXP_CANT_INTERPRET) {
					break;
				}
				if (e == EXP_CONTINUE_INTERPRET) {
					e = null;
					continue;
				}
				if (e == EXP_BREAK_INTERPRET) {
					e = null;
					break;
				}
				if (e != null) {
					break;
				}
			}
		}
		return e;
	}

	@Override
	public Statement semantic(Scope sc, SemanticContext context) {
		sc.noctor++;
		Scope scd = sc.push();
		scd.sbreak = this;
		scd.scontinue = this;

		for (int i = 0; i < statements.size(); i++) {
			Statement s = statements.get(i);
			if (s != null) {
				s = s.semantic(scd, context);
				statements.set(i, s);
			}
		}

		scd.pop();
		sc.noctor--;
		return this;
	}

	@Override
	public Statement syntaxCopy(SemanticContext context) {
		Statements a = new Statements(statements.size());
		a.setDim(statements.size());
		for (int i = 0; i < statements.size(); i++) {
			Statement s = statements.get(i);
			if (s != null) {
				s = s.syntaxCopy(context);
			}
			a.set(i, s);
		}
		UnrolledLoopStatement cs = new UnrolledLoopStatement(filename, lineNumber, a);
		return cs;
	}

	@Override
	public void toCBuffer(OutBuffer buf, HdrGenState hgs,
			SemanticContext context) {
		buf.writestring("unrolled {");
		buf.writenl();

		for (int i = 0; i < statements.size(); i++) {
			Statement s;

			s = statements.get(i);
			if (s != null) {
				s.toCBuffer(buf, hgs, context);
			}
		}

		buf.writeByte('}');
		buf.writenl();
	}

	@Override
	public boolean usesEH(SemanticContext context) {
		for (int i = 0; i < statements.size(); i++) {
			Statement s;

			s = statements.get(i);
			if (s != null && s.usesEH(context)) {
				return true;
			}
		}
		return false;
	}

}
