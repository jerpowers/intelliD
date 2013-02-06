package descent.internal.compiler.parser;

import static descent.internal.compiler.parser.STC.STCdeprecated;

import melnorme.utilbox.core.Assert;

import descent.core.Flags;
import descent.core.IJavaElement__Marker;
import descent.core.compiler.IProblem;
import descent.internal.compiler.parser.ast.IASTVisitor;

public class Dsymbol extends ASTDmdNode {

	public static Arguments arraySyntaxCopy(Arguments a, SemanticContext context) {
		Arguments b = new Arguments(a.size());
		b.setDim(a.size());
		for(int i = 0; i < a.size(); i++) {
			Argument s = a.get(i);
			b.set(i, s.syntaxCopy(context));
		}
		return b;
	}

	public static Dsymbols arraySyntaxCopy(Dsymbols a, SemanticContext context) {
		Dsymbols b = null;
		if (a != null) {
			b = new Dsymbols(a.size());
			b.setDim(a.size());
			for(int i = 0; i < a.size(); i++) {
				Dsymbol s = a.get(i);
				b.set(i, s.syntaxCopy(null, context));
			}
		}
		return b;
	}

	public static boolean oneMembers(Dsymbols members, Dsymbol[] ps,
			SemanticContext context) {
		Dsymbol s = null;

		if (members != null) {
			for (int i = 0; i < members.size(); i++) {
				Dsymbol sx = members.get(i);

				boolean x = sx.oneMember(ps, context);
				if (!x) {
					Assert.isTrue(ps[0] == null);
					return false;
				}
				if (ps[0] != null) {
					if (s != null) { // more than one symbol
						ps[0] = null;
						return false;
					}
					s = ps[0];
				}
			}
		}
		ps[0] = s; // s is the one symbol, NULL if none
		return true;
	}

	public IdentifierExp ident;
	public IdentifierExp c_ident;
	public Dsymbol parent;
	public int lineNumber;
	public char[] filename;
	public Dsymbol overprevious; // previous in overload list

	public Dsymbol() {
	}

	public Dsymbol(IdentifierExp ident) {
		this.ident = ident;
		this.c_ident = null;
		this.parent = null;
	}

	@Override
	protected void accept0(IASTVisitor visitor) {
	}

	public void addLocalClass(ClassDeclarations aclasses, SemanticContext context) {
		// empty
	}

	public int addMember(Scope sc, ScopeDsymbol sd, int memnum,
			SemanticContext context) {
		parent = sd;
		if (!isAnonymous()) // no name, so can't add it to symbol table
		{
			// Descent: if it's null, problems were reported
			if (sd == null) {
				return 0;
			}

			if (sd.symtab.insert(this) == null) // if name is already defined
			{
				Dsymbol s2;

				s2 = sd.symtab.lookup(ident); // SEMANTIC
				if (!s2.overloadInsert(this, context)) {
					ScopeDsymbol.multiplyDefined(null, 0, this, s2, context);
				}
			}
			if (sd.isAggregateDeclaration() != null
					|| sd.isEnumDeclaration() != null) {
				if (equals(ident, Id.__sizeof)
						|| equals(ident, Id.alignof)
						|| equals(ident, Id.mangleof)) {
					if (context.acceptsErrors()) {
						context.acceptProblem(Problem.newSemanticMemberError(
								IProblem.PropertyCanNotBeRedefined, 0, ident.start,
								ident.length,
								new String(ident.ident)));
					}
				}
			}
			return 1;
		}
		return 0;
	}

	public void checkCtorConstInit(SemanticContext context) {
		// empty
	}

	// Added "reference" parameter in order to signal better errors
	public void checkDeprecated(Scope sc, SemanticContext context, ASTDmdNode reference) {
		if (!context.global.params.useDeprecated && isDeprecated()) {
			// Don't complain if we're inside a deprecated symbol's scope
			for (Dsymbol sp = sc.parent; sp != null; sp = sp.parent) {
				if (sp.isDeprecated()) {
					return;
				}
			}

			for (; sc != null; sc = sc.enclosing) {
				if (sc.scopesym != null && sc.scopesym.isDeprecated()) {
					return;
				}

				// If inside a StorageClassDeclaration that is deprecated
			    if ((sc.stc & STCdeprecated) != 0) {
			    	return;
			    }
			}

			if (context.acceptsErrors()) {
				context.acceptProblem(Problem.newSemanticTypeError(IProblem.SymbolIsDeprecated, reference, this.toChars(context)));
			}
		}
	}

	public void defineRef(Dsymbol s) {
		throw new IllegalStateException("Must be implemented by subclasses");
	}

	@Override
	public DYNCAST dyncast() {
		return DYNCAST.DYNCAST_DSYMBOL;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}

		if (!(o instanceof Dsymbol)) {
			return false;
		}

		Dsymbol s = (Dsymbol) (o);
		if (ident == null) {
			return s.ident == null;
		}
		return equals(ident, s.ident);
	}

	public Module getModule() {
		Module m;
		Dsymbol s;

		s = this;
		while (s != null) {
			m = s.isModule();
			if (m != null) {
				return m;
			}
			s = s.parent;
		}
		return null;
	}

	@Override
	public int getNodeType() {
		return 0;
	}

	public Type getType(SemanticContext context) {
		return null;
	}

	public boolean hasPointers(SemanticContext context) {
		return false;
	}

	public AggregateDeclaration isAggregateDeclaration() {
		return null;
	}

	public AliasDeclaration isAliasDeclaration() {
		return null;
	}

	public boolean isAnonymous() {
		return ident == null;
	}

	public AnonymousAggregateDeclaration isAnonymousAggregateDeclaration() {
		return null;
	}

	public ArrayScopeSymbol isArrayScopeSymbol() {
		return null;
	}

	public AttribDeclaration isAttribDeclaration() {
		return null;
	}

	public ClassDeclaration isClassDeclaration() {
		return null;
	}

	// are we a member of a class?
	public ClassDeclaration isClassMember() {
		Dsymbol parent = toParent();
		if (parent != null && parent.isClassDeclaration() != null) {
			return (ClassDeclaration) parent;
		}
		return null;
	}

	public CtorDeclaration isCtorDeclaration() {
		return null;
	}

	public Declaration isDeclaration() {
		return null;
	}

	public DeleteDeclaration isDeleteDeclaration() {
		return null;
	}

	public boolean isDeprecated() {
		return false;
	}

	public DtorDeclaration isDtorDeclaration() {
		return null;
	}

	public EnumDeclaration isEnumDeclaration() {
		return null;
	}

	public EnumMember isEnumMember() {
		return null;
	}

	public boolean isExport() {
		return false;
	}

	public boolean isforwardRef() {
		return false;
	}

	public FuncAliasDeclaration isFuncAliasDeclaration() {
		return null;
	}

	public FuncDeclaration isFuncDeclaration() {
		return null;
	}

	public FuncLiteralDeclaration isFuncLiteralDeclaration() {
		return null;
	}

	public Import isImport() {
		return null;
	}

	public boolean isImportedSymbol(SemanticContext context) {
		return false;
	}

	public InterfaceDeclaration isInterfaceDeclaration() {
		return null;
	}

	public InvariantDeclaration isInvariantDeclaration() {
		return null;
	}

	public LabelDsymbol isLabel() {
		return null;
	}

	/**
	 * Is this symbol a member of an AggregateDeclaration?
	 */
	public AggregateDeclaration isMember() {
		Dsymbol parent = toParent();
		return parent != null ? parent.isAggregateDeclaration() : null;
	}

	public Module isModule() {
		return null;
	}

	public NewDeclaration isNewDeclaration() {
		return null;
	}

	public boolean isOverloadable() {
		return false;
	}

	public OverloadSet isOverloadSet() {
		return null;
	}

	public Package isPackage() {
		return null;
	}

	public PostBlitDeclaration isPostBlitDeclaration() {
		return null;
	}

	public ScopeDsymbol isScopeDsymbol() {
		return null;
	}

	public StaticCtorDeclaration isStaticCtorDeclaration() {
		return null;
	}

	public StructDeclaration isStructDeclaration() {
		return null;
	}

	public SymbolDeclaration isSymbolDeclaration()
	{
	    return null;
	}

	public TemplateDeclaration isTemplateDeclaration() {
		return null;
	}

	public TemplateInstance isTemplateInstance() {
		return null;
	}

	public TemplateMixin isTemplateMixin() {
		return null;
	}

	/**
	 * Is a 'this' required to access the member?
	 */
	public AggregateDeclaration isThis() {
		return null;
	}

	public ThisDeclaration isThisDeclaration() {
		return null;
	}

	public TupleDeclaration isTupleDeclaration() {
		return null;
	}

	public TypedefDeclaration isTypedefDeclaration() {
		return null;
	}

	public UnionDeclaration isUnionDeclaration() {
		return null;
	}

	public UnitTestDeclaration isUnitTestDeclaration() {
		return null;
	}

	public VarDeclaration isVarDeclaration() {
		return null;
	}

	public WithScopeSymbol isWithScopeSymbol() {
		return null;
	}

	public String kind() {
		return "symbol";
	}

	public String kindForError(SemanticContext context) {
		StringBuilder sb = new StringBuilder();
		String p = locToChars(context);
		if (p != null) {
			sb.append(p);
			sb.append(": ");
		}
		if (isAnonymous()) {
			sb.append(kind());
			sb.append(" ");
		} else {
			sb.append(kind());
			sb.append(" ");
			sb.append(toPrettyChars(context));
			sb.append(" ");
		}
		return sb.toString();
	}

	public String mangle(SemanticContext context) {
		return Dsymbol_mangle(context);
	}

	protected String Dsymbol_mangle(SemanticContext context) {
		OutBuffer buf = new OutBuffer();
		String id;

		id = this.ident != null ? this.ident.toChars() : this.toChars(context);
		if (this.parent != null) {
			try {
				String p = this.parent.mangle(context);
				if (p.charAt(0) == '_' && p.charAt(1) == 'D') {
					p = p.substring(2);
				}
				buf.writestring(p);
			} catch (NullPointerException e) {
				e.printStackTrace();
			}
		}
		buf.data.append(id.length()).append(id);
		id = buf.toChars();
		buf.data = null;
		return id;
	}

	public boolean needThis() {
		return false;
	}

	public boolean oneMember(Dsymbol[] ps, SemanticContext context) {
		ps[0] = this;
		return true;
	}

	public boolean overloadInsert(Dsymbol s, SemanticContext context) {
		return false;
	}

	public Dsymbol pastMixin() {
		Dsymbol s = this;
		while (s != null && s.isTemplateMixin() != null) {
			s = s.parent;
		}
		return s;
	}

	public PROT prot() {
		return PROT.PROTpublic;
	}

	public Dsymbol search(char[] filename, int lineNumber, char[] ident, int flags,
			SemanticContext context) {
		return null;
	}

	/**
	 * Search for ident as member of s.
	 * Input:
	 *	flags:	1	don't find private members
	 *		2	don't give error messages
	 *		4	return NULL if ambiguous
	 * Returns:
	 *	NULL if not found
	 */
	public final Dsymbol search(char[] filename, int lineNumber, IdentifierExp ident, int flags,
			SemanticContext context) {
		return search(filename, lineNumber, ident.ident, flags, context);
	}

	/**
	 * Search for ident as member of s.
	 * Input:
	 *	flags:	1	don't find private members
	 *		2	don't give error messages
	 *		4	return NULL if ambiguous
	 * Returns:
	 *	NULL if not found
	 */
	public Dsymbol searchX(char[] filename, int lineNumber, Scope sc, IdentifierExp id,
			SemanticContext context) {
		Dsymbol s = this.toAlias(context);

		// Added for Descent
		if (s == null && context.global.errors > 0) {
			return null;
		}

		Dsymbol sm;

		switch (id.dyncast()) {
		case DYNCAST_IDENTIFIER:
			sm = s.search(filename, lineNumber, id, 0, context);
			break;

		case DYNCAST_DSYMBOL: { // It's a template instance
			Dsymbol st = ((TemplateInstanceWrapper) id).tempinst;
			TemplateInstance ti = st.isTemplateInstance();
			id = ti.name;
			sm = s.search(filename, lineNumber, id, 0, context);
			if (null == sm) {
				if (context.acceptsErrors()) {
					context.acceptProblem(Problem.newSemanticTypeError(
							IProblem.TemplateIdentifierIsNotAMemberOf, this, id.toChars(), s.kind(), s.toChars(context)));
				}
				return null;
			}
			sm = sm.toAlias(context);
			TemplateDeclaration td = sm.isTemplateDeclaration();
			if (null == td) {
				if (context.acceptsErrors()) {
					context.acceptProblem(Problem.newSemanticTypeError(
							IProblem.SymbolIsNotATemplate, this, id.toChars(), sm.kind()));
				}
				return null;
			}
			ti.tempdecl = td;
			if (0 == ti.semanticdone) {
				ti.semantic(sc, context);
			}
			sm = ti.toAlias(context);
			break;
		}

		default:
			throw new IllegalStateException("assert(0);");
		}
		return sm;
	}

	public void semantic(Scope sc, SemanticContext context) {
		throw new IllegalStateException("No semantic routine for " + this);
	}

	public void semantic2(Scope sc, SemanticContext context) {

	}

	public void semantic3(Scope sc, SemanticContext context) {

	}

	public int size(SemanticContext context) {
		if (context.acceptsErrors()) {
			context.acceptProblem(Problem.newSemanticTypeError(
					IProblem.DSymbolHasNoSize, this, toChars(context)));
		}
		return 0;
	}

	public Dsymbol syntaxCopy(Dsymbol s, SemanticContext context) {
		throw new IllegalStateException("Must be implemented by subclasses");
	}

	public Dsymbol toAlias(SemanticContext context) {
		return this;
	}

	public Symbol toSymbol() {
		throw new IllegalStateException("assert(0); // implement");
	}

	public void toCBuffer(OutBuffer buf, HdrGenState hgs, SemanticContext context) {
		buf.writestring(this.toChars(context));
	}

	@Override
	public String toChars(SemanticContext context) {
		return (this.ident != null && this.ident.ident != null) ? this.ident.toChars() : "__anonymous";
	}

	public TemplateInstance inTemplateInstance() {
		for (Dsymbol parent = this.parent; parent != null; parent = parent.parent) {
			TemplateInstance ti = parent.isTemplateInstance();
			if (ti != null)
				return ti;
		}
		return null;
	}

	public Dsymbol toParent() {
		return this.parent != null ? this.parent.pastMixin() : null;
	}

	public Dsymbol toParent2() {
		Dsymbol s = this.parent;
		while (s != null && s.isTemplateInstance() != null) {
			s = s.parent;
		}
		return s;
	}

	@Override
	public String toPrettyChars(SemanticContext context) {
		// TODO semantic
		return toChars(context);
	}

	public String locToChars(SemanticContext context) {
		if (filename != null) {
			return new String(filename);
		} else {
			return null;
		}
	}

	@Override
	public int getErrorStart() {
		if (ident != null) {
			return ident.getErrorStart();
		}
		return super.getErrorStart();
	}

	@Override
	public int getErrorLength() {
		if (ident != null) {
			return ident.getErrorLength();
		}
		return super.getErrorLength();
	}

	@Override
	public int getLineNumber() {
		return lineNumber;
	}
	@Override
	public void setLineNumber(int lineNumber) {
		this.lineNumber = lineNumber;
	}

	// For Descent, used to get the type of a symbol
	public Type type() {
		return null;
	}

	public final String getSignature() {
		return getSignature(ISignatureOptions.Default);
	}

	public String getSignature(int options) {
		return SemanticMixin.getSignature(this, options);
	}

	public void appendSignature(StringBuilder sb, int options) {
		SemanticMixin.appendSignature(this, options, sb);
	}

	public IJavaElement__Marker getJavaElement() {
		return null;
	}

	public char getSignaturePrefix() {
		return 0;
	}

	/**
	 * This method is only intended to be invoked if the module
	 * is resolved.
	 */
	public long getFlags() {
		long flags = 0;
		long storage_class = getStorageClass();
		PROT protection = getProtection();

		if ((storage_class & STC.STCabstract) != 0) flags |= Flags.AccAbstract;
		if ((storage_class & STC.STCauto) != 0) flags |= Flags.AccAuto;
		// STC.STCcomdat
		if ((storage_class & STC.STCconst) != 0) flags |= Flags.AccConst;
		if ((storage_class & STC.STCctorinit) != 0) flags |= Flags.AccConst;
		if ((storage_class & STC.STCdeprecated) != 0) flags |= Flags.AccDeprecated;
		if ((storage_class & STC.STCextern) != 0) flags |= Flags.AccExtern;
		// STC.STCfield
		if ((storage_class & STC.STCfinal) != 0) flags |= Flags.AccFinal;
		// STC.STCforeach
		if ((storage_class & STC.STCin) != 0) flags |= Flags.AccIn;
		if ((storage_class & STC.STCinvariant) != 0) flags |= Flags.AccInvariant;
		if ((storage_class & STC.STClazy) != 0) flags |= Flags.AccLazy;
		if ((storage_class & STC.STCout) != 0) flags |= Flags.AccOut;
		if ((storage_class & STC.STCoverride) != 0) flags |= Flags.AccOverride;
		// STC.STCparameter
		if ((storage_class & STC.STCref) != 0) flags |= Flags.AccRef;
		if ((storage_class & STC.STCscope) != 0) flags |= Flags.AccScope;
		if ((storage_class & STC.STCstatic) != 0) flags |= Flags.AccStatic;
		if ((storage_class & STC.STCsynchronized) != 0) flags |= Flags.AccSynchronized;
		if ((storage_class & STC.STCpure) != 0) flags |= Flags.AccPure;
		if ((storage_class & STC.STCnothrow) != 0) flags |= Flags.AccNothrow;

		if (protection == null) {
			flags |= Flags.AccPublic;
		} else {
			switch(protection) {
			case PROTexport: flags |= Flags.AccExport; break;
			case PROTnone: break;
			case PROTpackage: flags |= Flags.AccPackage; break;
			case PROTprivate: flags |= Flags.AccPrivate; break;
			case PROTprotected: flags |= Flags.AccProtected; break;
			case PROTpublic: flags |= Flags.AccPublic; break;
			case PROTundefined: break;
			}
		}

		if (isDeprecated()) {
			flags |= Flags.AccDeprecated;
		}

		return flags;
	}

	/*
	 * For Descent, to make it easier to calcualte flags.
	 */
	public int getStorageClass() {
		return 0;
	}

	/*
	 * For Descent, to make it easier to calcualte flags.
	 */
	public PROT getProtection() {
		return PROT.PROTundefined;
	}

	public Dsymbol effectiveParent() {
		Dsymbol p = parent;
		while(p instanceof FuncLiteralDeclaration) {
			p = p.parent;
		}
		return p;
	}

	public boolean templated() {
		return false;
	}

}
