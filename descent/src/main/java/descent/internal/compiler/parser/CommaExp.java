package descent.internal.compiler.parser;

import static descent.internal.compiler.parser.TOK.TOKfloat64;
import static descent.internal.compiler.parser.TOK.TOKint64;
import melnorme.utilbox.tree.TreeVisitor;
import descent.internal.compiler.parser.ast.IASTVisitor;


public class CommaExp extends BinExp {

	public CommaExp(char[] filename, int lineNumber, Expression e1, Expression e2) {
		super(filename, lineNumber, TOK.TOKcomma, e1, e2);
	}

	@Override
	public void accept0(IASTVisitor visitor) {
		boolean children = visitor.visit(this);
		if (children) {
			TreeVisitor.acceptChildren(visitor, sourceE1);
			TreeVisitor.acceptChildren(visitor, sourceE2);
		}
		visitor.endVisit(this);
	}

	@Override
	public void checkEscape(SemanticContext context) {
		e2.checkEscape(context);
	}

	@Override
	public int checkSideEffect(int flag, SemanticContext context) {
		if (flag == 2) {
			int result = e1.checkSideEffect(2, context);
			return result > 0 ? result : e2.checkSideEffect(2, context);
		} else {
			// Don't check e1 until we cast(void) the a,b code generation
			return e2.checkSideEffect(flag, context);
		}
	}

	@Override
	public int getNodeType() {
		return COMMA_EXP;
	}

	@Override
	public Expression interpret(InterState istate, SemanticContext context) {
		Expression e = e1.interpret(istate, context);
		if (e != EXP_CANT_INTERPRET) {
			e = e2.interpret(istate, context);
		}
		return e;
	}

	@Override
	public boolean isBool(boolean result) {
		return e2.isBool(result);
	}
	
	@Override
	public boolean isLvalue(SemanticContext context) {
		return e2.isLvalue(context);
	}

	@Override
	public Expression modifiableLvalue(Scope sc, Expression e,
			SemanticContext context) {
		e2 = e2.modifiableLvalue(sc, e, context);
		return this;
	}

	@Override
	public Expression optimize(int result, SemanticContext context) {
		Expression e;

		e1 = e1.optimize(result & WANTinterpret, context);
		e2 = e2.optimize(result, context);
		if (null == e1 || e1.op == TOKint64 || e1.op == TOKfloat64
				|| 0 == e1.checkSideEffect(2, context)) {
			e = e2;
			if (e != null) {
				e.type = type;
			}
		} else {
			e = this;
		}
		return e;
	}

	@Override
	public Expression semantic(Scope sc, SemanticContext context) {
		if (type == null) {
			super.semanticp(sc, context);
			type = e2.type;
		}
		return this;
	}

	@Override
	public Expression toLvalue(Scope sc, Expression e, SemanticContext context) {
		e2 = e2.toLvalue(sc, null, context);
		return this;
	}

}
