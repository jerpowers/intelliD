package descent.internal.compiler.parser;

import melnorme.utilbox.tree.TreeVisitor;
import descent.core.compiler.IProblem;
import descent.internal.compiler.parser.ast.IASTVisitor;

public class AssertExp extends UnaExp {

	public Expression msg;

	public AssertExp(char[] filename, int lineNumber, Expression e) {
		this(filename, lineNumber, e, null);
	}

	public AssertExp(char[] filename, int lineNumber, Expression e, Expression msg) {
		super(filename, lineNumber, TOK.TOKassert, e);
		this.msg = msg;
	}
	
	@Override
	public void accept0(IASTVisitor visitor) {
		boolean children = visitor.visit(this);
		if (children) {
			TreeVisitor.acceptChildren(visitor, e1);
		}
		visitor.endVisit(this);
	}
	
	@Override
	public boolean canThrow(SemanticContext context) {
		if (context.isD1()) {
			return context.global.params.useAssert;
		} else {
			return false;
		}
	}
	
	@Override
	public int checkSideEffect(int flag, SemanticContext context) {
		return 1;
	}
	
	@Override
	public int getNodeType() {
		return ASSERT_EXP;
	}

	@Override
	public Expression interpret(InterState istate, SemanticContext context)
	{
		Expression e;
		Expression e1;
		
		e1 = this.e1.interpret(istate, context);
		if(e1 == EXP_CANT_INTERPRET)
			return EXP_CANT_INTERPRET; //goto Lcant;
		
		if(e1.isBool(true))
		{
			return e1;
		}
		else if(e1.isBool(false))
		{
			if(null != msg)
			{
				e = msg.interpret(istate, context);
				if(e == EXP_CANT_INTERPRET)
					return EXP_CANT_INTERPRET; //goto Lcant;
				if (context.acceptsErrors()) {
					context.acceptProblem(Problem.newSemanticTypeError(
							IProblem.AssertionFailed, this,
							e.toChars(context)));
				}
			}
			else
			{
				if (context.acceptsErrors()) {
					context.acceptProblem(Problem.newSemanticTypeError(
							IProblem.AssertionFailedNoMessage, this,
							e1.toChars(context)));
				}
			}
			return EXP_CANT_INTERPRET; //goto Lcant;
		}
		else
		{
			return EXP_CANT_INTERPRET; //goto Lcant;
		}
	}

	@Override
	public Expression semantic(Scope sc, SemanticContext context) {
		super.semantic(sc, context);
		e1 = resolveProperties(sc, e1, context);
		// BUG: see if we can do compile time elimination of the Assert
		e1 = e1.optimize(WANTvalue, context);
		e1 = e1.checkToBoolean(context);
		if (msg != null) {
			msg = msg.semantic(sc, context);
			msg = resolveProperties(sc, msg, context);
			if (context.isD2()) {
				msg = msg.implicitCastTo(sc, Type.tchar.constOf(context).arrayOf(context), context);
			} else {
				msg = msg.implicitCastTo(sc, Type.tchar.arrayOf(context), context);
			}
			msg = msg.optimize(WANTvalue, context);
		}
		if (e1.isBool(false)) {
			FuncDeclaration fd = sc.parent.isFuncDeclaration();
			fd.hasReturnExp |= 4;

			if (!context.global.params.useAssert) {
				Expression e = new HaltExp(filename, lineNumber);
				e = e.semantic(sc, context);
				return e;
			}
		}
		type = Type.tvoid;
		return this;
	}

	@Override
	public Expression syntaxCopy(SemanticContext context) {
		AssertExp ae = new AssertExp(filename, lineNumber, e1.syntaxCopy(context), msg != null ? msg
				.syntaxCopy(context) : null);
		return ae;
	}
	
	@Override
	public void toCBuffer(OutBuffer buf, HdrGenState hgs,
			SemanticContext context) {
		buf.writestring("assert(");
		expToCBuffer(buf, hgs, e1, PREC.PREC_assign, context);
		if (msg != null) {
			buf.writeByte(',');
			expToCBuffer(buf, hgs, msg, PREC.PREC_assign, context);
		}
		buf.writeByte(')');
	}

}
