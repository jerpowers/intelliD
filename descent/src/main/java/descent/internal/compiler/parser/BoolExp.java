package descent.internal.compiler.parser;

import static descent.internal.compiler.parser.Constfold.Bool;
import descent.internal.compiler.parser.ast.IASTVisitor;


public class BoolExp extends UnaExp {

	public BoolExp(char[] filename, int lineNumber, Expression e, Type t) {
		super(filename, lineNumber, TOK.TOKtobool, e);
		this.type = t;
	}

	@Override
	public void accept0(IASTVisitor visitor) {
		visitor.visit(this);
		visitor.endVisit(this);
	}

	@Override
	public int getNodeType() {
		return BOOL_EXP;
	}

	@Override
	public Expression interpret(InterState istate, SemanticContext context) {
		return interpretCommon(istate, Bool, context);
	}

	@Override
	public boolean isBit() {
		return true;
	}

	@Override
	public Expression optimize(int result, SemanticContext context) {
		Expression e;

		e1 = e1.optimize(result, context);
		if (e1.isConst()) {
			e = Bool.call(type, e1, context);
		} else {
			e = this;
		}
		return e;
	}

	@Override
	public Expression semantic(Scope sc, SemanticContext context) {
		super.semantic(sc, context);
		e1 = resolveProperties(sc, e1, context);
		e1 = e1.checkToBoolean(context);
		type = Type.tboolean;
		return this;
	}

}
