package descent.internal.compiler.parser;

import static descent.internal.compiler.parser.DYNCAST.DYNCAST_EXPRESSION;
import static descent.internal.compiler.parser.MATCH.MATCHconst;
import static descent.internal.compiler.parser.MATCH.MATCHexact;
import static descent.internal.compiler.parser.MATCH.MATCHnomatch;
import static descent.internal.compiler.parser.TOK.TOKstring;
import static descent.internal.compiler.parser.TY.Tarray;
import static descent.internal.compiler.parser.TY.Tchar;
import static descent.internal.compiler.parser.TY.Tdchar;
import static descent.internal.compiler.parser.TY.Tdelegate;
import static descent.internal.compiler.parser.TY.Tpointer;
import static descent.internal.compiler.parser.TY.Tsarray;
import static descent.internal.compiler.parser.TY.Tvoid;
import static descent.internal.compiler.parser.TY.Twchar;

import java.util.List;

import melnorme.utilbox.core.Assert;

import descent.core.compiler.CharOperation;
import descent.core.compiler.IProblem;
import descent.internal.compiler.parser.ast.IASTVisitor;


public class StringExp extends Expression {

	public char[] sourceString;

	public char[] string;
	public int len;
	public char postfix;
	public int sz; // 1: char, 2: wchar, 4: dchar
	public boolean committed; // !=0 if type is committed

	public List<StringExp> allStringExps;

	public StringExp(char[] filename, int lineNumber, char[] string) {
		this(filename, lineNumber, string, string.length);
	}

	public StringExp(char[] filename, int lineNumber, char[] string, char postfix) {
		this(filename, lineNumber, string, string.length, (char) 0);
	}

	public StringExp(char[] filename, int lineNumber, char[] string, int len) {
		this(filename, lineNumber, string, len, (char) 0);
	}

	public StringExp(char[] filename, int lineNumber, char[] string, int len, char postfix) {
		super(filename, lineNumber, TOK.TOKstring);
		this.string = string;
		this.len = len;
		this.sz = 1;
		this.committed = false;
		this.postfix = postfix;
	}

	@Override
	public void accept0(IASTVisitor visitor) {
		visitor.visit(this);
		visitor.endVisit(this);
	}

	@Override
	public String toChars(SemanticContext context) {
		OutBuffer out = new OutBuffer();
		HdrGenState hdr = new HdrGenState();
		toCBuffer(out, hdr, context);
		return out.toChars();
	}

	public int charAt(int i) {
		return string[i];
	}

	@Override
	public void toCBuffer(OutBuffer buf, HdrGenState hgs,
			SemanticContext context) {
		// TODO semantic: this is not the real implementation
		buf.data.append("\"");
		buf.data.append(this.string);
		buf.data.append("\"");
	}

	public StringExp toUTF8(Scope sc, SemanticContext context) {
		if (sz != 1) {
			// Convert to UTF-8 string
			committed = false;
			Expression e = castTo(sc, Type.tchar.arrayOf(context), context);
			e = e.optimize(WANTvalue, context);
			Assert.isTrue(e.op == TOK.TOKstring);
			StringExp se = (StringExp) e;
			Assert.isTrue(se.sz == 1);
			return se;
		}
		return this;
	}

	@Override
	public Expression semantic(Scope sc, SemanticContext context) {
		if (type == null) {
			OutBuffer buffer = new OutBuffer();
			int newlen = 0;
			int p;
			int[] u = { 0 };
			int[] c = { 0 };

			switch (postfix) {
			case 'd':
				for (u[0] = 0; u[0] < len;) {
					p = Utf.decodeChar(string, 0, len, u, c);
					// utf_decodeChar((unsigned char )string, len, &u, &c);
					if (p >= 0) {
						if (context.acceptsErrors()) {
							context.acceptProblem(Problem.newSemanticTypeError(p,
									this, new String[0]));
						}
						break;
					} else {
						buffer.write4(c[0]);
						newlen++;
					}
				}
				buffer.write4(0);
				string = buffer.extractData().toCharArray();
				len = newlen;
				sz = 4;
				if (context.isD1()) {
					type = new TypeSArray(Type.tdchar, new IntegerExp(filename, lineNumber, len,
							Type.tindex), context.encoder);
				} else {
					type = new TypeDArray(Type.tdchar.invariantOf(context));
				}
				committed = true;
				break;

			case 'w':
				for (u[0] = 0; u[0] < len;) {
					p = Utf.decodeChar(string, 0, len, u, c);
					if (p >= 0) {
						if (context.acceptsErrors()) {
							context.acceptProblem(Problem.newSemanticTypeError(p,
									this, new String[0]));
						}
						break;
					} else {
						buffer.writeUTF16(c[0]);
						newlen++;
						if (c[0] >= 0x10000) {
							newlen++;
						}
					}
				}
				buffer.writeUTF16(0);
				string = buffer.extractData().toCharArray();
				len = newlen;
				sz = 2;
				if (context.isD1()) {
					type = new TypeSArray(Type.twchar, new IntegerExp(filename, lineNumber, len,
							Type.tindex), context.encoder);
				} else {
					type = new TypeDArray(Type.twchar.invariantOf(context));
				}
				committed = true;
				break;

			case 'c':
				committed = true;
			default:
				if (context.isD1()) {
					type = new TypeSArray(Type.tchar, new IntegerExp(filename, lineNumber, len,
							Type.tindex), context.encoder);
				} else {
					type = new TypeDArray(Type.tchar.invariantOf(context));
				}
				break;
			}
			type = type.semantic(filename, lineNumber, sc, context);
		}
		return this;
	}

	@Override
	public int getNodeType() {
		return STRING_EXP;
	}

	@Override
	public Expression implicitCastTo(Scope sc, Type t, SemanticContext context) {
		if (context.isD1()) {
			return super.implicitCastTo(sc, t, context);
		} else {
			boolean committed = this.committed;
			Expression e = super.implicitCastTo(sc, t, context);
			if (e.op == TOKstring) {
				// Retain polysemous nature if it started out that way
				((StringExp) e).committed = committed;
			}
			return e;
		}
	}

	@Override
	public MATCH implicitConvTo(Type t, SemanticContext context) {
		if (!committed) {
			try {
				boolean comparison = context.isD2() ?
						t.ty == Tpointer && t.nextOf().ty == Tvoid :
						t.ty == Tpointer && t.nextOf().ty == Tvoid;
				if (!committed && comparison) {
					return MATCHnomatch;
				}
			} catch (NullPointerException e) {
				throw e;
			}
			if (type.ty == Tsarray || type.ty == Tarray || type.ty == Tpointer) {
				if (type.nextOf().ty == Tchar) {

					if (context.isD1()) {
						switch (t.ty) {
						case Tsarray:
							if (type.ty == Tsarray
									&& !((TypeSArray) type).dim.toInteger(context)
											.equals(
													((TypeSArray) t).dim
															.toInteger(context))) {
								return MATCHnomatch;
							}
						case Tarray:
						case Tpointer:
							if (t.nextOf().ty == Tchar) {
								return MATCHexact;
							} else if (t.nextOf().ty == Twchar) {
								return MATCHexact;
							} else if (t.nextOf().ty == Tdchar) {
								return MATCHexact;
							}
							break;
						}
					} else {
						Type tn;
						MATCH m;
						switch (t.ty) {
						case Tsarray:
							if (type.ty == Tsarray) {
								if (((TypeSArray) type).dim.toInteger(context) != ((TypeSArray) t).dim
										.toInteger(context))
									return MATCHnomatch;
								TY tynto = t.nextOf().ty;
								if (tynto == Tchar || tynto == Twchar
										|| tynto == Tdchar)
									return MATCHexact;
							} else if (type.ty == Tarray) {
								if (length(context) > ((TypeSArray) t).dim.toInteger(
										context).intValue())
									return MATCHnomatch;
								TY tynto = t.nextOf().ty;
								if (tynto == Tchar || tynto == Twchar
										|| tynto == Tdchar)
									return MATCHexact;
							}
						case Tarray:

						case Tpointer:
							tn = t.nextOf();
							m = MATCHexact;
							if (type.nextOf().mod != tn.mod) {
								if (!tn.isConst())
									return MATCHnomatch;
								m = MATCHconst;
							}
							switch (tn.ty) {
							case Tchar:
							case Twchar:
							case Tdchar:
								return m;
							}
							break;
						}
					}
				}
			}
		}
		return super.implicitConvTo(t, context);
	}

	@Override
	public boolean isBool(boolean result) {
		return result ? true : false;
	}

	@Override
	public Expression interpret(InterState istate, SemanticContext context) {
		return this;
	}

	@Override
	public Expression castTo(Scope sc, Type t, SemanticContext context) {
		/* This follows copy-on-write; any changes to 'this'
		 * will result in a copy.
		 * The this.string member is considered immutable.
		 */
		StringExp se;
		Type tb;
		int copied = 0;

		if (!committed && t.ty == Tpointer && t.nextOf().ty == Tvoid) {
			if (context.acceptsErrors()) {
				context.acceptProblem(Problem.newSemanticTypeError(
						IProblem.CannotConvertStringLiteralToVoidPointer, this));
			}
		}

		se = this;

		if (!committed) {
			se = (StringExp) copy();
			se.committed = true;
			copied = 1; // this is the only instance
		}

		if (same(type, t, context)) {
			return se;
		}

		tb = t.toBasetype(context);
		if (tb.ty == Tdelegate && type.toBasetype(context).ty != Tdelegate)
			return Expression_castTo(sc, t, context);

		Type typeb = type.toBasetype(context);
		if (same(typeb, tb, context)) {
			if (0 == copied) {
				se = (StringExp) copy();
				copied = 1;
			}
			se.type = t;
			return se;
		}

		if (!context.isD1()) {
		    if (committed && tb.ty == Tsarray && typeb.ty == Tarray) {
				se = (StringExp) copy();
				se.sz = tb.nextOf().size(context);
				se.len = (len * sz) / se.sz;
				se.committed = true;
				se.type = t;
				return se;
			}
		}

		if (tb.ty != Tsarray && tb.ty != Tarray && tb.ty != Tpointer) {
			if (0 == copied) {
				se = (StringExp) copy();
				copied = 1;
			}
			// goto Lcast;
			return castTo_Lcast(se, t);
		}
		if (typeb.ty != Tsarray && typeb.ty != Tarray && typeb.ty != Tpointer) {
			if (0 == copied) {
				se = (StringExp) copy();
				copied = 1;
			}
			// goto Lcast;
			return castTo_Lcast(se, t);
		}

		boolean gotoL2 = false;

		if (typeb.nextOf().size(context) == tb.nextOf().size(context)) {
			if (0 == copied) {
				se = (StringExp) copy();
				copied = 1;
			}
			if (tb.ty == Tsarray) {
				// goto L2;	// handle possible change in static array dimension
				gotoL2 = true;
			}

			if (!gotoL2) {
				se.type = t;
				return se;
			}
		}

		if (!gotoL2) {

			if (committed) {
				// goto Lcast;
				return castTo_Lcast(se, t);
			}

			int p;
			int[] u = { 0 };
			int[] c = { 0 };

			{
				OutBuffer buffer = new OutBuffer();
				int newlen = 0;
				TY tfty = se.type.nextOf().toBasetype(context).ty;
				TY ttty = tb.nextOf().toBasetype(context).ty;

				int x = X(tfty, ttty);
				if (x == X(Tchar, Tchar) || x == X(Twchar, Twchar)
						|| x == X(Tdchar, Tdchar)) {
					// break;
				} else if (x == X(Tchar, Twchar)) {
					for (u[0] = 0; u[0] < len;) {
						p = Utf.decodeChar(se.string, 0, len, u, c);
						if (p >= 0) {
							if (context.acceptsErrors()) {
								context.acceptProblem(Problem.newSemanticTypeError(p,
										this, new String[0]));
							}
						} else {
							buffer.writeUTF16(c[0]);
						}
					}
					newlen = buffer.offset() / 2;
					buffer.writeUTF16(0);
					// goto L1;
					if (0 == copied) {
						se = (StringExp) copy();
					}
					buffer.data.getChars(0, buffer.offset(),
							se.string = new char[buffer.offset()], 0);
					se.len = newlen;
					se.sz = tb.nextOf().size(context);
				} else if (x == X(Tchar, Tdchar)) {
					for (u[0] = 0; u[0] < len;) {
						p = Utf.decodeChar(se.string, 0, len, u, c);
						if (p >= 0) {
							if (context.acceptsErrors()) {
								context.acceptProblem(Problem.newSemanticTypeError(p,
										this, new String[0]));
							}
						}
						buffer.write4(c[0]);
						newlen++;
					}
					buffer.write4(0);
					// goto L1;
					if (0 == copied) {
						se = (StringExp) copy();
					}
					buffer.data.getChars(0, buffer.offset(),
							se.string = new char[buffer.offset()], 0);
					se.len = newlen;
					se.sz = tb.nextOf().size(context);
				} else if (x == X(Twchar, Tchar)) {
					for (u[0] = 0; u[0] < len;) {
						p = Utf.decodeWchar(se.string, 0, len, u, c);
						if (p >= 0) {
							if (context.acceptsErrors()) {
								context.acceptProblem(Problem.newSemanticTypeError(p,
										this, new String[0]));
							}
						} else {
							buffer.writeUTF8(c[0]);
						}
					}
					newlen = buffer.offset();
					buffer.writeUTF8(0);
					// goto L1;
					if (0 == copied) {
						se = (StringExp) copy();
					}
					buffer.data.getChars(0, buffer.offset(),
							se.string = new char[buffer.offset()], 0);
					se.len = newlen;
					se.sz = tb.nextOf().size(context);
				} else if (x == X(Twchar, Tdchar)) {
					for (u[0] = 0; u[0] < len;) {
						p = Utf.decodeWchar(se.string, 0, len, u, c);
						if (p >= 0) {
							if (context.acceptsErrors()) {
								context.acceptProblem(Problem.newSemanticTypeError(p,
										this, new String[0]));
							}
						}
						buffer.write4(c[0]);
						newlen++;
					}
					buffer.write4(0);
					// goto L1;
					if (0 == copied) {
						se = (StringExp) copy();
					}
					buffer.data.getChars(0, buffer.offset(),
							se.string = new char[buffer.offset()], 0);
					se.len = newlen;
					se.sz = tb.nextOf().size(context);
				} else if (x == X(Tdchar, Tchar)) {
					for (u[0] = 0; u[0] < len; u[0]++) {
						c[0] = se.string[u[0]];
						if (!Utf.isValidDchar(c[0])) {
							if (context.acceptsErrors()) {
								context.acceptProblem(Problem.newSemanticTypeError(
										IProblem.InvalidUCS32Char, this,
										new String[] { String.valueOf(c[0]) })); // TODO format in hexa
							}
						} else {
							buffer.writeUTF8(c[0]);
						}
						newlen++;
					}
					newlen = buffer.offset();
					buffer.writeUTF8(0);
					// goto L1;
					if (0 == copied) {
						se = (StringExp) copy();
					}
					buffer.data.getChars(0, buffer.offset(),
							se.string = new char[buffer.offset()], 0);
					se.len = newlen;
					se.sz = tb.nextOf().size(context);
				} else if (x == X(Tdchar, Twchar)) {
					for (u[0] = 0; u[0] < len; u[0]++) {
						c[0] = se.string[u[0]];
						if (!Utf.isValidDchar(c[0])) {
							if (context.acceptsErrors()) {
								context.acceptProblem(Problem.newSemanticTypeError(
										IProblem.InvalidUCS32Char, this,
										new String[] { String.valueOf(c[0]) })); // TODO format in hexa
							}
						} else {
							buffer.writeUTF16(c[0]);
						}
						newlen++;
					}
					newlen = buffer.offset() / 2;
					buffer.writeUTF16(0);
					// goto L1;
					// L1:
					if (0 == copied) {
						se = (StringExp) copy();
					}
					buffer.data.getChars(0, buffer.offset(),
							se.string = new char[buffer.offset()], 0);
					se.len = newlen;
					se.sz = tb.nextOf().size(context);
				} else {
					if (se.type.nextOf().size(context) == tb.nextOf().size(context)) {
						se.type = t;
						return se;
					}
					// goto Lcast;
					return castTo_Lcast(se, t);
				}
			}
		}

		// L2:

		// See if need to truncate or extend the literal
		if (tb.ty == Tsarray) {
			int dim2 = ((TypeSArray) tb).dim.toInteger(context).intValue();

			// Changing dimensions
			if (dim2 != se.len) {
				int newsz = se.sz;

				// Copy when changing the string literal
				char[] s = null;
				int d;

				d = (dim2 < se.len) ? dim2 : se.len;
				// TODO semantic
				// s = (unsigned char *)mem.malloc((dim2 + 1) * newsz);
				// memcpy(s, se.string, d * newsz);
				// Extend with 0, add terminating 0
				// TODO semantic
				// memset((char *)s + d * newsz, 0, (dim2 + 1 - d) * newsz);
				se = new StringExp(filename, lineNumber, s, dim2);
				se.string = s;
				se.len = dim2;
			}
		}
		se.type = t;
		return se;
	}

	private Expression castTo_Lcast(StringExp se, Type t) {
		Expression e = new CastExp(filename, lineNumber, se, t);
		e.type = t;
		return e;
	}

	@Override
	public boolean equals(Object obj, SemanticContext context) {
		if (!(obj instanceof ASTDmdNode)) {
			return false;
		}

		ASTDmdNode o = (ASTDmdNode) obj;

		if (o != null && o.dyncast() == DYNCAST_EXPRESSION) {
			Expression e = (Expression) o;

			if (e.op == TOKstring) {
				return compare(o) == 0;
			}
		}
		return false;
	}

	private int X(TY tf, TY tt) {
		return ((tf.ordinal()) * 256 + (tt.ordinal()));
	}

	public int compare(ASTDmdNode obj) {
		// Used to sort case statement expressions so we can do an efficient lookup
		StringExp se2 = (StringExp) (obj);

		// This is a kludge so isExpression() in template.c will return 5
		// for StringExp's.
		if (null == se2)
			return 5;

		if (se2.op != TOKstring) {
			throw new IllegalStateException("assert(se2.op == TOKstring);");
		}

		int len1 = len;
		int len2 = se2.len;

		if (len1 == len2) {
			switch (sz) {
			case 1:
				// TODO maybe do a CharOperation.compare for comparing char[]
				return CharOperation.equals(string, se2.string) ? 0 : 1;

			case 2: {
				// TODO semantic
				//				unsigned u;
				//				d_wchar s1 = (d_wchar) string;
				//				d_wchar s2 = (d_wchar) se2.string;
				//
				//				for (u = 0; u < len; u++) {
				//					if (s1[u] != s2[u])
				//						return s1[u] - s2[u];
				//				}
				// temporary workarround:
				return CharOperation.equals(string, se2.string) ? 0 : 1;
			}

			case 4: {
				// TODO semantic
				//				unsigned u;
				//				d_dchar s1 = (d_dchar) string;
				//				d_dchar s2 = (d_dchar) se2.string;
				//
				//				for (u = 0; u < len; u++) {
				//					if (s1[u] != s2[u])
				//						return s1[u] - s2[u];
				//				}
				// temporary workarround:
				return CharOperation.equals(string, se2.string) ? 0 : 1;
			}

			default:
				throw new IllegalStateException("assert(0)");
			}
		}
		return len1 - len2;
	}

	/**********************************
	 * Return length of string.
	 */
	public int length(SemanticContext context) {
		int result = 0;
		int[] c = { 0 };
		int p;

		switch (sz) {
		case 1:
			for (int u = 0; u < len;) {
				int[] pu = { u };
				p = Utf.decodeChar(string, 0, len, pu, c);
				u = pu[0];
				if (p >= 0) {
					if (context.acceptsErrors()) {
						context.acceptProblem(Problem.newSemanticTypeError(p,
								this, new String[0]));
					}
					break;
				} else
					result++;
			}
		break;

		case 2:
			for (int u = 0; u < len;) {
				int[] pu = { u };
				p = Utf.decodeChar(string, 0, len, pu, c);
				u = pu[0];
				if (p >= 0) {
					if (context.acceptsErrors()) {
						context.acceptProblem(Problem.newSemanticTypeError(p,
								this, new String[0]));
					}
					break;
				} else
					result++;
			}
		break;

		case 4:
			result = len;
		break;

		default:
			throw new IllegalStateException();
		}
		return result;
	}

	@Override
	public void toMangleBuffer(OutBuffer buf, SemanticContext context) {
		char m;
		OutBuffer tmp = new OutBuffer();
		int p;
		int[] c = { 0 };
		int[] u = { 0 };
		char[] q;
		int qlen;

		/* Write string in UTF-8 format
		 */
		switch (sz) {
		case 1:
			m = 'a';
			q = string;
			qlen = len;
			break;
		case 2:
			m = 'w';
			for (u[0] = 0; u[0] < len;) {
				p = Utf.decodeWchar(string, 0, len, u, c);
				if (p >= 0) {
					if (context.acceptsErrors()) {
						context.acceptProblem(Problem.newSemanticTypeError(p, this,
								new String[0]));
					}
				} else {
					tmp.writeUTF8(c[0]);
				}
				p = Utf.decodeWchar(string, 0, len, u, c);
				if (p >= 0)
					if (context.acceptsErrors()) {
						context.acceptProblem(Problem.newSemanticTypeError(p, this,
								new String[0]));
					}
				else
					tmp.writeUTF8(c[0]);
			}
			q = tmp.data.toString().toCharArray();
			qlen = tmp.data.length();
			break;
		case 4:
			m = 'd';
			for (u[0] = 0; u[0] < len; u[0]++) {
				c[0] = string[u[0]];
				if (!Utf.isValidDchar(c[0])) {
					if (context.acceptsErrors()) {
						context.acceptProblem(Problem.newSemanticTypeError(
								IProblem.InvalidUCS32Char, this,
								new String[] { String.valueOf(c[0]) })); // TODO format in hexa
					}
				} else
					tmp.writeUTF8(c[0]);
			}
			q = tmp.data.toString().toCharArray();
			qlen = tmp.data.length();
			break;
		default:
			throw new IllegalStateException("assert(0);");
		}
		buf.writeByte(m);
		buf.data.append(qlen).append("_");
		for (int i = 0; i < qlen; i++) {
			buf.data.append(q[i]);
			/* TODO semantic append correctly and remove the line above
			 buf.printf("%02x", q[i]);
			 */
		}
	}

}
