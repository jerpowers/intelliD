package descent.internal.compiler.parser;

import static descent.internal.compiler.parser.BE.BEfallthru;
import melnorme.utilbox.tree.TreeVisitor;
import descent.internal.compiler.parser.ast.IASTVisitor;

public class TryFinallyStatement extends Statement {

	public Statement body, sourceBody;
	public Statement finalbody, sourceFinalbody;
	public boolean isTryCatchFinally;

	public TryFinallyStatement(char[] filename, int lineNumber, Statement body, Statement finalbody) {
		this(filename, lineNumber, body, finalbody, false);
	}

	public TryFinallyStatement(char[] filename, int lineNumber, Statement body, Statement finalbody,
			boolean isTryCatchFinally) {
		super(filename, lineNumber);
		this.body = this.sourceBody = body;
		this.finalbody = this.sourceFinalbody = finalbody;
		this.isTryCatchFinally = isTryCatchFinally;
	}

	@Override
	public void accept0(IASTVisitor visitor) {
		boolean children = visitor.visit(this);
		if (children) {
			TreeVisitor.acceptChildren(visitor, sourceBody);
			TreeVisitor.acceptChildren(visitor, sourceFinalbody);
		}
		visitor.endVisit(this);
	}
	
	@Override
	public int blockExit(SemanticContext context) {
		if (body != null)
			return body.blockExit(context);
	    return BEfallthru;
	}

	@Override
	public int getNodeType() {
		return TRY_FINALLY_STATEMENT;
	}

	@Override
	public boolean hasBreak() {
		return false;
	}

	@Override
	public boolean hasContinue() {
		return false;
	}

	@Override
	public Statement semantic(Scope sc, SemanticContext context) {
		body = body.semantic(sc, context);
		sc = sc.push();
		sc.tf = this;
		sc.sbreak = null;
		sc.scontinue = null; // no break or continue out of finally block
		finalbody = finalbody.semantic(sc, context);
		sc.pop();
		
		if (context.isD2()) {
			if (null == body) {
				return finalbody;
			}
			if (null == finalbody) {
				return body;
			}
			if (body.blockExit(context) == BEfallthru) {
				Statement s = new CompoundStatement(filename, lineNumber, body, finalbody);
				return s;
			}
		}
		
		return this;
	}

	@Override
	public Statement syntaxCopy(SemanticContext context) {
		TryFinallyStatement s = context.newTryFinallyStatement(filename, lineNumber, body.syntaxCopy(context),
				finalbody.syntaxCopy(context));
		s.copySourceRange(this);
		return s;
	}

	@Override
	public void toCBuffer(OutBuffer buf, HdrGenState hgs,
			SemanticContext context) {
		buf.printf("try\n{\n");
		body.toCBuffer(buf, hgs, context);
		buf.printf("}\nfinally\n{\n");
		finalbody.toCBuffer(buf, hgs, context);
		buf.writeByte('}');
		buf.writenl();
	}

	@Override
	public boolean usesEH(SemanticContext context) {
		return true;
	}

}
