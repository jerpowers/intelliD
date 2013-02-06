package descent.internal.compiler.parser;

import static descent.internal.compiler.parser.Constfold.ArrayLength;
import static descent.internal.compiler.parser.PREC.PREC_primary;
import static descent.internal.compiler.parser.TOK.TOKarrayliteral;
import static descent.internal.compiler.parser.TOK.TOKassocarrayliteral;
import static descent.internal.compiler.parser.TOK.TOKnull;
import static descent.internal.compiler.parser.TOK.TOKstring;
import descent.internal.compiler.parser.ast.IASTVisitor;

public class ArrayLengthExp extends UnaExp {

	public ArrayLengthExp(char[] filename, int lineNumber, Expression e1) {
		super(filename, lineNumber, TOK.TOKarraylength, e1);
	}

	@Override
	public int getNodeType() {
		return ARRAY_LENGTH_EXP;
	}

	@Override
	protected void accept0(IASTVisitor visitor) {
		visitor.visit(this);
		visitor.endVisit(this);
	}

	@Override
	public Expression semantic(Scope sc, SemanticContext context) {
		if (type == null) {
			super.semantic(sc, context);
			e1 = resolveProperties(sc, e1, context);

			type = Type.tsize_t;
		}
		return this;
	}

	@Override
	public void toCBuffer(OutBuffer buf, HdrGenState hgs,
			SemanticContext context) {
		expToCBuffer(buf, hgs, e1, PREC_primary, context);
		buf.writestring(".length");
	}

	@Override
	public Expression interpret(InterState istate, SemanticContext context) {
		Expression e;
		Expression e1;

		e1 = this.e1.interpret(istate, context);
		if (e1 == EXP_CANT_INTERPRET)
			return EXP_CANT_INTERPRET;
		if (e1.op == TOKstring || e1.op == TOKarrayliteral
				|| e1.op == TOKassocarrayliteral) {
			e = ArrayLength.call(type, e1, context);
		} else if (e1.op == TOKnull) {
			e = new IntegerExp(filename, lineNumber, 0, type);
		} else {
			return EXP_CANT_INTERPRET;
		}
		return e;
	}

	@Override
	public Expression optimize(int result, SemanticContext context) {
		Expression e;

		e1 = e1.optimize(WANTvalue | (result & WANTinterpret), context);
		e = this;
		if (e1.op == TOKstring || e1.op == TOKarrayliteral
				|| e1.op == TOKassocarrayliteral) {
			e = ArrayLength.call(type, e1, context);
			e.copySourceRange(this);
		}
		return e;
	}

}
