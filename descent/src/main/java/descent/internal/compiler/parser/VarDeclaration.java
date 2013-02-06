package descent.internal.compiler.parser;

import static descent.internal.compiler.parser.PROT.PROTexport;
import static descent.internal.compiler.parser.STC.STC_TYPECTOR;
import static descent.internal.compiler.parser.STC.STCauto;
import static descent.internal.compiler.parser.STC.STCconst;
import static descent.internal.compiler.parser.STC.STCctorinit;
import static descent.internal.compiler.parser.STC.STCextern;
import static descent.internal.compiler.parser.STC.STCfield;
import static descent.internal.compiler.parser.STC.STCfinal;
import static descent.internal.compiler.parser.STC.STCforeach;
import static descent.internal.compiler.parser.STC.STCgshared;
import static descent.internal.compiler.parser.STC.STCimmutable;
import static descent.internal.compiler.parser.STC.STCin;
import static descent.internal.compiler.parser.STC.STCinit;
import static descent.internal.compiler.parser.STC.STCinvariant;
import static descent.internal.compiler.parser.STC.STClazy;
import static descent.internal.compiler.parser.STC.STCmanifest;
import static descent.internal.compiler.parser.STC.STCnodtor;
import static descent.internal.compiler.parser.STC.STCnothrow;
import static descent.internal.compiler.parser.STC.STCout;
import static descent.internal.compiler.parser.STC.STCparameter;
import static descent.internal.compiler.parser.STC.STCpure;
import static descent.internal.compiler.parser.STC.STCref;
import static descent.internal.compiler.parser.STC.STCscope;
import static descent.internal.compiler.parser.STC.STCshared;
import static descent.internal.compiler.parser.STC.STCstatic;
import static descent.internal.compiler.parser.STC.STCtemplateparameter;
import static descent.internal.compiler.parser.STC.STCtls;
import static descent.internal.compiler.parser.STC.STCundefined;
import static descent.internal.compiler.parser.TOK.TOKblit;
import static descent.internal.compiler.parser.TOK.TOKcall;
import static descent.internal.compiler.parser.TOK.TOKconstruct;
import static descent.internal.compiler.parser.TOK.TOKdotvar;
import static descent.internal.compiler.parser.TOK.TOKint64;
import static descent.internal.compiler.parser.TOK.TOKstar;
import static descent.internal.compiler.parser.TOK.TOKstring;
import static descent.internal.compiler.parser.TY.Taarray;
import static descent.internal.compiler.parser.TY.Tsarray;
import static descent.internal.compiler.parser.TY.Tstruct;
import melnorme.utilbox.tree.TreeVisitor;

import melnorme.utilbox.core.Assert;

import descent.core.IField__Marker;
import descent.core.Signature;
import descent.core.compiler.IProblem;
import descent.internal.compiler.parser.ast.IASTVisitor;


public class VarDeclaration extends Declaration {

	public boolean first = true; // is this the first declaration in a multi
	public VarDeclaration next;

	// declaration?
	public Initializer init, sourceInit;
	public Dsymbol aliassym; // if redone as alias to another symbol
	public Type htype;
	public Initializer hinit;;
	public int offset;
	public boolean noauto; // no auto semantics
	public FuncDeclarations nestedrefs;// referenced by these lexically nested functions
	public int onstack; // 1: it has been allocated on the stack
		// 2: on stack, run destructor anyway
	public int canassign;		// it can be assigned to
	public int nestedref;
	public boolean ctorinit;
	public Expression value; // when interpreting, this is the value
							// (NULL if value not determinable)
	public Object csym;
	public Object isym;
	public Scope scope;		// !=NULL means context to use

	private IField__Marker javaElement;

	public VarDeclaration(char[] filename, int lineNumber, Type type, char[] ident, Initializer init) {
		this(filename, lineNumber, type, new IdentifierExp(ident), init);
	}

	public VarDeclaration(char[] filename, int lineNumber, Type type, IdentifierExp id, Initializer init) {
		super(id);

//		Assert.isTrue(type != null || init != null);

		this.filename = filename;
		this.lineNumber = lineNumber;
		this.type = type;
		this.sourceType = type;
		this.init = init;
		this.sourceInit = init;
		this.htype = null;
		this.hinit = null;
		this.offset = 0;
		this.noauto = false;
		this.nestedref = 0;
		this.ctorinit = false;
		this.aliassym = null;
		this.onstack = 0;
		this.value = null;
	}

	@Override
	public void accept0(IASTVisitor visitor) {
		boolean children = visitor.visit(this);
		if (children) {
			TreeVisitor.acceptChildren(visitor, sourceType);
			TreeVisitor.acceptChildren(visitor, ident);
			TreeVisitor.acceptChildren(visitor, sourceInit);
		}
		visitor.endVisit(this);
	}

	public Expression callAutoDtor(Scope sc, SemanticContext context) {
		Expression e = null;

		if (!context.isD1()) {
			if (noauto || (storage_class & STCnodtor) != 0)
				return null;

			// Destructors for structs and arrays of structs
			boolean array = false;
			Type tv = type.toBasetype(context);
			while (tv.ty == Tsarray) {
//				TypeSArray ta = (TypeSArray) tv;
				array = true;
				tv = tv.nextOf().toBasetype(context);
			}
			if (tv.ty == Tstruct) {
				TypeStruct ts = (TypeStruct) tv;
				StructDeclaration sd = ts.sym;
				if (sd.dtor != null) {
					if (array) {
						// Typeinfo.destroy(cast(void*)&v);
						Expression ea = new SymOffExp(filename, lineNumber,
								this, integer_t.ZERO, false, context);
						ea = new CastExp(filename, lineNumber, ea,
								context.Type_tvoidptr);
						Expressions args = new Expressions(1);
						args.add(ea);

						Expression et = type.getTypeInfo(sc, context);
						et = new DotIdExp(filename, lineNumber, et, Id.destroy);
						e = new CallExp(filename, lineNumber, et, args);
					} else {
						e = new VarExp(filename, lineNumber, this);
						e = new DotVarExp(filename, lineNumber, e, sd.dtor, false);
						e = new CallExp(filename, lineNumber, e);
					}
					return e;
				}
			}
		}

		boolean condition;
		if (context.isD1()) {
			condition = (storage_class & (STCauto | STCscope)) != 0 && !noauto;
		} else {
		    condition = (storage_class & (STCauto | STCscope)) != 0;
		}

		if (condition) {
			for (ClassDeclaration cd = type.isClassHandle(); cd != null; cd = cd.baseClass) {
				/*
				 * We can do better if there's a way with onstack classes to
				 * determine if there's no way the monitor could be set.
				 */
				if (true || onstack != 0 || cd.dtors.size() > 0) // if any
				// destructors
				{
					// delete this;
					Expression ec;

					ec = new VarExp(filename, lineNumber, this);
					e = new DeleteExp(filename, lineNumber, ec);
					e.type = Type.tvoid;
					break;
				}
			}
		}
		return e;
	}

	@Override
	public void checkCtorConstInit(SemanticContext context) {
		if (context.isD1()) {
			if (!ctorinit && isCtorinit() && (storage_class & STCfield) == 0) {
				if (context.acceptsErrors()) {
					context.acceptProblem(Problem.newSemanticTypeError(
							IProblem.MissingInitializerInStaticConstructorForConstVariable, this));
				}
			}
		} else {
			/* doesn't work if more than one static ctor */
		}
	}

	public void checkNestedReference(Scope sc, char[] filename, int lineNumber, SemanticContext context) {
		if (context.isD1()) {
			if (parent != null && !this.isDataseg(context) && this.parent != sc.parent) {
				FuncDeclaration fdv = this.toParent().isFuncDeclaration();
				FuncDeclaration fdthis = (FuncDeclaration) sc.parent.isFuncDeclaration();

				if (fdv != null && fdthis != null) {
					if (filename != null)
						fdthis.getLevel(filename, lineNumber, fdv, context);
					this.nestedref(1);
					fdv.nestedFrameRef(true);
				}
			}
		} else {
			if (parent != null && !isDataseg(context) && parent != sc.parent
					&& 0 == (storage_class & STCmanifest)) {
				// The function that this variable is in
				FuncDeclaration fdv = toParent().isFuncDeclaration();
				// The current function
				FuncDeclaration fdthis = sc.parent.isFuncDeclaration();

				if (fdv != null && fdthis != null && fdv != fdthis) {
					if (filename != null) {
						fdthis.getLevel(filename, lineNumber, fdv, context);
					}

					boolean gotoL1 = false;

					for (int i = 0; i < size(nestedrefs); i++) {
						FuncDeclaration f = (FuncDeclaration) nestedrefs.get(i);
						if (f == fdthis) {
							// goto L1;
							gotoL1 = true;
							break;
						}
					}

					if (!gotoL1) {
						if (nestedrefs == null) {
							nestedrefs = new FuncDeclarations(3);
						}
						nestedrefs.add(fdthis);
					}
					// L1: ;

					boolean gotoL2 = false;

					for (int i = 0; i < size(fdv.closureVars); i++) {
						Dsymbol s = (Dsymbol) fdv.closureVars.get(i);
						if (s == this) {
							// goto L2;
							gotoL2 = true;
							break;
						}
					}

					if (!gotoL2) {
						if (fdv.closureVars == null) {
							fdv.closureVars = new Dsymbols(3);
						}
						fdv.closureVars.add(this);
					}
					// L2: ;
				}
			}
		}
	}

	/*******************************************
	 * If variable has a constant expression initializer, get it.
	 * Otherwise, return NULL.
	 */
	public Expression getConstInitializer(SemanticContext context) {
		if ((isConst() || isInvariant(context) || (storage_class & STCmanifest) != 0)
				&& (storage_class & STCinit) != 0) {
			ExpInitializer ei = getExpInitializer(context);
			if (ei != null)
				return ei.exp;
		}
		return null;
	}

	public ExpInitializer getExpInitializer(SemanticContext context) {
		ExpInitializer ei;

		if (this.init != null) {
			ei = this.init().isExpInitializer();
		} else {
			Expression e = this.type.defaultInit(filename, lineNumber, context);
			if (e != null) {
				ei = new ExpInitializer(this.filename, this.lineNumber, e);
			} else {
				ei = null;
			}
		}
		return ei;
	}

	@Override
	public int getNodeType() {
		return VAR_DECLARATION;
	}

	@Override
	public boolean hasPointers(SemanticContext context) {
		return (!isDataseg(context) && type.hasPointers(context));
	}

	@Override
	public boolean isDataseg(SemanticContext context) {
		if (context.isD1()) {
			Dsymbol parent = this.toParent();
			if (parent == null && (this.storage_class & (STCstatic | STCconst)) == 0) {
				if (context.acceptsErrors()) {
					context.acceptProblem(Problem.newSemanticTypeError(
							IProblem.CannotResolveForwardReference, this));
				}
				this.type = Type.terror;
				return false;
			}
			return ((this.storage_class & (STCstatic | STCconst)) != 0 || parent.isModule() != null || parent.isTemplateInstance() != null);
		} else {
			if ((storage_class & STCmanifest) != 0)
				return false;
			Dsymbol parent = this.toParent();
			if (null == parent && 0 == (storage_class & STCstatic)) {
				if (context.acceptsErrors()) {
					context.acceptProblem(Problem.newSemanticTypeError(
							IProblem.ForwardReferenceOfSymbol, this,
							new String[] { this.toChars(context) }));
				}
				type = Type.terror;
				return false;
			}
			return canTakeAddressOf()
					&& ((storage_class & (STCstatic | STCextern | STCtls | STCgshared)) != 0
							|| toParent().isModule() != null || toParent()
							.isTemplateInstance() != null);
		}
	}

	/*************************************
	 * Return !=0 if we can take the address of this variable.
	 */
	public boolean canTakeAddressOf() {
		if ((storage_class & STCmanifest) != 0)
			return false;
		return true;
	}

	@Override
	public boolean isImportedSymbol(SemanticContext context) {
		if (context.isD1()) {
			if (protection == PROTexport && init == null
					&& (isStatic() || isConst() || parent.isModule() == null)) {
				return true;
			}
		} else {
			if (protection == PROTexport
					&& null == init
					&& ((storage_class & STCstatic) != 0 || parent.isModule() != null))
				return true;
		}
		return false;
	}

	@Override
	public boolean isIn() {
		return (storage_class & STCin) != 0;
	}

	public boolean isInOut() {
		return (storage_class & (STCin | STCout)) == (STCin | STCout);
	}

	@Override
	public boolean isOut() {
		return (storage_class & STCout) != 0;
	}

	@Override
	public VarDeclaration isVarDeclaration() {
		return this;
	}

	@Override
	public String kind() {
		return "variable";
	}

	/******************************************
	 * Return TRUE if variable needs to call the destructor.
	 */
	public boolean needsAutoDtor(SemanticContext context) {
		if (noauto || (storage_class & STCnodtor) != 0)
			return false;

		// Destructors for structs and arrays of structs
		Type tv = type.toBasetype(context);
		while (tv.ty == Tsarray) {
//			TypeSArray ta = (TypeSArray) tv;
			tv = tv.nextOf().toBasetype(context);
		}
		if (tv.ty == Tstruct) {
			TypeStruct ts = (TypeStruct) tv;
			StructDeclaration sd = ts.sym;
			if (sd.dtor != null)
				return true;
		}

		// Destructors for classes
		if ((storage_class & (STCauto | STCscope)) != 0) {
			if (type.isClassHandle() != null)
				return true;
		}
		return false;
	}

	@Override
	public boolean needThis() {
		return (storage_class & STCfield) != 0;
	}

	@Override
	public void semantic(Scope sc, SemanticContext context) {
		semantic0(sc, context);

		// Descent: for code evaluate
		if (sourceInit != null) {
			((Initializer) sourceInit).resolvedInitializer = (Initializer) init;
		}
	}

	private void semantic0(Scope sc, SemanticContext context) {
		storage_class |= sc.stc;
		if ((storage_class & STCextern) != 0 && init != null) {
			if (context.acceptsErrors()) {
				context.acceptProblem(Problem.newSemanticTypeError(
						IProblem.ExternSymbolsCannotHaveInitializers, init));
			}
		}

		/*
		 * If auto type inference, do the inference
		 */
		int inferred = 0;
		if (type == null) {
			inuse++;

			type = init.inferType(sc, context);

			inuse--;
			inferred = 1;

			/*
			 * This is a kludge to support the existing syntax for RAII
			 * declarations.
			 */
			storage_class &= ~STCauto;
			originalType = type;
		} else {
			if (null == originalType) {
				originalType = type;
			}
			type = type.semantic(filename, lineNumber, sc, context);
		}

		// Added for Descent: case "auto foo = new"
		if (type == null) {
			return;
		}

		type.checkDeprecated(filename, lineNumber, sc, context);
		linkage = sc.linkage;
		this.parent = sc.parent;
		protection = sc.protection;

		if (!context.isD1()) {
		    if ((storage_class & STCgshared) != 0 && context.global.params.safe
					&& !sc.module.safe) {
		    	if (context.acceptsErrors()) {
					context.acceptProblem(Problem.newSemanticTypeError(
							IProblem.GsharedNotAllowedInSafeMode, this));
				}
			}
		}

		Dsymbol parent = toParent();
		FuncDeclaration fd = parent.isFuncDeclaration();

		Type tb = type.toBasetype(context);
		if (tb.ty == TY.Tvoid && (storage_class & STClazy) == 0) {
			if (context.acceptsErrors()) {
				context.acceptProblem(Problem.newSemanticTypeError(
						IProblem.VoidsHaveNoValue, sourceType == null ? this : sourceType));
			}
			type = Type.terror;
			tb = type;
		}
		if (tb.ty == TY.Tfunction) {
			if (context.acceptsErrors()) {
				context.acceptProblem(Problem.newSemanticTypeError(IProblem.SymbolCannotBeDeclaredToBeAFunction, ident, new String[] { toChars(context) }));
			}
			type = Type.terror;
			tb = type;
		}
		if (tb.ty == TY.Tstruct) {
			TypeStruct ts = (TypeStruct) tb;

			if (ts.sym.members == null) {
				if (context.acceptsErrors()) {
					context.acceptProblem(Problem.newSemanticTypeError(
							// "No definition of struct " + ts.sym.ident,
							IProblem.NoDefinition, sourceType, new String[] { new String(
									ts.sym.ident.ident) }));
				}
			}
		}

		if (tb.ty == TY.Ttuple) {
			/*
			 * Instead, declare variables for each of the tuple elements and add
			 * those.
			 */
			TypeTuple tt = (TypeTuple) tb;
			int nelems = Argument.dim(tt.arguments, context);
			Objects exps = new Objects(nelems);
			exps.setDim(nelems);
			Expression ie = init != null ? init.toExpression(context) : null;

			for (int i = 0; i < nelems; i++) {
				Argument arg = Argument.getNth(tt.arguments, i, context);

				OutBuffer buf = new OutBuffer();
				buf.data.append("_").append(ident.ident).append("_field_")
						.append(i);
				String name = buf.extractData();
				IdentifierExp id = new IdentifierExp(filename, lineNumber, name.toCharArray());

			    Expression einit = ie;
				if (ie != null && ie.op == TOK.TOKtuple) {
					einit = (Expression) ((TupleExp) ie).exps.get(i);
				}
				Initializer ti = init;
				if (einit != null) {
					ti = new ExpInitializer(einit.filename, einit.lineNumber, einit);
				}

				VarDeclaration v = new VarDeclaration(filename, lineNumber, arg.type, id, ti);
				v.semantic(sc, context);

				if (sc.scopesym != null) {
					if (sc.scopesym.members != null) {
						sc.scopesym.members.add(v);
					}
				}

				Expression e = new DsymbolExp(filename, lineNumber, v);
				exps.set(i, e);
			}
			TupleDeclaration v2 = new TupleDeclaration(filename, lineNumber, ident, exps);
			v2.isexp = true;
			aliassym = v2;
			return;
		}

		if (context.isD1()) {
			if ((storage_class & STCconst) != 0 && init == null && fd == null) {
				// Initialize by constructor only
				storage_class = (storage_class & ~STCconst) | STCctorinit;
			}

			if (isConst()) {
			} else if (isStatic()) {
			} else if (isSynchronized()) {
				if (ident != null) {
					if (context.acceptsErrors()) {
						context.acceptProblem(Problem.newSemanticTypeError(
										IProblem.ModifierCannotBeAppliedToVariables, ident, new String[] { "synchronized" }));
					}
				}
			} else if (isOverride()) {
				if (ident != null) {
					if (context.acceptsErrors()) {
						context.acceptProblem(Problem.newSemanticTypeError(
							IProblem.ModifierCannotBeAppliedToVariables, ident, new String[] { "override" }));
					}
				}
			} else if (isAbstract()) {
				if (ident != null) {
					if (context.acceptsErrors()) {
						context.acceptProblem(Problem.newSemanticTypeError(
								IProblem.ModifierCannotBeAppliedToVariables, ident, new String[] { "abstract" }));
					}
				}
			} else if ((storage_class & STCtemplateparameter) != 0) {
			} else {
				AggregateDeclaration aad = sc.anonAgg;
				if (aad == null) {
					aad = parent.isAggregateDeclaration();
				}
				if (aad != null) {
					aad.addField(sc, this, context);
				}

				InterfaceDeclaration id = parent.isInterfaceDeclaration();
				if (id != null) {
					if (context.acceptsErrors()) {
						context.acceptProblem(Problem.newSemanticTypeErrorLoc(
								IProblem.FieldsNotAllowedInInterfaces, this));
					}
				}

				TemplateInstance ti = parent.isTemplateInstance();
				if (ti != null) {
					// Take care of nested templates
					while (true) {
						TemplateInstance ti2 = ti.tempdecl.parent.isTemplateInstance();
						if (ti2 == null) {
							break;
						}
						ti = ti2;
					}

					// If it's a member template
					AggregateDeclaration ad = ti.tempdecl.isMember();
					if (ad != null && storage_class != STCundefined) {
						if (context.acceptsErrors()) {
							context.acceptProblem(Problem.newSemanticTypeError(
									IProblem.CannotUseTemplateToAddFieldToAggregate, this, new String[] { ad.toChars(context) }));
						}
					}
				}
			}
		} else {
			/*
			 * Storage class can modify the type
			 */
			type = type.addStorageClass(storage_class, context);

			/*
			 * Adjust storage class to reflect type
			 */
			if (type.isConst()) {
				storage_class |= STCconst;
				if (type.isShared())
					storage_class |= STCshared;
			} else if (type.isInvariant())
				storage_class |= STCimmutable;
			else if (type.isShared())
				storage_class |= STCshared;

			if (isSynchronized()) {
				if (context.acceptsErrors()) {
					context.acceptProblem(Problem.newSemanticTypeError(
							IProblem.ModifierCannotBeAppliedToVariables, ident,
							new String[] { "synchronized" }));
				}
			} else if (isOverride()) {
				if (context.acceptsErrors()) {
					context.acceptProblem(Problem.newSemanticTypeError(
							IProblem.ModifierCannotBeAppliedToVariables, ident,
							new String[] { "override" }));
				}
			} else if (isAbstract()) {
				if (context.acceptsErrors()) {
					context.acceptProblem(Problem.newSemanticTypeError(
							IProblem.ModifierCannotBeAppliedToVariables, ident,
							new String[] { "abstract" }));
				}
			} else if ((storage_class & STCfinal) != 0) {
				if (context.acceptsErrors()) {
					context.acceptProblem(Problem.newSemanticTypeError(
							IProblem.ModifierCannotBeAppliedToVariables, ident,
							new String[] { "final" }));
				}
			}

			if ((storage_class & (STCstatic | STCextern | STCmanifest
					| STCtemplateparameter | STCtls | STCgshared)) != 0) {
			} else {
				AggregateDeclaration aad = sc.anonAgg;
				if (null == aad)
					aad = parent.isAggregateDeclaration();
				if (aad != null) {
					// assert(!(storage_class & (STCextern | STCstatic | STCtls
					// | STCgshared)));

					if ((storage_class & (STCconst | STCimmutable)) != 0
							&& init != null) {
						if (null == type.toBasetype(context).isTypeBasic())
							storage_class |= STCstatic;
					} else
						aad.addField(sc, this, context);
				}

				InterfaceDeclaration id = parent.isInterfaceDeclaration();
				if (id != null) {
					if (context.acceptsErrors()) {
						context.acceptProblem(Problem.newSemanticTypeErrorLoc(
								IProblem.FieldsNotAllowedInInterfaces, this));
					}
				}

				/*
				 * Templates cannot add fields to aggregates
				 */
				TemplateInstance ti = parent.isTemplateInstance();
				if (ti != null) {
					// Take care of nested templates
					while (true) {
						TemplateInstance ti2 = ti.tempdecl.parent
								.isTemplateInstance();
						if (null == ti2)
							break;
						ti = ti2;
					}

					// If it's a member template
					AggregateDeclaration ad = ti.tempdecl.isMember();
					if (ad != null && storage_class != STCundefined) {
						if (context.acceptsErrors()) {
							context
									.acceptProblem(Problem
											.newSemanticTypeError(
													IProblem.CannotUseTemplateToAddFieldToAggregate,
													this, new String[] { ad
															.toChars(context) }));
						}
					}
				}
			}
		}

		if (!context.isD1()) {
			if ((storage_class & (STCref | STCparameter | STCforeach)) == STCref
					&& !equals(ident, Id.This)) {
				if (context.acceptsErrors()) {
					context
							.acceptProblem(Problem
									.newSemanticTypeError(
											IProblem.OnlyParametersOfForeachDeclarationsCanBeRef,
											this));
				}
			}
		}

		if (type.isauto() && !noauto) {
			if (context.isD1()) {
				if ((storage_class & (STCfield | STCout | STCref | STCstatic)) != 0
						|| fd == null) {
					context.acceptProblem(Problem.newSemanticTypeError(
							IProblem.GlobalsStaticsFieldsRefAndAutoParametersCannotBeAuto, this));
				}
			} else {
				if ((storage_class & (STCfield | STCout | STCref | STCstatic | STCmanifest | STCtls | STCgshared)) != 0
						|| fd == null) {
					context.acceptProblem(Problem.newSemanticTypeError(
							IProblem.GlobalsStaticsFieldsManifestConstantsRefAndAutoParametersCannotBeScope, this));
				}
			}

			if ((storage_class & (STCauto | STCscope)) == 0) {
				if ((storage_class & STCparameter) == 0
						&& equals(ident, Id.withSym)) {
					if (context.acceptsErrors()) {
						context.acceptProblem(Problem.newSemanticTypeError(
								IProblem.ReferenceToScopeClassMustBeScope, this, new String[] { toChars(context) }));
					}
				}
			}
		}

		if (!context.isD1()) {
			if ((isConst() || isInvariant(context)) && null == init && null == fd) {
				// Initialize by constructor only
				storage_class |= STCctorinit;
			}

			if (init != null) {
				// remember we had an explicit initializer
				storage_class |= STCinit;
			} else if ((storage_class & STCmanifest) != 0) {
				if (context.acceptsErrors()) {
					context.acceptProblem(Problem.newSemanticTypeError(
							IProblem.ManifestConstantsMustHaveInitializers, this));
				}
			}
		}

	    TOK op = TOKconstruct;

	    boolean condition;
	    if (context.isD1()) {
	    	condition = init == null && !sc.inunion && !isStatic() && !isConst()
				&& fd != null
				&& (storage_class & (STCfield | STCin | STCforeach)) == 0 &&
				type.size(context) != 0;
	    } else {
	    	condition = init == null && !sc.inunion && !isStatic()
	    		&& fd != null
	    		&& (0 == (storage_class & (STCfield | STCin | STCforeach | STCparameter)) || (storage_class & STCout) != 0) &&
	    		type.size(context) != 0;
	    }

		if (condition) {
			// Provide a default initializer
			if (type.ty == TY.Tstruct && ((TypeStruct) type).sym.zeroInit) {
				/* If a struct is all zeros, as a special case
			     * set it's initializer to the integer 0.
			     * In AssignExp.toElem(), we check for this and issue
			     * a memset() to initialize the struct.
			     * Must do same check in interpreter.
			     */
				Expression e = new IntegerExp(filename, lineNumber, Id.ZERO, 0, Type.tint32);
				Expression e1;
				e1 = new VarExp(filename, lineNumber, this);
				e = new AssignExp(filename, lineNumber, e1, e);
				if (context.isD1()) {
					e.type = e1.type;
				} else {
				    e.op = TOKconstruct;
				    e.type = e1.type;		// don't type check this, it would fail
				}
				init = new ExpInitializer(filename, lineNumber, e/* .type.defaultInit() */);
				return;
			} else if (type.ty == TY.Ttypedef) {
				TypeTypedef td = (TypeTypedef) type;
				if (td.sym.init != null) {
					init = td.sym.init;
					ExpInitializer ie = init.isExpInitializer();
					if (ie != null) {
						// Make copy so we can modify it
						init = new ExpInitializer(ie.filename, ie.lineNumber, ie.exp);
					}
				} else {
					init = getExpInitializer(context);
				}
			} else {
				init = getExpInitializer(context);
			}
			// Default initializer is always a blit
			op = TOKblit;
		}

		if (init != null) {
			sc = sc.push();

			if (context.isD1()) {
				sc.stc &= ~(STCconst | STCinvariant | STCpure);
			} else {
				sc.stc &= ~(STC_TYPECTOR | STCpure | STCnothrow | STCref);
			}

			ArrayInitializer ai = init.isArrayInitializer();
			if (ai != null && tb.ty == Taarray) {
				init = ai.toAssocArrayInitializer(context);
			}

			StructInitializer si = init.isStructInitializer();
			ExpInitializer ei = init.isExpInitializer();

			// See if we can allocate on the stack
			if (ei != null && isScope() && ei.exp.op == TOK.TOKnew) {
				NewExp ne = (NewExp) ei.exp;
				if (!(ne.newargs != null && ne.newargs.size() > 0)) {
					ne.onstack = true;
					onstack = 1;
					if (type.isBaseOf(ne.newtype.semantic(filename, lineNumber, sc, context),
							null, context)) {
						onstack = 2;
					}
				}
			}

			// If inside function, there is no semantic3() call
			if (sc.func != null) {
				boolean condition2;
				if (context.isD1()) {
					condition2 = fd != null && !isStatic() && !isConst()
						&& init.isVoidInitializer() == null;
				} else {
					condition2 = fd != null &&
					    		0 == (storage_class & (STCmanifest | STCstatic | STCtls | STCgshared | STCextern)) &&
					    		null == init.isVoidInitializer();
				}

				// If local variable, use AssignExp to handle all the various
				// possibilities.
				if (condition2) {
					Expression e1;
					Type t;
					int dim;

					if (ei == null) {
						Expression e = init.toExpression(context);
						if (e == null) {
							init = init.semantic(sc, type, context);
							e = init.toExpression(context);
							if (e == null) {
								if (context.acceptsErrors()) {
									context.acceptProblem(Problem.newSemanticTypeError(IProblem.SymbolNotAStaticAndCannotHaveStaticInitializer, this, new String[] { toChars(context) }));
								}
								return;
							}
						}
						ei = new ExpInitializer(init.filename, init.lineNumber, e);
						init = ei;
					}

					e1 = new VarExp(filename, lineNumber, this);

					t = type.toBasetype(context);
					if (t.ty == TY.Tsarray) {
					    ei.exp = ei.exp.semantic(sc, context);
						if (null == ei.exp.implicitConvTo(type, context)) {
							dim = ((TypeSArray) t).dim.toInteger(context)
									.intValue();
							// If multidimensional static array, treat as one large
							// array
							while (true) {
								t = t.nextOf().toBasetype(context);
								if (t.ty != TY.Tsarray) {
									break;
								}
								dim *= ((TypeSArray) t).dim.toInteger(
										context).intValue();
								e1.type = new TypeSArray(t.nextOf(),
										new IntegerExp(filename, lineNumber, Id.ZERO, dim,
												Type.tindex), context.encoder);
							}
						}
						e1 = new SliceExp(filename, lineNumber, e1, null, null);
					} else if (t.ty == TY.Tstruct) {
						ei.exp = ei.exp.semantic(sc, context);

						if (!context.isD1()) {
							/*
							 * Look to see if initializer is a call to the
							 * constructor
							 */
							StructDeclaration sd = ((TypeStruct) t).sym;
							if (sd.ctor != null
									&& // there are constructors
									ei.exp.type.ty == Tstruct
									&& // rvalue is the same struct
									((TypeStruct) ei.exp.type).sym == sd
									&& ei.exp.op == TOKstar) {
								/*
								 * Look for form of constructor call which is:
								 * *__ctmp.ctor(arguments...)
								 */
								PtrExp pe = (PtrExp) ei.exp;
								if (pe.e1.op == TOKcall) {
									CallExp ce = (CallExp) pe.e1;
									if (ce.e1.op == TOKdotvar) {
										DotVarExp dve = (DotVarExp) ce.e1;
										if (dve.var.isCtorDeclaration() != null) {
											/*
											 * It's a constructor call,
											 * currently constructing a
											 * temporary __ctmp.
											 */
											/*
											 * Before calling the constructor,
											 * initialize variable with a bit
											 * copy of the default initializer
											 */
											Expression e = new AssignExp(
													filename, lineNumber,
													new VarExp(filename,
															lineNumber, this),
													t
															.defaultInit(
																	filename,
																	lineNumber,
																	context));
											e.op = TOKblit;
											e.type = t;
											ei.exp = new CommaExp(filename,
													lineNumber, e, ei.exp);

											/*
											 * Replace __ctmp being constructed
											 * with e1
											 */
											dve.e1 = e1;
											return;
										}
									}
								}
							}
						}

						if (ei.exp.implicitConvTo(type, context) == MATCH.MATCHnomatch) {
							if (context.isD1()) {
								ei.exp = new CastExp(filename, lineNumber, ei.exp, type);
							} else {
							    Type ti = ei.exp.type.toBasetype(context);
								// Don't cast away invariant or mutability in initializer
								if (!(ti.ty == Tstruct && t.toDsymbol(sc, context) == ti.toDsymbol(sc, context))) {
								    ei.exp = new CastExp(filename, lineNumber, ei.exp, type);
								}
							}
						}
					}
					ei.exp = new AssignExp(filename, lineNumber, e1, ei.exp);

					if (context.isD1()) {
						ei.exp.op = TOKconstruct;
					} else {
						ei.exp.op = op;
					}
					canassign++;
					ei.exp = ei.exp.semantic(sc, context);
					canassign--;
					ei.exp.optimize(ASTDmdNode.WANTvalue, context);
				} else {
					init = init.semantic(sc, type, context);

					if (context.isD1()) {
						if (fd != null && isConst() && !isStatic()) {
							// Make it static
							storage_class |= STCstatic;
						}
					}
				}
			} else if (
				(context.isD1() && (isConst() || isFinal())) ||
				(!context.isD1() && ((storage_class & (STCconst | STCimmutable | STCmanifest)) != 0 || type.isConst() || type.isInvariant()))
				) {
				/* Because we may need the results of a const declaration in a
				 * subsequent type, such as an array dimension, before semantic2()
				 * gets ordinarily run, try to run semantic2() now.
				 * Ignore failure.
				 */

				if (0 == context.global.errors && 0 == inferred) {
					int errors = context.global.errors;
					context.global.gag++;
					Expression e = null;
					Initializer i2 = init;
					inuse++;
					if (ei != null) {
						e = ei.exp.syntaxCopy(context);
						e = e.semantic(sc, context);

						e = e.implicitCastTo(sc, type, context);
					} else if (si != null || ai != null) {
						i2 = init.syntaxCopy(context);
						i2 = i2.semantic(sc, type, context);
					}
					inuse--;
					context.global.gag--;
					if (errors != context.global.errors) // if errors happened
					{
						if (context.global.gag == 0) {
							context.global.errors = errors; // act as if nothing happened
						}

						if (!context.isD1()) {
						    /* Save scope for later use, to try again
						     */
						    scope = new Scope(sc);
						    scope.setNoFree();
						}

					} else if (ei != null) {
						if (context.isD1()) {
							e = e.optimize(WANTvalue | WANTinterpret, context);
							if (e.op == TOKint64 || e.op == TOKstring) {
								// Descent: instead of copying the result, do semantic analysis again,
								// in order to get binding resolution, but only for the root module
								if (this.getModule() == context.Module_rootModule) {
									ei.exp = ei.exp.semantic(sc, context);
								} else {
									ei.exp = e;		// no errors, keep result
								}
							}
						} else {
							if (isDataseg(context)) {
								/*
								 * static const/invariant does CTFE
								 */
								e = e.optimize(WANTvalue | WANTinterpret,
										context);
							} else {
								e = e.optimize(WANTvalue, context);
							}
							if (e.op == TOKint64 || e.op == TOKstring) {
								// Descent: instead of copying the result, do semantic analysis again,
								// in order to get binding resolution, but only for the root module
								if (this.getModule() == context.Module_rootModule) {
									ei.exp = ei.exp.semantic(sc, context);
								} else {
									ei.exp = e;		// no errors, keep result
								}
							} else {
								/*
								 * Save scope for later use, to try again
								 */
								scope = new Scope(sc);
								scope.setNoFree();
							}
						}
					} else {
						init = i2; // no errors, keep result
					}
				}
			}

			sc = sc.pop();
		}
	}

	@Override
	public void semantic2(Scope sc, SemanticContext context) {
		semantic20(sc, context);

		// Descent: for code evaluate
		if (sourceInit != null) {
			((Initializer) sourceInit).resolvedInitializer = (Initializer) init;
		}
	}

	private void semantic20(Scope sc, SemanticContext context) {
		Dsymbol top = toParent();
		if (init != null && top != null && top.isFuncDeclaration() == null) {
			inuse++;
			init = init.semantic(sc, type, context);
			inuse--;
		}
	}

	@Override
	public Dsymbol syntaxCopy(Dsymbol s, SemanticContext context) {
		VarDeclaration sv;
		if (s != null) {
			sv = (VarDeclaration) s;
		} else {
			Initializer init = null;
			if (this.init != null) {
				init = this.init.syntaxCopy(context);
				// init.isExpInitializer().exp.print();
				// init.isExpInitializer().exp.dump(0);
			}

			sv = context.newVarDeclaration(filename, lineNumber, type != null ? type.syntaxCopy(context)
					: null, ident.syntaxCopy(context), init);
			sv.storage_class = storage_class;
		}
		// Syntax copy for header file
		if (htype == null) // Don't overwrite original
		{
			if (type != null) // Make copy for both old and new instances
			{
				htype = type.syntaxCopy(context);
				sv.htype = type.syntaxCopy(context);
			}
		} else {
			// Make copy of original for new instance
			sv.htype = htype.syntaxCopy(context);
		}
		if (hinit == null) {
			if (init != null) {
				hinit = init.syntaxCopy(context);
				sv.hinit = init.syntaxCopy(context);
			}
		} else {
			sv.hinit = hinit.syntaxCopy(context);
		}
		sv.copySourceRange(this);
		sv.javaElement = javaElement;
		return sv;
	}

	@Override
	public Dsymbol toAlias(SemanticContext context) {
		Assert.isTrue(this != aliassym);
		Dsymbol s = aliassym != null ? aliassym.toAlias(context) : this;
		return s;
	}

	@Override
	public void toCBuffer(OutBuffer buf, HdrGenState hgs,
			SemanticContext context) {
		StorageClassDeclaration.stcToCBuffer(buf, storage_class, context);
		if (type != null) {
			type.toCBuffer(buf, ident, hgs, context);
		} else {
			buf.writestring(ident.toChars());
		}
		if (init != null) {
			buf.writestring(" = ");

			if (context.isD1()) {
				init.toCBuffer(buf, hgs, context);
			} else {
				ExpInitializer ie = init.isExpInitializer();
				if (ie != null && (ie.exp.op == TOKconstruct || ie.exp.op == TOKblit))
				    ((AssignExp)ie.exp).e2.toCBuffer(buf, hgs, context);
				else
				    init.toCBuffer(buf, hgs, context);
			}
		}
		buf.writeByte(';');
		buf.writenl();
	}

	@Override
	public int getLineNumber() {
		return lineNumber;
	}

	@Override
	public void setLineNumber(int lineNumber) {
		this.lineNumber = lineNumber;
	}

	public int inuse() {
		return inuse;
	}

	public Initializer init() {
		return init;
	}

	public void init(Initializer init) {
		this.init = init;
	}

	public boolean ctorinit() {
		return ctorinit;
	}

	public void ctorinit(boolean c) {
		this.ctorinit = c;
	}

	public boolean noauto() {
		return noauto;
	}

	public Expression value() {
		return value;
	}

	public void value(Expression value) {
		this.value = value;
	}

	public int offset() {
		return offset;
	}

	public void offset(int offset) {
		this.offset = offset;
	}

	public int canassign() {
		return canassign;
	}

	public int nestedref() {
		return nestedref;
	}

	public void nestedref(int nestedref) {
		this.nestedref = nestedref;
	}

	@Override
	public char getSignaturePrefix() {
		return Signature.C_VARIABLE;
	}

	public void setJavaElement(IField__Marker field) {
		this.javaElement = field;
	}

	@Override
	public IField__Marker getJavaElement() {
		return javaElement;
	}

	public boolean isTemplateArgument() {
		return false;
	}

	/************************************
	 * Does symbol go into thread local storage?
	 */
	public boolean isThreadlocal(SemanticContext context) {
		/*
		 * Data defaults to being thread-local. It is not thread-local if it is
		 * immutable, const or shared.
		 */
		boolean i = isDataseg(context)
				&& 0 == (storage_class & (STCimmutable | STCconst | STCshared | STCgshared));
		return i;
	}

}
