package descent.internal.compiler.parser;

import static descent.internal.compiler.parser.MATCH.MATCHconst;
import static descent.internal.compiler.parser.MATCH.MATCHconvert;
import static descent.internal.compiler.parser.MATCH.MATCHexact;
import static descent.internal.compiler.parser.MATCH.MATCHnomatch;
import static descent.internal.compiler.parser.TY.Tenum;
import static descent.internal.compiler.parser.TY.Tint32;
import static descent.internal.compiler.parser.TY.Tpointer;
import static descent.internal.compiler.parser.TY.Tuns32;
import static descent.internal.compiler.parser.TY.Tuns64;

import java.math.BigInteger;

import descent.core.compiler.CharOperation;
import descent.core.compiler.IProblem;
import descent.internal.compiler.parser.ast.IASTVisitor;


public class IntegerExp extends Expression {

	private final static BigInteger N_0x8000000000000000 = new BigInteger(
			"8000000000000000", 16);
	private final static BigInteger N_0xFFFFFFFF80000000 = new BigInteger(
			"FFFFFFFF80000000", 16);
	private final static BigInteger N_0xFF = new BigInteger("FF", 16);
	private final static BigInteger N_0xFFFF = new BigInteger("FFFF", 16);
	private final static BigInteger N_0xFFFFFFFF = new BigInteger("FFFFFFFF",
			16);
	private final static BigInteger N_0x10FFFF = new BigInteger("10FFFF", 16);
	private final static BigInteger N_SLASH_SLASH = new BigInteger("197");

	public char[] str;
	public integer_t value;

	public IntegerExp(int value) {
		this(null, 0, value);
	}

	public IntegerExp(char[] filename, int lineNumber, char[] str, int value, Type type) {
		this(filename, lineNumber, str, new integer_t(value), type);
	}
	
	public IntegerExp(char[] filename, int lineNumber, char[] str, integer_t value, Type type) {
		super(filename, lineNumber, TOK.TOKint64);
		this.str = str;
		if (value == null) {
			throw new IllegalStateException("assert(value)");
		}
		this.value = value;
		this.type = type;
	}

	public IntegerExp(char[] filename, int lineNumber, int value) {
		this(filename, lineNumber, new integer_t(value));
	}

	public IntegerExp(char[] filename, int lineNumber, int value, Type type) {
		this(filename, lineNumber, new integer_t(value), type);
	}
	
	public IntegerExp(char[] filename, int lineNumber, integer_t value) {
		this(filename, lineNumber, CharOperation.NO_CHAR, value, Type.tint32);
	}

	public IntegerExp(char[] filename, int lineNumber, integer_t value, Type type) {
		this(filename, lineNumber, null, value, type);
	}
	
	@Override
	public void accept0(IASTVisitor visitor) {
		visitor.visit(this);
		visitor.endVisit(this);
	}

	private integer_t cast(integer_t num, TY ty) {
		// TODO implement cast in BigInteger
		return num;
	}

	@Override
	public boolean equals(Object o, SemanticContext context) {
		if (this == o) {
			return true;
		}

		if (o instanceof Expression) {
			if (((Expression) o).op == TOK.TOKint64) {
				IntegerExp ne = (IntegerExp) o;
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
		return INTEGER_EXP;
	}

	@Override
	public MATCH implicitConvTo(Type t, SemanticContext context) {
		TY ty;
		TY toty;
		
		// Added for Descent
		if ((type == null || t == null) && context.global.errors > 0) {
			return MATCHnomatch;
		}

		if (context.isD2()) {
			MATCH m = type.implicitConvTo(t, context);
			if (m.ordinal() >= MATCHconst.ordinal()) {
				return m;
			}

			ty = type.toBasetype(context).ty;
			toty = t.toBasetype(context).ty;

			if (m == MATCHnomatch && t.ty == Tenum) {
				// goto Lno;
				return MATCHnomatch;
			}
		} else {
			if (type.equals(t)) {
				return MATCHexact;
			}

			ty = type.toBasetype(context).ty;
			toty = t.toBasetype(context).ty;

			if (type.implicitConvTo(t, context) == MATCHnomatch
					&& t.ty == Tenum) {
				return MATCHnomatch;
			}
		}

		switch (ty) {
		case Tbit:
		case Tbool:
			value = value.and(BigInteger.ONE);
			ty = Tint32;
			break;

		case Tint8:
			value = value.castToInt8();
			ty = Tint32;
			break;

		case Tchar:
		case Tuns8:
			value = value.and(N_0xFF);
			ty = Tint32;
			break;

		case Tint16:
			value = value.castToInt16();
			ty = Tint32;
			break;

		case Tuns16:
		case Twchar:
			value = value.and(N_0xFFFF);
			ty = Tint32;
			break;

		case Tint32:
			value = value.castToInt32();
			break;

		case Tuns32:
		case Tdchar:
			value = value.and(N_0xFFFFFFFF);
			ty = Tuns32;
			break;

		default:
			break;
		}

		// Only allow conversion if no change in value
		switch (toty) {
		case Tbit:
		case Tbool:
			if (!(value.and(BigInteger.ONE)).equals(value)) {
				return MATCHnomatch;
			}
			return MATCHconvert;

		case Tint8:
			if (!value.castToInt8().equals(value)) {
				return MATCHnomatch;
			}
			return MATCHconvert;

		case Tchar:
		case Tuns8:
			if (!value.castToUns8().equals(value)) {
				return MATCHnomatch;
			}
			return MATCHconvert;

		case Tint16:
			if (!value.castToInt16().equals(value)) {
				return MATCHnomatch;
			}
			return MATCHconvert;

		case Tuns16:
			if (!value.castToUns16().equals(value)) {
				return MATCHnomatch;
			}
			return MATCHconvert;

		case Tint32:
			if (ty == Tuns32) {
			} else if (!value.castToInt32().equals(value)) {
				return MATCHnomatch;
			}
			return MATCHconvert;

		case Tuns32:
			if (ty == Tint32) {
			} else if (!value.castToUns32().equals(value)) {
				return MATCHnomatch;
			}
			return MATCHconvert;

		case Tdchar:
			if (value.compareTo(N_0x10FFFF) > 0) {
				return MATCHnomatch;
			}
			return MATCHconvert;

		case Twchar:
			if (!value.castToUns16().equals(value)) {
				return MATCHnomatch;
			}
			return MATCHconvert;

		case Tfloat32:
			float f;
			f = value.floatValue();
			if (!value.isExactly(f))
				return MATCHnomatch;
			return MATCHconvert;

		case Tfloat64:
			double d;
			d = value.floatValue();
			if (!value.isExactly(d))
				return MATCHnomatch;
			return MATCHconvert;

			// TODO this isn't right, make sure th integer can fit in 80 bits.
			// in practice, since there' no cent types yet, any 64-bit integer
			// will fit in an 80-bit real
		case Tfloat80:
			return MATCHconvert;

		case Tpointer:
			if (context.isD2()) {
				if (ty == Tpointer
						&& type.toBasetype(context).nextOf().ty == t
								.toBasetype(context).nextOf().ty) {
					/* Allow things like:
					 *	const char* P = cast(char *)3;
					 *	char* q = P;
					 */
					// goto Lyes;
					return MATCHconvert;
				}
			}
			break;

		}
		return super.implicitConvTo(t, context);
	}

	@Override
	public Expression interpret(InterState istate, SemanticContext context) {
		return this;
	}

	@Override
	public boolean isBool(boolean result) {
		return result ? value.compareTo(BigInteger.ZERO) != 0 : value
				.compareTo(BigInteger.ZERO) == 0;
	}

	@Override
	public boolean isConst() {
		return true;
	}

	@Override
	public Expression semantic(Scope sc, SemanticContext context) {
		if (type == null) {
			// Determine what the type of this number is
			integer_t number = value;

			if (number.compareTo(N_0x8000000000000000) >= 0) {
				type = Type.tuns64;
			} else if (number.compareTo(N_0xFFFFFFFF80000000) >= 0) {
				type = Type.tint64;
			} else {
				type = Type.tint32;
			}
		} else {
			if (context.isD2()) {
				if (null == type.deco) {
					type = type.semantic(filename, lineNumber, sc, context);
				}
			} else {
				if (type.deco == null) {
					type = type.semantic(filename, lineNumber, sc, context);
				}
			}
		}
		return this;
	}

	@Override
	public void toCBuffer(OutBuffer buf, HdrGenState hgs,
			SemanticContext context) {
		integer_t v = toInteger(context);

		if (type != null) {
			Type t = type;

			boolean loop = true;
			// L1: 
				while (loop) {
				loop = false;
				switch (t.ty) {
				case Tenum: {
					TypeEnum te = (TypeEnum) t;
					buf.writestring("cast(");
					buf.writestring(te.sym.toChars(context));
					buf.writestring(")");
					t = te.sym.memtype;
					// goto L1;
					loop = true;
					continue;
				}

				case Ttypedef: {
					TypeTypedef tt = (TypeTypedef) t;
					buf.writestring("cast(");
					buf.writestring(tt.sym.toChars(context));
					buf.writestring(")");
					t = tt.sym.basetype;
					// goto L1;
					loop = true;
					continue;
				}

				case Twchar: // BUG: need to cast(wchar)
				case Tdchar: // BUG: need to cast(dchar)
					if (v.castToUns64().compareTo(N_0xFF) > 0) {
						/* TODO semantic
						 buf.printf("'\\U%08x'", v);
						 */
						break;
					}
				case Tchar:
					if (v.equals('\'')) {
						buf.writestring("'\\''");
					} else if (Chars.isprint(v) && !v.equals('\\')) {
					    buf.writestring("'");
						buf.writestring((char) v.intValue());
						buf.writestring("'");
					} else {
						/* TODO semantic
					    buf.printf("'\\x%02x'", (int)v)
					    */
					}
					break;

				case Tint8:
					buf.writestring("cast(byte)");
					buf.writestring(v.castToInt32().toString());
					break;

				case Tint16:
					buf.writestring("cast(short)");
					buf.writestring(v.castToInt32().toString());
					break;

				case Tint32:
					// L2:
					buf.writestring(v.castToInt32().toString());
					break;

				case Tuns8:
					buf.writestring("cast(ubyte)");
					buf.writestring(v.castToUns32().toString());
					buf.writestring("u");
					break;

				case Tuns16:
					buf.writestring("cast(ushort)");
					buf.writestring(v.castToUns32().toString());
					buf.writestring("u");
					break;

				case Tuns32:
					// L3:
					buf.writestring(v.castToUns32().toString());
					buf.writestring("u");
					break;

				case Tint64:
					buf.writestring(v);
					buf.writestring("L");
					/* TODO semantic and remove the previous lines
					 buf.printf("%jdL", v);
					 */
					break;

				case Tuns64:
					buf.writestring(v);
					buf.writestring("LU");
					/* TODO semantic and remove the previous lines
					 buf.printf("%juLU", v);
					 */
					break;

				case Tbit:
				case Tbool:
					buf.writestring(v.isTrue() ? "true"
							: "false");
					break;

				case Tpointer:
					buf.writestring("cast(");
					buf.writestring(t.toChars(context));
					buf.writeByte(')');
					buf.writestring(v.castToUns32().toString());
					buf.writestring("u");
					break;

				default:
					/* This can happen if errors, such as
					 * the type is painted on like in fromConstInitializer().
					 */
					if (0 == context.global.errors) {
						throw new IllegalStateException("assert(0);");
					}
					break;
				}
			}
		} else if (v.and(N_0x8000000000000000).compareTo(BigInteger.ZERO) != 0) {
			buf.writestring("0x");
			buf.writestring(v.bigIntegerValue().toString(16));
		} else {
			buf.writestring(v.toString());
		}
	}

	@Override
	public char[] toCharArray() {
		return str;
	}

	@Override
	public String toChars(SemanticContext context) {
		return super.toChars(context);
	}

	@Override
	public complex_t toComplex(SemanticContext context) {
		return new complex_t(toReal(context), real_t.ZERO);
	}

	@Override
	public real_t toImaginary(SemanticContext context) {
		return real_t.ZERO;
	}
	
	@Override
	public integer_t toInteger(SemanticContext context) {
		Type t;

		t = type;
		while (t != null) {
			switch (t.ty) {
			case Tbit:
			case Tbool:
				value = (value.compareTo(BigInteger.ZERO) != 0) ? integer_t.ONE
						: integer_t.ZERO;
				break;
			case Tint8:
				value = value.castToInt8();
				break;
			case Tchar:
			case Tuns8:
				value = value.castToUns8();
				break;
			case Tint16:
				value = value.castToInt16();
				break;
			case Twchar:
			case Tuns16:
				value = value.castToUns16();
				break;
			case Tint32:
				value = value.castToInt32();
				break;
			case Tdchar:
			case Tuns32:
				value = value.castToUns32();
				break;
			case Tint64:
				value = value.castToInt64();
				break;
			case Tuns64:
				value = value.castToUns64();
				break;
			case Tpointer:
                if (Type.PTRSIZE == 4)
                    value = value.castToUns32();
                else if (Type.PTRSIZE == 8)
                    value = value.castToUns64();
                else
                	throw new IllegalStateException("Type.PTRSIZE must be 4 or 8");
				break;
			case Tenum: {
				TypeEnum te = (TypeEnum) t;
				t = te.sym.memtype;
				continue;
			}

			case Ttypedef: {
				TypeTypedef tt = (TypeTypedef) t;
				t = tt.sym.basetype;
				continue;
			}

			default:
				/* This can happen if errors, such as
				 * the type is painted on like in fromConstInitializer().
				 */
				if (context.isD2()) {
					throw new IllegalStateException("assert(0);");
				} else {
					if (0 == context.global.errors) {
						throw new IllegalStateException("assert(0);");
					}
				}
			}
			break;
		}
		return value;
	}
	
	@Override
	public Expression toLvalue(Scope sc, Expression e, SemanticContext context) {
		if (e == null) {
			e = this;
		} else if (filename == null) {
			filename = e.filename;
		}
		if (context.acceptsErrors()) {
			context.acceptProblem(Problem.newSemanticTypeError(IProblem.ConstantIsNotAnLValue, 
					e, new String[] { e.toChars(context) }));
		}
		return this;
	}
	
	@Override
	public void toMangleBuffer(OutBuffer buf, SemanticContext context) {
		// TODO what's %jd. I've ommited the "d" printing here
		if (value.longValue() < 0) {
			buf.writestring("N");
			buf.writestring(value.negate());
		} else {
			buf.writestring(value);
		}
	}
	
	@Override
	public real_t toReal(SemanticContext context) {
		Type t;

		toInteger(context);
		t = type.toBasetype(context);
		if (t.ty == Tuns64) {
			return new real_t(value.castToUns64());
		} else {
			return new real_t(value.castToInt64());
		}
	}

}
