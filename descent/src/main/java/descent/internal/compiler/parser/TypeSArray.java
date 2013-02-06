package descent.internal.compiler.parser;

import static descent.internal.compiler.parser.DYNCAST.DYNCAST_DSYMBOL;
import static descent.internal.compiler.parser.DYNCAST.DYNCAST_EXPRESSION;
import static descent.internal.compiler.parser.DYNCAST.DYNCAST_TYPE;
import static descent.internal.compiler.parser.MATCH.MATCHconst;
import static descent.internal.compiler.parser.MATCH.MATCHconvert;
import static descent.internal.compiler.parser.MATCH.MATCHexact;
import static descent.internal.compiler.parser.MATCH.MATCHnomatch;
import static descent.internal.compiler.parser.STC.STCtemplateparameter;
import static descent.internal.compiler.parser.TOK.TOKvar;
import static descent.internal.compiler.parser.TY.Taarray;
import static descent.internal.compiler.parser.TY.Tarray;
import static descent.internal.compiler.parser.TY.Tbit;
import static descent.internal.compiler.parser.TY.Tchar;
import static descent.internal.compiler.parser.TY.Tclass;
import static descent.internal.compiler.parser.TY.Tdchar;
import static descent.internal.compiler.parser.TY.Tident;
import static descent.internal.compiler.parser.TY.Tpointer;
import static descent.internal.compiler.parser.TY.Tsarray;
import static descent.internal.compiler.parser.TY.Tvoid;
import static descent.internal.compiler.parser.TY.Twchar;
import melnorme.utilbox.tree.TreeVisitor;
import descent.core.Signature;
import descent.core.compiler.IProblem;
import descent.internal.compiler.parser.ast.IASTVisitor;


public class TypeSArray extends TypeArray {

	public Expression dim, sourceDim;
	
	// Descent: to improve performance, must be set by Parser or ModuleBuilder
	public ASTNodeEncoder encoder;  

	public TypeSArray(Type next, Expression dim, ASTNodeEncoder encoder) {
		super(TY.Tsarray, next);
		this.dim = dim;
		this.sourceDim = dim;
		this.encoder = encoder;
	}

	@Override
	public void accept0(IASTVisitor visitor) {
		boolean children = visitor.visit(this);
		if (children) {
			TreeVisitor.acceptChildren(visitor, sourceNext);
			TreeVisitor.acceptChildren(visitor, sourceDim);
		}
		visitor.endVisit(this);
	}

	@Override
	public int alignsize(SemanticContext context) {
		return next.alignsize(context);
	}
	
	@Override
	public MATCH constConv(Type to, SemanticContext context) {
		if (to.ty == Tsarray) {
			TypeSArray tsa = (TypeSArray) to;
			if (!dim.equals(tsa.dim))
				return MATCHnomatch;
		}
		return super.constConv(to, context);
	}

	@Override
	public MATCH deduceType(Scope sc, Type tparam,
			TemplateParameters parameters, Objects dedtypes,
			SemanticContext context) {
		// Extra check that array dimensions must match
		if (null != tparam) {
			if (tparam.ty == Tsarray) {
				TypeSArray tp = (TypeSArray) tparam;
				
			    if (tp.dim.op == TOKvar
						&& (((VarExp) tp.dim).var.storage_class & STCtemplateparameter) != 0) {
					int i = templateIdentifierLookup(
							((VarExp) tp.dim).var.ident, parameters);
					// This code matches code in TypeInstance::deduceType()
					if (i == -1) {
						// goto Lnomatch;
						return MATCHnomatch;
					}
					TemplateParameter tp2 = (TemplateParameter) parameters
							.get(i);
					TemplateValueParameter tvp = tp2.isTemplateValueParameter();
					if (null == tvp) {
						// goto Lnomatch;
						return MATCHnomatch;
					}
					Expression e = (Expression) dedtypes.get(i);
					if (e != null) {
						if (!dim.equals(e)) {
							// goto Lnomatch;
							return MATCHnomatch;
						}
					} else {
						Type vt = tvp.valType.semantic(null, 0, sc, context);
						MATCH m = (MATCH) dim.implicitConvTo(vt, context);
						if (m == MATCHnomatch) {
							// goto Lnomatch;
							return MATCHnomatch;
						}
						dedtypes.set(i, dim);
					}
				} else if (dim.toInteger(context) != tp.dim.toInteger(context)) {
					return MATCHnomatch;
				}
			}

			else if (tparam.ty == Taarray) {
				TypeAArray tp = (TypeAArray) tparam;
				if (tp.index.ty == Tident) {
					TypeIdentifier tident = (TypeIdentifier) tp.index;

					if (tident.idents.size() == 0) {
						IdentifierExp id = tident.ident;

						for (int i = 0; i < parameters.size(); i++) {
							TemplateParameter $tp = (TemplateParameter) parameters
									.get(i);

							if (equals($tp.ident, id)) { // Found the corresponding template parameter
								TemplateValueParameter tvp = $tp
										.isTemplateValueParameter();
								if (null == tvp || !tvp.valType.isintegral())
									return MATCHnomatch; // goto Lnomatch;

								if (null != dedtypes.get(i)) {
									if (!dim.equals(dedtypes.get(i), context))
										return MATCHnomatch; // goto Lnomatch;
								} else {
									dedtypes.set(i, dim);
								}
								return next.deduceType(sc, tparam.nextOf(),
										parameters, dedtypes, context);
							}
						}
					}
				}
			}

			else if (tparam.ty == Tarray) {
				MATCH m;

				m = next.deduceType(sc, tparam.nextOf(), parameters, dedtypes,
						context);
				if (m == MATCHexact)
					m = MATCHconvert;
				return m;
			}
		}

		return super.deduceType(sc, tparam, parameters, dedtypes, context);
	}

	@Override
	public Expression defaultInit(char[] filename, int lineNumber, SemanticContext context) {
		return next.defaultInit(filename, lineNumber, context);
	}

	@Override
	public Expression dotExp(Scope sc, Expression e, IdentifierExp ident,
			SemanticContext context) {
		if (equals(ident, Id.length)) {
			e = dim;
		} else if (equals(ident, Id.ptr)) {
			e = e.castTo(sc, next.pointerTo(context), context);
		} else {
			e = super.dotExp(sc, e, ident, context);
		}
		return e;
	}

	@Override
	public int getNodeType() {
		return TYPE_S_ARRAY;
	}

	@Override
	public TypeInfoDeclaration getTypeInfoDeclaration(SemanticContext context) {
		return new TypeInfoStaticArrayDeclaration(this, context);
	}

	@Override
	public boolean hasPointers(SemanticContext context) {
		return next.hasPointers(context);
	}

	@Override
	public MATCH implicitConvTo(Type to, SemanticContext context) {
		// Allow implicit conversion of static array to pointer or dynamic array
		if (context.isD1()) {
			if ((context.IMPLICIT_ARRAY_TO_PTR() && to.ty == Tpointer)
					&& (to.nextOf().ty == Tvoid || next.equals(to.nextOf())
					/* || to.next.isBaseOf(next) */)) {
				return MATCHconvert;
			}
			if (to.ty == Tarray) {
				int offset = 0;
	
				if (next.equals(to.nextOf())
						|| (to.nextOf().isBaseOf(next, new int[] { offset }, context) && offset == 0)
						|| to.nextOf().ty == Tvoid)
					return MATCHconvert;
			}
			return super.implicitConvTo(to, context);
		} else {
		    // Allow implicit conversion of static array to pointer or dynamic array
			if (context.IMPLICIT_ARRAY_TO_PTR() && to.ty == Tpointer) {
				TypePointer tp = (TypePointer) to;

				if (next.mod != tp.next.mod && tp.next.mod != MODconst)
					return MATCHnomatch;

				if (tp.next.ty == Tvoid || next.constConv(tp.next, context) != MATCHnomatch) {
					return MATCHconvert;
				}
				return MATCHnomatch;
			}
			if (to.ty == Tarray) {
				int[] offset = { 0 };
				TypeDArray ta = (TypeDArray) to;

				if (next.mod != ta.next.mod && ta.next.mod != MODconst)
					return MATCHnomatch;

				if (next.equals(ta.next) || next.implicitConvTo(ta.next, context).ordinal() >= MATCHconst.ordinal()
						|| (ta.next.isBaseOf(next, offset, context) && offset[0] == 0) || ta.next.ty == Tvoid)
					return MATCHconvert;
				return MATCHnomatch;
			}
			if (to.ty == Tsarray) {
				if (this == to)
					return MATCHexact;

				TypeSArray tsa = (TypeSArray) to;

				if (dim.equals(tsa.dim)) {
					/*
					 * Since static arrays are value types, allow conversions
					 * from const elements to non-const ones, just like we allow
					 * conversion from const int to int.
					 */
					MATCH m = next.implicitConvTo(tsa.next, context);
					if (m.ordinal() >= MATCHconst.ordinal()) {
						if (mod != to.mod)
							m = MATCHconst;
						return m;
					}
				}
			}
			return MATCHnomatch;
		}
	}

	@Override
	public boolean isString(SemanticContext context) {
		TY nty = next.toBasetype(context).ty;
		return nty == Tchar || nty == Twchar || nty == Tdchar;
	}

	@Override
	public boolean isZeroInit(char[] filename, int lineNumber, SemanticContext context) {
		return next.isZeroInit(filename, lineNumber, context);
	}

	@Override
	public int memalign(int salign, SemanticContext context) {
		return next.memalign(salign, context);
	}

	@Override
	public void resolve(char[] filename, int lineNumber, Scope sc, Expression[] pe, Type[] pt,
			Dsymbol[] ps, SemanticContext context) {
		next.resolve(filename, lineNumber, sc, pe, pt, ps, context);
		if (null != pe[0]) { // It's really an index expression
			Expression e;
			e = new IndexExp(filename, lineNumber, pe[0], dim);
			pe[0] = e;
		} else if (null != ps[0]) {
			Dsymbol s = ps[0];
			TupleDeclaration td = s.isTupleDeclaration();
			if (null != td) {
				ScopeDsymbol sym = new ArrayScopeSymbol(sc, td);
				sym.parent = sc.scopesym;
				sc = sc.push(sym);

				dim = dim.semantic(sc, context);
				dim = dim.optimize(WANTvalue | WANTinterpret, context);
				int d = dim.toUInteger(context).intValue();

				sc = sc.pop();

				if (d >= td.objects.size()) {
					if (context.acceptsErrors()) {
						context.acceptProblem(Problem.newSemanticTypeError(
								IProblem.TupleIndexExceedsBounds, this, new String[] { String.valueOf(d), String.valueOf(td.objects.size()) }));
					}
					super.resolve(filename, lineNumber, sc, pe, pt, ps, context); // goto
					// Ldefault;
				}
				ASTDmdNode o = (ASTDmdNode) td.objects.get(d);
				if (o.dyncast() == DYNCAST_DSYMBOL) {
					ps[0] = (Dsymbol) o;
					return;
				}
				if (o.dyncast() == DYNCAST_EXPRESSION) {
					ps[0] = null;
					pe[0] = (Expression) o;
					return;
				}

				/*
				 * Create a new TupleDeclaration which is a slice [d..d+1] out
				 * of the old one. Do it this way because
				 * TemplateInstance.semanticTiargs() can handle unresolved
				 * Objects this way.
				 */
				Objects objects = new Objects(1);
				objects.add(o);

				TupleDeclaration tds = new TupleDeclaration(filename, lineNumber, td.ident,
						objects);
				ps[0] = tds;
			} else
				super.resolve(filename, lineNumber, sc, pe, pt, ps, context); // goto
			// Ldefault;
		} else {
			// Ldefault:
			super.resolve(filename, lineNumber, sc, pe, pt, ps, context);
		}
	}

	@Override
	public Type semantic(char[] filename, int lineNumber, Scope sc, SemanticContext context) {
		Type t = null;
		Expression e = null;
		Dsymbol s = null;
		next.resolve(filename, lineNumber, sc, new Expression[] { e }, new Type[] { t },
				new Dsymbol[] { s }, context);
		if (null != dim && null != s && null != s.isTupleDeclaration()) {
			TupleDeclaration sd = s.isTupleDeclaration();

			dim = semanticLength(sc, sd, dim, context);
			dim = dim.optimize(WANTvalue | WANTinterpret, context);
			int d = dim.toUInteger(context).intValue();

			if (d >= sd.objects.size()) {
				if (context.acceptsErrors()) {
					context.acceptProblem(Problem.newSemanticTypeError(
							IProblem.TupleIndexExceedsBounds, this, new String[] { String.valueOf(d), String.valueOf(sd.objects.size()) }));
				}
				return Type.terror;
			}
			ASTDmdNode o = (ASTDmdNode) sd.objects.get(d);
			if (o.dyncast() != DYNCAST_TYPE) {
				if (context.acceptsErrors()) {
					context.acceptProblem(Problem.newSemanticTypeError(IProblem.SymbolNotAType, this, new String[] { toChars(context) }));
				}
				return Type.terror;
			}
			t = (Type) o;
			return t;
		}

		next = next.semantic(filename, lineNumber, sc, context);
		
		if (context.isD2()) {
			transitive(context);
		}
		
		Type tbn = next.toBasetype(context);

		if (null != dim) {
			long n, n2;

			dim = semanticLength(sc, tbn, dim, context);

			dim = dim.optimize(WANTvalue | WANTinterpret, context);
			if (sc != null && sc.parameterSpecialization != 0 && dim.op == TOKvar &&
				    (((VarExp) dim).var.storage_class & STCtemplateparameter) != 0)
			{
			    /* It could be a template parameter N which has no value yet:
			     *   template Foo(T : T[N], size_t N);
			     */
			    return this;
			}
			
			int d1 = dim.toInteger(context).intValue();
			dim = dim.castTo(sc, tsize_t, context);
			dim = dim.optimize(WANTvalue, context);
			int d2 = dim.toInteger(context).intValue();

			if (d1 != d2) {
				// goto Loverflow;
				if (context.acceptsErrors()) {
					context.acceptProblem(Problem.newSemanticTypeError(IProblem.IndexOverflowForStaticArray, sourceDim, new String[] { String.valueOf(d1) }));
				}
				dim = new IntegerExp(null, 0, 1, tsize_t);
			}

			if (tbn.isintegral() || tbn.isfloating() || tbn.ty == Tpointer
					|| tbn.ty == Tarray || tbn.ty == Tsarray
					|| tbn.ty == Taarray || tbn.ty == Tclass) {
				/* Only do this for types that don't need to have semantic()
				 * run on them for the size, since they may be forward referenced.
				 */
				n = tbn.size(filename, lineNumber, context);
				n2 = n * d2;
				if ((int) n2 < 0) {
					//goto Loverflow;
					if (context.acceptsErrors()) {
						context.acceptProblem(Problem.newSemanticTypeError(IProblem.IndexOverflowForStaticArray, sourceDim, new String[] { String.valueOf(d1) }));
					}
					dim = new IntegerExp(null, 0, 1, tsize_t);
				}
				if (n2 >= 0x1000000) // put a 'reasonable' limit on it
				{
					//goto Loverflow;
					if (context.acceptsErrors()) {
						context.acceptProblem(Problem.newSemanticTypeError(IProblem.IndexOverflowForStaticArray, sourceDim, new String[] { String.valueOf(d1) }));
					}
					dim = new IntegerExp(null, 0, 1, tsize_t);
				}
				if (n != 0 && ((n2 / n) != d2)) {
					//Loverflow:
					if (context.acceptsErrors()) {
						context.acceptProblem(Problem.newSemanticTypeError(IProblem.IndexOverflowForStaticArray, sourceDim, new String[] { String.valueOf(d1) }));
					}
					dim = new IntegerExp(null, 0, 1, tsize_t);
				}
			}
		}

		switch (tbn.ty) {
		case Ttuple: { // Index the tuple to get the type
			assert (null != dim);
			TypeTuple tt = (TypeTuple) tbn;
			int d = dim.toUInteger(context).intValue();

			if (d >= tt.arguments.size()) {
				if (context.acceptsErrors()) {
					context.acceptProblem(Problem.newSemanticTypeError(
							IProblem.TupleIndexExceedsBounds, this, new String[] { String.valueOf(d), String.valueOf(tt.arguments.size()) }));
				}
				return Type.terror;
			}
			Argument arg = (Argument) tt.arguments.get(d);
			return arg.type;
		}
		case Tstruct:
			if (context.isD2()) {
				TypeStruct ts = (TypeStruct)tbn;
			    if (ts.sym.isnested) {
			    	if (context.acceptsErrors()) {
			    		context.acceptProblem(Problem.newSemanticTypeError(IProblem.CannotHaveArrayOfType, this, "inner structs " + ts.toChars(context)));
			    	}
			    }
			    break;
			}
			break;
		case Tfunction:
		case Tnone:
			if (context.acceptsErrors()) {
				context.acceptProblem(Problem.newSemanticTypeError(IProblem.CannotHaveArrayOfType, this, new String[] { tbn.toChars(context) }));
			}
			tbn = next = tint32;
			break;
		}
		if (tbn.isauto()) {
			if (context.acceptsErrors()) {
				context.acceptProblem(Problem.newSemanticTypeError(
						IProblem.CannotHaveArrayOfAuto, this, new String[] { tbn.toChars(context) }));
			}
		}

		return merge(context);
	}

	@Override
	public int size(char[] filename, int lineNumber, SemanticContext context) {
		int sz;

		if (null == dim)
			return super.size(filename, lineNumber, context);
		sz = dim.toInteger(context).intValue();
		if (next.toBasetype(context).ty == Tbit) // if array of bits
		{
			if (sz + 31 < sz) {
				// goto Loverflow;
				if (context.acceptsErrors()) {
					context.acceptProblem(Problem.newSemanticTypeError(IProblem.IndexOverflowForStaticArray, sourceDim, new String[] { String.valueOf(sz) }));
				}
				return 1;
			}
			sz = ((sz + 31) & ~31) / 8; // size in bytes, rounded up to 32 bit
			// dwords
		} else {
			int n, n2;

			n = next.size(context);
			n2 = n * sz;
			if ((n != 0) && (n2 / n) != sz) {
				// goto Loverflow;
				if (context.acceptsErrors()) {
					context.acceptProblem(Problem.newSemanticTypeError(IProblem.IndexOverflowForStaticArray, sourceDim, new String[] { String.valueOf(sz) }));
				}
				return 1;
			}
			sz = n2;
		}
		return sz;
	}

	@Override
	public Type syntaxCopy(SemanticContext context) {
		Type t = next.syntaxCopy(context);
		Expression e = dim.syntaxCopy(context);
		t = new TypeSArray(t, e, context.encoder);
		t.mod = mod;
		t.copySourceRange(this);
		return t;
	}

	@Override
	public void toDecoBuffer(OutBuffer buf, int flag, SemanticContext context) {
		Type_toDecoBuffer(buf, flag, context);
		buf.writeByte(ty.mangleChar);
		if (null != dim)
			buf.data.append(dim.toInteger(context));
		if (null != next) {
			/* Note that static arrays are value types, so
			 * for a parameter, propagate the 0x100 to the next
			 * level, since for T[4][3], any const should apply to the T,
			 * not the [4].
			 */
			next.toDecoBuffer(buf, (flag & 0x100) != 0 ? flag : mod, context);
		}
	}

	@Override
	public Expression toExpression() {
		Expression e = next.toExpression();
		if (e != null) {
			Expressions arguments = new Expressions(1);
			arguments.add(dim);
			e = new ArrayExp(dim.filename, dim.lineNumber, e, arguments);
			e.setSourceRange(start, length);
		}
		return e;
	}
	
	@Override
	public void toCBuffer2(OutBuffer buf, HdrGenState hgs, int mod, SemanticContext context) {
	    if (mod != this.mod) {
			toCBuffer3(buf, hgs, mod, context);
			return;
		}
		next.toCBuffer2(buf, hgs, this.mod, context);
		buf.data.append('[');
		buf.data.append(dim);
		buf.data.append(']');
	}
	
	@Override
	protected void appendSignature0(StringBuilder sb, int options) {
		sb.append(Signature.C_STATIC_ARRAY);
		next.appendSignature(sb, options);
		sb.append(Signature.C_STATIC_ARRAY2);
		
		char[] expc = encoder.encodeExpression(dim);
		sb.append(expc.length);
		sb.append(Signature.C_STATIC_ARRAY);
		sb.append(expc);
	}

	// PERHAPS type *toCtype();
	// PERHAPS type *toCParamtype();
	// PERHAPS dt_t **toDt(dt_t **pdt);
}
