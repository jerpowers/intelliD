package descent.internal.compiler.parser;

import static descent.internal.compiler.parser.LINK.LINKd;
import static descent.internal.compiler.parser.PROT.PROTpublic;

import melnorme.utilbox.core.Assert;

import descent.core.compiler.IProblem;


public class Scope {

	public final static int CSXthis_ctor = 0x0001;
	public final static int CSXsuper_ctor = 0x0002;
	public final static int CSXthis = 0x0004;
	public final static int CSXsuper = 0x0008;
	public final static int CSXlabel = 0x0010;
	public final static int CSXreturn = 0x0020;
	public final static int CSXany_ctor = 0x0040;

	public final static int SCOPEctor = 0x0001; // constructor type
	public final static int SCOPEstaticif = 0x0002; // inside static if

	public static Scope createGlobal(Module module, SemanticContext context) {
		Scope sc;

		sc = new Scope(context);
		sc.module = module;
		sc.scopesym = new ScopeDsymbol();
		sc.scopesym.symtab = new DsymbolTable();

		// Add top level package as member of this global scope
		Dsymbol m = module;
		while (m.parent != null) {
			m = m.parent;
		}

		m.addMember(null, sc.scopesym, 1, context);
		m.parent = null; // got changed by addMember()

		// Create the module scope underneath the global scope
		sc = sc.push(module);
		sc.parent = module;
		return sc;
	}
	public Scope enclosing; // enclosing Scope
	public Module module; // Root module
	public ScopeDsymbol scopesym; // current symbol
	public ScopeDsymbol sd; // if in static if, and declaring new symbols,
	public FuncDeclaration func; // function we are in
	public Dsymbol parent; // parent to use
	public LabelStatement slabel; // enclosing labelled statement
	public SwitchStatement sw; // enclosing switch statement
	public TryFinallyStatement tf; // enclosing try finally statement
    public TemplateInstance tinst;    // enclosing template instance
	public Statement sbreak; // enclosing statement that supports "break"
	public Statement scontinue; // enclosing statement that supports "continue"
	public ForeachStatement fes; // if nested function for ForeachStatement, this is it
	public int offset; // next offset to use in aggregate
	public boolean inunion; // we're processing members of a union
	public int incontract; // we're inside contract code
	public boolean nofree; // set if shouldn't free it
	public int noctor; // set if constructor calls aren't allowed
	public int intypeof; // in typeof(exp)
	public int parameterSpecialization; // // if in template parameter specialization
    public int noaccesscheck;		// don't do access checks
	public int callSuper; // primitive flow analysis for constructors
	public int structalign; // alignment for struct members
	public LINK linkage; // linkage for external functions
	public PROT protection; // protection for class members
	public int explicitProtection; // set if in an explicit protection attribute
	public int stc; // storage class
	public int flags;

	public AnonymousAggregateDeclaration anonAgg; // for temporary analysis

	/*
	 * The number of this scope, for determining the correct signature of local
	 * variables. The first scope is 0, the second is 1. For example:
	 * void foo() {
	 *   { 0 }
	 *   { 1 { 0 } { 1 } }
	 *   { 2 }
	 * }
	 */
	public int numberForLocalVariables = -1;

	public Scope() {

	}

	public Scope(SemanticContext context) {
		this.module = null;
		this.scopesym = null;
		this.sd = null;
		this.enclosing = null;
		this.parent = null;
		this.sw = null;
		this.tf = null;
		this.tinst = null;
		this.sbreak = null;
		this.scontinue = null;
		this.fes = null;
		this.structalign = context.global.structalign;
		this.func = null;
		this.slabel = null;
		this.linkage = LINKd;
		this.protection = PROTpublic;
		this.explicitProtection = 0;
		this.stc = 0;
		this.offset = 0;
		this.inunion = false;
		this.incontract = 0;
		this.nofree = false;
		this.noctor = 0;
		this.noaccesscheck = 0;
		this.intypeof = 0;
		this.parameterSpecialization = 0;
		this.callSuper = 0;
		this.flags = 0;
		this.anonAgg = null;
	}

	public Scope(Scope enclosing) {
		this.module = enclosing.module;
		this.func = enclosing.func;
		this.parent = enclosing.parent;
		this.scopesym = null;
		this.sd = null;
		this.sw = enclosing.sw;
		this.tf = enclosing.tf;
		this.tinst = enclosing.tinst;
		this.sbreak = enclosing.sbreak;
		this.scontinue = enclosing.scontinue;
		this.fes = enclosing.fes;
		this.structalign = enclosing.structalign;
		this.enclosing = enclosing;
		this.slabel = null;
		this.linkage = enclosing.linkage;
		this.protection = enclosing.protection;
		this.explicitProtection = enclosing.explicitProtection;
		this.stc = enclosing.stc;
		this.offset = 0;
		this.inunion = enclosing.inunion;
		this.incontract = enclosing.incontract;
		this.nofree = false;
		this.noctor = enclosing.noctor;
		this.noaccesscheck = enclosing.noaccesscheck;
		this.intypeof = enclosing.intypeof;
		this.parameterSpecialization = enclosing.parameterSpecialization;
		this.callSuper = enclosing.callSuper;
		this.flags = 0;
		this.anonAgg = null;
	}

	public static Scope copy(Scope scope) {
		Scope copy = new Scope();
		copy.enclosing = scope.enclosing;
		copy.module = scope.module;
		copy.scopesym = scope.scopesym;
		copy.sd = scope.sd;
		copy.func = scope.func;
		copy.parent = scope.parent;
		copy.slabel = scope.slabel;
		copy.sw = scope.sw;
		copy.tf = scope.tf;
		copy.sbreak = scope.sbreak;
		copy.scontinue = scope.scontinue;
		copy.fes = scope.fes;
		copy.offset = scope.offset;
		copy.inunion = scope.inunion;
		copy.incontract = scope.incontract;
		copy.nofree = scope.nofree;
		copy.noctor = scope.noctor;
		copy.intypeof = scope.intypeof;
		copy.parameterSpecialization = scope.parameterSpecialization;
		copy.noaccesscheck = scope.noaccesscheck;
		copy.callSuper = scope.callSuper;
		copy.structalign = scope.structalign;
		copy.linkage = scope.linkage;
		copy.protection = scope.protection;
		copy.explicitProtection = scope.explicitProtection;
		copy.stc = scope.stc;
		copy.flags = scope.flags;
		return copy;
	}

	public void error(String s) {
		throw new IllegalStateException("Problem reporting not implemented");
	}

	public ClassDeclaration getClassScope() {
		Scope sc;

		for (sc = this; sc != null; sc = sc.enclosing) {
			ClassDeclaration cd;

			if (sc.scopesym != null) {
				cd = sc.scopesym.isClassDeclaration();
				if (cd != null) {
					return cd;
				}
			}
		}
		return null;
	}

	public AggregateDeclaration getStructClassScope() {
		Scope sc;

		for (sc = this; sc != null; sc = sc.enclosing) {
			AggregateDeclaration ad;

			if (sc.scopesym != null) {
				ad = sc.scopesym.isClassDeclaration();
				if (ad != null) {
					return ad;
				} else {
					ad = sc.scopesym.isStructDeclaration();
					if (ad != null) {
						return ad;
					}
				}
			}
		}
		return null;
	}

	public Dsymbol insert(Dsymbol s) {
		Scope sc;

		for (sc = this; sc != null; sc = sc.enclosing) {
			if (sc.scopesym != null) {
				if (sc.scopesym.symtab == null) {
					sc.scopesym.symtab = new DsymbolTable();
				}
				return sc.scopesym.symtab.insert(s);
			}
		}
		Assert.isTrue(false);
		return null;
	}

	public void mergeCallSuper(char[] filename, int lineNumber, int cs, ASTDmdNode reference, SemanticContext context) {
		// This does a primitive flow analysis to support the restrictions
		// regarding when and how constructors can appear.
		// It merges the results of two paths.
		// The two paths are callSuper and cs; the result is merged into
		// callSuper.

		if (cs != callSuper) {
			boolean a;
			boolean b;

			callSuper |= cs & (CSXany_ctor | CSXlabel);
			if ((cs & CSXreturn) != 0) {
			} else if ((callSuper & CSXreturn) != 0) {
				callSuper = cs | (callSuper & (CSXany_ctor | CSXlabel));
			} else {
				a = (cs & (CSXthis_ctor | CSXsuper_ctor)) != 0;
				b = (callSuper & (CSXthis_ctor | CSXsuper_ctor)) != 0;
				if (a != b) {
					if (context.acceptsErrors()) {
						context.acceptProblem(Problem.newSemanticTypeError(IProblem.OnePathSkipsConstructor, reference));
					}
				}
				callSuper |= cs;
			}
		}
	}

	public Scope pop() {
		Scope enc = enclosing;

		if (enclosing != null) {
			enclosing.callSuper |= callSuper;
		}

		return enc;
	}

	public Scope push() {
		Scope s = new Scope(this);
		assert (this != s);
		return s;
	}

	public Scope push(ScopeDsymbol ss) {
		Scope s = push();
		s.scopesym = ss;
		return s;
	}

	public Dsymbol search(char[] filename, int lineNumber, IdentifierExp ident, Dsymbol[] pscopesym,
			SemanticContext context) {
		Dsymbol s;
		Scope sc;

		if (ASTDmdNode.equals(ident, Id.empty)) {
			// Look for module scope
			for (sc = this; sc != null; sc = sc.enclosing) {
				assert (sc != sc.enclosing);
				if (sc.scopesym != null) {
					s = sc.scopesym.isModule();
					if (s != null) {
						if (pscopesym != null) {
							pscopesym[0] = sc.scopesym;
						}
						return s;
					}
				}
			}
			return null;
		}

		for (sc = this; sc != null; sc = sc.enclosing) {
			assert (sc != sc.enclosing);
			if (sc.scopesym != null) {
				s = sc.scopesym.search(filename, lineNumber, ident, 0, context);
				if (s != null) {
					if ((context.global.params.warnings)
							&& ASTDmdNode.equals(ident, Id.length)
							&& sc.scopesym.isArrayScopeSymbol() != null
							&& sc.enclosing != null
							&& sc.enclosing.search(filename, lineNumber, ident, null, context) != null) {
						if (context.acceptsWarnings()) {
							context.acceptProblem(Problem.newSemanticTypeWarning(IProblem.ArrayLengthHidesOtherLengthNameInOuterScope, ident));
						}
					}

					if (pscopesym != null) {
						pscopesym[0] = sc.scopesym;
					}
					return s;
				}
			}
		}

		return null;
	}

	public void setNoFree() {
		Scope sc;
		for (sc = this; sc != null; sc = sc.enclosing) {
			sc.nofree = true;
		}
	}

}
