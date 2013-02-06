package descent.internal.compiler.parser;

import java.math.BigInteger;

import descent.internal.compiler.parser.ast.IASTVisitor;


public class ComplexExp extends Expression {

	public complex_t value;

	public ComplexExp(char[] filename, int lineNumber, complex_t value, Type type) {
		super(filename, lineNumber, TOK.TOKcomplex80);
		this.value = value;
		this.type = type;
	}

	@Override
	public void accept0(IASTVisitor visitor) {
		visitor.visit(this);
		visitor.endVisit(this);
	}

	@Override
	public Expression castTo(Scope sc, Type t, SemanticContext context) {
		Expression e = this;
		if (!same(type, t, context)) {
			if (type.iscomplex() && t.iscomplex()) {
				e = copy();
				e.type = t;
			} else {
				e = super.castTo(sc, t, context);
			}
		}
		
		// Descent	
		e.copySourceRange(this);
		
		return e;

	}

	@Override
	public integer_t toInteger(SemanticContext context) {
		// TODO missing a cast to sinteger_t, check original source
		return toReal(context).to_integer_t();
	}

	@Override
	public integer_t toUInteger(SemanticContext context) {
		// TODO toBigUInteger ?
		return toReal(context).to_integer_t();
	}

	@Override
	public real_t toReal(SemanticContext context) {
		return value.re;
	}

	@Override
	public real_t toImaginary(SemanticContext context) {
		return value.im;
	}

	@Override
	public complex_t toComplex(SemanticContext context) {
		return value;
	}

	@Override
	public Expression semantic(Scope sc, SemanticContext context) {
		if (type == null) {
			type = Type.tcomplex80;
		} else {
			type = type.semantic(filename, lineNumber, sc, context);
		}
		return this;
	}

	@Override
	public boolean isBool(boolean result) {
		// TODO check this
		if (result) {
			return !value.re.to_integer_t().equals(BigInteger.ZERO);
		} else {
			return value.re.to_integer_t().equals(BigInteger.ZERO);
		}
	}

	@Override
	public boolean equals(Object o, SemanticContext context) {
		if (this == o) {
			return true;
		}

		if (o instanceof Expression) {
			if (((Expression) o).op == TOK.TOKcomplex80) {
				ComplexExp ne = (ComplexExp) o;
				if (context.isD2()) {
					return (type.toHeadMutable(context).equals(ne.type.toHeadMutable(context)))
					 	&& value.equals(ne.value);
				} else {
					return type.equals(ne.type) && value.equals(ne.value);
				}
			}
		}

		return false;
	}

	@Override
	public int getNodeType() {
		return COMPLEX_EXP;
	}

	@Override
	public boolean isConst() {
		return true;
	}

	@Override
	public String toChars(SemanticContext context) {
		StringBuilder sb = new StringBuilder();
		sb.append("(");
		sb.append(complex_t.creall(value));
		sb.append("+");
		sb.append(complex_t.cimagl(value));
		sb.append("i)");
		return sb.toString();
	}

	@Override
	public void toCBuffer(OutBuffer buf, HdrGenState hgs,
			SemanticContext context) {
		buf.data.append(toString());
	}

	@Override
	public void toMangleBuffer(OutBuffer buf, SemanticContext context) {
		buf.writeByte('c');
		real_t r = toReal(context);
		realToMangleBuffer(buf, r);
		buf.writeByte('c'); // separate the two
		r = toImaginary(context);
		realToMangleBuffer(buf, r);
	}

}
