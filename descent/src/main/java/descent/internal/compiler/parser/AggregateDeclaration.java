package descent.internal.compiler.parser;

import static descent.internal.compiler.parser.PROT.PROTpackage;
import static descent.internal.compiler.parser.PROT.PROTpublic;
import static descent.internal.compiler.parser.STC.STCref;
import static descent.internal.compiler.parser.TY.Tsarray;
import static descent.internal.compiler.parser.TY.Tstruct;

import java.util.ArrayList;
import java.util.List;

import descent.core.IType__Marker;
import descent.core.compiler.IProblem;

public abstract class AggregateDeclaration extends ScopeDsymbol {

	public Type type;
	public PROT protection;
	public Type handle; // 'this' type
	public int storage_class;
	public boolean isdeprecated;
	public int structsize; // size of struct
	public int alignsize; // size of struct for alignment purposes
	public int structalign; // struct member alignment in effect
	public int hasUnions; // set if aggregate has overlapping fields
	public int sizeok; // set when structsize contains valid data
	// 0: no size
	// 1: size is correct
	// 2: cannot determine size; fwd referenced
	public boolean com; // !=0 if this is a COM class (meaning it derives from IUnknown)
	public boolean isauto; // !=0 if this is an auto class
	public boolean isabstract; // !=0 if abstract class
	public Scope scope; // !=NULL means context to use
	public boolean isnested;
	public VarDeclaration vthis;	// 'this' parameter if this aggregate is nested
    public FuncDeclarations dtors;	// Array of destructors
    public FuncDeclaration dtor;	// aggregate destructor

	// Special member functions
	public InvariantDeclaration inv; // invariant
	public NewDeclaration aggNew; // allocator
	public DeleteDeclaration aggDelete; // deallocator

	public List<VarDeclaration> fields;
	
	protected Dsymbol ctor;			// CtorDeclaration or TemplateDeclaration
	public CtorDeclaration defaultCtor;	// default constructor
	public Dsymbol aliasthis;			// forward unresolved lookups to aliasthis
	
	// Back end
    Symbol stag;		// tag symbol for debug data
    Symbol sinit;
	
	// Wether this aggregate is actually a templated aggregate 
	public boolean templated;
	
	protected IType__Marker javaElement;
	
	// Descent: whether special members (aggNew, aggDelete, ctor, etc.) were initialized
	protected boolean specialInitialized;

	public AggregateDeclaration(char[] filename, int lineNumber, IdentifierExp id) {
		super(id);
		this.filename = filename;
		this.lineNumber = lineNumber;
		fields = new ArrayList<VarDeclaration>(0);
	}
	
	protected void initializeSpecial(SemanticContext context) {
		
	}

	// The "reference" is not in DMD. It holds the source range of the node
	// that needs the access check, so that we can point errors in the correct place
	public void accessCheck(Scope sc, Dsymbol smember, SemanticContext context, ASTDmdNode reference) {
		boolean result;

		FuncDeclaration f = sc.func;
		AggregateDeclaration cdscope = sc.getStructClassScope();
		PROT access;

		Dsymbol smemberparent = smember.toParent();
		if (smemberparent == null
				|| smemberparent.isAggregateDeclaration() == null) {
			return; // then it is accessible
		}

		// BUG: should enable this check
		// assert(smember.parent.isBaseOf(this, NULL));

		// TODO don't do reference comparison
		if (smemberparent instanceof AggregateDeclaration && SemanticMixin.equals((AggregateDeclaration) smemberparent, this)) {
			PROT access2 = smember.prot();

			result = access2.level >= PROTpublic.level || this.hasPrivateAccess(f)
					|| this.isFriendOf(cdscope)
					|| (access2 == PROTpackage && ASTDmdNode.hasPackageAccess(sc, this));
		} else if ((access = this.getAccess(smember)).level >= PROTpublic.level) {
			result = true;
		} else if (access == PROTpackage && ASTDmdNode.hasPackageAccess(sc, this)) {
			result = true;
		} else {
			result = ASTDmdNode.accessCheckX(smember, f, this, cdscope);
		}
		if (!result) {
			if (context.acceptsErrors()) {
				context.acceptProblem(Problem.newSemanticTypeError(IProblem.MemberIsNotAccessible, reference, smember.toChars(context)));
			}
		}
	}

	public void addField(Scope sc, VarDeclaration v, SemanticContext context) {
		int memsize; // size of member
		int memalignsize; // size of member for alignment purposes
		int xalign; // alignment boundaries

		// Check for forward referenced types which will fail the size() call
		Type t = v.type.toBasetype(context);
		
		if (!context.isD1()) {
			if ((v.storage_class & STCref) != 0) {
				// References are the size of a pointer
				t = context.Type_tvoidptr;
			}
		}
		
		if (t.ty == TY.Tstruct /* && isStructDeclaration() */) {
			TypeStruct ts = (TypeStruct) t;
			
			if (context.isD2()) {
				if (ts.sym == this) {
					if (context.acceptsErrors()) {
						context.acceptProblem(Problem.newSemanticTypeErrorLoc(IProblem.CannotHaveFieldWithSameStructType, v));
					}
				}
			}

			if (ts.sym.sizeok != 1) {
				sizeok = 2; // cannot finish; flag as forward referenced
				return;
			}
		}
		if (t.ty == TY.Tident) {
			sizeok = 2; // cannot finish; flag as forward referenced
			return;
		}

		if (context.isD1()) {
			memsize = v.type().size(filename, lineNumber, context);
			memalignsize = v.type().alignsize(context);
			xalign = v.type().memalign(sc.structalign, context);
		} else {
			memsize = t.size(filename, lineNumber, context);
			memalignsize = t.alignsize(context);
			xalign = t.memalign(sc.structalign, context);
		}

		int[] sc_offset_pointer = { sc.offset };
		alignmember(xalign, memalignsize, sc_offset_pointer);
		sc.offset = sc_offset_pointer[0];

		v.offset(sc.offset);
		sc.offset += memsize;
		if (sc.offset > structsize) {
			structsize = sc.offset;
		}
		if (sc.structalign < memalignsize) {
			memalignsize = sc.structalign;
		}
		if (alignsize < memalignsize) {
			alignsize = memalignsize;
		}

		v.storage_class = STC.STCfield;
		if (fields == null) {
			fields = new ArrayList<VarDeclaration>();
		}
		fields.add(v);
	}

	public void alignmember(int salign, int size, int[] poffset) {
		if (salign > 1) {
			assert(size != 3);
			int sa = size;
			if (sa == 0 || salign < sa)
			    sa = salign;
			poffset[0] = (poffset[0] + sa - 1) & ~(sa - 1);
		}
	}

	public PROT getAccess(Dsymbol smember) {
		return PROT.PROTpublic;
	}

	@Override
	public Type getType(SemanticContext context) {
		return type;
	}
	
	/*****************************************
	 * Create inclusive destructor for struct/class by aggregating
	 * all the destructors in dtors[] with the destructors for
	 * all the members.
	 * Note the close similarity with StructDeclaration::buildPostBlit(),
	 * and the ordering changes (runs backward instead of forwards).
	 */

	public FuncDeclaration buildDtor(Scope sc, SemanticContext context) {
		Expression e = null;

		for (int i = 0; i < size(fields); i++) {
			Dsymbol s = (Dsymbol) fields.get(i);
			VarDeclaration v = s.isVarDeclaration();
			if ((v.storage_class & STCref) != 0)
			    continue;

			Type tv = v.type.toBasetype(context);
			int dim = 1;
			while (tv.ty == Tsarray) {
//				TypeSArray ta = (TypeSArray) tv;
				dim *= ((TypeSArray) tv).dim.toInteger(context).intValue();
				tv = tv.nextOf().toBasetype(context);
			}
			if (tv.ty == Tstruct) {
				TypeStruct ts = (TypeStruct) tv;
				StructDeclaration sd = ts.sym;
				if (sd.dtor != null) {
					Expression ex;

					// this.v
					ex = new ThisExp(null, 0);
					ex = new DotVarExp(null, 0, ex, v, false);

					if (dim == 1) { // this.v.dtor()
						ex = new DotVarExp(null, 0, ex, sd.dtor, false);
						ex = new CallExp(null, 0, ex);
					} else {
						// Typeinfo.destroy(cast(void*)&this.v);
						Expression ea = new AddrExp(null, 0, ex);
						ea = new CastExp(null, 0, ea, Type.tvoid
								.pointerTo(context));

						Expression et = v.type.getTypeInfo(sc, context);
						et = new DotIdExp(null, 0, et, Id.destroy);

						ex = new CallExp(null, 0, et, ea);
					}
					e = Expression.combine(ex, e); // combine in reverse order
				}
			}
		}

		/*
		 * Build our own "destructor" which executes e
		 */
		if (e != null) {
			DtorDeclaration dd = new DtorDeclaration(null, 0, new IdentifierExp(Id.__fieldDtor));
			dd.fbody = new ExpStatement(null, 0, e);
			if (dtors == null) {
				dtors = new FuncDeclarations(1);
			}
			dtors.add(0, dd);

			if (members == null) {
				members = new Dsymbols(1);
			}
			members.add(dd);
			dd.semantic(sc, context);
		}

		switch (size(dtors)) {
		case 0:
			return null;

		case 1:
			return (FuncDeclaration) dtors.get(0);

		default:
			e = null;
			for (int i = 0; i < size(dtors); i++) {
				FuncDeclaration fd = (FuncDeclaration) dtors.get(i);
				Expression ex = new ThisExp(null, 0);
				ex = new DotVarExp(null, 0, ex, fd, false);
				ex = new CallExp(null, 0, ex);
				e = Expression.combine(ex, e);
			}
			DtorDeclaration dd = new DtorDeclaration(null, 0, new IdentifierExp(Id.__aggrDtor));
			dd.fbody = new ExpStatement(null, 0, e);
			if (members == null) {
				members = new Dsymbols();
			}
			members.add(dd);
			dd.semantic(sc, context);
			return dd;
		}
	}

	/***************************************************************************
	 * Determine if smember has access to private members of this declaration.
	 */

	public boolean hasPrivateAccess(Dsymbol smember) {
		if (smember != null) {
			AggregateDeclaration cd = null;
			Dsymbol smemberparent = smember.toParent();
			if (smemberparent != null) {
				cd = smemberparent.isAggregateDeclaration();
			}

			if (SemanticMixin.equals(this, cd)) { // smember is a member of this class
				return true; // so we get private access
			}

			// If both are members of the same module, grant access
			while (true) {
				Dsymbol sp = smember.toParent();
				if (sp.isFuncDeclaration() != null
						&& smember.isFuncDeclaration() != null) {
					smember = sp;
				} else {
					break;
				}
			}
			// TODO check reference comparison
			if (cd == null && this.toParent() == smember.toParent()) {
				return true;
			}
			if (cd == null && this.getModule() == smember.getModule()) {
				return true;
			}
		}
		return false;
	}

	@Override
	public AggregateDeclaration isAggregateDeclaration() {
		return this;
	}

	@Override
	public boolean isDeprecated() {
		return isdeprecated;
	}

	/***************************************************************************
	 * Determine if this is the same or friend of cd.
	 */

	public boolean isFriendOf(AggregateDeclaration cd) {
		if (SemanticMixin.equals(this, cd)) {
			return true;
		}

		// Friends if both are in the same module
		// if (toParent() == cd.toParent())
		// TODO check reference comparison
		if (cd != null && this.getModule() == cd.getModule()) {
			return true;
		}

		return false;
	}
	
	public boolean isNested() {
		return isnested;
	}

	@Override
	public PROT prot() {
		return protection;
	}

	@Override
	public void semantic2(Scope sc, SemanticContext context) {
		if (scope != null) {
			if (context.acceptsErrors()) {
				context.acceptProblem(Problem.newSemanticTypeError(IProblem.SymbolHasForwardReferences, this, toChars(context)));
			}
		}
		if (members != null) {
			sc = sc.push(this);
			
			semantic2Scope(sc);
			
			for (int i = 0; i < members.size(); i++) {
				Dsymbol s = members.get(i);
				s.semantic2(sc, context);
			}
			sc.pop();
		}
	}

	@Override
	public void semantic3(Scope sc, SemanticContext context) {
		int i;

		if (members != null) {
			sc = sc.push(this);
			
			semantic3Scope(sc);
			
			for (i = 0; i < members.size(); i++) {
				Dsymbol s = members.get(i);
				s.semantic3(sc, context);
			}
			sc.pop();
		}
	}
	
	protected void semanticScope(Scope sc) {
		
	}
	
	protected void semantic2Scope(Scope sc) {
		
	}
	
	protected void semantic3Scope(Scope sc) {
		
	}

	@Override
	public int size(SemanticContext context) {
		if (null == members) {
			if (context.acceptsErrors()) {
				context.acceptProblem(Problem.newSemanticTypeError(
						IProblem.UnknownSize, this));
			}
		}
		if (sizeok != 1) {
			context.acceptProblem(Problem.newSemanticTypeError(
					IProblem.NoSizeYetForForwardReference, this));
		}
		return structsize;
	}

	public Symbol toInitializer() {
		// TODO semantic back-end
		if (null == sinit) {
			sinit = new Symbol();
		}
		return sinit;
	}
	
	@Override
	public int getLineNumber() {
		return lineNumber;
	}
	
	@Override
	public boolean templated() {
		return templated;
	}
	
	public void setJavaElement(IType__Marker javaElement) {
		this.javaElement = javaElement;
	}
	
	@Override
	public IType__Marker getJavaElement() {
		return javaElement;
	}
	
	@Override
	public PROT getProtection() {
		return protection;
	}
	
	@Override
	public int getStorageClass() {
		return storage_class;
	}
	
	@Override
	public Type type() {
		return type;
	}
	
	@Override
	public AggregateDeclaration unlazy(char[] prefix, SemanticContext context) {
		return this;
	}
	
	public NewDeclaration aggNew(SemanticContext context) {
		if (!specialInitialized) {
			specialInitialized = true;
			initializeSpecial(context);
		}
		return aggNew;
	}
	
	public void aggNew(NewDeclaration aggNew) {
		this.aggNew = aggNew;
	}
	
	public DeleteDeclaration aggDelete(SemanticContext context) {
		if (!specialInitialized) {
			specialInitialized = true;
			initializeSpecial(context);
		}
		return aggDelete;
	}
	
	public void aggDelete(DeleteDeclaration aggDelete) {
		this.aggDelete = aggDelete;
	}
	
	public CtorDeclaration ctor(SemanticContext context) {
		if (!specialInitialized) {
			specialInitialized = true;
			initializeSpecial(context);
		}
		return (CtorDeclaration) ctor;
	}
	
	public void ctor(CtorDeclaration ctor) {
		this.ctor = ctor;
	}
	
}
