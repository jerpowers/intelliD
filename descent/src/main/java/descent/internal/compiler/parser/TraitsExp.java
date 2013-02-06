package descent.internal.compiler.parser;

import static descent.internal.compiler.parser.TOK.TOKdotvar;
import static descent.internal.compiler.parser.TOK.TOKvar;
import melnorme.utilbox.tree.TreeVisitor;
import descent.core.compiler.IProblem;
import descent.internal.compiler.parser.ast.IASTVisitor;

// DMD 2.003
public class TraitsExp extends Expression {

	public IdentifierExp ident;
	public Objects args;

	public TraitsExp(char[] filename, int lineNumber, IdentifierExp ident, Objects args) {
		super(filename, lineNumber, TOK.TOKtraits);
		this.ident = ident;
		this.args = args;
	}

	@Override
	public void accept0(IASTVisitor visitor) {
		boolean children = visitor.visit(this);
		if (children) {
			TreeVisitor.acceptChildren(visitor, ident);
			TreeVisitor.acceptChildren(visitor, args);
		}
		visitor.endVisit(this);
	}

	@Override
	public int getNodeType() {
		return TRAITS_EXP;
	}

	@Override
	public Expression semantic(final Scope sc, final SemanticContext context) {
		if (!equals(ident, Id.compiles) && !equals(ident, Id.isSame)) {
			TemplateInstance.semanticTiargs(filename, lineNumber, sc, args, 1, context);
		}
		
	    int dim = args != null ? args.size() : 0;

		if (equals(ident, Id.isArithmetic)) {
			// ISTYPE(t.isintegral() || t.isfloating())
			return isType(new ISTYPE_Conditional() {
				@Override
				public boolean check(Type t) {
					return t.isintegral() || t.isfloating();
				}
			});
		}

		else if (equals(ident, Id.isFloating)) {
			// ISTYPE(t.isfloating())
			return isType(new ISTYPE_Conditional() {
				@Override
				public boolean check(Type t) {
					return t.isfloating();
				}
			});
		}

		else if (equals(ident, Id.isIntegral)) {
			// ISTYPE(t.isintegral())
			return isType(new ISTYPE_Conditional() {
				@Override
				public boolean check(Type t) {
					return t.isintegral();
				}
			});
		}

		else if (equals(ident, Id.isScalar)) {
			// ISTYPE(t.isscalar())
			return isType(new ISTYPE_Conditional() {
				@Override
				public boolean check(Type t) {
					return t.isscalar(context);
				}
			});
		}

		else if (equals(ident, Id.isUnsigned)) {
			// ISTYPE(t.isunsigned())
			return isType(new ISTYPE_Conditional() {
				@Override
				public boolean check(Type t) {
					return t.isunsigned();
				}
			});
		}

		else if (equals(ident, Id.isAssociativeArray)) {
			// ISTYPE(t.toBasetype().ty == Taarray)
			return isType(new ISTYPE_Conditional() {
				@Override
				public boolean check(Type t) {
					return t.toBasetype(context).ty == TY.Taarray;
				}
			});
		}

		else if (equals(ident, Id.isStaticArray)) {
			// ISTYPE(t.toBasetype().ty == Tsarray)
			return isType(new ISTYPE_Conditional() {
				@Override
				public boolean check(Type t) {
					return t.toBasetype(context).ty == TY.Tsarray;
				}
			});
		}

		else if (equals(ident, Id.isAbstractClass)) {
			// ISTYPE(t.toBasetype().ty == Tclass && ((TypeClass
			// *)t.toBasetype()).sym.isAbstract())
			return isType(new ISTYPE_Conditional() {
				@Override
				public boolean check(Type t) {
					return t.toBasetype(context).ty == TY.Tclass
							&& ((TypeClass) t.toBasetype(context)).sym
									.isAbstract();
				}
			});
		}

		else if (equals(ident, Id.isFinalClass)) {
			// ISTYPE(t.toBasetype().ty == Tclass && ((TypeClass
			// *)t.toBasetype()).sym.storage_class & STCfinal)
			return isType(new ISTYPE_Conditional() {
				@Override
				public boolean check(Type t) {
					return t.toBasetype(context).ty == TY.Tclass
							&& ((((TypeClass) t.toBasetype(context)).sym.storage_class & STC.STCfinal) != 0);
				}
			});
		}

		else if (equals(ident, Id.isAbstractFunction)) {
			// ISDSYMBOL((f = s.isFuncDeclaration()) != NULL && f.isAbstract())
			return isDSymbol(new ISDSYMBOL_Conditional() {
				@Override
				public boolean check(Dsymbol s) {
					FuncDeclaration f = s.isFuncDeclaration();
					return null != f && f.isAbstract();
				}
			});
		}

		else if (equals(ident, Id.isVirtualFunction)) {
			// ISDSYMBOL((f = s.isFuncDeclaration()) != NULL && f.isVirtual())
			return isDSymbol(new ISDSYMBOL_Conditional() {
				@Override
				public boolean check(Dsymbol s) {
					FuncDeclaration f = s.isFuncDeclaration();
					return null != f && f.isVirtual(context);
				}
			});
		}

		else if (equals(ident, Id.isFinalFunction)) {
			// ISDSYMBOL((f = s.isFuncDeclaration()) != NULL && f.isFinal())
			return isDSymbol(new ISDSYMBOL_Conditional() {
				@Override
				public boolean check(Dsymbol s) {
					FuncDeclaration f = s.isFuncDeclaration();
					return null != f && f.isFinal();
				}
			});
		}

		else if (equals(ident, Id.hasMember) || equals(ident, Id.getMember)
				|| equals(ident, Id.getVirtualFunctions)) {
			if (dim != 2) {
				if (context.acceptsErrors()) {
					context.acceptProblem(Problem.newSemanticTypeError(
							IProblem.WrongNumberOfArguments, this,
							new String[] { String.valueOf(dim) }));
				}
				return new IntegerExp(filename, lineNumber, 0, Type.tbool);
			}

			ASTDmdNode o = args.get(0);
			Expression e = isExpression((ASTDmdNode) args.get(1));
			if (null == e) {
				if (context.acceptsErrors()) {
					context.acceptProblem(Problem.newSemanticTypeError(
							IProblem.StringExpectedAsSecondArgument, this));
				}
				return new IntegerExp(filename, lineNumber, 0, Type.tbool);
			}

			e = e.optimize(WANTvalue | WANTinterpret, context);
			if (e.op != TOK.TOKstring) {
				if (context.acceptsErrors()) {
					context.acceptProblem(Problem.newSemanticTypeError(
							IProblem.StringExpectedAsSecondArgument, this));
				}
				return new IntegerExp(filename, lineNumber, 0, Type.tbool);
			}

			StringExp se = (StringExp) e;
			se = se.toUTF8(sc, context);
			if (se.sz != 1) {
				if (context.acceptsErrors()) {
					context.acceptProblem(Problem.newSemanticTypeError(
							IProblem.StringMustBeChars, this));
				}
				return new IntegerExp(filename, lineNumber, 0, Type.tbool);
			}

			IdentifierExp id = context.uniqueId(new String(se.string));
			Type t = isType(o);
			e = isExpression(o);
			Dsymbol s = isDsymbol(o);
			if (null != t)
				e = context.newTypeDotIdExp(filename, lineNumber, t, id);
			else if (null != e)
				e = new DotIdExp(filename, lineNumber, e, id);
			else if (null != s) {
				e = new DsymbolExp(filename, lineNumber, s);
				e = new DotIdExp(filename, lineNumber, e, id);
			} else {
				if (context.acceptsErrors()) {
					context.acceptProblem(Problem.newSemanticTypeError(
							IProblem.InvalidFirstArgument, this));
				}
				return new IntegerExp(filename, lineNumber, 0, Type.tbool);
			}

			if (equals(ident, Id.hasMember)) {
				/*
				 * Take any errors as meaning it wasn't found
				 */
				if (context.isD1()) {
					int errors = context.global.errors;
					context.global.gag++;
					e = e.semantic(sc, context);
					context.global.gag--;
					if (errors != context.global.errors) {
						if (context.global.gag == 0)
							context.global.errors = errors;
						return new IntegerExp(filename, lineNumber, 0, Type.tbool);
					} else
						return new IntegerExp(filename, lineNumber, 1, Type.tbool);
				} else {
					e = e.trySemantic(sc, context);
					if (null == e) {
						if (context.global.gag != 0)
							context.global.errors++;
						return new IntegerExp(filename, lineNumber, 0, Type.tbool);
					} else
						return new IntegerExp(filename, lineNumber, 1, Type.tbool);
				}
			} else if (equals(ident, Id.getMember)) {
				e = e.semantic(sc, context);
				return e;
			} else if (equals(ident, Id.getVirtualFunctions)) {
				int errors = context.global.errors;
				Expression ex = e;
				e = e.semantic(sc, context);
				if (errors < context.global.errors) {
					if (context.acceptsErrors()) {
						context.acceptProblem(Problem.newSemanticTypeError(
								IProblem.UndefinedIdentifier, ex, ex
										.toChars(context)));
					}
				}

				/*
				 * Create tuple of virtual function overloads of e
				 */
				Expressions exps = new Expressions(3);
				FuncDeclaration f;
				if (e.op == TOKvar) {
					VarExp ve = (VarExp) e;
					f = ve.var.isFuncDeclaration();
				} else if (e.op == TOKdotvar) {
					DotVarExp dve = (DotVarExp) e;
					f = dve.var.isFuncDeclaration();
				} else
					f = null;
				Pvirtuals p = new Pvirtuals();
				p.exps = exps;
				p.e1 = e;
				overloadApply(f, p, p, context);

				TupleExp tup = new TupleExp(filename, lineNumber, exps);
				return tup.semantic(sc, context);
			} else {
				throw new IllegalStateException();
			}
		}

		else if (equals(ident, Id.classInstanceSize)) {
			if (dim != 1) {
				if (context.acceptsErrors()) {
					context.acceptProblem(Problem.newSemanticTypeError(
							IProblem.WrongNumberOfArguments, this,
							new String[] { String.valueOf(dim) }));
				}
				return new IntegerExp(filename, lineNumber, 0, Type.tbool);
			}

			ASTDmdNode o = args.get(0);
			Dsymbol s = getDsymbol(o, context);
			if (null == s) {
				if (context.acceptsErrors()) {
					context.acceptProblem(Problem.newSemanticTypeError(
							IProblem.FirstArgumentIsNotAClass, this));
				}
				return new IntegerExp(filename, lineNumber, 0, Type.tbool);
			}

			ClassDeclaration cd = s.isClassDeclaration();
			if (null == cd) {
				if (context.acceptsErrors()) {
					context.acceptProblem(Problem.newSemanticTypeError(
							IProblem.FirstArgumentIsNotAClass, this));
				}
				return new IntegerExp(filename, lineNumber, 0, Type.tbool);
			}
			return new IntegerExp(filename, lineNumber, cd.structsize, Type.tsize_t);
		}

		else if (equals(ident, Id.allMembers)
				|| equals(ident, Id.derivedMembers)) {
			if (dim != 1) {
				if (context.acceptsErrors()) {
					context.acceptProblem(Problem.newSemanticTypeError(
							IProblem.WrongNumberOfArguments, this,
							new String[] { String.valueOf(dim) }));
				}
				return new IntegerExp(filename, lineNumber, 0, Type.tbool);
			}

			ASTDmdNode o = args.get(0);
			Dsymbol s = getDsymbol(o, context);
			if (null == s) {
				if (context.acceptsErrors()) {
					context.acceptProblem(Problem.newSemanticTypeError(
							IProblem.ArgumentHasNoMembers, this));
				}
				return new IntegerExp(filename, lineNumber, 0, Type.tbool);
			}

			ScopeDsymbol sd = s.isScopeDsymbol();
			if (null == sd) {
				if (context.acceptsErrors()) {
					context.acceptProblem(Problem.newSemanticTypeError(
							IProblem.KindSymbolHasNoMembers, this,
							new String[] { s.kind(), s.toChars(context) }));
				}
				return new IntegerExp(filename, lineNumber, 0, Type.tbool);
			}

			Expressions exps = new Expressions(3);
			Louter: while (true) {
				Linner: for (int i = 0; i < sd.members.size(); i++) {
					Dsymbol sm = (Dsymbol) sd.members.get(i);
					if (null != sm.ident) {
						char[] str = sm.ident.ident;

						/*
						 * Skip if already present in exps[]
						 */
						for (Expression exp : exps) {
							StringExp se2 = (StringExp) exp;
							if (equals(str, se2.string))
								continue Linner;
						}

						StringExp se = new StringExp(filename, lineNumber, str, str.length);
						exps.add(se);
					}
				}
				ClassDeclaration cd = sd.isClassDeclaration();
				if (null != cd && null != cd.baseClass
						&& equals(ident, Id.allMembers))
					sd = cd.baseClass; // do again with base class
				else
					break Louter;
			}

			Expression e = new ArrayLiteralExp(filename, lineNumber, exps);
			e = e.semantic(sc, context);
			return e;
		} else if (equals(ident, Id.compiles)) {
			/*
			 * Determine if all the objects - types, expressions, or symbols -
			 * compile without error
			 */
			if (0 == dim) {
				return new IntegerExp(filename, lineNumber, 0, Type.tbool);
			}

			for (int i = 0; i < dim; i++) {
				ASTDmdNode o = (ASTDmdNode) args.get(i);

				Expression[] e = { null };

				int errors = context.global.errors;
				context.global.gag++;

				Type[] t = { isType(o) };
				if (t[0] != null) {
					Dsymbol[] s = { null };
					t[0].resolve(filename, lineNumber, sc, e, t, s, context);
					if (t[0] != null) {
						t[0].semantic(filename, lineNumber, sc, context);
					} else if (e[0] != null) {
						e[0].semantic(sc, context);
					}
				} else {
					e[0] = isExpression(o);
					if (e[0] != null) {
						e[0].semantic(sc, context);
					}
				}

				context.global.gag--;
				if (errors != context.global.errors) {
					if (context.global.gag == 0)
						context.global.errors = errors;
					return new IntegerExp(filename, lineNumber, 0, Type.tbool);
				}
			}
			return new IntegerExp(filename, lineNumber, 1, Type.tbool);
		} else if (equals(ident, Id.isSame)) {
			/*
			 * Determine if two symbols are the same
			 */
			if (dim != 2) {
				if (context.acceptsErrors()) {
					context.acceptProblem(Problem.newSemanticTypeError(
							IProblem.WrongNumberOfArguments, this,
							new String[] { String.valueOf(dim) }));
				}
				return new IntegerExp(filename, lineNumber, 0, Type.tbool);
			}
			TemplateInstance.semanticTiargs(filename, lineNumber, sc, args, 0, context);
			ASTDmdNode o1 = (ASTDmdNode) args.get(0);
			ASTDmdNode o2 = (ASTDmdNode) args.get(1);
			Dsymbol s1 = getDsymbol(o1, context);
			Dsymbol s2 = getDsymbol(o2, context);

			if (null == s1 && null == s2) {
				Expression ea1 = isExpression(o1);
				Expression ea2 = isExpression(o2);
				if (ea1 != null && ea2 != null && ea1.equals(ea2)) {
					return new IntegerExp(filename, lineNumber, 1, Type.tbool);
				}
			}

			if (null == s1 || null == s2) {
				return new IntegerExp(filename, lineNumber, 0, Type.tbool);
			}

			s1 = s1.toAlias(context);
			s2 = s2.toAlias(context);

			if (s1 == s2) {
				return new IntegerExp(filename, lineNumber, 1, Type.tbool);
			} else {
				return new IntegerExp(filename, lineNumber, 0, Type.tbool);
			}
		} else {
			if (context.acceptsErrors()) {
				context.acceptProblem(Problem.newSemanticTypeError(
						IProblem.UnrecongnizedTrait, this.ident, ident
								.toString()));
			}
			return new IntegerExp(filename, lineNumber, 0, Type.tbool);
		}
	}

	/*
	 * #define ISTYPE(cond) \ for (size_t i = 0; i < dim; i++) \ { Typet =
	 * getType((Object)args.data[i]); \ if (!t) \ goto Lfalse; \ if (!(cond)) \
	 * goto Lfalse; \ } \ if (!dim) \ goto Lfalse; \ goto Ltrue;
	 */
	private static interface ISTYPE_Conditional {
		public boolean check(Type t);
	}

	private IntegerExp isType(ISTYPE_Conditional cond) {
		int dim = null != args ? args.size() : 0;
		for (int i = 0; i < dim; i++) {
			Type t = null; /* TODO semantic getType((Object)args.get(i)); */
			if (null == t)
				return new IntegerExp(filename, lineNumber, 0, Type.tbool);
			if (!cond.check(t))
				return new IntegerExp(filename, lineNumber, 0, Type.tbool);
		}

		if (0 == dim)
			return new IntegerExp(filename, lineNumber, 0, Type.tbool);

		return new IntegerExp(filename, lineNumber, 1, Type.tbool);
	}

	/*
	 * #define ISDSYMBOL(cond) \ for (size_t i = 0; i < dim; i++) \ { Dsymbols =
	 * getDsymbol((Object)args.data[i]); \ if (!s) \ goto Lfalse; \ if (!(cond))
	 * \ goto Lfalse; \ } \ if (!dim) \ goto Lfalse; \ goto Ltrue;
	 */
	private static interface ISDSYMBOL_Conditional {
		public boolean check(Dsymbol s);
	}

	private IntegerExp isDSymbol(ISDSYMBOL_Conditional cond) {
		int dim = null != args ? args.size() : 0;
		for (int i = 0; i < dim; i++) {
			Dsymbol s = null; /* TODO semantic getDsymbol((Object) args.data[i]); */
			if (null == s)
				return new IntegerExp(filename, lineNumber, 0, Type.tbool);
			if (!cond.check(s))
				return new IntegerExp(filename, lineNumber, 0, Type.tbool);
		}
		if (0 == dim)
			return new IntegerExp(filename, lineNumber, 0, Type.tbool);

		return new IntegerExp(filename, lineNumber, 1, Type.tbool);
	}

	@Override
	public Expression syntaxCopy(SemanticContext context) {
		return new TraitsExp(filename, lineNumber, ident, TemplateInstance.arraySyntaxCopy(args,
				context));
	}

	@Override
	public void toCBuffer(OutBuffer buf, HdrGenState hgs,
			SemanticContext context) {
		buf.writestring("__traits(");
		buf.writestring(ident.toChars());
		if (args != null) {
			for (int i = 0; i < args.size(); i++) {
				buf.writeByte(',');
				ASTDmdNode oarg = (ASTDmdNode) args.get(i);
				ObjectToCBuffer(buf, hgs, oarg, context);
			}
		}
		buf.writeByte(')');
	}

}
