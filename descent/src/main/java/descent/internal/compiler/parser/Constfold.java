package descent.internal.compiler.parser;

import static descent.internal.compiler.parser.ASTDmdNode.EXP_CANT_INTERPRET;
import static descent.internal.compiler.parser.ASTDmdNode.expType;
import static descent.internal.compiler.parser.MATCH.MATCHconst;
import static descent.internal.compiler.parser.TOK.TOKadd;
import static descent.internal.compiler.parser.TOK.TOKaddress;
import static descent.internal.compiler.parser.TOK.TOKarrayliteral;
import static descent.internal.compiler.parser.TOK.TOKassocarrayliteral;
import static descent.internal.compiler.parser.TOK.TOKequal;
import static descent.internal.compiler.parser.TOK.TOKidentity;
import static descent.internal.compiler.parser.TOK.TOKint64;
import static descent.internal.compiler.parser.TOK.TOKnotequal;
import static descent.internal.compiler.parser.TOK.TOKnotidentity;
import static descent.internal.compiler.parser.TOK.TOKnull;
import static descent.internal.compiler.parser.TOK.TOKstring;
import static descent.internal.compiler.parser.TOK.TOKstructliteral;
import static descent.internal.compiler.parser.TOK.TOKsymoff;
import static descent.internal.compiler.parser.TY.Tarray;
import static descent.internal.compiler.parser.TY.Tbool;
import static descent.internal.compiler.parser.TY.Tchar;
import static descent.internal.compiler.parser.TY.Tdchar;
import static descent.internal.compiler.parser.TY.Tsarray;
import static descent.internal.compiler.parser.TY.Tstruct;
import static descent.internal.compiler.parser.TY.Tvoid;
import static descent.internal.compiler.parser.TY.Twchar;
import descent.core.compiler.CharOperation;
import descent.core.compiler.IProblem;

/**
 * A class to hold constant-folding functions used by the interpreter. The
 * functions that use these are in UnaExp and BinExp. In DMD, they are in the
 * file constfold.c. Arguably, these should be moved into their respective
 * classes so the code looks more like DMD's (or even inlined as anonymous
 * classes), but since they're all in one file in DMD, I put them all in one
 * file here.
 * 
 * @author Walter Bright, port by Robert Fraser & Ary Manzana
 */


public class Constfold {

	public static interface UnaExp_fp {
		public Expression call(Type type, Expression e1, SemanticContext context);
	}

	public static final UnaExp_fp Neg = new UnaExp_fp() {
		@Override
		public Expression call(Type type, Expression e1, SemanticContext context) {
			Expression e;
			char[] filename = e1.filename;
			int lineNumber = e1.lineNumber;

			if (e1.type.isreal()) {
				e = new RealExp(filename, lineNumber, e1.toReal(context).negate(), type);
			} else if (e1.type.isimaginary()) {
				e = new RealExp(filename, lineNumber, e1.toImaginary(context).negate(), type);
			} else if (e1.type.iscomplex()) {
				e = new ComplexExp(filename, lineNumber, e1.toComplex(context).negate(), type);
			} else {
				e = new IntegerExp(filename, lineNumber, e1.toInteger(context).negate(), type);
			}

			e.copySourceRange(e1);
			return e;
		}
	};

	public static final UnaExp_fp Com = new UnaExp_fp() {
		@Override
		public Expression call(Type type, Expression e1, SemanticContext context) {
			Expression e;
			char[] filename = e1.filename;
			int lineNumber = e1.lineNumber;

			e = new IntegerExp(filename, lineNumber, e1.toInteger(context).complement(), type);
			e.copySourceRange(e1);
			return e;
		}
	};

	public static final UnaExp_fp Not = new UnaExp_fp() {
		@Override
		public Expression call(Type type, Expression e1, SemanticContext context) {
			Expression e;
			char[] filename = e1.filename;
			int lineNumber = e1.lineNumber;
			// And, for the crowd's amusement, we now have a triple-negative!
			e = new IntegerExp(filename, lineNumber, e1.isBool(false) ? 1 : 0, type);
			e.copySourceRange(e1);
			return e;
		}
	};

	public static final UnaExp_fp Ptr = new UnaExp_fp() {
		@Override
		public Expression call(Type type, Expression e1, SemanticContext context) {
			if (e1.op == TOKadd) {
				AddExp ae = (AddExp) e1;
				if (ae.e1.op == TOKaddress && ae.e2.op == TOKint64) {
					AddrExp ade = (AddrExp) ae.e1;
					if (ade.e1.op == TOKstructliteral) {
						StructLiteralExp se = (StructLiteralExp) ade.e1;
						int offset = ae.e2.toInteger(context).intValue();
						Expression e = se.getField(type, offset, context);
						if (null == e) {
							e = EXP_CANT_INTERPRET;
						}
						return e;
					}
				}
			}
			return EXP_CANT_INTERPRET;
		}
	};

	public static final UnaExp_fp Bool = new UnaExp_fp() {
		@Override
		public Expression call(Type type, Expression e1, SemanticContext context) {
			Expression e;
			char[] filename = e1.filename;
			int lineNumber = e1.lineNumber;

			e = new IntegerExp(filename, lineNumber, e1.isBool(true) ? 1 : 0, type);
			e.copySourceRange(e1);
			return e;
		}
	};

	public static final UnaExp_fp ArrayLength = new UnaExp_fp() {
		@Override
		public Expression call(Type type, Expression e1, SemanticContext context) {
			Expression e;
			char[] filename = e1.filename;
			int lineNumber = e1.lineNumber;
			
			if (e1.op == TOKstring) {
				StringExp es1 = (StringExp) e1;

				e = new IntegerExp(filename, lineNumber, es1.len, type);
			} else if (e1.op == TOKarrayliteral) {
				ArrayLiteralExp ale = (ArrayLiteralExp) e1;
				int dim;

				dim = null != ale.elements ? ale.elements.size() : 0;
				e = new IntegerExp(filename, lineNumber, dim, type);
			} else if (e1.op == TOKassocarrayliteral) {
				AssocArrayLiteralExp ale = (AssocArrayLiteralExp) e1;
				int dim = ale.keys.size();

				e = new IntegerExp(filename, lineNumber, dim, type);
			} else {
				e = EXP_CANT_INTERPRET;
			}
			e.copySourceRange(e1);
			return e;
		}
	};

	public static final UnaExp_fp expType = new UnaExp_fp() {
		@Override
		public Expression call(Type type, Expression e, SemanticContext context) {
			if (!type.equals(e.type)) {
				e = e.copy();
				e.type = type;
			}
			return e;
		}
	};

	// --------------------------------------------------------------------------
	public static interface BinExp_fp {
		public Expression call(Type type, Expression e1, Expression e2,
				SemanticContext context);
	}

	public static final BinExp_fp Add = new BinExp_fp() {
		@Override
		public Expression call(Type type, Expression e1, Expression e2,
				SemanticContext context) {
			Expression e;
			char[] filename = e1.filename;
			int lineNumber = e1.lineNumber;

			if (type.isreal()) {
				e = new RealExp(filename, lineNumber,
						e1.toReal(context).add(e2.toReal(context)), type);
			} else if (type.isimaginary()) {
				e = new RealExp(filename, lineNumber, e1.toImaginary(context).add(
						e2.toImaginary(context)), type);
			} else if (type.iscomplex()) {
				// This rigamarole is necessary so that -0.0 doesn't get
				// converted to +0.0 by doing an extraneous add with +0.0
				complex_t c1 = complex_t.ZERO;
				real_t r1 = real_t.ZERO;
				real_t i1 = real_t.ZERO;

				complex_t c2 = complex_t.ZERO;
				real_t r2 = real_t.ZERO;
				real_t i2 = real_t.ZERO;

				complex_t v = complex_t.ZERO;
				int x = 0;

				if (e1.type.isreal()) {
					r1 = e1.toReal(context);
					x = 0;
				} else if (e1.type.isimaginary()) {
					i1 = e1.toImaginary(context);
					x = 3;
				} else {
					c1 = e1.toComplex(context);
					x = 6;
				}

				if (e2.type.isreal()) {
					r2 = e2.toReal(context);
				} else if (e2.type.isimaginary()) {
					i2 = e2.toImaginary(context);
					x += 1;
				} else {
					c2 = e2.toComplex(context);
					x += 2;
				}

				switch (x) {
				case 0 + 0:
					v = new complex_t(r1.add(r2), real_t.ZERO);
					break;
				case 0 + 1:
					v = new complex_t(r1, i2);
					break;
				case 0 + 2:
					v = new complex_t(r1.add(complex_t.creall(c2)), complex_t.cimagl(c2));
					break;
				case 3 + 0:
					v = new complex_t(r2, i1);
					break;
				case 3 + 1:
					v = new complex_t(real_t.ZERO, i1.add(i2));
					break;
				case 3 + 2:
					v = new complex_t(complex_t.creall(c2), i1.add(complex_t.cimagl(c2)));
					break;
				case 6 + 0:
					v = new complex_t(complex_t.creall(c1).add(r2), complex_t.cimagl(c2));
					break;
				case 6 + 1:
					v = new complex_t(complex_t.creall(c1), complex_t.cimagl(c1).add(i2));
					break;
				case 6 + 2:
					v = c1.add(c2);
					break;
				default:
					throw new IllegalStateException("assert(0);");
				}
				e = new ComplexExp(filename, lineNumber, v, type);
			} else if (e1.op == TOKsymoff) {
				SymOffExp soe = (SymOffExp) e1;
				e = new SymOffExp(filename, lineNumber, soe.var, soe.offset.add(e2
						.toInteger(context)), context);
				e.type = type;
			} else if (e2.op == TOKsymoff) {
				SymOffExp soe = (SymOffExp) e2;
				e = new SymOffExp(filename, lineNumber, soe.var, soe.offset.add(e1
						.toInteger(context)), context);
				e.type = type;
			} else {
				e = new IntegerExp(filename, lineNumber, e1.toInteger(context).add(
						e2.toInteger(context)), type);
			}
			e.copySourceRange(e1, e2);
			return e;
		}
	};

	public static final BinExp_fp Min = new BinExp_fp() {
		@Override
		public Expression call(Type type, Expression e1, Expression e2,
				SemanticContext context) {
			Expression e;
			char[] filename = e1.filename;
			int lineNumber = e1.lineNumber;

			if (type.isreal()) {
				e = new RealExp(filename, lineNumber, e1.toReal(context).subtract(
						e2.toReal(context)), type);
			} else if (type.isimaginary()) {
				e = new RealExp(filename, lineNumber, e1.toImaginary(context).subtract(
						e2.toImaginary(context)), type);
			} else if (type.iscomplex()) {
				// This rigamarole is necessary so that -0.0 doesn't get
				// converted to +0.0 by doing an extraneous add with +0.0
				complex_t c1 = complex_t.ZERO;
				real_t r1 = real_t.ZERO;
				real_t i1 = real_t.ZERO;

				complex_t c2 = complex_t.ZERO;
				;
				real_t r2 = real_t.ZERO;
				real_t i2 = real_t.ZERO;

				complex_t v = complex_t.ZERO;
				;
				int x;

				if (e1.type.isreal()) {
					r1 = e1.toReal(context);
					x = 0;
				} else if (e1.type.isimaginary()) {
					i1 = e1.toImaginary(context);
					x = 3;
				} else {
					c1 = e1.toComplex(context);
					x = 6;
				}

				if (e2.type.isreal()) {
					r2 = e2.toReal(context);
				} else if (e2.type.isimaginary()) {
					i2 = e2.toImaginary(context);
					x += 1;
				} else {
					c2 = e2.toComplex(context);
					x += 2;
				}

				switch (x) {
				case 0 + 0:
					v = new complex_t(r1.subtract(r2), real_t.ZERO);
					break;
				case 0 + 1:
					v = new complex_t(r1, i2.negate());
					break;
				case 0 + 2:
					v = new complex_t(r1.subtract(complex_t.creall(c2)), complex_t.cimagl(c2)
							.negate());
					break;
				case 3 + 0:
					v = new complex_t(r2.negate(), i1);
					break;
				case 3 + 1:
					v = new complex_t(real_t.ZERO, i1.subtract(i2));
					break;
				case 3 + 2:
					v = new complex_t(complex_t.creall(c2).negate(), i1
							.subtract(complex_t.cimagl(c2)));
					break;
				case 6 + 0:
					v = new complex_t(complex_t.creall(c1).subtract(r2), complex_t.cimagl(c1));
					break;
				case 6 + 1:
					v = new complex_t(complex_t.creall(c1), complex_t.cimagl(c1).subtract(i2));
					break;
				case 6 + 2:
					v = c1.subtract(c2);
					break;
				default:
					throw new IllegalStateException("assert(0);");
				}
				e = new ComplexExp(filename, lineNumber, v, type);
			} else if (e1.op == TOKsymoff) {
				SymOffExp soe = (SymOffExp) e1;
				e = new SymOffExp(filename, lineNumber, soe.var, soe.offset.subtract(e2
						.toInteger(context)), context);
				e.type = type;
			} else {
				e = new IntegerExp(filename, lineNumber, e1.toInteger(context).subtract(
						e2.toInteger(context)), type);
			}
			e.copySourceRange(e1, e2);
			return e;
		}
	};

	public static final BinExp_fp Mul = new BinExp_fp() {
		@Override
		public Expression call(Type type, Expression e1, Expression e2,
				SemanticContext context) {
			Expression e;
			char[] filename = e1.filename;
			int lineNumber = e1.lineNumber;

			if (type.isfloating()) {
				complex_t c;
				real_t r;

				if (e1.type.isreal()) {
					r = e1.toReal(context);
					c = e2.toComplex(context);
					c = new complex_t(r.multiply(c.re), r.multiply(c.im));
				} else if (e1.type.isimaginary()) {
					r = e1.toImaginary(context);
					c = e2.toComplex(context);
					c = new complex_t(r.negate().multiply(c.im), r
							.multiply(c.re));
				} else if (e2.type.isreal()) {
					r = e2.toReal(context);
					c = e1.toComplex(context);
					c = new complex_t(r.multiply(c.re), r.multiply(c.im));
				} else if (e2.type.isimaginary()) {
					r = e2.toImaginary(context);
					c = e1.toComplex(context);
					c = new complex_t(r.negate().multiply(c.im), r
							.multiply(c.re));
				} else {
					c = e1.toComplex(context).multiply(e2.toComplex(context));
				}

				if (type.isreal()) {
					e = new RealExp(filename, lineNumber, c.re, type);
				} else if (type.isimaginary()) {
					e = new RealExp(filename, lineNumber, c.im, type);
				} else if (type.iscomplex()) {
					e = new ComplexExp(filename, lineNumber, c, type);
				} else {
					assert (false);
					e = null;
				}
			} else {
				e = new IntegerExp(filename, lineNumber, e1.toInteger(context).multiply(
						e2.toInteger(context)), type);
			}
			e.copySourceRange(e1, e2);
			return e;
		}
	};

	public static final BinExp_fp Div = new BinExp_fp() {
		@Override
		public Expression call(Type type, Expression e1, Expression e2,
				SemanticContext context) {
			Expression e;
			char[] filename = e1.filename;
			int lineNumber = e1.lineNumber;

			if (type.isfloating()) {
				complex_t c;
				real_t r;

				// e1.type.print();
				// e2.type.print();
				if (e2.type.isreal()) {
					if (e1.type.isreal()) {
						e = new RealExp(filename, lineNumber, e1.toReal(context).divide(
								e2.toReal(context)), type);
						return e;
					}
					r = e2.toReal(context);
					c = e1.toComplex(context);
					c = new complex_t(c.re.divide(r), c.im.divide(r));
				} else if (e2.type.isimaginary()) {
					r = e2.toImaginary(context);
					c = e1.toComplex(context);
					c = new complex_t(c.im.divide(r), c.re.negate().divide(r));
				} else {
					c = e1.toComplex(context).divide(e2.toComplex(context));
				}

				if (type.isreal()) {
					e = new RealExp(filename, lineNumber, c.re, type);
				} else if (type.isimaginary()) {
					e = new RealExp(filename, lineNumber, c.im, type);
				} else if (type.iscomplex()) {
					e = new ComplexExp(filename, lineNumber, c, type);
				} else {
					assert (false);
					e = null;
				}
			} else {
				integer_t n1;
				integer_t n2;
				integer_t n;

				n1 = e1.toInteger(context).castToSinteger_t();
				n2 = e2.toInteger(context).castToSinteger_t();
				if (n2.equals(0)) {
					if (context.acceptsErrors()) {
						context.acceptProblem(Problem.newSemanticTypeError(
								IProblem.DivisionByZero, e1, e2));
					}
					e2 = new IntegerExp(filename, lineNumber, integer_t.ONE, e2.type);
					n2 = integer_t.ONE;
				}
				if (e1.type.isunsigned() || e2.type.isunsigned()) {
					n = n1.castToUns64().divide(n2.castToUns64());
				} else {
					n = n1.divide(n2);
				}
				e = new IntegerExp(filename, lineNumber, n, type);
			}
			e.copySourceRange(e1, e2);
			return e;
		}
	};

	public static final BinExp_fp Mod = new BinExp_fp() {
		@Override
		public Expression call(Type type, Expression e1, Expression e2,
				SemanticContext context) {
			Expression e;
			char[] filename = e1.filename;
			int lineNumber = e1.lineNumber;

			if (type.isfloating()) {
				complex_t c;

				if (e2.type.isreal()) {
					real_t r2 = e2.toReal(context);
					c = new complex_t(e1.toReal(context).remainder(r2), e1
							.toImaginary(context).remainder(r2));
				} else if (e2.type.isimaginary()) {
					real_t i2 = e2.toImaginary(context);
					c = new complex_t(e1.toReal(context).remainder(i2), e1
							.toImaginary(context).remainder(i2));
				} else {
					assert (false);
					c = null;
				}

				if (type.isreal()) {
					e = new RealExp(filename, lineNumber, c.re, type);
				} else if (type.isimaginary()) {
					e = new RealExp(filename, lineNumber, c.im, type);
				} else if (type.iscomplex()) {
					e = new ComplexExp(filename, lineNumber, c, type);
				} else {
					assert (false);
					e = null;
				}
			} else {
				integer_t n1;
				integer_t n2;
				integer_t n;

				n1 = e1.toInteger(context).castToSinteger_t();
				n2 = e2.toInteger(context).castToSinteger_t();
				if (n2.equals(0)) {
					context.acceptProblem(Problem.newSemanticTypeError(
							IProblem.DivisionByZero, e1, e2));
					e2 = new IntegerExp(filename, lineNumber, integer_t.ONE, e2.type);
					n2 = integer_t.ONE;
				}
				if (e1.type.isunsigned() || e2.type.isunsigned()) {
					n = n1.castToUns64().mod(n2.castToUns64());
				} else {
					n = n1.mod(n2);
				}
				e = new IntegerExp(filename, lineNumber, n, type);
			}
			e.copySourceRange(e1, e2);
			return e;
		}
	};

	public static final BinExp_fp Shl = new BinExp_fp() {
		@Override
		public Expression call(Type type, Expression e1, Expression e2,
				SemanticContext context) {
			Expression e;
			char[] filename = e1.filename;
			int lineNumber = e1.lineNumber;

			e = new IntegerExp(filename, lineNumber, e1.toInteger(context).shiftLeft(
					e2.toInteger(context)), type);
			e.start = e1.start;
			e.length = e2.start + e2.length - e1.start;
			return e;
		}
	};

	public static final BinExp_fp Shr = new BinExp_fp() {
		@Override
		public Expression call(Type type, Expression e1, Expression e2,
				SemanticContext context) {
			Expression e;
			char[] filename = e1.filename;
			int lineNumber = e1.lineNumber;
			int count;
			integer_t value;

			value = e1.toInteger(context);
			count = e2.toInteger(context).intValue();
			switch (e1.type.toBasetype(context).ty) {
			case Tint8:
				value = value.castToInt8().shiftRight(count);
				break;
			case Tuns8:
				value = value.castToUns8().shiftRight(count);
				break;
			case Tint16:
				value = value.castToInt16().shiftRight(count);
				break;
			case Tuns16:
				value = value.castToUns16().shiftRight(count);
				break;
			case Tint32:
				value = value.castToInt32().shiftRight(count);
				break;
			case Tuns32:
				value = value.castToUns32().shiftRight(count);
				break;
			case Tint64:
				value = value.castToInt64().shiftRight(count);
				break;
			case Tuns64:
				value = value.castToUns64().shiftRight(count);
				break;
			default:
				throw new IllegalStateException("assert(0);");
			}
			e = new IntegerExp(filename, lineNumber, value, type);
			e.copySourceRange(e1, e2);
			return e;
		}
	};

	public static final BinExp_fp Ushr = new BinExp_fp() {
		@Override
		public Expression call(Type type, Expression e1, Expression e2,
				SemanticContext context) {
			Expression e;
			char[] filename = e1.filename;
			int lineNumber = e1.lineNumber;
			int count;
			integer_t value;

			value = e1.toInteger(context);
			count = e2.toInteger(context).intValue();
			switch (e1.type.toBasetype(context).ty) {
			case Tint8:
			case Tuns8:
				throw new IllegalStateException("assert(0);"); // no way to trigger this

			case Tint16:
			case Tuns16:
				throw new IllegalStateException("assert(0);"); // no way to trigger this

			case Tint32:
			case Tuns32:
				value = (value.and(0xFFFFFFFFL)).shiftRight(count);
				break;

			case Tint64:
			case Tuns64:
				value = value.castToUns64().shiftRight(count);
				break;

			default:
				throw new IllegalStateException("assert(0);");
			}
			e = new IntegerExp(filename, lineNumber, value, type);
			e.copySourceRange(e1, e2);
			return e;
		}
	};

	public static final BinExp_fp And = new BinExp_fp() {
		@Override
		public Expression call(Type type, Expression e1, Expression e2,
				SemanticContext context) {
			Expression e;

			e = new IntegerExp(e1.filename, e1.lineNumber, e1.toInteger(context).and(
					e2.toInteger(context)), type);
			e.start = e1.start;
			e.length = e2.start + e2.length - e1.start;
			return e;
		}
	};

	public static final BinExp_fp Or = new BinExp_fp() {
		@Override
		public Expression call(Type type, Expression e1, Expression e2,
				SemanticContext context) {
			Expression e;

			e = new IntegerExp(e1.filename, e1.lineNumber, e1.toInteger(context).or(
					e2.toInteger(context)), type);
			e.copySourceRange(e1, e2);
			return e;
		}
	};

	public static final BinExp_fp Xor = new BinExp_fp() {
		@Override
		public Expression call(Type type, Expression e1, Expression e2,
				SemanticContext context) {
			Expression e;

			e = new IntegerExp(e1.filename, e1.lineNumber, e1.toInteger(context).xor(
					e2.toInteger(context)), type);
			e.copySourceRange(e1, e2);
			return e;
		}
	};

	public static final BinExp_fp Index = new BinExp_fp() {
		@Override
		public Expression call(Type type, Expression e1, Expression e2,
				SemanticContext context) {
			Expression e = EXP_CANT_INTERPRET;
			char[] filename = e1.filename;
			int lineNumber = e1.lineNumber;

			assert (null != e1.type);
			if (e1.op == TOKstring && e2.op == TOKint64) {
				StringExp es1 = (StringExp) e1;
				integer_t i = e2.toInteger(context);

				if (i.compareTo(es1.len) >= 0) {
					if (context.acceptsErrors()) {
						context.acceptProblem(Problem.newSemanticTypeError(IProblem.StringIndexOutOfBounds, e2, i.toString(), String.valueOf(es1.len)));
					}
				} else {
					int value = es1.charAt(i.intValue());
					e = new IntegerExp(filename, lineNumber, value, type);
				}
			} else if (e1.type.toBasetype(context).ty == Tsarray
					&& e2.op == TOKint64) {
				TypeSArray tsa = (TypeSArray) e1.type.toBasetype(context);
				int length = tsa.dim.toInteger(context).intValue();
				int i = e2.toInteger(context).intValue();

				if (i >= length) {
					context.acceptProblem(Problem.newSemanticTypeError(
							IProblem.ArrayIndexOutOfBounds2, e2, String.valueOf(i),
						            e1.toChars(context),
						            String.valueOf(length)));
				} else if (e1.op == TOKarrayliteral
						&& 0 == e1.checkSideEffect(2, context)) {
					ArrayLiteralExp ale = (ArrayLiteralExp) e1;
					e = ale.elements.get(i);
					e.type = type;
				}
			} else if (e1.type.toBasetype(context).ty == Tarray
					&& e2.op == TOKint64) {
				integer_t i = e2.toInteger(context).castToUns64();

				if (e1.op == TOKarrayliteral
						&& 0 == e1.checkSideEffect(2, context)) {
					ArrayLiteralExp ale = (ArrayLiteralExp) e1;
					if (i.compareTo(ale.elements.size()) >= 0) {
						context.acceptProblem(Problem.newSemanticTypeError(
								IProblem.ArrayIndexOutOfBounds2, e2, i.toString(), e1.toChars(context), 
							            Integer.toString(ale.elements.size())));
					} else {
						e = ale.elements.get(i.intValue());
						e.type = type;
					}
				}
			} else if (e1.op == TOKassocarrayliteral
					&& 0 == e1.checkSideEffect(2, context)) {
				AssocArrayLiteralExp ae = (AssocArrayLiteralExp) e1;
				/*
				 * Search the keys backwards, in case there are duplicate keys
				 */
				int i = ae.keys.size();
				while (true) {
					i--;
					Expression ekey = ae.keys.get(i);
					Expression ex = Equal.call(TOKequal, Type.tbool, ekey, e2,
							context);
					if (ex == EXP_CANT_INTERPRET) {
						return ex;
					}
					if (ex.isBool(true)) {
						e = ae.values.get(i);
						e.type = type;
						break;
					}
				}
			}
			e.copySourceRange(e1, e2);
			return e;
		}
	};

	public static final BinExp_fp Cat = new BinExp_fp() {
		@Override
		public Expression call(Type type, Expression e1, Expression e2,
				SemanticContext context) {
			Expression e = EXP_CANT_INTERPRET;
			char[] filename = e1.filename;
			int lineNumber = e1.lineNumber;
			Type t;
		    Type t1 = e1.type.toBasetype(context);
		    Type t2 = e2.type.toBasetype(context);

			if ((e1.op == TOKnull && (e2.op == TOKint64 || e2.op == TOKstructliteral))
					|| ((e1.op == TOKint64 || e1.op == TOKstructliteral) && e2.op == TOKnull)) {
				if (e1.op == TOKnull && (e2.op == TOKint64 || e2.op == TOKstructliteral)) {
					e = e2;
				} else {
					e = e1;
				}

				Type tn = e.type.toBasetype(context);
				if (tn.ty == Tchar || tn.ty == Twchar || tn.ty == Tdchar) {
					// Create a StringExp
					char[] s = new char[1];
					StringExp es;
					int len = 1;
					int sz = tn.size(context);
					integer_t v = e.toInteger(context);

					s[0] = (char) v.intValue();

					es = new StringExp(filename, lineNumber, s, len);
					es.sz = sz;
					es.committed = true;
					e = es;
				} else {
					// Create an ArrayLiteralExp
					Expressions elements = new Expressions(1);
					elements.add(e);
					e = new ArrayLiteralExp(e.filename, e.lineNumber,  elements);
				}

				e.type = type;
				return e;
			}

			else if (e1.op == TOKstring && e2.op == TOKstring) {
				// Concatenate the strings
				StringExp es1 = (StringExp) e1;
				StringExp es2 = (StringExp) e2;
				// TODO implement this correctly
				StringExp es = new StringExp(filename, lineNumber, CharOperation.concat(
						es1.string, es2.string), es1.len + es2.len);

				int sz = es1.sz;
				if (sz != es2.sz) {
				    /* Can happen with:
				     *   auto s = "foo"d ~ "bar"c;
				     */
//				    assert(global.errors);
				    return e;
				}
				es.sz = sz;
				es.committed = es1.committed | es2.committed;
				if (es1.committed) {
					t = es1.type;
				} else {
					t = es2.type;
				}
				es.type = type;
				e = es;
			}

			else if (e1.op == TOKstring && e2.op == TOKint64) {
				// Concatenate the strings
				StringExp es1 = (StringExp) e1;
				StringExp es;
//				int len = es1.len + 1;
				int sz = es1.sz;

				/*
				 * PERHAPS java's char type is 16 bits wide -- dchar is 32 bits --
				 * this could be a problem, if, say, one was creating a
				 * cuneiform string by concatenating integers at compile-time,
				 * and then using said string for mixin identifier generation or
				 * something else Descent cares about... one of my favorite
				 * activities!
				 */
				char[] v = new char[] { (char) e2.toInteger(context).intValue() };

				// TODO implement this correctly
				es = new StringExp(filename, lineNumber, CharOperation.concat(es1.string, v),
						es1.len + v.length);
				es.sz = sz;
				es.committed = es1.committed;
				t = es1.type;
				es.type = type;
				e = es;
			}

			else if (e1.op == TOKint64 && e2.op == TOKstring) {
				// Concatenate the strings
				StringExp es2 = (StringExp) e2;
				StringExp es;
				int sz = es2.sz;
				char[] v = new char[] { (char) e1.toInteger(context).intValue() };

				es = new StringExp(filename, lineNumber, CharOperation.concat(v, es2.string),
						v.length + es2.len);
				es.sz = sz;
				es.committed = es2.committed;
				t = es2.type;
				es.type = type;
				e = es;
			} else if (
				(context.isD1() && (
					e1.op == TOKarrayliteral && e2.op == TOKarrayliteral
					&& e1.type.equals(e2.type))) ||
				(!context.isD1() && e1.op == TOKarrayliteral && e2.op == TOKarrayliteral &&
						t1.nextOf().equals(t2.nextOf())
						)) {
				// Concatenate the arrays
				ArrayLiteralExp es1 = (ArrayLiteralExp) e1;
				ArrayLiteralExp es2 = (ArrayLiteralExp) e2;

				ArrayLiteralExp ale = new ArrayLiteralExp(es1.filename, es1.lineNumber,
						new Expressions(es1.elements.size()
								+ es2.elements.size()));
				ale.elements.addAll(es1.elements);
				ale.elements.addAll(es2.elements);
				e = ale;

				if (type.toBasetype(context).ty == Tsarray) {
					if (context.isD1()) {
						e.type = new TypeSArray(e1.type.toBasetype(context).nextOf(),
								new IntegerExp(null, 0, es1.elements.size(),
										Type.tindex), context.encoder);
					} else {
						e.type = new TypeSArray(t1.nextOf(),
								new IntegerExp(filename, lineNumber, es1.elements.size(),
										Type.tindex), context.encoder);
					}
					e.type = e.type.semantic(filename, lineNumber, null, context);
				} else {
					e.type = type;
				}
			}
		    else if ((e1.op == TOKarrayliteral && e2.op == TOKnull && t1
					.nextOf().equals(t2.nextOf()))
					|| (e1.op == TOKnull && e2.op == TOKarrayliteral && t1
							.nextOf().equals(t2.nextOf()))) {
				if ((e1.op == TOKarrayliteral && e2.op == TOKnull && t1
						.nextOf().equals(t2.nextOf()))) {
					e = e1;
				} else {
					e = e2;
				}
				// L3:
				// Concatenate the array with null
				ArrayLiteralExp es = (ArrayLiteralExp) e;

				es = new ArrayLiteralExp(es.filename, es.lineNumber, (Expressions) es.elements
						.copy());
				e = es;

				if (type.toBasetype(context).ty == Tsarray) {
					e.type = new TypeSArray(t1.nextOf(), new IntegerExp(filename, lineNumber,
							ASTDmdNode.size(es.elements), Type.tindex),
							context.encoder);
					e.type = e.type.semantic(filename, lineNumber, null, context);
				} else {
					e.type = type;
				}
			}
			else if ((e1.op == TOKarrayliteral || e1.op == TOKnull)
					&& e1.type.toBasetype(context).nextOf().equals(e2.type)) {
				ArrayLiteralExp es1;
				if (e1.op == TOKarrayliteral) {
					es1 = (ArrayLiteralExp) e1;
					es1 = new ArrayLiteralExp(es1.filename, es1.lineNumber, es1.elements.copy());
					es1.elements.add(e2);
				} else {
					 es1 = new ArrayLiteralExp(e1.filename, e1.lineNumber, e2);
				}
				e = es1;

				if (type.toBasetype(context).ty == Tsarray) {
					e.type = new TypeSArray(e2.type, new IntegerExp(filename, lineNumber,
							es1.elements.size(), Type.tindex), context.encoder);
					e.type = e.type.semantic(filename, lineNumber, null, context);
				} else {
					e.type = type;
				}
			}

			else if (e2.op == TOKarrayliteral
					&& e2.type.toBasetype(context).nextOf().equals(e1.type)) {
				ArrayLiteralExp es2 = (ArrayLiteralExp) e2;

				ArrayLiteralExp ale = new ArrayLiteralExp(es2.filename, es2.lineNumber,
						new Expressions(es2.elements.size() + 1));
				ale.elements.add(e1);
				ale.elements.addAll(es2.elements);
				e = ale;

				if (type.toBasetype(context).ty == Tsarray) {
					e.type = new TypeSArray(e1.type, new IntegerExp(filename, lineNumber,
							es2.elements.size(), Type.tindex), context.encoder);
					e.type = e.type.semantic(filename, lineNumber, null, context);
				} else {
					e.type = type;
				}
			}

			else if ((e1.op == TOKnull && e2.op == TOKstring)
					|| (e1.op == TOKstring && e2.op == TOKnull)) {
				if (e1.op == TOKnull && e2.op == TOKstring) {
					t = e1.type;
					e = e2;
				} else {
					e = e1;
					t = e2.type;
				}
				Type tb = t.toBasetype(context);
				if (tb.ty == Tarray && tb.nextOf().equals(e.type)) {
					Expressions expressions = new Expressions(1);
					expressions.add(e);
					e = new ArrayLiteralExp(filename, lineNumber, expressions);
					e.type = t;
				}
				if (!e.type.equals(type)) {
					StringExp se = (StringExp) e.copy();
					e = se.castTo(null, type, context);
				}
			}
			e.copySourceRange(e1, e2);
			return e;
		}
	};

	// --------------------------------------------------------------------------
	public static interface BinExp_fp2 {
		public Expression call(TOK op, Type type, Expression e1, Expression e2,
				SemanticContext context);
	}

	public static final BinExp_fp2 Equal = new BinExp_fp2() {
		@Override
		public Expression call(TOK op, Type type, Expression e1, Expression e2,
				SemanticContext context) {
			Expression e;
			char[] filename = e1.filename;
			int lineNumber = e1.lineNumber;
			boolean cmp = false;
			real_t r1;
			real_t r2;

			assert (op == TOKequal || op == TOKnotequal);

		    if (e1.op == TOKnull) {
				if (e2.op == TOKnull) {
					cmp = true;
				}
				else if (e2.op == TOKstring) {
					StringExp es2 = (StringExp) e2;
					cmp = (0 == es2.len);
				} else if (e2.op == TOKarrayliteral) {
					ArrayLiteralExp es2 = (ArrayLiteralExp) e2;
					cmp = null == es2.elements || (0 == es2.elements.size());
				} else {
					return EXP_CANT_INTERPRET;
				}
			} else if (e2.op == TOKnull) {
				if (e1.op == TOKstring) {
					StringExp es1 = (StringExp) e1;
					cmp = (0 == es1.len);
				} else if (e1.op == TOKarrayliteral) {
					ArrayLiteralExp es1 = (ArrayLiteralExp) e1;
					cmp = null == es1.elements || (0 == es1.elements.size());
				} else {
					return EXP_CANT_INTERPRET;
				}
			} else if (e1.op == TOKstring && e2.op == TOKstring) {
				StringExp es1 = (StringExp) e1;
				StringExp es2 = (StringExp) e2;

				if (es1.sz != es2.sz) {
//					assert(context.global.errors);
					return EXP_CANT_INTERPRET;
				}
				if (es1.len == es2.len
						&& CharOperation.equals(es1.string, es2.string)) {
					cmp = true;
				} else {
					cmp = false;
				}
			} else if (e1.op == TOKarrayliteral && e2.op == TOKarrayliteral) {
				ArrayLiteralExp es1 = (ArrayLiteralExp) e1;
				ArrayLiteralExp es2 = (ArrayLiteralExp) e2;

				if ((null == es1.elements || es1.elements.isEmpty())
						&& (null == es2.elements || es2.elements.isEmpty())) {
					cmp = true; // both arrays are empty
				} else if (null == es1.elements || es2.elements.isEmpty()) {
					cmp = false;
				} else if (es1.elements.size() != es2.elements.size()) {
					cmp = false;
				} else {
					cmp = true;
					for (int i = 0; i < es1.elements.size(); i++) {
						Expression ee1 = es1.elements.get(i);
						Expression ee2 = es2.elements.get(i);

						Expression v = call(TOKequal, Type.tint32, ee1, ee2,
								context);
						if (v == EXP_CANT_INTERPRET) {
							return EXP_CANT_INTERPRET;
						}
						cmp = v.toInteger(context).isTrue();
						if (cmp == false) {
							break;
						}
					}
				}
			} else if ((e1.op == TOKarrayliteral && e2.op == TOKstring) ||
					(e1.op == TOKstring && e2.op == TOKarrayliteral)) {
				if (e1.op == TOKarrayliteral && e2.op == TOKstring) { 
					// Swap operands and use common code
					Expression ex = e1;
					e1 = e2;
					e2 = ex;
				}
				StringExp es1 = (StringExp) e1;
				ArrayLiteralExp es2 = (ArrayLiteralExp) e2;
				int dim1 = es1.len;
				int dim2 = es2.elements != null ? es2.elements.size() : 0;
				if (dim1 != dim2) {
					cmp = false;
				} else {
					for (int i = 0; i < dim1; i++) {
						integer_t c = new integer_t((int) es1.charAt(i));
						Expression ee2 = (Expression) es2.elements.get(i);
						if (ee2.isConst()) {
							return EXP_CANT_INTERPRET;
						}
						cmp = (c.equals(ee2.toInteger(context)));
						if (cmp == false) {
							break;
						}
					}
				}
			} else if (e1.op == TOKstructliteral && e2.op == TOKstructliteral) {
				StructLiteralExp es1 = (StructLiteralExp) e1;
				StructLiteralExp es2 = (StructLiteralExp) e2;

				if (es1.sd != es2.sd) {
					cmp = false;
				} else if ((null == es1.elements || es1.elements.isEmpty())
						&& (null == es2.elements || es2.elements.isEmpty())) {
					cmp = true; // both arrays are empty
				} else if (null == es1.elements || null == es2.elements) {
					cmp = false;
				} else if (es1.elements.size() != es2.elements.size()) {
					cmp = false;
				} else {
					cmp = true;
					for (int i = 0; i < es1.elements.size(); i++) {
						Expression ee1 = es1.elements.get(i);
						Expression ee2 = es2.elements.get(i);

						if (ee1 == ee2) {
							continue;
						}
						if (null == ee1 || null == ee2) {
							cmp = false;
							break;
						}

						Expression v = call(TOKequal, Type.tint32, ee1, ee2,
								context);
						if (v == EXP_CANT_INTERPRET) {
							return EXP_CANT_INTERPRET;
						}
						cmp = v.toInteger(context).isTrue();
						if (cmp == false) {
							break;
						}
					}
				}
			} else if (!e1.isConst() || !e2.isConst()) {
				return EXP_CANT_INTERPRET;
			} else if (e1.type.isreal() || e1.type.isimaginary()) {
				if (e1.type.isreal()) {
					r1 = e1.toReal(context);
					r2 = e2.toReal(context);
				} else {
					r1 = e1.toImaginary(context);
					r2 = e2.toImaginary(context);
				}

				if (r1.isNaN() || r2.isNaN()) // if unordered
				{
					cmp = false;
				} else {
					cmp = r1.equals(r2);
				}
			} else if (e1.type.iscomplex()) {
				cmp = e1.toComplex(context).equals(e2.toComplex(context));
			} else if (e1.type.isintegral()) {
				cmp = e1.toInteger(context).equals(e2.toInteger(context));
			} else {
				return EXP_CANT_INTERPRET;
			}
			if (op == TOKnotequal) {
				cmp ^= true;
			}
			e = new IntegerExp(filename, lineNumber, cmp ? 1 : 0, type);
			e.copySourceRange(e1, e2);
			return e;
		}
	};

	public static final BinExp_fp2 Cmp = new BinExp_fp2() {
		@Override
		public Expression call(TOK op, Type type, Expression e1, Expression e2,
				SemanticContext context) {
			Expression e;
			char[] filename = e1.filename;
			int lineNumber = e1.lineNumber;
			int n = 0; // Just to keep Java from complaining, the default
			// should never be used.
			real_t r1;
			real_t r2;

		    if (!context.isD1() && e1.op == TOKstring && e2.op == TOKstring)
		    {	StringExp es1 = (StringExp)e1;
			StringExp es2 = (StringExp)e2;
			int sz = es1.sz;
			assert(sz == es2.sz);

			int len = es1.len;
			if (es2.len < len)
			    len = es2.len;
			
			int cmp = new String(es1.string).compareTo(new String(es2.string));
			if (cmp == 0)
			    cmp = es1.len - es2.len;

			switch (op)
			{
			    case TOKlt:	n = cmp <  0 ? 1 : 0;	break;
			    case TOKle:	n = cmp <= 0 ? 1 : 0;	break;
			    case TOKgt:	n = cmp >  0 ? 1 : 0;	break;
			    case TOKge:	n = cmp >= 0 ? 1 : 0;	break;

			    case TOKleg:   n = 1;		break;
			    case TOKlg:	   n = cmp != 0 ? 1 : 0;	break;
			    case TOKunord: n = 0;		break;
			    case TOKue:	   n = cmp == 0 ? 1 : 0;	break;
			    case TOKug:	   n = cmp >  0 ? 1 : 0;	break;
			    case TOKuge:   n = cmp >= 0 ? 1 : 0;	break;
			    case TOKul:	   n = cmp <  0 ? 1 : 0;	break;
			    case TOKule:   n = cmp <= 0 ? 1 : 0;	break;

			    default:
			    	throw new IllegalStateException();
			}
		    }
		    else if (!context.isD1() && !e1.isConst() || !e2.isConst())
			return EXP_CANT_INTERPRET;
		    else if (e1.type.isreal() || e1.type.isimaginary()) {
				if (e1.type.isreal()) {
					r1 = e1.toReal(context);
					r2 = e2.toReal(context);
				} else {
					r1 = e1.toImaginary(context);
					r2 = e2.toImaginary(context);
				}
				// Don't rely on compiler, handle NAN arguments separately
				if (r1.isNaN() || r2.isNaN()) // if unordered
				{
					switch (op) {
					case TOKlt:
						n = 0;
						break;
					case TOKle:
						n = 0;
						break;
					case TOKgt:
						n = 0;
						break;
					case TOKge:
						n = 0;
						break;

					case TOKleg:
						n = 0;
						break;
					case TOKlg:
						n = 0;
						break;
					case TOKunord:
						n = 1;
						break;
					case TOKue:
						n = 1;
						break;
					case TOKug:
						n = 1;
						break;
					case TOKuge:
						n = 1;
						break;
					case TOKul:
						n = 1;
						break;
					case TOKule:
						n = 1;
						break;

					default:
						assert (false);
					}
				} else {
					switch (op) {
					case TOKlt:
						n = (r1.compareTo(r2) < 0) ? 1 : 0;
						break;
					case TOKle:
						n = (r1.compareTo(r2) <= 0) ? 1 : 0;
						break;
					case TOKgt:
						n = (r1.compareTo(r2) > 0) ? 1 : 0;
						break;
					case TOKge:
						n = (r1.compareTo(r2) >= 0) ? 1 : 0;
						break;

					case TOKleg:
						n = 1;
						break;
					case TOKlg:
						n = (r1.compareTo(r2) != 0) ? 1 : 0;
						break;
					case TOKunord:
						n = 0;
						break;
					case TOKue:
						n = (r1.compareTo(r2) == 0) ? 1 : 0;
						break;
					case TOKug:
						n = (r1.compareTo(r2) > 0) ? 1 : 0;
						break;
					case TOKuge:
						n = (r1.compareTo(r2) >= 0) ? 1 : 0;
						break;
					case TOKul:
						n = (r1.compareTo(r2) < 0) ? 1 : 0;
						break;
					case TOKule:
						n = (r1.compareTo(r2) <= 0) ? 1 : 0;
						break;

					default:
						assert (false);
					}
				}
			} else if (e1.type.iscomplex()) {
				assert (false);
			} else {
				integer_t n1;
				integer_t n2;

				n1 = e1.toInteger(context).castToSinteger_t();
				n2 = e2.toInteger(context).castToSinteger_t();
				if (e1.type.isunsigned() || e2.type.isunsigned()) {
					switch (op) {
					case TOKlt:
						n = n1.castToUns64().compareTo(n2.castToUns64()) < 0 ? 1
								: 0;
						break;
					case TOKle:
						n = n1.castToUns64().compareTo(n2.castToUns64()) <= 0 ? 1
								: 0;
						break;
					case TOKgt:
						n = n1.castToUns64().compareTo(n2.castToUns64()) > 0 ? 1
								: 0;
						break;
					case TOKge:
						n = n1.castToUns64().compareTo(n2.castToUns64()) >= 0 ? 1
								: 0;
						break;

					case TOKleg:
						n = 1;
						break;
					case TOKlg:
						n = n1.castToUns64().compareTo(n2.castToUns64()) != 0 ? 1
								: 0;
						break;
					case TOKunord:
						n = 0;
						break;
					case TOKue:
						n = n1.castToUns64().compareTo(n2.castToUns64()) == 0 ? 1
								: 0;
						break;
					case TOKug:
						n = n1.castToUns64().compareTo(n2.castToUns64()) > 0 ? 1
								: 0;
						break;
					case TOKuge:
						n = n1.castToUns64().compareTo(n2.castToUns64()) >= 0 ? 1
								: 0;
						break;
					case TOKul:
						n = n1.castToUns64().compareTo(n2.castToUns64()) < 0 ? 1
								: 0;
						break;
					case TOKule:
						n = n1.castToUns64().compareTo(n2.castToUns64()) <= 0 ? 1
								: 0;
						break;
					default:
						throw new IllegalStateException("assert(0);");
					}
				} else {
					switch (op) {
					case TOKlt:
						n = (n1.compareTo(n2) < 0) ? 1 : 0;
						break;
					case TOKle:
						n = (n1.compareTo(n2) <= 0) ? 1 : 0;
						break;
					case TOKgt:
						n = (n1.compareTo(n2) > 0) ? 1 : 0;
						break;
					case TOKge:
						n = (n1.compareTo(n2) >= 0) ? 1 : 0;
						break;

					case TOKleg:
						n = 1;
						break;
					case TOKlg:
						n = (n1.compareTo(n2) != 0) ? 1 : 0;
						break;
					case TOKunord:
						n = 0;
						break;
					case TOKue:
						n = (n1.compareTo(n2) == 0) ? 1 : 0;
						break;
					case TOKug:
						n = (n1.compareTo(n2) > 0) ? 1 : 0;
						break;
					case TOKuge:
						n = (n1.compareTo(n2) >= 0) ? 1 : 0;
						break;
					case TOKul:
						n = (n1.compareTo(n2) < 0) ? 1 : 0;
						break;
					case TOKule:
						n = (n1.compareTo(n2) <= 0) ? 1 : 0;
						break;

					default:
						assert false;
					}
				}
			}
			e = new IntegerExp(filename, lineNumber, n, type);
			e.copySourceRange(e1, e2);
			return e;
		}
	};

	public static final BinExp_fp2 Identity = new BinExp_fp2() {
		@Override
		public Expression call(TOK op, Type type, Expression e1, Expression e2,
				SemanticContext context) {
			char[] filename = e1.filename;
			int lineNumber = e1.lineNumber;
			boolean cmp;

		    if (e1.op == TOKnull && e2.op == TOKnull) {
				cmp = true;
			} else if (e1.op == TOKsymoff && e2.op == TOKsymoff) {
				SymOffExp es1 = (SymOffExp) e1;
				SymOffExp es2 = (SymOffExp) e2;

				cmp = (es1.var == es2.var && es1.offset == es2.offset);
			} else if (e1.isConst() && e2.isConst()) {
				return Equal.call((op == TOKidentity) ? TOKequal : TOKnotequal,
						type, e1, e2, context);
			} else {
				assert (false);
				cmp = false;
			}

			if (op == TOKnotidentity) {
				cmp = !cmp;
			}

			Expression e = new IntegerExp(filename, lineNumber, cmp ? 1 : 0, type);
			e.copySourceRange(e1, e2);
			return e;
		}
	};

	// --------------------------------------------------------------------------
	public static final Expression Slice(Type type, Expression e1,
			Expression lwr, Expression upr, SemanticContext context) {
		Expression e = EXP_CANT_INTERPRET;
		char[] filename = e1.filename;
		int lineNumber = e1.lineNumber;

		if (e1.op == TOKstring && lwr.op == TOKint64 && upr.op == TOKint64) {
			StringExp es1 = (StringExp) e1;
			int ilwr = lwr.toInteger(context).intValue();
			int iupr = upr.toInteger(context).intValue();

			if (iupr > es1.len || ilwr > iupr) {
				context.acceptProblem(Problem.newSemanticTypeError(IProblem.StringSliceIsOutOfBounds, e1, String.valueOf(ilwr), String.valueOf(iupr)));
			} else {
				int len = iupr - ilwr;
				int sz = es1.sz;
				StringExp es;
				char[] s = new char[len];
				System.arraycopy(es1.string, ilwr, s, 0, len);
				es = new StringExp(filename, lineNumber, s, es1.postfix);
				es.sz = sz;
				es.committed = true;
				es.type = type;
				e = es;
			}
		}

		else if (e1.op == TOKarrayliteral && lwr.op == TOKint64
				&& upr.op == TOKint64 && 0 == e1.checkSideEffect(2, context)) {
			ArrayLiteralExp es1 = (ArrayLiteralExp) e1;
			int ilwr = lwr.toInteger(context).intValue();
			int iupr = upr.toInteger(context).intValue();

			if (iupr > es1.elements.size() || ilwr > iupr) {
				context.acceptProblem(Problem.newSemanticTypeError(
						IProblem.ArraySliceIfOutOfBounds, es1, String.valueOf(ilwr), String.valueOf(iupr)));
			} else {
				Expressions elements = new Expressions(iupr - ilwr);
				elements.setDim(iupr - ilwr);

				for (int i = ilwr; i < iupr; i++) {
					elements.set(i - ilwr, es1.elements.get(i));
				}
				e = new ArrayLiteralExp(e1.filename, e1.lineNumber, elements);
				e.type = type;
			}
		}
		return e;
	}

	public final static Expression Cast(Type type, Type to, Expression e1,
			SemanticContext context) {
		Expression e = EXP_CANT_INTERPRET;
		char[] filename = e1.filename;
		int lineNumber = e1.lineNumber;
		
		Type typeb = null;
		if (context.isD1()) {
			if (type.equals(e1.type) && to.equals(type)) {
				return e1;
			}

			if (!e1.isConst()) {
				return EXP_CANT_INTERPRET;
			}
		} else {
			if (e1.type.equals(type) && type.equals(to))
				return e1;
			if (e1.type.implicitConvTo(to, context).ordinal() >= MATCHconst
					.ordinal()
					|| to.implicitConvTo(e1.type, context).ordinal() >= MATCHconst
							.ordinal())
				return expType(to, e1, context);

			Type tb = to.toBasetype(context);
			typeb = type.toBasetype(context);

			if (e1.op == TOKstring) {
				if (tb.ty == Tarray
						&& typeb.ty == Tarray
						&& tb.nextOf().size(context) == typeb.nextOf().size(
								context)) {
					return expType(to, e1, context);
				}
			}

			if (!e1.isConst())
				return EXP_CANT_INTERPRET;
		}

		Type tb = to.toBasetype(context);
		if (tb.ty == Tbool) {
			e = new IntegerExp(filename, lineNumber, e1.toInteger(context).equals(0) ? 0 : 1,
					type);
		} else if (type.isintegral()) {
			if (e1.type.isfloating()) {
				integer_t result;
				real_t r = e1.toReal(context);

				switch (context.isD1() ? type.toBasetype(context).ty : typeb.ty) {
				case Tint8:
					result = NumberUtils.castToInt8(r);
					break;
				case Tchar:
				case Tuns8:
					result = NumberUtils.castToUns8(r);
					break;
				case Tint16:
					result = NumberUtils.castToInt16(r);
					break;
				case Twchar:
				case Tuns16:
					result = NumberUtils.castToUns16(r);
					break;
				case Tint32:
					result = NumberUtils.castToInt32(r);
					break;
				case Tdchar:
				case Tuns32:
					result = NumberUtils.castToUns32(r);
					break;
				case Tint64:
					result = NumberUtils.castToInt64(r);
					break;
				case Tuns64:
					result = NumberUtils.castToUns64(r);
					break;
				default:
					throw new IllegalStateException("assert(0);");
				}

				e = new IntegerExp(filename, lineNumber, result, type);
			} else if (type.isunsigned()) {
				e = new IntegerExp(filename, lineNumber, e1.toUInteger(context), type);
			} else {
				e = new IntegerExp(filename, lineNumber, e1.toInteger(context), type);
			}
		} else if (tb.isreal()) {
			real_t value = e1.toReal(context);

			e = new RealExp(filename, lineNumber, value, type);
		} else if (tb.isimaginary()) {
			real_t value = e1.toImaginary(context);

			e = new RealExp(filename, lineNumber, value, type);
		} else if (tb.iscomplex()) {
			complex_t value = e1.toComplex(context);

			e = new ComplexExp(filename, lineNumber, value, type);
		} else if (tb.isscalar(context)) {
			e = new IntegerExp(filename, lineNumber, e1.toInteger(context), type);
		} else if (tb.ty == Tvoid) {
			e = EXP_CANT_INTERPRET;
		} else if (tb.ty == Tstruct && e1.op == TOKint64) { // Struct = 0;
			StructDeclaration sd = tb.toDsymbol(null, context)
					.isStructDeclaration();
			if (sd == null) {
				throw new IllegalStateException("assert(sd);");
			}
			Expressions elements = new Expressions(sd.fields.size());
			for (int i = 0; i < sd.fields.size(); i++) {
				Dsymbol s = sd.fields.get(i);
				VarDeclaration v = s.isVarDeclaration();
				if (v == null) {
					throw new IllegalStateException("assert(v);");
				}
				
				// SEMANTIC
				if (v.isConst()) {
					continue;
				}

				Expression exp = new IntegerExp(0);
				exp = Cast(v.type, v.type, exp, context);
				if (exp == EXP_CANT_INTERPRET) {
					return exp;
				}
				elements.add(exp);
			}
			e = new StructLiteralExp(filename, lineNumber, sd, elements);
			e.type = type;
		} else {
			if (context.acceptsErrors()) {
				context.acceptProblem(Problem.newSemanticTypeError(
						IProblem.CannotCastSymbolToSymbol, e1, e1.type.toChars(context),
						        type.toChars(context)));
			}
			e = new IntegerExp(filename, lineNumber, 0, Type.tint32);
		}
		e.copySourceRange(e1);
		return e;
	}

}