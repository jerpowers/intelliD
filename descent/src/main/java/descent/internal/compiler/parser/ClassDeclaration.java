package descent.internal.compiler.parser;

import static descent.internal.compiler.parser.PROT.PROTnone;
import static descent.internal.compiler.parser.STC.STC_TYPECTOR;
import static descent.internal.compiler.parser.STC.STCabstract;
import static descent.internal.compiler.parser.STC.STCauto;
import static descent.internal.compiler.parser.STC.STCconst;
import static descent.internal.compiler.parser.STC.STCdeprecated;
import static descent.internal.compiler.parser.STC.STCfinal;
import static descent.internal.compiler.parser.STC.STCgshared;
import static descent.internal.compiler.parser.STC.STCimmutable;
import static descent.internal.compiler.parser.STC.STCscope;
import static descent.internal.compiler.parser.STC.STCshared;
import static descent.internal.compiler.parser.STC.STCstatic;
import static descent.internal.compiler.parser.STC.STCtls;
import static descent.internal.compiler.parser.TY.Tclass;
import static descent.internal.compiler.parser.TY.Tstruct;

import java.util.ArrayList;
import java.util.List;

import melnorme.utilbox.tree.TreeVisitor;

import melnorme.utilbox.core.Assert;

import descent.core.Signature;
import descent.core.compiler.CharOperation;
import descent.core.compiler.IProblem;
import descent.internal.compiler.parser.ast.IASTVisitor;


public class ClassDeclaration extends AggregateDeclaration {

	public final static int OFFSET_RUNTIME = 0x76543210;

	public BaseClasses sourceBaseclasses;
	public BaseClasses baseclasses;

	public ClassDeclaration baseClass; // null only if this is Object
//	public CtorDeclaration ctor;
//	public CtorDeclaration defaultCtor; // default constructor
	public FuncDeclarations dtors; // Array of destructors
	public FuncDeclaration staticCtor;
	public FuncDeclaration staticDtor;
	public BaseClasses interfaces;
	public BaseClasses vtblInterfaces; // array of base interfaces that
	// have
	// their own vtbl[]
	public PROT protection;
	public boolean isnested; // !=0 if is nested
	public VarDeclaration vthis; // 'this' parameter if this class is nested

	public ClassInfoDeclaration vclassinfo; // the ClassInfo object for this ClassDeclaration
	public boolean com; // !=0 if this is a COM class
	public boolean isauto; // !=0 if this is an auto class
	public boolean isabstract; // !=0 if abstract class
	public List vtbl; // Array of FuncDeclaration's making up the vtbl[]
	public List vtblFinal; // More FuncDeclaration's that aren't in vtbl[]
	public boolean cpp;				// !=0 if this is a C++ interface

	// Scope used in the initializeSpecial method
	private Scope specialInitializeScope;

	public ClassDeclaration(char[] filename, int lineNumber, char[] id) {
		this(filename, lineNumber, id, null);
	}

	public ClassDeclaration(char[] filename, int lineNumber, IdentifierExp id) {
		this(filename, lineNumber, id, null);
	}

	public ClassDeclaration(char[] filename, int lineNumber, char[] id, BaseClasses baseclasses) {
		this(filename, lineNumber, new IdentifierExp(filename, lineNumber, id), baseclasses);
	}

	public ClassDeclaration(char[] filename, int lineNumber, IdentifierExp id,
			BaseClasses baseclasses) {
		super(filename, lineNumber, id);
		if (baseclasses == null) {
			this.baseclasses = new BaseClasses(0);
		} else {
			this.baseclasses = baseclasses;
			this.sourceBaseclasses = new BaseClasses(baseclasses
					.size());
			this.sourceBaseclasses.addAll(baseclasses);
		}
		this.type = new TypeClass(this);
		this.vtbl = new ArrayList(0);
		this.vtblFinal = new ArrayList(0);
		handle = type;

		// TODO missing semantic scode
	}

	@Override
	public void accept0(IASTVisitor visitor) {
		boolean children = visitor.visit(this);
		if (children) {
			TreeVisitor.acceptChildren(visitor, ident);
			TreeVisitor.acceptChildren(visitor, sourceBaseclasses);
			TreeVisitor.acceptChildren(visitor, members);

//			acceptSynthetic(visitor);
		}
		visitor.endVisit(this);
	}

	@Override
	public void addLocalClass(ClassDeclarations aclasses,
			SemanticContext context) {
		aclasses.add(this);
	}

	public FuncDeclaration findFunc(IdentifierExp id, TypeFunction tf,
			SemanticContext context) {
		ClassDeclaration cd = this;
		List vtbl = cd.vtbl;
		while (true) {
			for (int i = 0; i < vtbl.size(); i++) {
				FuncDeclaration fd = ((Dsymbol) vtbl.get(i)).isFuncDeclaration();
				if (fd == null)
					continue;		// the first entry might be a ClassInfo

				if (equals(ident, fd.ident) && fd.type.covariant(tf, context) == 1) {
					return fd;
				}
			}
			if (cd == null) {
				break;
			}
			vtbl = cd.vtblFinal;
			cd = cd.baseClass;
		}

		return null;
	}

	@Override
	public PROT getAccess(Dsymbol smember) {
		PROT access_ret = PROTnone;

		Dsymbol p = smember.toParent();
		if (p != null && p.isAggregateDeclaration() != null && SemanticMixin.equals(p.isAggregateDeclaration(), this)) {
			access_ret = smember.prot();
		} else {
			PROT access;
			int i;

			if (smember.isDeclaration().isStatic()) {
				access_ret = smember.prot();
			}

			for (i = 0; i < this.baseclasses.size(); i++) {
				BaseClass b = this.baseclasses.get(i);
				access = b.base.getAccess(smember);
				switch (access) {
				case PROTnone:
					break;

				case PROTprivate:
					access = PROTnone; // private members of base class not
					// accessible
					break;

				case PROTpackage:
				case PROTprotected:
				case PROTpublic:
				case PROTexport:
					// If access is to be tightened
					if (b.protection.level < access.level) {
						access = b.protection;
					}

					// Pick path with loosest access
					if (access.level > access_ret.level) {
						access_ret = access;
					}
					break;

				default:
					Assert.isTrue(false);
				}
			}
		}
		return access_ret;
	}

	@Override
	public int getNodeType() {
		return CLASS_DECLARATION;
	}

	public void interfaceSemantic(Scope sc, SemanticContext context) {
	    InterfaceDeclaration id = isInterfaceDeclaration();

		vtblInterfaces = new BaseClasses(interfaces.size());

		for (int i = 0; i < interfaces.size(); i++) {
			BaseClass b = interfaces.get(i);

			// If this is an interface, and it derives from a COM interface,
			// then this is a COM interface too.
			if (b.base.isCOMinterface()) {
				com = true;
			}

			if (context.isD2()) {
				if (b.base.isCPPinterface() && id != null) {
				    id.cpp = true;
				}
			}

			vtblInterfaces.add(b);
			b.copyBaseInterfaces(vtblInterfaces);
		}
	}

	public boolean isAbstract() {
		if (isabstract) {
			return true;
		}
		for (int i = 1; i < vtbl.size(); i++) {
			FuncDeclaration fd = ((Dsymbol) vtbl.get(i)).isFuncDeclaration();

			if (fd == null || fd.isAbstract()) {
				isabstract |= true;
				return true;
			}
		}
		return false;
	}

	public boolean isBaseOf(ClassDeclaration cd, int[] poffset,
			SemanticContext context) {
		if (poffset != null) {
			poffset[0] = 0;
		}
		while (cd != null) {
			if (this == cd.baseClass) {
				return true;
			}

			/*
			 * cd.baseClass might not be set if cd is forward referenced.
			 */
			if (cd.baseClass == null && cd.baseclasses.size() > 0
					&& cd.isInterfaceDeclaration() == null) {
				if (context.acceptsErrors()) {
					context.acceptProblem(Problem.newSemanticTypeError(
							IProblem.BaseClassIsForwardReferenced, this, toChars(context)));
				}
			}

			cd = cd.baseClass;
		}
		return false;
	}

	public boolean isBaseOf2(ClassDeclaration cd) {
		if (cd == null) {
			return false;
		}
		for (int i = 0; i < cd.baseclasses.size(); i++) {
			BaseClass b = cd.baseclasses.get(i);

			if (b.base == this || isBaseOf2(b.base)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public ClassDeclaration isClassDeclaration() {
		return this;
	}

	public boolean isCOMclass() {
		return com;
	}

	public boolean isCOMinterface() {
		return false;
	}

	public boolean isCPPinterface() {
		return false;
	}
	@Override
	public boolean isNested() {
		return isnested;
	}

	@Override
	public String kind() {
		return "class";
	}

	@Override
	public Dsymbol search(char[] filename, int lineNumber, char[] ident, int flags,
			SemanticContext context) {
		Dsymbol s;

		if (scope != null) {
			semantic(scope, context);
		}

		if (members == null || symtab == null || scope != null) {
			if (context.acceptsErrors()) {
				context.acceptProblem(Problem.newSemanticTypeError(
						IProblem.ForwardReferenceWhenLookingFor, this,
						new String(this.ident.ident),
								new String(ident)));
			}
			return null;
		}

		s = super.search(filename, lineNumber, ident, flags, context);
		if (s == null) {
			// Search bases classes in depth-first, left to right order

			int i;

			for (i = 0; i < baseclasses.size(); i++) {
				BaseClass b = baseclasses.get(i);

				if (b.base != null) {
					if (b.base.symtab == null) {
						if (context.acceptsErrors()) {
							context.acceptProblem(Problem.newSemanticTypeError(
									IProblem.BaseIsForwardReferenced, this, b.base.ident
											.toChars()));
						}
					} else {
						s = b.base.search(filename, lineNumber, ident, flags, context);
						if (s == this) {
							// derives from this
							s = null;
						} else if (s != null) {
							break;
						}
					}
				}
			}
		}
		return s;
	}

	@Override
	public void semantic(Scope sc, SemanticContext context) {
		int i;
		// int offset;

		if (ident == null) { // if anonymous class
			String id = "__anonclass";
			ident = context.generateId(id);
		}

		if (null == scope) {
			if (parent == null && sc.parent != null
					&& sc.parent.isModule() == null) {
				parent = sc.parent;
			}

			type = type.semantic(filename, lineNumber, sc, context);
			handle = handle.semantic(filename, lineNumber, sc, context);
		}
		if (members == null) // if forward reference
		{
			return;
		}
		if (symtab != null) {
			if (scope == null) {
				return; // semantic() already completed
			}
		} else {
			symtab = new DsymbolTable();
		}

		Scope scx = null;
		if (scope != null) {
			sc = scope;
			scx = scope; // save so we don't make redundant copies
			scope = null;
		}

	    if ((sc.stc & STCdeprecated) != 0) {
	    	isdeprecated = true;
	    }

	    if (!context.isD1() && sc.linkage == LINK.LINKcpp) {
	    	if (context.acceptsErrors()) {
	    		context.acceptProblem(Problem.newSemanticTypeErrorLoc(IProblem.CannotCreateCppClasses, this));
	    	}
	    }

		// Expand any tuples in baseclasses[]
		for (i = 0; i < baseclasses.size();) {
			BaseClass b = baseclasses.get(i);

			b.type = b.type.semantic(filename, lineNumber, sc, context);

			if (getModule() == context.Module_rootModule) {
				unlazy(b, context);
			}

			Type tb = b.type.toBasetype(context);

			if (tb.ty == TY.Ttuple) {
				TypeTuple tup = (TypeTuple) tb;
				PROT protection = b.protection;
				baseclasses.remove(i);
				int dim = Argument.dim(tup.arguments, context);
				for (int j = 0; j < dim; j++) {
					Argument arg = Argument.getNth(tup.arguments, j, context);
					b = new BaseClass(arg.type, protection);
					baseclasses.add(i + j, b);
				}
			} else {
				i++;
			}
		}

		if (context.isD2()) {
		    if (0 == size(baseclasses) && sc.linkage == LINK.LINKcpp) {
		    	cpp = true;
		    }
		}


		// See if there's a base class as first in baseclasses[]
		if (baseclasses.size() > 0) {
			TypeClass tc;
			BaseClass b;
			Type tb;

			b = baseclasses.get(0);
			// b.type = b.type.semantic(filename, lineNumber, sc);
			tb = b.type.toBasetype(context);
			if (tb.ty != TY.Tclass) {
				// If already reported error, don't report it twice
				if (tb.ty != TY.Terror) {
					if (context.acceptsErrors()) {
						context.acceptProblem(Problem.newSemanticTypeError(
								IProblem.BaseTypeMustBeClassOrInterface, b.sourceType));
					}
				}
				baseclasses.remove(0);
			} else {
				tc = (TypeClass) (tb);
			    if (tc.sym.isDeprecated()) {
					if (!isDeprecated()) {
						// Deriving from deprecated class makes this one deprecated too
						isdeprecated = true;

						tc.checkDeprecated(filename, lineNumber, sc, context);
					}
				}

				if (tc.sym.isInterfaceDeclaration() != null) {
					;
				} else {
					boolean gotoL7 = false;
					for (ClassDeclaration cdb = tc.sym; cdb != null; cdb = cdb.baseClass) {
						if (SemanticMixin.equals(cdb, this)) {
							BaseClass firstBaseClass = this.baseclasses.get(0);
							if (context.acceptsErrors()) {
								context.acceptProblem(Problem.newSemanticTypeError(
										IProblem.CircularDefinition, firstBaseClass.sourceType, toChars(context)));
							}
							baseclasses.remove(0);
							// goto L7;
							gotoL7 = true;
							break;
						}
					}
					if (!gotoL7) {
						if (tc.sym.symtab == null || tc.sym.scope != null
								|| tc.sym.sizeok == 0) {
							scope = scx != null ? scx : new Scope(sc);
							scope.setNoFree();
							scope.module.addDeferredSemantic(this, context);
							return;
						} else {
							baseClass = tc.sym;
							b.base = baseClass;
						}
					}
					// L7: ;
				}
			}
		}

		// Treat the remaining entries in baseclasses as interfaces
		// Check for errors, handle forward references
		for (i = (baseClass != null ? 1 : 0); i < baseclasses.size();) {
			TypeClass tc;
			BaseClass b;
			Type tb;

			b = baseclasses.get(i);
			b.type = b.type.semantic(filename, lineNumber, sc, context);
			tb = b.type.toBasetype(context);
			if (tb.ty == TY.Tclass) {
				tc = (TypeClass) tb;
			} else {
				tc = null;
			}
			if (tc == null || tc.sym.isInterfaceDeclaration() == null) {
				// If already reported error, don't report it twice
				if (tb.ty != TY.Terror) {
					if (context.acceptsErrors()) {
						context.acceptProblem(Problem.newSemanticTypeError(
								IProblem.BaseTypeMustBeClassOrInterface, b.sourceType));
					}
				}
				baseclasses.remove(i);
				continue;
			} else {
			    if (tc.sym.isDeprecated()) {
					if (!isDeprecated()) {
						// Deriving from deprecated class makes this one deprecated too
						isdeprecated = true;

						tc.checkDeprecated(filename, lineNumber, sc, context);
					}
				}

				// Check for duplicate interfaces
				for (int j = (baseClass != null ? 1 : 0); j < i; j++) {
					BaseClass b2 = baseclasses.get(j);
					if (b2.base == tc.sym) {
						if (context.acceptsErrors()) {
							context.acceptProblem(Problem.newSemanticTypeError(
									IProblem.DuplicatedInterfaceInheritance, b.sourceType,
									b.sourceType.toString(),
											new String(this.ident.ident)));
						}
					}
				}

				b.base = tc.sym;
				if (b.base.symtab == null || b.base.scope != null) {
					scope = scx != null ? scx : new Scope(sc);
					scope.setNoFree();
					scope.module.addDeferredSemantic(this, context);
					return;
				}
			}
			i++;
		}

		// If no base class, and this is not an Object, use Object as base class
		if (baseClass == null && !equals(ident, Id.Object)) {
			// BUG: what if Object is redefined in an inner scope?
			Type tbase = new TypeIdentifier(filename, lineNumber, Id.Object);
			BaseClass b;
			TypeClass tc;
			Type bt;

			if (context.ClassDeclaration_object == null) {
				if (context.acceptsErrors()) {
					context.acceptProblem(Problem.newSemanticTypeErrorLoc(
							IProblem.MissingOrCurruptObjectDotD, this));
				}
				fatal(context);
			}
			bt = tbase.semantic(filename, lineNumber, sc, context).toBasetype(context);
			b = new BaseClass(bt, PROT.PROTpublic);
			baseclasses.add(0, b);
			if (b.type.ty != Tclass) {

				// This may happen if object.d is not found.
				// So, just return: another error somewhere else be reported
				return;

				//throw new IllegalStateException("assert(b.type.ty == Tclass);");
			}
			tc = (TypeClass) (b.type);
			baseClass = tc.sym;

			if (baseClass.isInterfaceDeclaration() != null) {
				throw new IllegalStateException(
						"assert(!baseClass.isInterfaceDeclaration());");
			}
			b.base = baseClass;
		}

		interfaces = new BaseClasses(baseclasses.size());
		interfaces.addAll(baseclasses);

		if (baseClass != null) {
			if ((baseClass.storage_class & STCfinal) != 0) {
				if (context.acceptsErrors()) {
					context.acceptProblem(Problem.newSemanticTypeError(
							IProblem.CannotInheritFromFinalClass, this, baseClass
									.toString()));
				}
			}

			interfaces.remove(0);

			// Copy vtbl[] from base class
			if (baseClass.vtbl != null) {

				// Descent: to get "override" errors ok
				baseClass = (ClassDeclaration) baseClass.unlazy(context);

				vtbl = new ArrayList(baseClass.vtbl.size());
 				vtbl.addAll(baseClass.vtbl);
			}

			// Inherit properties from base class
			com = baseClass.isCOMclass();
			isauto = baseClass.isauto;
			vthis = baseClass.vthis;

			if (context.isD2()) {
				storage_class |= baseClass.storage_class & STC_TYPECTOR;
			}
		} else {
			// No base class, so this is the root of the class hierarchy
			vtbl = new ArrayList(1);
			vtbl.add(this); // leave room for classinfo as first member
		}

		protection = sc.protection;
		storage_class |= sc.stc;

		if (sizeok == 0) {
			interfaceSemantic(sc, context);

			for (Dsymbol s : members) {
				s.addMember(sc, this, 1, context);
			}

			/*
			 * If this is a nested class, add the hidden 'this' member which is
			 * a pointer to the enclosing scope.
			 */
			if (vthis != null) // if inheriting from nested class
			{ // Use the base class's 'this' member
				isnested = true;
				if ((storage_class & STC.STCstatic) != 0) {
					if (context.acceptsErrors()) {
						context.acceptProblem(Problem.newSemanticTypeError(
								IProblem.StaticClassCannotInheritFromNestedClass, this, baseClass.toChars(context)));
					}
				}
				if (toParent2() != baseClass.toParent2()) {
//					if (context.acceptsErrors()) {
//						context.acceptProblem(Problem.newSemanticTypeError(
//								IProblem.SuperClassIsNestedWithin, this, baseClass.toChars(context), baseClass.toParent2()
//										.toChars(context), toParent2().toChars(
//												context)));
//					}
					if (toParent2() != null)
					{
						if (context.acceptsErrors()) {
						    context.acceptProblem(Problem.newSemanticTypeError(IProblem.SuperClassIsNestedWithin,
						    	this,
						    	this.toChars(context),
								toParent2().toChars(context),
								baseClass.toChars(context),
								baseClass.toParent2().toChars(context)));
						}
					}
					else
					{
						if (context.acceptsErrors()) {
							context.acceptProblem(Problem.newSemanticTypeError(IProblem.SuperClassIsNotNestedWithin,
						    	this,
						    	this.toChars(context),
								baseClass.toChars(context),
								baseClass.toParent2().toChars(context)));
						}
					}
					isnested = false;
				}
			} else if ((storage_class & STC.STCstatic) == 0) {
				Dsymbol s = toParent2();
				if (s != null) {
					if (context.isD1()) {
						ClassDeclaration cd = s.isClassDeclaration();
						FuncDeclaration fd = s.isFuncDeclaration();

						if (cd != null || fd != null) {
							isnested = true;
							Type t = null;
							if (cd != null) {
								t = cd.type;
							} else if (fd != null) {
								AggregateDeclaration ad = fd.isMember2();
								if (ad != null) {
									t = ad.handle;
								} else {
									t = new TypePointer(Type.tvoid);
									t = t.semantic(filename, lineNumber, sc, context);
								}
							} else {
								Assert.isTrue(false);
							}
							Assert.isTrue(vthis == null);
							vthis = new ThisDeclaration(filename, lineNumber, t);
							members.add(vthis);
						}
					} else {
						AggregateDeclaration ad = s.isClassDeclaration();
						FuncDeclaration fd = s.isFuncDeclaration();

						if (ad != null || fd != null) {
							isnested = true;
							Type t;
							if (ad != null) {
								t = ad.handle;
							} else if (fd != null) {
								AggregateDeclaration ad2 = fd.isMember2();
								if (ad2 != null) {
									t = ad2.handle;
								} else {
									t = context.Type_tvoidptr;
								}
							} else {
								throw new IllegalStateException();
							}
							if (t.ty == Tstruct) {// ref to struct
								t = context.Type_tvoidptr;
							}
							assert (null == vthis);
							vthis = new ThisDeclaration(filename, lineNumber, t);
							members.add(vthis);
						}
					}
				}
			}
		}

		if ((storage_class & (STC.STCauto | STC.STCscope)) != 0) {
			isauto = true;
		}
		if ((storage_class & STC.STCabstract) != 0) {
			isabstract = true;
		}

		if (context.isD2()) {
			if ((storage_class & STCimmutable) != 0) {
				type = type.invariantOf(context);
			} else if ((storage_class & STCconst) != 0) {
				type = type.constOf(context);
			} else if ((storage_class & STCshared) != 0) {
				type = type.sharedOf(context);
			}
		}

		sc = sc.push(this);
		specialInitializeScope = sc;

		if (context.isD2()) {
		    sc.stc &= ~(STCfinal | STCauto | STCscope | STCstatic |
		   		 STCabstract | STCdeprecated | STC_TYPECTOR | STCtls | STCgshared);
		    sc.stc |= storage_class & STC_TYPECTOR;
		} else {
			sc.stc &= ~(STCfinal | STCauto | STCscope | STCstatic | STCabstract | STCdeprecated);
		}

		sc.parent = this;
		sc.inunion = false;

		if (isCOMclass()) {
			if (context._WIN32) {
				sc.linkage = LINK.LINKwindows;
			} else {
				sc.linkage = LINK.LINKc;
			}
		}
		sc.protection = PROT.PROTpublic;
		sc.explicitProtection = 0;
		sc.structalign = 8;
		structalign = sc.structalign;
		if (baseClass != null) {
			sc.offset = baseClass.structsize;
			alignsize = baseClass.alignsize;
			// if (isnested)
			// sc.offset += PTRSIZE; // room for uplevel context pointer
		} else {
			sc.offset = Type.PTRSIZE * 2; // allow room for vptr[] and monitor
			alignsize = Type.PTRSIZE;
		}
		structsize = sc.offset;

		semanticScope(sc);

		Scope scsave = sc;
		int members_dim = members.size();
		sizeok = 0;
		for (i = 0; i < members_dim; i++) {
			Dsymbol s = members.get(i);
			s.semantic(sc, context);
		}

		if (sizeok == 2) { // semantic() failed because of forward
			// references.
			// Unwind what we did, and defer it for later
			fields.clear();
			structsize = 0;
			alignsize = 0;
			structalign = 0;

			sc = sc.pop();

			scope = scx != null ? scx : new Scope(sc);
			scope.setNoFree();
			scope.module.addDeferredSemantic(this, context);

			return;
		}

		structsize = sc.offset;
		// members.print();

		// Descent: changed to defer this part of the semantic analysis
		// see intiializeSpecial()

		sizeok = 1;

		context.Module_dprogress++;

		// TODO Semantic check if this is necessary for Descent
		// dtor = buildDtor(sc, context);

		sc.pop();
	}

	protected void unlazy(BaseClass bc, SemanticContext context) {
		unlazy(bc, CharOperation.NO_CHAR, context);
	}

	protected void unlazy(BaseClass bc, char[] prefix, SemanticContext context) {
		if (bc.type instanceof TypeClass) {
			unlazy((TypeClass) bc.type, prefix, context);
		}
		bc.base = bc.base == null ? null : bc.base.unlazy(prefix, context);
	}

	protected void unlazy(TypeClass type, SemanticContext context) {
		unlazy(type, CharOperation.NO_CHAR, context);
	}

	protected void unlazy(TypeClass type, char[] prefix, SemanticContext context) {
		type.sym = type.sym.unlazy(prefix, context);
		type.sym.baseClass = type.sym.baseClass == null ? null : type.sym.baseClass.unlazy(prefix, context);

		if (type.sym.baseclasses != null) {
			for(BaseClass bc : type.sym.baseclasses) {
				unlazy(bc, prefix, context);
			}
		}
	}

	/**********************************************************
	 * fd is in the vtbl[] for this class.
	 * Return 1 if function is hidden (not findable through search).
	 */

	private static int isf(Object param, FuncDeclaration fd)
	{
	    return param == fd ? 1 : 0;
	}

	public final static OverloadApply_fp isf = new OverloadApply_fp() {
		@Override
		public int call(Object param, FuncDeclaration f, SemanticContext context) {
			return isf(param, f);
		}
	};

	private boolean isFuncHidden(FuncDeclaration fd, SemanticContext context) {
		Dsymbol s = search(null, 0, fd.ident, 4 | 2, context);
		if (null == s) {
			/* Because, due to a hack, if there are multiple definitions
			 * of fd.ident, NULL is returned.
			 */
			return false;
		}

		if (context.isD2()) {
			s = s.toAlias(context);
			OverloadSet os = s.isOverloadSet();
			if (os != null) {
				for (int i = 0; i < size(os.a); i++) {
					Dsymbol s2 = (Dsymbol) os.a.get(i);
					FuncDeclaration f2 = s2.isFuncDeclaration();
					if (f2 != null && overloadApply(f2, isf, fd, context))
						return false;
				}
				return true;
			} else {
				FuncDeclaration fdstart = s.isFuncDeclaration();
				return !overloadApply(fdstart, isf, fd, context);
			}
		} else {
			FuncDeclaration fdstart = s.toAlias(context).isFuncDeclaration();
			return !overloadApply(fdstart, isf, fd, context);
		}
	}


	@Override
	public Dsymbol syntaxCopy(Dsymbol s, SemanticContext context) {
		ClassDeclaration cd;

		if (s != null) {
			cd = (ClassDeclaration) s;
		} else {
			cd = context.newClassDeclaration(filename, lineNumber, ident, null);
		}

		// Descent
		cd.templated = templated;

		cd.storage_class |= storage_class;

		cd.baseclasses = new BaseClasses(this.baseclasses.size());
		for (int i = 0; i < this.baseclasses.size(); i++) {
			BaseClass b = this.baseclasses.get(i);
			BaseClass b2 = new BaseClass(b.type.syntaxCopy(context), b.protection);
			cd.baseclasses.add(b2);
		}

		super.syntaxCopy(cd, context);

		cd.copySourceRange(this);
		cd.javaElement = javaElement;
		cd.templated = templated;

		return cd;
	}

	@Override
	public void toCBuffer(OutBuffer buf, HdrGenState hgs,
			SemanticContext context) {
		if (!isAnonymous()) {
			buf.writestring(kind());
			buf.writestring(toChars(context));
			if (baseclasses.size() > 0) {
				buf.writestring(" : ");
			}
		}
		for (int i = 0; i < baseclasses.size(); i++) {
			BaseClass b = baseclasses.get(i);

			if (i != 0) {
				buf.writeByte(',');
			}
			b.type.toCBuffer(buf, null, hgs, context);
		}
		buf.writenl();
		buf.writeByte('{');
		buf.writenl();
		if (members != null) {
			for (int i = 0; i < members.size(); i++) {
				Dsymbol s = members.get(i);

				buf.writestring("    ");
				s.toCBuffer(buf, hgs, context);
			}
		}
		buf.writestring("}");
		buf.writenl();
	}

	public int vtblOffset(SemanticContext context) {
		return 1;
	}

	@Override
	public String mangle(SemanticContext context) {
		Dsymbol parentsave = this.parent;

		/* These are reserved to the compiler, so keep simple
		 * names for them.
		 */
		if (ASTDmdNode.equals(this.ident, Id.Exception)) {
			if (this.parent.ident != null
					&& ASTDmdNode.equals(this.parent.ident, Id.object)) {
				this.parent = null;
			}
		} else if (ASTDmdNode.equals(this.ident, Id.TypeInfo)
				||
				//		CharOperation.equals(ident.ident, Id.Exception) ||
				ASTDmdNode.equals(this.ident, Id.TypeInfo_Struct)
				|| ASTDmdNode.equals(this.ident, Id.TypeInfo_Class)
				|| ASTDmdNode.equals(this.ident, Id.TypeInfo_Typedef)
				|| ASTDmdNode.equals(this.ident, Id.TypeInfo_Tuple)
				|| this == context.ClassDeclaration_object
				|| this == context.ClassDeclaration_classinfo
				|| this == context.Module_moduleinfo
				|| this.ident.toChars().startsWith("TypeInfo_")) {
			this.parent = null;
		}

		String id = Dsymbol_mangle(context);
		this.parent = parentsave;
		return id;
	}

	@Override
	public char getSignaturePrefix() {
		if (templated) {
			return Signature.C_TEMPLATED_CLASS;
		} else {
			return Signature.C_CLASS;
		}
	}

	@Override
	public ClassDeclaration unlazy(char[] prefix, SemanticContext context) {
		return this;
	}

	@Override
	protected void initializeSpecial(SemanticContext context) {
		Scope sc = specialInitializeScope;

		/*
		 * Look for special member functions. They must be in this class, not in
		 * a base class.
		 */
		ctor = (CtorDeclaration) search(filename, lineNumber, Id.ctor, 0, context);
		if (ctor != null && (ctor.toParent() != this || ctor.isCtorDeclaration() == null)) {
			ctor = null;
		}

		// dtor = (DtorDeclaration *)search(Id::dtor, 0);
		// if (dtor && dtor.toParent() != this)
		// dtor = NULL;

		// inv = (InvariantDeclaration *)search(Id::classInvariant, 0);
		// if (inv && inv.toParent() != this)
		// inv = NULL;

		// Can be in base class
		aggNew((NewDeclaration) search(filename, lineNumber, Id.classNew, 0, context));
		aggDelete((DeleteDeclaration) search(filename, lineNumber, Id.classDelete, 0, context));

		// If this class has no constructor, but base class does, create
		// a constructor:
		// this() { }
		if (ctor == null && baseClass != null && baseClass.ctor != null) {
			// toChars());
			CtorDeclaration ctor = new CtorDeclaration(filename, lineNumber, null, 0);
			ctor.fbody = context.newCompoundStatement(filename, lineNumber, new Statements(0));
			this.ctor = ctor;

			members.add(ctor);
			ctor.addMember(sc, this, 1, context);
			sc = scope; // why? What about sc.nofree?
			sc.offset = structsize;
			ctor.semantic(sc, context);
			defaultCtor = ctor;
		}

		 // Allocate instance of each new interface
        for (int i = 0; i < size(vtblInterfaces); i++)
        {
            BaseClass b = vtblInterfaces.get(i);
            int thissize = Type.PTRSIZE;

            int[] p_sc_offset = new int[]
            { sc.offset };
            alignmember(structalign, thissize, p_sc_offset);
            sc.offset = p_sc_offset[0];
            // SEMANTIC
            if (b.offset != 0) {
            	continue;
            }
            Assert.isTrue(b.offset == 0);
            b.offset = sc.offset; // Take care of single inheritance offsets
            while (b.baseInterfaces.size() > 0)
            {
                b = b.baseInterfaces.get(0);
                b.offset = sc.offset;
            }

            sc.offset += thissize;
            if (alignsize < thissize) {
				alignsize = thissize;
			}
        }

		structsize = sc.offset;
	}

}
