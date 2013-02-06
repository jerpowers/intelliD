package descent.internal.compiler.parser;

import static descent.internal.compiler.parser.Constfold.Equal;
import static descent.internal.compiler.parser.Constfold.Index;
import static descent.internal.compiler.parser.LINK.LINKc;
import static descent.internal.compiler.parser.MATCH.MATCHnomatch;
import static descent.internal.compiler.parser.PROT.PROTpublic;
import static descent.internal.compiler.parser.STC.STCundefined;
import static descent.internal.compiler.parser.TOK.TOKadd;
import static descent.internal.compiler.parser.TOK.TOKaddass;
import static descent.internal.compiler.parser.TOK.TOKandass;
import static descent.internal.compiler.parser.TOK.TOKarrayliteral;
import static descent.internal.compiler.parser.TOK.TOKassign;
import static descent.internal.compiler.parser.TOK.TOKassocarrayliteral;
import static descent.internal.compiler.parser.TOK.TOKblit;
import static descent.internal.compiler.parser.TOK.TOKcast;
import static descent.internal.compiler.parser.TOK.TOKcatass;
import static descent.internal.compiler.parser.TOK.TOKconstruct;
import static descent.internal.compiler.parser.TOK.TOKdivass;
import static descent.internal.compiler.parser.TOK.TOKdottd;
import static descent.internal.compiler.parser.TOK.TOKdotvar;
import static descent.internal.compiler.parser.TOK.TOKequal;
import static descent.internal.compiler.parser.TOK.TOKforeach;
import static descent.internal.compiler.parser.TOK.TOKge;
import static descent.internal.compiler.parser.TOK.TOKgt;
import static descent.internal.compiler.parser.TOK.TOKin;
import static descent.internal.compiler.parser.TOK.TOKindex;
import static descent.internal.compiler.parser.TOK.TOKint64;
import static descent.internal.compiler.parser.TOK.TOKle;
import static descent.internal.compiler.parser.TOK.TOKlt;
import static descent.internal.compiler.parser.TOK.TOKmin;
import static descent.internal.compiler.parser.TOK.TOKminass;
import static descent.internal.compiler.parser.TOK.TOKminusminus;
import static descent.internal.compiler.parser.TOK.TOKmodass;
import static descent.internal.compiler.parser.TOK.TOKmulass;
import static descent.internal.compiler.parser.TOK.TOKnull;
import static descent.internal.compiler.parser.TOK.TOKorass;
import static descent.internal.compiler.parser.TOK.TOKplusplus;
import static descent.internal.compiler.parser.TOK.TOKremove;
import static descent.internal.compiler.parser.TOK.TOKshlass;
import static descent.internal.compiler.parser.TOK.TOKshrass;
import static descent.internal.compiler.parser.TOK.TOKslice;
import static descent.internal.compiler.parser.TOK.TOKstar;
import static descent.internal.compiler.parser.TOK.TOKstring;
import static descent.internal.compiler.parser.TOK.TOKstructliteral;
import static descent.internal.compiler.parser.TOK.TOKsymoff;
import static descent.internal.compiler.parser.TOK.TOKug;
import static descent.internal.compiler.parser.TOK.TOKuge;
import static descent.internal.compiler.parser.TOK.TOKul;
import static descent.internal.compiler.parser.TOK.TOKule;
import static descent.internal.compiler.parser.TOK.TOKushrass;
import static descent.internal.compiler.parser.TOK.TOKvar;
import static descent.internal.compiler.parser.TOK.TOKxorass;
import static descent.internal.compiler.parser.TY.Tarray;
import static descent.internal.compiler.parser.TY.Tbool;
import static descent.internal.compiler.parser.TY.Tclass;
import static descent.internal.compiler.parser.TY.Terror;
import static descent.internal.compiler.parser.TY.Tpointer;
import static descent.internal.compiler.parser.TY.Tsarray;
import static descent.internal.compiler.parser.TY.Tstruct;
import static descent.internal.compiler.parser.TY.Tvoid;

import java.math.BigInteger;
import java.util.Arrays;

import melnorme.utilbox.core.Assert;

import descent.core.compiler.IProblem;
import descent.internal.compiler.parser.Constfold.BinExp_fp;
import descent.internal.compiler.parser.Constfold.BinExp_fp2;


public abstract class BinExp extends Expression {

	public Expression e1, sourceE1;
	public Expression e2, sourceE2;

	public BinExp(char[] filename, int lineNumber, TOK op, Expression e1, Expression e2) {
		super(filename, lineNumber, op);
		this.e1 = e1;
		this.sourceE1 = e1;
		this.e2 = e2;
		this.sourceE2 = e2;
		if (e1 != null && e2 != null) {
			this.start = e1.start;
			this.length = e2.start + e2.length - e1.start;
		}
	}

	public Expression arrayOp(Scope sc, SemanticContext context) {
		if (type.toBasetype(context).nextOf().toBasetype(context).ty == Tvoid) {
			if (context.acceptsErrors()) {
				context.acceptProblem(Problem.newSemanticTypeError(IProblem.CannotPerformArrayOperationsOnVoidArrays, this));
			}
			return new ErrorExp();
		}

		Expressions arguments = new Expressions(3);

		/*
		 * The expression to generate an array operation for is mangled into a
		 * name to use as the array operation function name. Mangle in the
		 * operands and operators in RPN order, and type.
		 */
		OutBuffer buf = new OutBuffer();
		buf.writestring("_array");
		buildArrayIdent(buf, arguments);
		buf.writeByte('_');

		/*
		 * Append deco of array element type
		 */
		if (context.isD2()) {
			buf.writestring(type.toBasetype(context).nextOf().toBasetype(
					context).mutableOf(context).deco);
		} else {
			buf.writestring(type.toBasetype(context).nextOf().toBasetype(
					context).deco);
		}

		int namelen = buf.offset();
		String name = buf.extractData();

		/*
		 * Look up name in hash table
		 */
		StringValue sv = context.ArrayOp_arrayfuncs.update(name.toCharArray(),
				0, namelen);
		FuncDeclaration fd = (FuncDeclaration) sv.ptrvalue;
		if (null == fd) {
			int i = Arrays.binarySearch(libArrayopFuncs, name);
			if (i == -1) {
				/*
				 * Not in library, so generate it. Construct the function body:
				 * foreach (i; 0 .. p.length) for (size_t i = 0; i < p.length;
				 * i++) loopbody; return p;
				 */

				Arguments fparams = new Arguments(1);
				Expression loopbody = buildArrayLoop(fparams, context);
				Argument p = (Argument) fparams.get(0 /* fparams.dim - 1 */);

				Statement s1;
				if (!context.isD2()) {
					// for (size_t i = 0; i < p.length; i++)
					Initializer init = new ExpInitializer(null, 0,
							new IntegerExp(null, 0, 0, Type.tsize_t));
					Dsymbol d = new VarDeclaration(null, 0, Type.tsize_t,
							Id.p, init);
					s1 = new ForStatement(null, 0, new DeclarationStatement(
							null, 0, d), new CmpExp(null, 0, TOKlt,
							new IdentifierExp(null, 0, Id.p),
							new ArrayLengthExp(null, 0, new IdentifierExp(
									null, 0, p.ident))), new PostExp(null, 0,
							TOKplusplus, new IdentifierExp(null, 0, Id.p)),
							new ExpStatement(null, 0, loopbody));
				} else {
					// foreach (i; 0 .. p.length)
					s1 = new ForeachRangeStatement(
							null, 0,
							TOKforeach,
							new Argument(0, null, new IdentifierExp(Id.p), null),
							new IntegerExp(null, 0, 0, Type.tint32),
							new ArrayLengthExp(null, 0, new IdentifierExp(
									null, 0, p.ident)), new ExpStatement(
									null, 0, loopbody));
				}
				Statement s2 = new ReturnStatement(null, 0, new IdentifierExp(
						null, 0, p.ident));
				Statement fbody = new CompoundStatement(null, 0, s1, s2);

				/*
				 * Construct the function
				 */
				TypeFunction ftype = new TypeFunction(fparams, type, 0, LINKc);
				fd = new FuncDeclaration(null, 0, new IdentifierExp(name
						.toCharArray()), STCundefined, ftype);
				fd.fbody = fbody;
				fd.protection = PROTpublic;
				fd.linkage = LINKc;

				sc.module.importedFrom.members.add(fd);

				sc = sc.push();
				sc.parent = sc.module.importedFrom;
				sc.stc = 0;
				sc.linkage = LINKc;
				fd.semantic(sc, context);
				fd.semantic2(sc, context);
				fd.semantic3(sc, context);
				sc.pop();
			} else { /*
						 * In library, refer to it.
						 */
				fd = context.genCfunc(type, name.toCharArray());
			}
			sv.ptrvalue = fd; // cache symbol in hash table
		}

		/*
		 * Call the function fd(arguments)
		 */
		Expression ec = new VarExp(null, 0, fd);
		Expression e = new CallExp(filename, lineNumber, ec, arguments);
		e.copySourceRange(this);
		e.type = type;
		return e;
	}

	@Override
	public boolean canThrow(SemanticContext context) {
		return e1.canThrow(context) || e2.canThrow(context);
	}

	public Expression BinExp_semantic(Scope sc, SemanticContext context) {
		e1 = e1.semantic(sc, context);

		// Descent: for binding resolution
		sourceE1.setResolvedExpression(e1, context);

		if (e1.type == null &&
				!(op == TOKassign && e1.op == TOKdottd)) {	// a.template = e2
			if (context.acceptsErrors()) {
				context.acceptProblem(Problem.newSemanticTypeError(IProblem.SymbolHasNoValue, e1, e1.toChars(context)));
			}
			e1.type = Type.terror;
		}
		e2 = e2.semantic(sc, context);

		// Descent: for binding resolution
		sourceE2.setResolvedExpression(e2, context);

		if (e2.type == null) {
			if (context.acceptsErrors()) {
				context.acceptProblem(Problem.newSemanticTypeError(IProblem.SymbolHasNoValue, e2, e2.toChars(context)));
			}
			e2.type = Type.terror;
		}
		return this;
	}

	@Override
	public int checkSideEffect(int flag, SemanticContext context) {
		if (op == TOKplusplus || op == TOKminusminus || op == TOKassign
				|| op == TOKconstruct || op == TOKblit
				|| op == TOKaddass || op == TOKminass || op == TOKcatass
				|| op == TOKmulass || op == TOKdivass || op == TOKmodass
				|| op == TOKshlass || op == TOKshrass || op == TOKushrass
				|| op == TOKandass || op == TOKorass || op == TOKxorass
				|| op == TOKin || op == TOKremove) {
			return 1;
		}
		return super.checkSideEffect(flag, context);
	}

	public Expression commonSemanticAssignIntegral(Scope sc,
			SemanticContext context) {
		Expression e;

		if (type == null) {
			BinExp_semantic(sc, context);
			e2 = resolveProperties(sc, e2, context);

			e = op_overload(sc, context);
			if (e != null) {
				return e;
			}

			if (e1.op == TOKslice)
			{   // T[] op= ...
			    typeCombine(sc, context);
			    type = e1.type;
			    return arrayOp(sc, context);
			}

			e1 = e1.modifiableLvalue(sc, e1, context);
			e1.checkScalar(context);
			type = e1.type;
			if (type.toBasetype(context).ty == Tbool) {
				e2 = e2.implicitCastTo(sc, type, context);
			}

			typeCombine(sc, context);
			e1.checkIntegral(context);
			e2.checkIntegral(context);
		}

		return this;
	}

	public Expression commonSemmanticAssign(Scope sc, SemanticContext context) {
		Expression e;

		if (type == null) {
			BinExp_semantic(sc, context);
			e2 = resolveProperties(sc, e2, context);

			e = op_overload(sc, context);
			if (e != null) {
				return e;
			}

			if (e1.op == TOKslice)
			{   // T[] op= ...
			    typeCombine(sc, context);
			    type = e1.type;
			    return arrayOp(sc, context);
			}

			e1 = e1.modifiableLvalue(sc, e1, context);
			e1.checkScalar(context);
			type = e1.type;
			if (type.toBasetype(context).ty == Tbool) {
				if (context.acceptsErrors()) {
					context.acceptProblem(Problem.newSemanticTypeError(
							IProblem.OperatorNotAllowedOnBoolExpression, this, toChars(context)));
				}
			}
			typeCombine(sc, context);
			e1.checkArithmetic(context);
			e2.checkArithmetic(context);

			if (op == TOKmodass && e2.type.iscomplex()) {
				if (context.acceptsErrors()) {
					context.acceptProblem(Problem.newSemanticTypeError(
							IProblem.CannotPerformModuloComplexArithmetic, this));
				}
				if (context.isD1()) {
					return new IntegerExp(filename, lineNumber, 0);
				} else {
					return new ErrorExp();
				}
			}
		}

		return this;
	}

	public void incompatibleTypes(SemanticContext context) {
		if (context.acceptsErrors()) {
			context.acceptProblem(Problem.newSemanticTypeError(IProblem.IncompatibleTypesForOperator, e1, e2, e1.type.toChars(context), e2.type.toChars(context), op.toString()));
		}
	}

	public boolean isunsigned() {
		return e1.type.isunsigned() || e2.type.isunsigned();
	}

	public Expression scaleFactor(Scope sc, SemanticContext context) {
		BigInteger stride;
		Type t1b = e1.type.toBasetype(context);
		Type t2b = e2.type.toBasetype(context);

		if (t1b.ty == Tpointer && t2b.isintegral()) { // Need to adjust operator by the stride
			// Replace (ptr + int) with (ptr + (int * stride))
			Type t = Type.tptrdiff_t;

			stride = new BigInteger(String.valueOf(t1b.nextOf().size(filename, lineNumber, context)));
			if (!t.equals(t2b)) {
				e2 = e2.castTo(sc, t, context);
			}

			if (context.isD1()) {
				if (t1b.nextOf().isbit()) {
					// BUG: should add runtime check for misaligned offsets
					// This perhaps should be done by rewriting as &p[i]
					// and letting back end do it.
					e2 = new UshrExp(filename, lineNumber, e2, new IntegerExp(filename, lineNumber, 3, t));
				} else {
					e2 = new MulExp(filename, lineNumber, e2, new IntegerExp(filename, lineNumber, new integer_t(
							stride), t));
				}
			} else {
				e2 = new MulExp(filename, lineNumber, e2, new IntegerExp(filename, lineNumber, new integer_t(
						stride), t));
			}
			e2.type = t;
			type = e1.type;
		} else if (t2b.ty == Tpointer && t1b.isintegral()) { // Need to adjust operator by the stride
			// Replace (int + ptr) with (ptr + (int * stride))
			Type t = Type.tptrdiff_t;
			Expression e;

			stride = new BigInteger(String.valueOf(t2b.nextOf().size(filename, lineNumber, context)));
			if (!t.equals(t1b)) {
				e = e1.castTo(sc, t, context);
			} else {
				e = e1;
			}

			if (context.isD1()) {
				if (t2b.nextOf().isbit()) {
					// BUG: should add runtime check for misaligned offsets
					e = new UshrExp(filename, lineNumber, e, new IntegerExp(filename, lineNumber, 3, t));
				} else {
					e = new MulExp(filename, lineNumber, e, new IntegerExp(filename, lineNumber,
							new integer_t(stride), t));
				}
			} else {
				e = new MulExp(filename, lineNumber, e, new IntegerExp(filename, lineNumber,
						new integer_t(stride), t));
			}
			e.type = t;
			type = e2.type;
			e1 = e2;
			e2 = e;
		}
		return this;
	}

	@Override
	public Expression semantic(Scope sc, SemanticContext context) {
		return BinExp_semantic(sc, context);
	}

	public Expression semanticp(Scope sc, SemanticContext context) {
		BinExp_semantic(sc, context);
		e1 = resolveProperties(sc, e1, context);
		e2 = resolveProperties(sc, e2, context);
		return this;
	}

	@Override
	public Expression syntaxCopy(SemanticContext context) {
		BinExp e;

		e = (BinExp) copy();
		e.type = null;
		e.e1 = e.e1.syntaxCopy(context);
		e.e2 = e.e2.syntaxCopy(context);
		return e;
	}

	@Override
	public void toCBuffer(OutBuffer buf, HdrGenState hgs,
			SemanticContext context) {
		expToCBuffer(buf, hgs, e1, op.precedence, context);
		buf.writeByte(' ');
		buf.writestring(op.toString());
		buf.writeByte(' ');
		expToCBuffer(buf, hgs, e2, op.precedence.ordinal() + 1,
				context);
	}

	public Expression typeCombine(Scope sc, SemanticContext context) {
		if (context.isD1()) {
			Type t1;
			Type t2;
			Type t;
			TY ty;

			e1 = e1.integralPromotions(sc, context);
			e2 = e2.integralPromotions(sc, context);

			// BUG: do toBasetype()
			t1 = e1.type;
			t2 = e2.type;
			Assert.isNotNull(t1);
			Assert.isNotNull(t2);

			Type t1b = t1.toBasetype(context);
			Type t2b = t2.toBasetype(context);

			ty = Type.impcnvResult[t1b.ty.ordinal()][t2b.ty.ordinal()];
			if (ty != Terror) {
				TY ty1;
				TY ty2;

				ty1 = Type.impcnvType1[t1b.ty.ordinal()][t2b.ty.ordinal()];
				ty2 = Type.impcnvType2[t1b.ty.ordinal()][t2b.ty.ordinal()];

				if (t1b.ty == ty1) // if no promotions
				{
					if (same(t1, t2, context)) {
						if (type == null) {
							type = t1;
						}
						return this;
					}

					if (same(t1b, t2b, context)) {
						if (type == null) {
							type = t1b;
						}
						return this;
					}
				}

				if (type == null) {
					type = Type.basic[ty.ordinal()];
				}

				t1 = Type.basic[ty1.ordinal()];
				t2 = Type.basic[ty2.ordinal()];
				e1 = e1.castTo(sc, t1, context);
				e2 = e2.castTo(sc, t2, context);
				return this;
			}

			t = t1;
			if (same(t1, t2, context)) {
				if ((t1.ty == Tstruct || t1.ty == Tclass)
						&& (op == TOKmin || op == TOKadd)) {
					return typeCombine_Lincompatible_End(t, context);
				}
			} else if (t1.isintegral() && t2.isintegral()) {
				int sz1 = t1.size(filename, lineNumber, context);
				int sz2 = t2.size(filename, lineNumber, context);
				boolean sign1 = t1.isunsigned();
				boolean sign2 = t2.isunsigned();

				if (sign1 == sign2) {
					if (sz1 < sz2) {
						// goto Lt2;
						e1 = e1.castTo(sc, t2, context);
						t = t2;
						if (type == null) {
							type = t;
						}
						return this;
					} else {
						// goto Lt1;
						e2 = e2.castTo(sc, t1, context);
						t = t1;
						if (type == null) {
							type = t;
						}
						return this;
					}
				}
				if (!sign1) {
					if (sz1 >= sz2) {
						// goto Lt1;
						e2 = e2.castTo(sc, t1, context);
						t = t1;
						if (type == null) {
							type = t;
						}
						return this;
					} else {
						// goto Lt2
						e1 = e1.castTo(sc, t2, context);
						t = t2;
						if (type == null) {
							type = t;
						}
						return this;
					}
				} else {
					if (sz2 >= sz1) {
						// goto Lt2
						e1 = e1.castTo(sc, t2, context);
						t = t2;
						if (type == null) {
							type = t;
						}
						return this;
					} else {
						// goto Lt1;
						e2 = e2.castTo(sc, t1, context);
						t = t1;
						if (type == null) {
							type = t;
						}
						return this;
					}
				}
			} else if (t1.ty == Tpointer && t2.ty == Tpointer) {
				// Bring pointers to compatible type
				Type t1n = t1.nextOf();
				Type t2n = t2.nextOf();

				assert (!same(t1n, t2n, context));
				if (t1n.ty == Tvoid) {
					t = t2;
				} else if (t2n.ty == Tvoid) {
					;
				} else if (t1n.ty == Tclass && t2n.ty == Tclass) {
					ClassDeclaration cd1 = t1n.isClassHandle();
					ClassDeclaration cd2 = t2n.isClassHandle();
					int offset[] = { 0 };

					if (cd1.isBaseOf(cd2, offset, context)) {
						if (offset[0] != 0) {
							e2 = e2.castTo(sc, t, context);
						}
					} else if (cd2.isBaseOf(cd1, offset, context)) {
						t = t2;
						if (offset[0] != 0) {
							e1 = e1.castTo(sc, t, context);
						}
					} else {
						return typeCombine_Lincompatible_End(t, context);
					}
				} else {
					return typeCombine_Lincompatible_End(t, context);
				}
			} else if ((t1.ty == Tsarray || t1.ty == Tarray) && e2.op == TOKnull
					&& t2.ty == Tpointer && t2.nextOf().ty == Tvoid) {
				// goto Lx1;
				t = t1.nextOf().arrayOf(context);
				e1 = e1.castTo(sc, t, context);
				e2 = e2.castTo(sc, t, context);
				if (type == null) {
					type = t;
				}
				return this;
			} else if ((t2.ty == Tsarray || t2.ty == Tarray) && e1.op == TOKnull
					&& t1.ty == Tpointer && t1.nextOf().ty == Tvoid) {
				// goto Lx2;
				t = t2.nextOf().arrayOf(context);
				e1 = e1.castTo(sc, t, context);
				e2 = e2.castTo(sc, t, context);
				if (type == null) {
					type = t;
				}
				return this;
			} else if ((t1.ty == Tsarray || t1.ty == Tarray)
					&& t1.implicitConvTo(t2, context) != MATCHnomatch) {
				// goto Lt2;
				e1 = e1.castTo(sc, t2, context);
				t = t2;
				if (type == null) {
					type = t;
				}
				return this;
			} else if ((t2.ty == Tsarray || t2.ty == Tarray)
					&& t2.implicitConvTo(t1, context) != MATCHnomatch) {
				// goto Lt1;
				e2 = e2.castTo(sc, t1, context);
				t = t1;
				if (type == null) {
					type = t;
				}
				return this;
			} else if (t1.ty == Tclass || t2.ty == Tclass) {

				while(true) {
					MATCH i1 = e2.implicitConvTo(t1, context);
					MATCH i2 = e1.implicitConvTo(t2, context);

					if (i1 != MATCHnomatch && i2 != MATCHnomatch) {
						// We have the case of class vs. void*, so pick class
						if (t1.ty == Tpointer) {
							i1 = MATCHnomatch;
						} else if (t2.ty == Tpointer) {
							i2 = MATCHnomatch;
						}
					}

					if (i2 != MATCHnomatch) {
						// goto Lt2;
						e1 = e1.castTo(sc, t2, context);
						t = t2;
						if (type == null) {
							type = t;
						}
						return this;
					} else if (i1 != MATCHnomatch) {
						// goto Lt1;
						e2 = e2.castTo(sc, t1, context);
						t = t1;
						if (type == null) {
							type = t;
						}
						return this;
					} else if (t1.ty == Tclass && t2.ty == Tclass) {
					    TypeClass tc1 = (TypeClass) t1;
						TypeClass tc2 = (TypeClass) t2;

						/* Pick 'tightest' type
						 */
						ClassDeclaration cd1 = tc1.sym.baseClass;
						ClassDeclaration cd2 = tc2.sym.baseClass;

						if (cd1 != null && cd2 != null) {
							t1 = cd1.type;
							t2 = cd2.type;
						} else if (cd1 != null)
							t1 = cd1.type;
						else if (cd2 != null)
							t2 = cd2.type;
						else {
							// goto Lincompatible;
							return typeCombine_Lincompatible_End(t, context);
						}
					} else {
						return typeCombine_Lincompatible_End(t, context);
					}
				}
			} else if ((e1.op == TOKstring || e1.op == TOKnull)
					&& e1.implicitConvTo(t2, context) != MATCHnomatch) {
				// goto Lt2;
				e1 = e1.castTo(sc, t2, context);
				t = t2;
				if (type == null) {
					type = t;
				}
				return this;
			} else if ((e2.op == TOKstring || e2.op == TOKnull)
					&& e2.implicitConvTo(t1, context) != MATCHnomatch) {
				// goto Lt1;
				e2 = e2.castTo(sc, t1, context);
				t = t1;
				if (type == null) {
					type = t;
				}
				return this;
			} else if (t1.ty == Tsarray
					&& t2.ty == Tsarray
					&& e2.implicitConvTo(t1.nextOf().arrayOf(context), context) != MATCHnomatch) {
				t = t1.nextOf().arrayOf(context);
				e1 = e1.castTo(sc, t, context);
				e2 = e2.castTo(sc, t, context);
			} else if (t1.ty == Tsarray
					&& t2.ty == Tsarray
					&& e1.implicitConvTo(t2.nextOf().arrayOf(context), context) != MATCHnomatch) {
				t = t2.nextOf().arrayOf(context);
				e1 = e1.castTo(sc, t, context);
				e2 = e2.castTo(sc, t, context);
			} else if (t1.isintegral() && t2.isintegral()) {
		    	throw new IllegalStateException("assert(0);");
			} else if (e1.op == TOKslice && t1.ty == Tarray &&
				     e2.implicitConvTo(t1.nextOf(), context) != MATCHnomatch)
		    {	// T[] op T
			e2 = e2.castTo(sc, t1.nextOf(), context);
			t = t1.nextOf().arrayOf(context);
		    } else if (e2.op == TOKslice && t2.ty == Tarray
					&& e1.implicitConvTo(t2.nextOf(), context) != MATCHnomatch) { // T op T[]
				e1 = e1.castTo(sc, t2.nextOf(), context);
				t = t2.nextOf().arrayOf(context);

				e1 = e1.optimize(WANTvalue, context);
				if (isCommutative() && e1.isConst()) {
					/*
					 * Swap operands to minimize number of functions generated
					 */
					Expression tmp = e1;
					e1 = e2;
					e2 = tmp;
				}
			} else {
				incompatibleTypes(context);
				type = Type.terror;
				e1 = new ErrorExp();
				e2 = new ErrorExp();
			}
			if (type == null) {
				type = t;
			}
			return this;
		} else {
		    Type t1 = e1.type.toBasetype(context);
			Type t2 = e2.type.toBasetype(context);

			if (op == TOKmin || op == TOKadd) {
				if (t1 == t2 && (t1.ty == Tstruct || t1.ty == Tclass)) {
					// goto Lerror;
					return typeCombine_Lerror(context);
				}
			}

			Type[] pt = { type };
			Expression[] pe1 = { e1 };
			Expression[] pe2 = { e2 };

			boolean res = typeMerge(sc, this, pt, pe1, pe2, context);
			type = pt[0];
			e1 = pe1[0];
			e2 = pe2[0];
			if (!res) {
				// goto Lerror;
				return typeCombine_Lerror(context);
			}
			return this;
		}
	}

	private Expression typeCombine_Lerror(SemanticContext context) {
		incompatibleTypes(context);
		type = Type.terror;
		e1 = new ErrorExp();
		e2 = new ErrorExp();
		return this;
	}

	private Expression typeCombine_Lincompatible_End(Type t,
			SemanticContext context) {
		incompatibleTypes(context);
		if (type == null) {
			type = t;
		}
		return this;
	}

	public final Expression interpretCommon(InterState istate, BinExp_fp fp,
			SemanticContext context) {
		Expression e;
		Expression e1;
		Expression e2;

		e1 = this.e1.interpret(istate, context);
		if (e1 == EXP_CANT_INTERPRET)
			return EXP_CANT_INTERPRET; //goto Lcant;
		if (!e1.isConst())
			return EXP_CANT_INTERPRET; //goto Lcant;

		e2 = this.e2.interpret(istate, context);
		if (e2 == EXP_CANT_INTERPRET)
			return EXP_CANT_INTERPRET; //goto Lcant;
		if (!e2.isConst())
			return EXP_CANT_INTERPRET; //goto Lcant;

		e = fp.call(type, e1, e2, context);
		return e;
	}

	public final Expression interpretCommon2(InterState istate, BinExp_fp2 fp,
			SemanticContext context) {
		Expression e;
		Expression e1;
		Expression e2;

		e1 = this.e1.interpret(istate, context);
		if (e1 == EXP_CANT_INTERPRET) {
			// goto Lcant;
			return EXP_CANT_INTERPRET;
		}
		if (!e1.isConst() &&
				e1.op != TOKnull &&
				e1.op != TOKstring && e1.op != TOKarrayliteral
				&& e1.op != TOKstructliteral) {
			// goto Lcant;
			return EXP_CANT_INTERPRET;
		}

		e2 = this.e2.interpret(istate, context);
		if (e2 == EXP_CANT_INTERPRET) {
			// goto Lcant;
			return EXP_CANT_INTERPRET;
		}
		if (!e2.isConst() &&
				e1.op != TOKnull &&
				e2.op != TOKstring && e2.op != TOKarrayliteral
				&& e2.op != TOKstructliteral) {
			// goto Lcant;
			return EXP_CANT_INTERPRET;
		}

		e = fp.call(op, type, e1, e2, context);
		return e;

		//	Lcant:
		//	    return EXP_CANT_INTERPRET;
	}

	public final Expression interpretAssignCommon(InterState istate,
			BinExp_fp fp, SemanticContext context) {
		return interpretAssignCommon(istate, fp, 0, context);
	}

	public final Expression interpretAssignCommon(InterState istate,
			BinExp_fp fp, int post, SemanticContext context) {
		Expression e = EXP_CANT_INTERPRET;
		Expression e1 = this.e1;

		if (null != fp) {
			if (e1.op == TOKcast) {
				CastExp ce = (CastExp) e1;
				e1 = ce.e1;
			}
		}
		if (e1 == EXP_CANT_INTERPRET)
			return e1;
		Expression e2 = this.e2.interpret(istate, context);
		if (e2 == EXP_CANT_INTERPRET)
			return e2;

		/* Assignment to variable of the form:
		 *        v = e2
		 */
		if (e1.op == TOKvar) {
			VarExp ve = (VarExp) e1;
			VarDeclaration v = ve.var.isVarDeclaration();
			if (null != v && !v.isDataseg(context)) {
				/* Chase down rebinding of out and ref
				 */
				if (null != v.value() && v.value().op == TOKvar) {
					VarExp ve2 = (VarExp) v.value;
					if (ve2.var.isSymbolDeclaration() != null) {
					    /* This can happen if v is a struct initialized to
					     * 0 using an __initZ SymbolDeclaration from
					     * TypeStruct.defaultInit()
					     */
					} else {
					    v = ve2.var.isVarDeclaration();
					}
				}

				Expression ev = v.value();
				if (null != fp && null == ev) {
					if (context.acceptsErrors()) {
						context.acceptProblem(Problem.newSemanticTypeError(
								IProblem.VariableIsUsedBeforeInitialization, v, v.toChars(context)));
					}
					return e;
				}
				if (null != fp) {
					e2 = fp.call(v.type, ev, e2, context);
				} else {
			    	/* Look for special case of struct being initialized with 0.
					 */
					if (v.type.toBasetype(context).ty == Tstruct
							&& e2.op == TOKint64) {
						e2 = v.type.defaultInit(context);
					}
					e2 = Constfold.Cast(v.type, v.type, e2, context);
				}
				if (e2 != EXP_CANT_INTERPRET) {
					if (!v.isParameter()) {
						for (int i = 0; true; i++) {
							if (i == size(istate.vars)) {
								if (istate.vars == null) {
									istate.vars = new Dsymbols();
								}
								istate.vars.add(v);
								break;
							}
							if (v == (VarDeclaration) istate.vars.get(i))
								break;
						}
					}
					v.value(e2);
					e = Constfold.Cast(type, type, post > 0 ? ev : e2, context);
				}
			}
		}
		/* Assignment to struct member of the form:
		 *   v.var = e2
		 */
		else if (e1.op == TOKdotvar && ((DotVarExp) e1).e1.op == TOKvar) {
			VarExp ve = (VarExp) ((DotVarExp) e1).e1;
			VarDeclaration v = ve.var.isVarDeclaration();

			if (v.isDataseg(context))
				return EXP_CANT_INTERPRET;
			if (null != fp && null == v.value()) {
				if (context.acceptsErrors()) {
					context.acceptProblem(Problem.newSemanticTypeError(
							IProblem.VariableIsUsedBeforeInitialization, v, v.toChars(context)));
				}
				return e;
			}

			if (v.value == null && v.init.isVoidInitializer() != null) {
				/* Since a void initializer initializes to undefined
			     * values, it is valid here to use the default initializer.
			     * No attempt is made to determine if someone actually relies
			     * on the void value - to do that we'd need a VoidExp.
			     * That's probably a good enhancement idea.
			     */
			    v.value = v.type.defaultInit(context);
			}

			Expression vie = v.value;
			if (vie.op == TOKvar) {
				Declaration d = ((VarExp) vie).var;
				vie = getVarExp(e1.filename, e1.lineNumber, istate, d, context);
			}
			if (vie.op != TOKstructliteral) {
				return EXP_CANT_INTERPRET;
			}
			StructLiteralExp se = (StructLiteralExp) vie;
			VarDeclaration vf = ((DotVarExp)e1).var.isVarDeclaration();
			if (null == vf)
			    return EXP_CANT_INTERPRET;

			int fieldi = se.getFieldIndex(type, vf.offset, context);
			if (fieldi == -1)
				return EXP_CANT_INTERPRET;
			Expression ev = se.getField(type, vf.offset, context);
			if (null != fp)
				e2 = fp.call(type, ev, e2, context);
			else
				e2 = Constfold.Cast(type, type, e2, context);
			if (e2 == EXP_CANT_INTERPRET)
				return e2;

			if (!v.isParameter()) {
				for (int i = 0; true; i++) {
					if (i == istate.vars.size()) {
						istate.vars.add(v);
						break;
					}
					if (v == (VarDeclaration) istate.vars.get(i))
						break;
				}
			}

			/* Create new struct literal reflecting updated fieldi
			 */
			Expressions expsx = new Expressions(se.elements.size());
			expsx.setDim(se.elements.size());
			for (int j = 0; j < se.elements.size(); j++) {
				if (j == fieldi) {
					expsx.set(j, e2);
				} else {
					expsx.set(j, se.elements.get(j));
				}
			}
			v.value(new StructLiteralExp(se.filename, se.lineNumber, se.sd, expsx));
			v.value().type = se.type;

			e = Constfold.Cast(type, type, post > 0 ? ev : e2, context);
		}
	    /*
		 * Assignment to struct member of the form: *(symoffexp) = e2
		 */
		else if (e1.op == TOKstar && ((PtrExp) e1).e1.op == TOKsymoff) {
			SymOffExp soe = (SymOffExp) ((PtrExp) e1).e1;
			VarDeclaration v = soe.var.isVarDeclaration();

			if (v.isDataseg(context))
				return EXP_CANT_INTERPRET;
			if (fp != null && null == v.value) {
				if (context.acceptsErrors()) {
					context.acceptProblem(Problem.newSemanticTypeError(
							IProblem.VariableIsUsedBeforeInitialization, v, v
									.toChars(context)));
				}
				return e;
			}
			Expression vie = v.value;
			if (vie.op == TOKvar) {
				Declaration d = ((VarExp) vie).var;
				vie = getVarExp(e1.filename, e1.lineNumber, istate, d, context);
			}
			if (vie.op != TOKstructliteral)
				return EXP_CANT_INTERPRET;
			StructLiteralExp se = (StructLiteralExp) vie;
			int fieldi = se.getFieldIndex(type, soe.offset.intValue(), context);
			if (fieldi == -1)
				return EXP_CANT_INTERPRET;
			Expression ev = se.getField(type, soe.offset.intValue(), context);
			if (fp != null)
				e2 = fp.call(type, ev, e2, context);
			else
				e2 = Constfold.Cast(type, type, e2, context);
			if (e2 == EXP_CANT_INTERPRET)
				return e2;

			if (!v.isParameter()) {
				for (int i = 0; true; i++) {
					if (i == size(istate.vars)) {
						istate.vars.add(v);
						break;
					}
					if (v == (VarDeclaration) istate.vars.get(i))
						break;
				}
			}

		/*
		 * Create new struct literal reflecting updated fieldi
		 */
		Expressions expsx = new Expressions(size(se.elements));
		expsx.setDim(size(se.elements));
		for (int j = 0; j < size(expsx); j++)
		{
		    if (j == fieldi)
			expsx.set(j, e2);
		    else
			expsx.set(j, se.elements.get(j));
		}
		v.value = new StructLiteralExp(se.filename, se.lineNumber, se.sd, expsx);
		v.value.type = se.type;

		e = Constfold.Cast(type, type, post != 0 ? ev : e2, context);
	    }
		/* Assignment to array element of the form:
		 *   a[i] = e2
		 */
		else if (e1.op == TOKindex && ((IndexExp) e1).e1.op == TOKvar) {
			IndexExp ie = (IndexExp) e1;
			VarExp ve = (VarExp) ie.e1;
			VarDeclaration v = ve.var.isVarDeclaration();

			if (null == v || v.isDataseg(context))
				return EXP_CANT_INTERPRET;
			if (null == v.value()) {
				if (null != fp) {
					if (context.acceptsErrors()) {
						context.acceptProblem(Problem.newSemanticTypeError(
								IProblem.VariableIsUsedBeforeInitialization, v, v.toChars(context)));
					}
					return e;
				}

				Type t = v.type.toBasetype(context);
				if (t.ty == Tsarray) {
					/* This array was void initialized. Create a
					 * default initializer for it.
					 * What we should do is fill the array literal with
					 * null data, so use-before-initialized can be detected.
					 * But we're too lazy at the moment to do it, as that
					 * involves redoing Index() and whoever calls it.
					 */
					Expression ev = v.type.defaultInit(context);
					int dim = ((TypeSArray) t).dim.toInteger(context)
							.intValue();
					Expressions elements = new Expressions(dim);
					for (int i = 0; i < dim; i++)
						elements.add(ev);
					ArrayLiteralExp ae = new ArrayLiteralExp(null, 0, elements);
					ae.type = v.type;
					v.value(ae);
				} else
					return EXP_CANT_INTERPRET;
			}

			ArrayLiteralExp ae = null;
			AssocArrayLiteralExp aae = null;
			StringExp se = null;
			if (v.value().op == TOKarrayliteral)
				ae = (ArrayLiteralExp) v.value();
			else if (v.value().op == TOKassocarrayliteral)
				aae = (AssocArrayLiteralExp) v.value();
			else if (v.value().op == TOKstring)
				se = (StringExp) v.value();
			else
				return EXP_CANT_INTERPRET;

			Expression index = ie.e2.interpret(istate, context);
			if (index == EXP_CANT_INTERPRET)
				return EXP_CANT_INTERPRET;
			Expression ev = null;
			if (null != fp || null != ae || null != se) // not for aae, because key might not be there
			{
				ev = Index.call(type, v.value(), index, context);
				if (ev == EXP_CANT_INTERPRET)
					return EXP_CANT_INTERPRET;
			}

			if (null != fp)
				e2 = fp.call(type, ev, e2, context);
			else
				e2 = Constfold.Cast(type, type, e2, context);
			if (e2 == EXP_CANT_INTERPRET)
				return e2;

			if (!v.isParameter()) {
				for (int i = 0; true; i++) {
					if (i == istate.vars.size()) {
						istate.vars.add(v);
						break;
					}
					if (v == (VarDeclaration) istate.vars.get(i))
						break;
				}
			}

			if (null != ae) {
				/* Create new array literal reflecting updated elem
				 */
				int elemi = index.toInteger(context).intValue();
				Expressions expsx = new Expressions(ae.elements.size());
				expsx.setDim(ae.elements.size());
				for (int j = 0; j < ae.elements.size(); j++) {
					if (j == elemi)
						expsx.add(e2);
					else
						expsx.add(ae.elements.get(j));
				}
				v.value(new ArrayLiteralExp(ae.filename, ae.lineNumber, expsx));
				v.value().type = ae.type;
			} else if (null != aae) {
				/* Create new associative array literal reflecting updated key/value
				 */
				Expressions keysx = aae.keys;
				Expressions valuesx = new Expressions(aae.values.size());
				valuesx.setDim(aae.values.size());
				int updated = 0;
				for (int j = aae.values.size(); j > 0;) {
					j--;
					Expression ekey = (Expression) aae.keys.get(j);
					Expression ex = Equal.call(TOKequal, Type.tbool, ekey,
							index, context);
					if (ex == EXP_CANT_INTERPRET)
						return EXP_CANT_INTERPRET;
					if (ex.isBool(true)) {
						valuesx.set(j, e2);
						updated = 1;
					} else
						valuesx.set(j, aae.values.get(j));
				}
				if (0 == updated) { // Append index/e2 to keysx[]/valuesx[]
					valuesx.add(e2);
					keysx = new Expressions(keysx);
					keysx.add(index);
				}
				v.value(new AssocArrayLiteralExp(aae.filename, aae.lineNumber, keysx, valuesx));
				v.value().type = aae.type;
			} else if (null != se) {
				/* Create new string literal reflecting updated elem
				 */
				int elemi = index.toInteger(context).intValue();
				char[] s = new char[se.len + 1];
				//s = (unsigned char )mem.calloc(se.len + 1, se.sz);
				System.arraycopy(se.string, 0, s, 0, se.length);
				//memcpy(s, se.string, se.len  se.sz);
				int value = e2.toInteger(context).intValue();
				switch (se.sz) {
				/* FIXME semantic
				 * (I'm not sure what sort of bit manipulation I can do
				 *  with Java's char[] type) and whether there will be
				 *  adequate space allocated. The char[] array in StringExp
				 *  may need to be changed to a byte[] which would suck.
				 *
				 case 1:        s[elemi] = value; break;
				 case 2:        ((unsigned short )s)[elemi] = value; break;
				 case 4:        ((unsigned )s)[elemi] = value; break;
				 */

				//---temporary code for Descent testing---
				case 1:
				case 2:
				case 4:
					s[elemi] = (char) value;
					break;
				//---end temporary code block---

				default:
					assert (false);
					break;
				}
				StringExp se2 = new StringExp(se.filename, se.lineNumber, s, se.len);
				se2.committed = se.committed;
				se2.postfix = se.postfix;
				se2.type = se.type;
				v.value(se2);
			} else {
				assert (false);
			}
			e = Constfold.Cast(type, type, post > 0 ? ev : e2, context);
		}
		return e;
	}

	public Expression op_overload(Scope sc, SemanticContext context) {
		//AggregateDeclaration ad;
		Type t1 = e1.type.toBasetype(context);
		Type t2 = e2.type.toBasetype(context);
		char[] id = opId(context);
		char[] id_r = opId_r();

		Match m;
		Expressions args1 = new Expressions(1);
		Expressions args2 = new Expressions(1);
		int argsset = 0;

		AggregateDeclaration ad1;
		if (t1.ty == Tclass)
			ad1 = ((TypeClass) t1).sym;
		else if (t1.ty == Tstruct)
			ad1 = ((TypeStruct) t1).sym;
		else
			ad1 = null;

		AggregateDeclaration ad2;
		if (t2.ty == Tclass)
			ad2 = ((TypeClass) t2).sym;
		else if (t2.ty == Tstruct)
			ad2 = ((TypeStruct) t2).sym;
		else
			ad2 = null;

		Dsymbol s = null;
		Dsymbol s_r = null;
		FuncDeclaration fd = null;
		TemplateDeclaration td = null;
		if (ad1 != null && id != null) {
			s = search_function(ad1, id, context);
		}
		if (ad2 != null && id_r != null) {
			s_r = search_function(ad2, id_r, context);
		}

		if (s != null || s_r != null) {
			/* Try:
			 *	a.opfunc(b)
			 *	b.opfunc_r(a)
			 * and see which is better.
			 */
			Expression e;
			FuncDeclaration lastf;

			args1.setDim(1);
			args1.set(0, e1);
			args2.setDim(1);
			args2.set(0, e2);
			argsset = 1;

			m = new Match();
			m.last = MATCHnomatch;

			if (s != null) {
				fd = s.isFuncDeclaration();
				if (fd != null) {
					overloadResolveX(m, fd, null, args2, context);
				} else {
					td = s.isTemplateDeclaration();
					templateResolve(m, td, sc, filename, lineNumber, null, null, args2, context);
				}
			}

			lastf = m.lastf;

			if (s_r != null) {
				fd = s_r.isFuncDeclaration();
				if (fd != null) {
					overloadResolveX(m, fd, null, args1, context);
				} else {
					td = s_r.isTemplateDeclaration();
					templateResolve(m, td, sc, filename, lineNumber, null, null, args1, context);
				}
			}

			if (m.count > 1) {
				// Error, ambiguous
				if (context.acceptsErrors()) {
					context.acceptProblem(Problem.newSemanticTypeError(IProblem.BothOverloadsMuchArgumentList, this, m.lastf.type.toChars(context), m.nextf.type
									.toChars(context), m.lastf.toChars(context)));
				}
			} else if (m.last == MATCHnomatch) {
				m.lastf = m.anyf;
			}

			if (op == TOKplusplus || op == TOKminusminus)
				// Kludge because operator overloading regards e++ and e--
				// as unary, but it's implemented as a binary.
				// Rewrite (e1 ++ e2) as e1.postinc()
				// Rewrite (e1 -- e2) as e1.postdec()
				e = build_overload(filename, lineNumber, sc, e1, null, id, context);
			else if (lastf != null && m.lastf == lastf
					|| m.last == MATCHnomatch)
				// Rewrite (e1 op e2) as e1.opfunc(e2)
				e = build_overload(filename, lineNumber, sc, e1, e2, id, context);
			else
				// Rewrite (e1 op e2) as e2.opfunc_r(e1)
				e = build_overload(filename, lineNumber, sc, e2, e1, id_r, context);
			return e;
		}

		if (isCommutative()) {
			s = null;
			s_r = null;
			if (ad1 != null && id_r != null) {
				s_r = search_function(ad1, id_r, context);
			}
			if (ad2 != null && id != null) {
				s = search_function(ad2, id, context);
			}

			if (s != null || s_r != null) {
				/* Try:
				 *	a.opfunc_r(b)
				 *	b.opfunc(a)
				 * and see which is better.
				 */
				Expression e;
				FuncDeclaration lastf;

				if (0 == argsset) {
					args1.setDim(1);
					args1.set(0, e1);
					args2.setDim(1);
					args2.set(0, e2);
				}

				m = new Match();
				m.last = MATCHnomatch;

				if (s_r != null) {
					fd = s_r.isFuncDeclaration();
					if (fd != null) {
						overloadResolveX(m, fd, null, args2, context);
					} else {
						td = s_r.isTemplateDeclaration();
						templateResolve(m, td, sc, filename, lineNumber, null, null, args2, context);
					}
				}
				lastf = m.lastf;

				if (s != null) {
					fd = s.isFuncDeclaration();
					if (fd != null) {
						overloadResolveX(m, fd, null, args1, context);
					} else {
						td = s.isTemplateDeclaration();
						templateResolve(m, td, sc, filename, lineNumber, null, null, args1, context);
					}
				}

				if (m.count > 1) {
					// Error, ambiguous
					if (context.acceptsErrors()) {
						context.acceptProblem(Problem.newSemanticTypeError(
								IProblem.BothOverloadsMuchArgumentList, this, m.lastf.type.toChars(context), m.nextf.type.toChars(context), m.lastf.toChars(context)));
					}
				} else if (m.last == MATCHnomatch) {
					m.lastf = m.anyf;
				}

				if (lastf != null && m.lastf == lastf || id_r != null
						&& m.last == MATCHnomatch)
					// Rewrite (e1 op e2) as e1.opfunc_r(e2)
					e = build_overload(filename, lineNumber, sc, e1, e2, id_r, context);
				else
					// Rewrite (e1 op e2) as e2.opfunc(e1)
					e = build_overload(filename, lineNumber, sc, e2, e1, id, context);

				// When reversing operands of comparison operators,
				// need to reverse the sense of the op
				switch (op) {
				case TOKlt:
					op = TOKgt;
					break;
				case TOKgt:
					op = TOKlt;
					break;
				case TOKle:
					op = TOKge;
					break;
				case TOKge:
					op = TOKle;
					break;

				// Floating point compares
				case TOKule:
					op = TOKuge;
					break;
				case TOKul:
					op = TOKug;
					break;
				case TOKuge:
					op = TOKule;
					break;
				case TOKug:
					op = TOKul;
					break;

				// These are symmetric
				case TOKunord:
				case TOKlg:
				case TOKleg:
				case TOKue:
					break;
				}

				return e;
			}
		}

		if (!context.isD1()) {
			// Try alias this on first operand
			if (ad1 != null && ad1.aliasthis != null) {
				/*
				 * Rewrite (e1 op e2) as: (e1.aliasthis op e2)
				 */
				Expression e1 = new DotIdExp(filename, lineNumber, this.e1, ad1.aliasthis.ident);
				Expression e = copy();
				((BinExp) e).e1 = e1;
				e = e.semantic(sc, context);
				return e;
			}

			// Try alias this on second operand
			if (ad2 != null && ad2.aliasthis != null) {
				/*
				 * Rewrite (e1 op e2) as: (e1 op e2.aliasthis)
				 */
				Expression e2 = new DotIdExp(filename, lineNumber, this.e2, ad2.aliasthis.ident);
				Expression e = copy();
				((BinExp) e).e2 = e2;
				e = e.semantic(sc, context);
				return e;
			}
		}

		return null;
	}

	@Override
	public Expression optimize(int result, SemanticContext context) {
		boolean condition;
		if (context.isD1()) {
			condition = true;
		} else {
		    condition = op != TOKconstruct && op != TOKblit; // don't replace const variable with its initializer
		}

		if (condition)
			e1 = e1.optimize(result, context);
		e2 = e2.optimize(result, context);
		if (op == TOKshlass || op == TOKshrass || op == TOKushrass) {
			if (e2.isConst()) {
				integer_t i2 = e2.toInteger(context);
				integer_t sz = new integer_t(e1.type.size(context)).multiply(8);
				if (i2.compareTo(0) < 0 || i2.compareTo(sz) > 0) {
					if (context.acceptsErrors()) {
						context.acceptProblem(Problem.newSemanticTypeError(
								IProblem.ShiftAssignIsOutsideTheRange,
								sourceE1, sourceE2,
										i2.toString(), sz.toString()));
					}
					e2 = new IntegerExp(0);
				}
			}
		}

		return this;
	}

	/* Some of the array op functions are written as library functions,
	 * presumably to optimize them with special CPU vector instructions.
	 * List those library functions here, in alpha order.
	 */
	private static String[] libArrayopFuncs =
	{
	    "_arrayExpSliceAddass_a",
	    "_arrayExpSliceAddass_d",		// T[]+=T
	    "_arrayExpSliceAddass_f",		// T[]+=T
	    "_arrayExpSliceAddass_g",
	    "_arrayExpSliceAddass_h",
	    "_arrayExpSliceAddass_i",
	    "_arrayExpSliceAddass_k",
	    "_arrayExpSliceAddass_s",
	    "_arrayExpSliceAddass_t",
	    "_arrayExpSliceAddass_u",
	    "_arrayExpSliceAddass_w",

	    "_arrayExpSliceDivass_d",		// T[]/=T
	    "_arrayExpSliceDivass_f",		// T[]/=T

	    "_arrayExpSliceMinSliceAssign_a",
	    "_arrayExpSliceMinSliceAssign_d",	// T[]=T-T[]
	    "_arrayExpSliceMinSliceAssign_f",	// T[]=T-T[]
	    "_arrayExpSliceMinSliceAssign_g",
	    "_arrayExpSliceMinSliceAssign_h",
	    "_arrayExpSliceMinSliceAssign_i",
	    "_arrayExpSliceMinSliceAssign_k",
	    "_arrayExpSliceMinSliceAssign_s",
	    "_arrayExpSliceMinSliceAssign_t",
	    "_arrayExpSliceMinSliceAssign_u",
	    "_arrayExpSliceMinSliceAssign_w",

	    "_arrayExpSliceMinass_a",
	    "_arrayExpSliceMinass_d",		// T[]-=T
	    "_arrayExpSliceMinass_f",		// T[]-=T
	    "_arrayExpSliceMinass_g",
	    "_arrayExpSliceMinass_h",
	    "_arrayExpSliceMinass_i",
	    "_arrayExpSliceMinass_k",
	    "_arrayExpSliceMinass_s",
	    "_arrayExpSliceMinass_t",
	    "_arrayExpSliceMinass_u",
	    "_arrayExpSliceMinass_w",

	    "_arrayExpSliceMulass_d",		// T[]*=T
	    "_arrayExpSliceMulass_f",		// T[]*=T
	    "_arrayExpSliceMulass_i",
	    "_arrayExpSliceMulass_k",
	    "_arrayExpSliceMulass_s",
	    "_arrayExpSliceMulass_t",
	    "_arrayExpSliceMulass_u",
	    "_arrayExpSliceMulass_w",

	    "_arraySliceExpAddSliceAssign_a",
	    "_arraySliceExpAddSliceAssign_d",	// T[]=T[]+T
	    "_arraySliceExpAddSliceAssign_f",	// T[]=T[]+T
	    "_arraySliceExpAddSliceAssign_g",
	    "_arraySliceExpAddSliceAssign_h",
	    "_arraySliceExpAddSliceAssign_i",
	    "_arraySliceExpAddSliceAssign_k",
	    "_arraySliceExpAddSliceAssign_s",
	    "_arraySliceExpAddSliceAssign_t",
	    "_arraySliceExpAddSliceAssign_u",
	    "_arraySliceExpAddSliceAssign_w",

	    "_arraySliceExpDivSliceAssign_d",	// T[]=T[]/T
	    "_arraySliceExpDivSliceAssign_f",	// T[]=T[]/T

	    "_arraySliceExpMinSliceAssign_a",
	    "_arraySliceExpMinSliceAssign_d",	// T[]=T[]-T
	    "_arraySliceExpMinSliceAssign_f",	// T[]=T[]-T
	    "_arraySliceExpMinSliceAssign_g",
	    "_arraySliceExpMinSliceAssign_h",
	    "_arraySliceExpMinSliceAssign_i",
	    "_arraySliceExpMinSliceAssign_k",
	    "_arraySliceExpMinSliceAssign_s",
	    "_arraySliceExpMinSliceAssign_t",
	    "_arraySliceExpMinSliceAssign_u",
	    "_arraySliceExpMinSliceAssign_w",

	    "_arraySliceExpMulSliceAddass_d",	// T[] += T[]*T
	    "_arraySliceExpMulSliceAddass_f",
	    "_arraySliceExpMulSliceAddass_r",

	    "_arraySliceExpMulSliceAssign_d",	// T[]=T[]*T
	    "_arraySliceExpMulSliceAssign_f",	// T[]=T[]*T
	    "_arraySliceExpMulSliceAssign_i",
	    "_arraySliceExpMulSliceAssign_k",
	    "_arraySliceExpMulSliceAssign_s",
	    "_arraySliceExpMulSliceAssign_t",
	    "_arraySliceExpMulSliceAssign_u",
	    "_arraySliceExpMulSliceAssign_w",

	    "_arraySliceExpMulSliceMinass_d",	// T[] -= T[]*T
	    "_arraySliceExpMulSliceMinass_f",
	    "_arraySliceExpMulSliceMinass_r",

	    "_arraySliceSliceAddSliceAssign_a",
	    "_arraySliceSliceAddSliceAssign_d",	// T[]=T[]+T[]
	    "_arraySliceSliceAddSliceAssign_f",	// T[]=T[]+T[]
	    "_arraySliceSliceAddSliceAssign_g",
	    "_arraySliceSliceAddSliceAssign_h",
	    "_arraySliceSliceAddSliceAssign_i",
	    "_arraySliceSliceAddSliceAssign_k",
	    "_arraySliceSliceAddSliceAssign_r",	// T[]=T[]+T[]
	    "_arraySliceSliceAddSliceAssign_s",
	    "_arraySliceSliceAddSliceAssign_t",
	    "_arraySliceSliceAddSliceAssign_u",
	    "_arraySliceSliceAddSliceAssign_w",

	    "_arraySliceSliceAddass_a",
	    "_arraySliceSliceAddass_d",		// T[]+=T[]
	    "_arraySliceSliceAddass_f",		// T[]+=T[]
	    "_arraySliceSliceAddass_g",
	    "_arraySliceSliceAddass_h",
	    "_arraySliceSliceAddass_i",
	    "_arraySliceSliceAddass_k",
	    "_arraySliceSliceAddass_s",
	    "_arraySliceSliceAddass_t",
	    "_arraySliceSliceAddass_u",
	    "_arraySliceSliceAddass_w",

	    "_arraySliceSliceMinSliceAssign_a",
	    "_arraySliceSliceMinSliceAssign_d",	// T[]=T[]-T[]
	    "_arraySliceSliceMinSliceAssign_f",	// T[]=T[]-T[]
	    "_arraySliceSliceMinSliceAssign_g",
	    "_arraySliceSliceMinSliceAssign_h",
	    "_arraySliceSliceMinSliceAssign_i",
	    "_arraySliceSliceMinSliceAssign_k",
	    "_arraySliceSliceMinSliceAssign_r",	// T[]=T[]-T[]
	    "_arraySliceSliceMinSliceAssign_s",
	    "_arraySliceSliceMinSliceAssign_t",
	    "_arraySliceSliceMinSliceAssign_u",
	    "_arraySliceSliceMinSliceAssign_w",

	    "_arraySliceSliceMinass_a",
	    "_arraySliceSliceMinass_d",		// T[]-=T[]
	    "_arraySliceSliceMinass_f",		// T[]-=T[]
	    "_arraySliceSliceMinass_g",
	    "_arraySliceSliceMinass_h",
	    "_arraySliceSliceMinass_i",
	    "_arraySliceSliceMinass_k",
	    "_arraySliceSliceMinass_s",
	    "_arraySliceSliceMinass_t",
	    "_arraySliceSliceMinass_u",
	    "_arraySliceSliceMinass_w",

	    "_arraySliceSliceMulSliceAssign_d",	// T[]=T[]*T[]
	    "_arraySliceSliceMulSliceAssign_f",	// T[]=T[]*T[]
	    "_arraySliceSliceMulSliceAssign_i",
	    "_arraySliceSliceMulSliceAssign_k",
	    "_arraySliceSliceMulSliceAssign_s",
	    "_arraySliceSliceMulSliceAssign_t",
	    "_arraySliceSliceMulSliceAssign_u",
	    "_arraySliceSliceMulSliceAssign_w",

	    "_arraySliceSliceMulass_d",		// T[]*=T[]
	    "_arraySliceSliceMulass_f",		// T[]*=T[]
	    "_arraySliceSliceMulass_i",
	    "_arraySliceSliceMulass_k",
	    "_arraySliceSliceMulass_s",
	    "_arraySliceSliceMulass_t",
	    "_arraySliceSliceMulass_u",
	    "_arraySliceSliceMulass_w",
	};

}
