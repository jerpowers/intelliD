package descent.internal.compiler.parser;

import static descent.internal.compiler.parser.BE.BEbreak;
import static descent.internal.compiler.parser.BE.BEcontinue;
import static descent.internal.compiler.parser.BE.BEfallthru;
import static descent.internal.compiler.parser.BE.BEthrow;
import static descent.internal.compiler.parser.Constfold.ArrayLength;
import static descent.internal.compiler.parser.Constfold.Index;
import static descent.internal.compiler.parser.MATCH.MATCHconst;
import static descent.internal.compiler.parser.STC.STC_TYPECTOR;
import static descent.internal.compiler.parser.STC.STCconst;
import static descent.internal.compiler.parser.STC.STCfinal;
import static descent.internal.compiler.parser.STC.STCforeach;
import static descent.internal.compiler.parser.STC.STCin;
import static descent.internal.compiler.parser.STC.STCinvariant;
import static descent.internal.compiler.parser.STC.STClazy;
import static descent.internal.compiler.parser.STC.STCmanifest;
import static descent.internal.compiler.parser.STC.STCout;
import static descent.internal.compiler.parser.STC.STCref;
import static descent.internal.compiler.parser.TOK.TOKdelegate;
import static descent.internal.compiler.parser.TOK.TOKforeach;
import static descent.internal.compiler.parser.TOK.TOKforeach_reverse;
import static descent.internal.compiler.parser.TOK.TOKstring;
import static descent.internal.compiler.parser.TOK.TOKtuple;
import static descent.internal.compiler.parser.TOK.TOKtype;
import static descent.internal.compiler.parser.TOK.TOKvar;
import static descent.internal.compiler.parser.TY.Taarray;
import static descent.internal.compiler.parser.TY.Tarray;
import static descent.internal.compiler.parser.TY.Tchar;
import static descent.internal.compiler.parser.TY.Tclass;
import static descent.internal.compiler.parser.TY.Tdchar;
import static descent.internal.compiler.parser.TY.Tdelegate;
import static descent.internal.compiler.parser.TY.Tfunction;
import static descent.internal.compiler.parser.TY.Tint32;
import static descent.internal.compiler.parser.TY.Tint64;
import static descent.internal.compiler.parser.TY.Tsarray;
import static descent.internal.compiler.parser.TY.Tstruct;
import static descent.internal.compiler.parser.TY.Ttuple;
import static descent.internal.compiler.parser.TY.Tuns32;
import static descent.internal.compiler.parser.TY.Tuns64;
import static descent.internal.compiler.parser.TY.Twchar;

import java.util.List;

import melnorme.utilbox.tree.TreeVisitor;

import melnorme.utilbox.core.Assert;

import descent.core.compiler.IProblem;
import descent.internal.compiler.parser.ast.IASTVisitor;


public class ForeachStatement extends Statement {

	private final static char[] _aaApply = { '_', 'a', 'a', 'A', 'p', 'p', 'l',
			'y', };
	private final static char[] _aaApply2 = { '_', 'a', 'a', 'A', 'p', 'p',
			'l', 'y', '2' };

	public final static String[] fntab = { "cc", "cw", "cd", "wc", "cc", "wd",
			"dc", "dw", "dd" };
	public TOK op;
	public Arguments arguments;
	public Expression aggr, sourceAggr;

	public Statement body, sourceBody;
	public VarDeclaration key;

	public VarDeclaration value;

	public FuncDeclaration func; // function we're lexically in
	public List cases; // put breaks, continues, gotos and returns here

	public List gotos; // forward referenced goto's go here

	public ForeachStatement(char[] filename, int lineNumber, TOK op, Arguments arguments,
			Expression aggr, Statement body) {
		super(filename, lineNumber);
		this.op = op;
		this.arguments = arguments;
		this.aggr = this.sourceAggr = aggr;
		this.body = this.sourceBody = body;
	}

	@Override
	public void accept0(IASTVisitor visitor) {
		boolean children = visitor.visit(this);
		if (children) {
			TreeVisitor.acceptChildren(visitor, arguments);
			TreeVisitor.acceptChildren(visitor, sourceAggr);
			TreeVisitor.acceptChildren(visitor, sourceBody);
		}
		visitor.endVisit(this);
	}

	@Override
	public int blockExit(SemanticContext context) {
		int result = BEfallthru;

		if (aggr.canThrow(context)) {
			result |= BEthrow;
		}

		if (body != null) {
			result |= body.blockExit(context) & ~(BEbreak | BEcontinue);
		}
		return result;
	}

	@Override
	public boolean comeFrom() {
		if (body != null) {
			return body.comeFrom();
		}
		return false;
	}

	@Override
	public int getNodeType() {
		return FOREACH_STATEMENT;
	}

	@Override
	public boolean hasBreak() {
		return true;
	}

	@Override
	public boolean hasContinue() {
		return true;
	}

	@Override
	public Expression interpret(InterState istate, SemanticContext context) {
		if (istate.start == this) {
			istate.start = null;
		}
		if (istate.start != null) {
			return null;
		}

		Expression e = null;
		Expression eaggr;

		if (value.isOut() || value.isRef()) {
			return EXP_CANT_INTERPRET;
		}

		eaggr = aggr.interpret(istate, context);
		if (eaggr == EXP_CANT_INTERPRET) {
			return EXP_CANT_INTERPRET;
		}

		Expression dim = ArrayLength.call(Type.tsize_t, eaggr, context);
		if (dim == EXP_CANT_INTERPRET) {
			return EXP_CANT_INTERPRET;
		}

		Expression keysave = key != null ? key.value : null;
		Expression valuesave = value.value;

		integer_t d = dim.toUInteger(context);
		integer_t index;

		if (op == TOKforeach) {
			for (index = integer_t.ZERO; index.compareTo(d) < 0; index = index
					.add(1)) {
				Expression ekey = new IntegerExp(filename, lineNumber, index, Type.tsize_t);
				if (key != null) {
					key.value = ekey;
				}
				e = Index.call(value.type, eaggr, ekey, context);
				if (e == EXP_CANT_INTERPRET) {
					break;
				}
				value.value = e;

				e = body != null ? body.interpret(istate, context) : null;
				if (e == EXP_CANT_INTERPRET) {
					break;
				}
				if (e == EXP_BREAK_INTERPRET) {
					e = null;
					break;
				}
				if (e == EXP_CONTINUE_INTERPRET) {
					e = null;
				} else if (e != null) {
					break;
				}
			}
		} else // TOKforeach_reverse
		{
			for (index = d; !(index = index.subtract(1)).equals(0);) {
				Expression ekey = new IntegerExp(filename, lineNumber, index, Type.tsize_t);
				if (key != null) {
					key.value = ekey;
				}
				e = Index.call(value.type, eaggr, ekey, context);
				if (e == EXP_CANT_INTERPRET) {
					break;
				}
				value.value = e;

				e = body != null ? body.interpret(istate, context) : null;
				if (e == EXP_CANT_INTERPRET) {
					break;
				}
				if (e == EXP_BREAK_INTERPRET) {
					e = null;
					break;
				}
				if (e == EXP_CONTINUE_INTERPRET) {
					e = null;
				} else if (e != null) {
					break;
				}
			}
		}
		value.value = valuesave;
		if (key != null) {
			key.value = keysave;
		}
		return e;
	}

	@Override
	public Statement semantic(Scope sc, SemanticContext context) {
		// Descent: for local variable binding signature
		sc.numberForLocalVariables++;

		ScopeDsymbol sym;
		Statement temp;
		Statement s = this;
		int dim = arguments.size();
		int i;
		TypeAArray taa = null;

		Type tn = null;
		Type tnv = null;

		func = sc.func;
		if (func.fes != null) {
			func = func.fes.func;
		}

		aggr = aggr.semantic(sc, context);
		aggr = resolveProperties(sc, aggr, context);
	    aggr = aggr.optimize(WANTvalue, context);

		if (context.isD2()) {
			aggr = aggr.optimize(WANTvalue, context);
		}

		if (aggr.type == null) {
			if (context.acceptsErrors()) {
				context.acceptProblem(Problem.newSemanticTypeError(IProblem.InvalidForeachAggregate, this, new String[] { aggr.toChars(context) }));
			}
			return this;
		}

		inferApplyArgTypes(op, arguments, aggr, context);

		/*
		 * Check for inference errors
		 */
		if (dim != arguments.size()) {
			if (context.acceptsErrors()) {
				context.acceptProblem(Problem.newSemanticTypeError(IProblem.CannotUniquelyInferForeachArgumentTypes, this));
			}
			return this;
		}

		Type tab = aggr.type.toBasetype(context);

		if (tab.ty == Ttuple) // don't generate new scope for tuple loops
		{
			if (dim < 1 || dim > 2) {
				if (context.acceptsErrors()) {
					context.acceptProblem(Problem.newSemanticTypeError(IProblem.OnlyOneValueOrTwoKeyValueArgumentsForTupleForeach, this));
				}
				return s;
			}

			TypeTuple tuple = (TypeTuple) tab;
			Statements statements = new Statements();
			int n = 0;
			TupleExp te = null;
			if (aggr.op == TOKtuple) {
				te = (TupleExp) aggr;
				n = te.exps.size();
			} else if (aggr.op == TOKtype) {
				n = Argument.dim(tuple.arguments, context);
			} else {
				Assert.isTrue(false);
			}
			for (int j = 0; j < n; j++) {
				int k = (op == TOKforeach) ? j : n - 1 - j;
				Expression e = null;
				Type t = null;
				if (te != null) {
					e = te.exps.get(k);
				} else {
					t = Argument.getNth(tuple.arguments, k, context).type;
				}
				Argument arg = arguments.get(0);
				Statements st = new Statements();

				if (dim == 2) { // Declare key
					if ((arg.storageClass & (STCout | STCref | STClazy)) != 0) {
						if (context.acceptsErrors()) {
							context.acceptProblem(Problem.newSemanticTypeError(IProblem.NoStorageClassForSymbol, this, new String[] { arg.ident.toChars() }));
						}
					}
					TY keyty = arg.type.ty;
					if (keyty != Tint32 && keyty != Tuns32) {
						if (context.global.params.isX86_64) {
							if (keyty != Tint64 && keyty != Tuns64) {
								if (context.acceptsErrors()) {
									context.acceptProblem(Problem.newSemanticTypeError(IProblem.ForeachKeyTypeMustBeIntOrUintLongOrUlong, arg, new String[] { arg.type.toChars(context) }));
								}
							}
						} else {
							if (context.acceptsErrors()) {
								context.acceptProblem(Problem.newSemanticTypeError(IProblem.ForeachKeyTypeMustBeIntOrUint, arg, new String[] { arg.type.toChars(context) }));
							}
						}
					}
					Initializer ie = new ExpInitializer(filename, lineNumber, new IntegerExp(
							filename, lineNumber, k));
					VarDeclaration var = new VarDeclaration(filename, lineNumber, arg.type,
							arg.ident, ie);

					// Descent: for binding resolution
					if (arg.ident != null) {
						var.copySourceRange(arg.ident);
					}
					arg.var = var;

					if (context.isD2()) {
						var.storage_class |= STCmanifest;
					} else {
						var.storage_class |= STCconst;
					}

					DeclarationExp de = new DeclarationExp(filename, lineNumber, var);
					st.add(new ExpStatement(filename, lineNumber, de));
					arg = arguments.get(1); // value
				}
				// Declare value
				if ((arg.storageClass & (STCout | STCref | STClazy)) != 0) {
					if (context.acceptsErrors()) {
						context.acceptProblem(Problem.newSemanticTypeError(IProblem.NoStorageClassForSymbol, this, new String[] { arg.ident.toChars() }));
					}
				}
				Dsymbol var = null;
				if (te != null) {
					Type tb = e.type.toBasetype(context);

					boolean condition;
					if (context.isD2()) {
						condition = (tb.ty == Tfunction || tb.ty == Tsarray) && e.op == TOKvar;
					} else {
						condition = tb.ty == Tfunction && e.op == TOKvar;
					}

					if (condition) {
						VarExp ve = (VarExp) e;
						var = new AliasDeclaration(filename, lineNumber, arg.ident, ve.var);
					} else {
						arg.type = e.type;
						Initializer ie = new ExpInitializer(null, 0, e);
						VarDeclaration v = new VarDeclaration(filename, lineNumber, arg.type,
								arg.ident, ie);
						if (e.isConst()) {
							v.storage_class |= STCconst;
						} else if (context.isD2()) {
							v.storage_class |= STCfinal;
						}
						var = v;
					}
				} else {
					var = new AliasDeclaration(filename, lineNumber, arg.ident, t);
				}

				// Descent: for binding resolution
				if (arg.ident != null) {
					var.copySourceRange(arg.ident);
				}
				arg.var = var;

				DeclarationExp de = new DeclarationExp(filename, lineNumber, var);
				st.add(new ExpStatement(filename, lineNumber, de));

				st.add(body.syntaxCopy(context));
				s = new CompoundStatement(filename, lineNumber, st);
				s = new ScopeStatement(filename, lineNumber, s);
				statements.add(s);
			}

			s = new UnrolledLoopStatement(filename, lineNumber, statements);
			s = s.semantic(sc, context);
			return s;
		}

		if (context.isD1()) {
			for (i = 0; i < dim; i++) {
				Argument arg = arguments.get(i);
				if (arg.type == null) {
					if (context.acceptsErrors()) {
						context.acceptProblem(Problem.newSemanticTypeErrorLoc(IProblem.CannotInferTypeForSymbol, arg, new String[] { arg.ident.toChars() }));
					}
					return this;
				}
			}
		}

		sym = new ScopeDsymbol();
		sym.parent = sc.scopesym;
		sc = sc.push(sym);

		sc.noctor++;

		switch (tab.ty) {
		case Tarray:
		case Tsarray:
			if (context.isD2()) {
			    if (!checkForArgTypes(context))
					return this;
			}

			if (dim < 1 || dim > 2) {
				if (context.acceptsErrors()) {
					context.acceptProblem(Problem.newSemanticTypeError(IProblem.OnlyOneOrTwoArgumentsForArrayForeach, this));
				}
				break;
			}

			/*
			 * Look for special case of parsing char types out of char type
			 * array.
			 */
			tn = tab.nextOf().toBasetype(context);
			if (tn.ty == Tchar || tn.ty == Twchar || tn.ty == Tdchar) {
				Argument arg;

				i = (dim == 1) ? 0 : 1; // index of value
				arg = arguments.get(i);
				arg.type = arg.type.semantic(filename, lineNumber, sc, context);
				tnv = arg.type.toBasetype(context);
				if (tnv.ty != tn.ty
						&& (tnv.ty == Tchar || tnv.ty == Twchar || tnv.ty == Tdchar)) {
					if ((arg.storageClass & STCref) != 0) {
						if (context.acceptsErrors()) {
							context.acceptProblem(Problem.newSemanticTypeError(IProblem.ForeachValueOfUTFConversionCannotBeInout, this));
						}
					}
					if (dim == 2) {
						arg = arguments.get(0);
						if ((arg.storageClass & STCref) != 0) {
							if (context.acceptsErrors()) {
								context.acceptProblem(Problem.newSemanticTypeError(IProblem.ForeachKeyCannotBeInout, this));
							}
						}
					}
					// goto Lapply;
					Statement[] ps = { null };
					temp = semantic_Lapply(sc, context, dim, ps, tab, taa, tn, tnv);
					if (temp != null)
						return this;

					s = ps[0];
					break;
				}
			}

			for (i = 0; i < dim; i++) { // Declare args
				Argument arg = arguments.get(i);
				VarDeclaration var;

				var = new VarDeclaration(filename, lineNumber, arg.type, arg.ident, null);
				var.copySourceRange(arg);
				var.storage_class |= STCforeach;

				if (context.isD2()) {
					var.storage_class |= arg.storageClass & (STCin | STCout | STCref | STC_TYPECTOR);
				} else {
					var.storage_class |= arg.storageClass & (STCin | STCout | STCref);
				}

				// Descent: for binding resolution
				if (arg.ident != null) {
					var.copySourceRange(arg.ident);
				}
				arg.var = var;

				if (context.isD1()) {
					DeclarationExp de = new DeclarationExp(filename, lineNumber, var);
					de.semantic(sc, context);

					if (dim == 2 && i == 0)
						key = var;
					else
						value = var;
				} else {
					if (dim == 2 && i == 0) {
						key = var;
					} else {
						value = var;
						/*
						 * Reference to immutable data should be marked as const
						 */
						if ((var.storage_class & STCref) != 0 && !tn.isMutable()) {
							var.storage_class |= STCconst;
						}
					}

					DeclarationExp de = new DeclarationExp(filename, lineNumber, var);
					de.semantic(sc, context);
				}
			}

			sc.sbreak = this;
			sc.scontinue = this;
			body = body.semantic(sc, context);

			boolean condition;
			if (context.isD2()) {
				condition = tab.nextOf().implicitConvTo(value.type, context).ordinal() < MATCHconst.ordinal();
			} else {
				condition = !value.type.equals(tab.nextOf());
			}

			if (condition) {
				if (aggr.op == TOKstring) {
					aggr = aggr.implicitCastTo(sc, value.type.arrayOf(context),
							context);
				} else {
					if (context.acceptsErrors()) {
						context.acceptProblem(Problem.newSemanticTypeError(IProblem.ForeachTargetIsNotAnArrayOf, sourceAggr, new String[] { tab.toChars(context), value.type.toChars(context) }));
					}
				}
			}

			if (key != null
					&& ((key.type.ty != Tint32 && key.type.ty != Tuns32))) {
				if (context.global.params.isX86_64) {
					if (key.type.ty != Tint64 && key.type.ty != Tuns64) {
						if (context.acceptsErrors()) {
							context.acceptProblem(Problem.newSemanticTypeError(IProblem.ForeachKeyTypeMustBeIntOrUintLongOrUlong, key, new String[] { key.type.toChars(context) }));
						}
					}
				} else {
					if (context.acceptsErrors()) {
						context.acceptProblem(Problem.newSemanticTypeError(IProblem.ForeachKeyTypeMustBeIntOrUint, key, new String[] { key.type.toChars(context) }));
					}
				}
			}

			if (key != null && (key.storage_class & (STCout | STCref)) != 0) {
				if (context.acceptsErrors()) {
					context.acceptProblem(Problem.newSemanticTypeError(IProblem.ForeachKeyCannotBeOutOrRef, key));
				}
			}
			break;

		case Taarray:
			if (context.isD2()) {
			    if (!checkForArgTypes(context))
					return this;
			}

			taa = (TypeAArray) tab;
			if (dim < 1 || dim > 2) {
				if (context.acceptsErrors()) {
					context.acceptProblem(Problem.newSemanticTypeError(IProblem.OnlyOneOrTwoArgumentsForAssociativeArrayForeach, this));
				}
				break;
			}
			if (op == TOKforeach_reverse) {
				if (context.acceptsErrors()) {
					context.acceptProblem(Problem.newSemanticTypeError(IProblem.NoReverseIterationOnAssociativeArrays, this));
				}
			}
			// goto Lapply
			Statement[] ps = { null };
			temp = semantic_Lapply(sc, context, dim, ps, tab, taa, tn, tnv);
			if (temp != null)
				return this;

			s = ps[0];
			break;

		case Tclass:
		case Tstruct:
		case Tdelegate:
			// Lapply:
		{
			if (context.isD2() && (tab.ty == Tclass || tab.ty == Tstruct)) {
				/*
				 * Look for range iteration, i.e. the properties .empty, .next,
				 * .retreat, .head and .rear foreach (e; range) { ... }
				 * translates to: for (auto __r = range; !__r.empty; __r.next) {
				 * auto e = __r.head; ... }
				 */
				if (dim != 1) { // only one argument allowed with ranges
					// goto Lapply;
				}
				AggregateDeclaration ad = (tab.ty == Tclass) ? (AggregateDeclaration) ((TypeClass) tab).sym
						: (AggregateDeclaration) ((TypeStruct) tab).sym;
				char[] idhead;
				char[] idnext;
				if (op == TOKforeach) {
					idhead = Id.Fhead;
					idnext = Id.Fnext;
				} else {
					idhead = Id.Ftoe;
					idnext = Id.Fretreat;
				}
				Dsymbol shead = search_function(ad, idhead, context);
				if (null == shead) {
					// goto Lapply;
				}

				/*
				 * Generate a temporary __r and initialize it with the
				 * aggregate.
				 */
				IdentifierExp id = context.generateId("__r");
			    aggr = aggr.semantic(sc, context);
			    Expression rinit = new SliceExp(filename, lineNumber, aggr, null, null);
			    rinit = rinit.trySemantic(sc, context);
			    if (null == rinit)			// if application of [] failed
				rinit = aggr;
			    VarDeclaration r = new VarDeclaration(filename, lineNumber, null, id, new ExpInitializer(filename, lineNumber, rinit));
				Statement init = new DeclarationStatement(filename, lineNumber, r);

				// !__r.empty
				Expression e = new VarExp(filename, lineNumber, r);
				e = new DotIdExp(filename, lineNumber, e, Id.Fempty);
				Expression condition2 = new NotExp(filename, lineNumber, e);

				// __r.next
				e = new VarExp(filename, lineNumber, r);
				Expression increment = new DotIdExp(filename, lineNumber, e, idnext);

				/*
				 * Declaration statement for e: auto e = __r.idhead;
				 */
				e = new VarExp(filename, lineNumber, r);
				Expression einit = new DotIdExp(filename, lineNumber, e, idhead);
				if (context.isD1()) {
					einit = einit.semantic(sc, context);
				}
				Argument arg = (Argument) arguments.get(0);
				VarDeclaration ve = new VarDeclaration(filename, lineNumber, arg.type,
						arg.ident, new ExpInitializer(filename, lineNumber, einit));
				ve.storage_class |= STCforeach;
				if (context.isD1()) {
					ve.storage_class |= arg.storageClass
							& (STCin | STCout | STCref | STCconst | STCinvariant);
				} else {
					ve.storage_class |= arg.storageClass
							& (STCin | STCout | STCref | STC_TYPECTOR);
				}

				DeclarationExp de = new DeclarationExp(filename, lineNumber, ve);

				Statement body = new CompoundStatement(filename, lineNumber,
						new DeclarationStatement(filename, lineNumber, de), this.body);

				s = new ForStatement(filename, lineNumber, init, condition2, increment, body);
				s = s.semantic(sc, context);
				break;
			}

			Statement[] pointer_s = { null };
			temp = semantic_Lapply(sc, context, dim, pointer_s, tab, taa, tn, tnv);
			if (temp != null)
				return this;

			s = pointer_s[0];
			break;
		}

		default:
			if (context.acceptsErrors()) {
				context.acceptProblem(Problem.newSemanticTypeError(
						IProblem.NotAnAggregateType, sourceAggr, new String[] { aggr.type.toString() }));
			}
			if (context.isD2()) {
				// error recovery
				s = null;
			}
			break;
		}
		sc.noctor--;
		sc.pop();
		return s;
	}

	private Statement semantic_Lapply(Scope sc, SemanticContext context, int dim,
			Statement[] s, Type tab, TypeAArray taa, Type tn, Type tnv) {
		if (!checkForArgTypes(context)) {
			body = body.semantic(sc, context);
			return this;
		}

		FuncDeclaration fdapply;
		Arguments args;
		Expression ec;
		Expression e;
		FuncLiteralDeclaration fld;
		Argument a;
		Type t;
		Expression flde;
		IdentifierExp id;
		Type tret;

		tret = func.type.nextOf();

		// Need a variable to hold value from any return statements in body.
		if (sc.func.vresult == null && tret != null && !same(tret, Type.tvoid, context)) {
			VarDeclaration v;

			v = new VarDeclaration(filename, lineNumber, tret, Id.result, null);
			v.noauto = true;
			v.semantic(sc, context);
			if (sc.insert(v) == null) {
				Assert.isTrue(false);
			}
			v.parent = sc.func;
			sc.func.vresult = v;
		}

		/*
		 * Turn body into the function literal: int delegate(ref T arg) {
		 * body }
		 */
		args = new Arguments(dim);
		for (int i = 0; i < dim; i++) {
			Argument arg = arguments.get(i);

			arg.type = arg.type.semantic(filename, lineNumber, sc, context);
			if ((arg.storageClass & STCref) != 0) {
				id = arg.ident;
			} else { // Make a copy of the inout argument so it isn't
				// a reference.
				VarDeclaration v;
				Initializer ie;
				id = context.uniqueId("__applyArg", i);

				ie = new ExpInitializer(filename, lineNumber, id);
				v = new VarDeclaration(filename, lineNumber, arg.type, arg.ident, ie);

				// Descent: for binding resolution
				if (arg.ident != null) {
					v.copySourceRange(arg.ident);
				}
				arg.var = v;

				s[0] = new DeclarationStatement(filename, lineNumber, v);
				body = new CompoundStatement(filename, lineNumber, s[0], body);
			}
			a = new Argument(STCref, arg.type, id, null);
			args.add(a);
		}
		t = new TypeFunction(args, Type.tint32, 0, LINK.LINKd);
		fld = new FuncLiteralDeclaration(filename, lineNumber, t, TOKdelegate, this);
		fld.fbody = body;
		flde = new FuncExp(filename, lineNumber, fld);
		flde = flde.semantic(sc, context);

		if (context.isD2()) {
			fld.tookAddressOf = 0;
		}

		// Resolve any forward referenced goto's
		if (gotos != null) {
			for (int j = 0; j < gotos.size(); j++) {
				CompoundStatement cs = (CompoundStatement) gotos.get(j);
				GotoStatement gs = (GotoStatement) cs.statements.get(0);

				if (gs.label.statement == null) { // 'Promote' it to this scope, and replace with a return
					cases.add(gs);
					s[0] = new ReturnStatement(filename, lineNumber, new IntegerExp(filename, lineNumber, cases
							.size() + 1));
					cs.statements.set(0, s[0]);
				}
			}
		}

		if (tab.ty == Taarray) {
			// Check types
			Argument arg = arguments.get(0);
			if (dim == 2) {
				if ((arg.storageClass & STCref) != 0) {
					if (context.acceptsErrors()) {
						context.acceptProblem(Problem.newSemanticTypeError(
								IProblem.ForeachIndexCannotBeRef, arg));
					}
				}
				if (!arg.type.equals(taa.index)) {
					if (context.acceptsErrors()) {
						context.acceptProblem(Problem.newSemanticTypeError(
								IProblem.ForeachIndexMustBeType, arg, new String[] { taa.index.toChars(context), arg.type.toChars(context) }));
					}
				}
				arg = arguments.get(1);
			}
			if (!arg.type.equals(taa.next)) {
				if (context.acceptsErrors()) {
					context.acceptProblem(Problem.newSemanticTypeError(
							IProblem.ForeachValueMustBeType, arg, new String[] { taa.next.toChars(context), arg.type.toChars(context) }));
				}
			}

			/*
			 * Call: _aaApply(aggr, keysize, flde)
			 */
			if (dim == 2) {
				fdapply = context.genCfunc(Type.tindex, _aaApply2);
			} else {
				fdapply = context.genCfunc(Type.tindex, _aaApply);
			}
			ec = new VarExp(filename, lineNumber, fdapply);
			Expressions exps = new Expressions(3);
			exps.add(aggr);
			int keysize;

			if (context.isD2()) {
				keysize = taa.index.size(filename, lineNumber, context);
			} else {
				keysize = taa.key.size(filename, lineNumber, context);
			}
			keysize = (keysize + Type.PTRSIZE - 1) & ~(Type.PTRSIZE - 1);
			exps.add(new IntegerExp(filename, lineNumber, keysize, Type.tsize_t));
			exps.add(flde);
			e = new CallExp(filename, lineNumber, ec, exps);
			e.type = Type.tindex; // don't run semantic() on e
		} else if (tab.ty == Tarray || tab.ty == Tsarray) {
			/*
			 * Call: _aApply(aggr, flde)
			 */
			int flag = 0;

			switch (tn.ty) {
			case Tchar:
				flag = 0;
				break;
			case Twchar:
				flag = 3;
				break;
			case Tdchar:
				flag = 6;
				break;
			default:
				Assert.isTrue(false);
			}
			switch (tnv.ty) {
			case Tchar:
				flag += 0;
				break;
			case Twchar:
				flag += 1;
				break;
			case Tdchar:
				flag += 2;
				break;
			default:
				Assert.isTrue(false);
			}

			String r = (op == TOKforeach_reverse) ? "R" : "";
			String fdname = "_aApply" + r + 2 + "." + fntab[flag] + dim;

			fdapply = context.genCfunc(Type.tindex, fdname.toCharArray());

			ec = new VarExp(filename, lineNumber, fdapply);
			Expressions exps = new Expressions(2);
			if (tab.ty == Tsarray) {
				aggr = aggr.castTo(sc, tn.arrayOf(context), context);
			}
			exps.add(aggr);
			exps.add(flde);
			e = new CallExp(filename, lineNumber, ec, exps);
			e.type = Type.tindex; // don't run semantic() on e
		} else if (tab.ty == Tdelegate) {
			/*
			 * Call: aggr(flde)
			 */
			Expressions exps = new Expressions(2);
			exps.add(flde);
			e = new CallExp(filename, lineNumber, aggr, exps);
			e = e.semantic(sc, context);
			if (e.type.singleton != Type.tint32) {
				if (context.acceptsErrors()) {
					context.acceptProblem(Problem.newSemanticTypeError(
							IProblem.OpApplyFunctionMustReturnAnInt, this, new String[] { tab.toChars(context) }));
				}
			}
		} else {
			Expressions exps = new Expressions(2);
			char[] idapply = null;
			Dsymbol sapply = null;

			if (context.isD2()) {
				assert(tab.ty == Tstruct || tab.ty == Tclass);
				idapply = (op == TOKforeach_reverse)
						? Id.applyReverse : Id.apply;
				sapply = search_function((AggregateDeclaration)tab.toDsymbol(sc, context), idapply, context);
			}

			/*
			 * Call: aggr.apply(flde)
			 */
			if (context.isD1()) {
				ec = new DotIdExp(filename, lineNumber, aggr, (op == TOKforeach_reverse) ? Id.applyReverse : Id.apply);
				exps.add(flde);
			} else {
			    ec = new DotIdExp(filename, lineNumber, aggr, idapply);
			    exps.add(flde);
			}

			// TODO: kludge to not lose source ranges, fixme
			int oldSourceAggrStart = sourceAggr.start;
			int oldSourceAggrLength = sourceAggr.length;

			e = new CallExp(filename, lineNumber, ec, exps);
			e = e.semantic(sc, context);

			sourceAggr.start = oldSourceAggrStart;
			sourceAggr.length = oldSourceAggrLength;

			if (!same(e.type, Type.tint32, context)) {
				if (context.acceptsErrors()) {
					context.acceptProblem(Problem.newSemanticTypeError(
							IProblem.OpApplyFunctionMustReturnAnInt, sourceAggr, new String[] { tab.toChars(context) }));
				}
			}
		}

		if (size(cases) == 0) {
			// Easy case, a clean exit from the loop
			s[0] = new ExpStatement(filename, lineNumber, e);
		} else { // Construct a switch statement around the return value
			// of the apply function.
			Statements a2 = new Statements(cases.size() + 1);

			// default: break; takes care of cases 0 and 1
			s[0] = new BreakStatement(filename, lineNumber, null);
			s[0] = new DefaultStatement(filename, lineNumber, s[0]);
			a2.add(s[0]);

			// cases 2...
			for (int j = 0; j < cases.size(); j++) {
				s[0] = (Statement) cases.get(j);
				s[0] = new CaseStatement(filename, lineNumber, new IntegerExp(filename, lineNumber, j + 2), s[0]);
				a2.add(s[0]);
			}

			s[0] = new CompoundStatement(filename, lineNumber, a2);
			s[0] = new SwitchStatement(filename, lineNumber, e, s[0], false);
			s[0] = s[0].semantic(sc, context);
		}

		return null;
	}

	public boolean checkForArgTypes(SemanticContext context) {
		boolean result = true;

		for (int i = 0; i < size(arguments); i++) {
			Argument arg = (Argument) arguments.get(i);
			if (null == arg.type) {
				if (context.acceptsErrors()) {
					context.acceptProblem(Problem.newSemanticTypeError(IProblem.CannotInferTypeForSymbol, arg, arg.ident.toChars(context)));
				}
				arg.type = Type.terror;
				result = false;
			}
		}
		return result;
	}


	@Override
	public Statement syntaxCopy(SemanticContext context) {
		Arguments args = Argument.arraySyntaxCopy(arguments, context);
		Expression exp = aggr.syntaxCopy(context);
		ForeachStatement s = context.newForeachStatement(filename, lineNumber, op, args, exp,
				body != null ? body.syntaxCopy(context) : null);
		s.copySourceRange(this);
		return s;
	}

	@Override
	public void toCBuffer(OutBuffer buf, HdrGenState hgs,
			SemanticContext context) {
		buf.writestring(op.toString());
		buf.writestring(" (");
		for (int i = 0; i < arguments.size(); i++) {
			Argument a = arguments.get(i);
			if (i != 0) {
				buf.writestring(", ");
			}
			if ((a.storageClass & STCref) != 0) {
				buf
						.writestring((context.global.params.Dversion == 1) ? "inout "
								: "ref ");
			}
			if (a.type != null) {
				a.type.toCBuffer(buf, a.ident, hgs, context);
			} else {
				buf.writestring(a.ident.toChars());
			}
		}
		buf.writestring("; ");
		aggr.toCBuffer(buf, hgs, context);
		buf.writebyte(')');
		buf.writenl();
		buf.writebyte('{');
		buf.writenl();
		if (body != null) {
			body.toCBuffer(buf, hgs, context);
		}
		buf.writebyte('}');
		buf.writenl();
	}

	@Override
	public boolean usesEH(SemanticContext context) {
		return body.usesEH(context);
	}

}
