package descent.internal.compiler.parser;

import melnorme.utilbox.tree.TreeVisitor;
import descent.core.compiler.IProblem;
import descent.internal.compiler.parser.ast.IASTVisitor;


public class StaticAssert extends Dsymbol {

	public Expression exp, sourceExp;
	public Expression msg, sourceMsg;

	public StaticAssert(char[] filename, int lineNumber, Expression exp, Expression msg) {
		super(IdentifierExp.EMPTY);
		this.filename = filename;
		this.lineNumber = lineNumber;
		this.exp = this.sourceExp = exp;
		this.msg = this.sourceMsg = msg;
	}

	@Override
	public void accept0(IASTVisitor visitor) {
		boolean children = visitor.visit(this);
		if (children) {
			TreeVisitor.acceptChildren(visitor, sourceExp);
			TreeVisitor.acceptChildren(visitor, sourceMsg);
		}
		visitor.endVisit(this);
	}

	@Override
	public int addMember(Scope sc, ScopeDsymbol sd, int memnum,
			SemanticContext context) {
		return 0; // we didn't add anything
	}

	@Override
	public int getNodeType() {
		return STATIC_ASSERT;
	}
	
	@Override
	public String kind() {
		return "static assert";
	}

	@Override
	public boolean oneMember(Dsymbol[] ps, SemanticContext context) {
		ps[0] = null;
		return true;
	}

	@Override
	public void semantic(Scope sc, SemanticContext context) {
		// empty
	}

	@Override
	public void semantic2(Scope sc, SemanticContext context) {
		Expression e;

		e = exp.semantic(sc, context);
		e = e.optimize(WANTvalue | WANTinterpret, context);
		if (e.isBool(false)) {
			if (msg != null) {
				HdrGenState hgs = new HdrGenState();
				OutBuffer buf = new OutBuffer();

				msg = msg.semantic(sc, context);
				msg = msg.optimize(WANTvalue | WANTinterpret, context);
				hgs.console = 1;
				msg.toCBuffer(buf, hgs, context);
				if (context.acceptsErrors()) {
					context.acceptProblem(Problem.newSemanticTypeError(
							IProblem.AssertionFailed, exp,
							new String[] { buf.toChars() }));
				}
			} else {
				if (context.acceptsErrors()) {
					context.acceptProblem(Problem.newSemanticTypeError(
							IProblem.AssertionFailedNoMessage, exp, new String[] { exp.toChars(context) }));
				}
			}
			if (context.global.gag == 0) {
//				fatal(context);
			}
		} else if (!e.isBool(true)) {
			if (context.acceptsErrors()) {
				context.acceptProblem(Problem.newSemanticTypeError(
						IProblem.ExpressionIsNotEvaluatableAtCompileTime, exp,
						new String[] { exp.toChars(context) }));
			}
		}
	}

	@Override
	public Dsymbol syntaxCopy(Dsymbol s, SemanticContext context) {
		StaticAssert sa;

		if (s != null) {
			throw new IllegalStateException("assert(!s);");
		}
		sa = context.newStaticAssert(filename, lineNumber, exp.syntaxCopy(context), msg != null ? msg
				.syntaxCopy(context) : null);
		sa.copySourceRange(this);
		return sa;
	}

	@Override
	public void toCBuffer(OutBuffer buf, HdrGenState hgs,
			SemanticContext context) {
		buf.writestring(kind());
		buf.writeByte('(');
		exp.toCBuffer(buf, hgs, context);
		if (msg != null) {
			buf.writeByte(',');
			msg.toCBuffer(buf, hgs, context);
		}
		buf.writestring(");");
		buf.writenl();
	}
	
	@Override
	public String getSignature(int optionsoptions) {
		// TODO Auto-generated method stub
		return null;
	}

}
