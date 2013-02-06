package descent.internal.compiler.parser;

import static descent.internal.compiler.parser.STC.STCfield;
import static descent.internal.compiler.parser.STC.STCin;
import static descent.internal.compiler.parser.STC.STCmanifest;
import descent.core.compiler.IProblem;


public abstract class Declaration extends Dsymbol {

	public Type type;
	public Type originalType;
	public Type sourceType;
	public int storage_class;
	public LINK linkage;
	public PROT protection;
	public int inuse;

	public Declaration(IdentifierExp ident) {
		super(ident);
		this.type = null;
		this.originalType = null;
		this.storage_class = STC.STCundefined;
		this.protection = PROT.PROTundefined;
		this.linkage = LINK.LINKdefault;
		this.inuse = 0;
	}

	@Override
	public void semantic(Scope sc, SemanticContext context) {

	}
	
	/*************************************
	 * Check to see if declaration can be modified in this context (sc).
	 * Issue error if not.
	 */

	public void checkModify(char[] filename, int lineNumber, Scope sc, Type t, SemanticContext context) {
		if (sc.incontract != 0 && isParameter()) {
			if (context.acceptsErrors()) {
				context.acceptProblem(Problem.newSemanticTypeError(IProblem.CannotModifyParameterInContract, this, toChars(context)));
			}
		}

		if (isCtorinit()) { // It's only modifiable if inside the right
							// constructor
			Dsymbol s = sc.func;
			while (true) {
				FuncDeclaration fd = null;
				if (s != null)
					fd = s.isFuncDeclaration();
				if (fd != null
						&& ((fd.isCtorDeclaration() != null && (storage_class & STCfield) != 0) || (fd
								.isStaticCtorDeclaration() != null && 0 == (storage_class & STCfield)))
						&& fd.toParent() == toParent()) {
					VarDeclaration v = isVarDeclaration();
					v.ctorinit = true;
				} else {
					if (s != null) {
						s = s.toParent2();
						continue;
					} else {
						if (context.acceptsErrors()) {
							String p = isStatic() ? "static " : "";
							context.acceptProblem(Problem.newSemanticTypeError(IProblem.CanOnlyInitiailizeConstMemberInsideConstructor, this, p, toChars(context), p));
						}
					}
				}
				break;
			}
		} else {
			VarDeclaration v = isVarDeclaration();
			if (v != null && v.canassign == 0) {
				String p = null;
				if (isConst())
					p = "const";
				else if (isInvariant(context))
					p = "mutable";
				else if ((storage_class & STCmanifest) != 0)
					p = "enum";
				else if (!t.isAssignable(context))
					p = "struct with immutable members";
				if (p != null) {
					if (context.acceptsErrors()) {
						context.acceptProblem(Problem.newSemanticTypeError(IProblem.CannotModifySymbol, this, p));
					}
					// halt();
				}
			}
		}
	}

	@Override
	public String kind() {
		return "declaration";
	}

	@Override
	public int size(SemanticContext context) {
		return type.size(filename, lineNumber, context);
	}

	public boolean isStaticConstructor() {
		return false;
	}

	public boolean isStaticDestructor() {
		return false;
	}

	@Override
	public Declaration isDeclaration() {
		return this;
	}

	public boolean isDelete() {
		return false;
	}

	public boolean isDataseg(SemanticContext context) {
		return false;
	}

	public boolean isCodepseg() {
		return false;
	}
	
	public boolean isThreadLocal() {
		return false;
	}

	@Override
	public PROT prot() {
		return protection;
	}

	public boolean isCtorinit() {
		return (this.storage_class & STC.STCctorinit) != 0;
	}

	public boolean isFinal() {
		return (this.storage_class & STC.STCfinal) != 0;
	}

	public boolean isAbstract() {
		return (this.storage_class & STC.STCabstract) != 0;
	}

	public boolean isConst() {
		return (this.storage_class & STC.STCconst) != 0;
	}
	
	public boolean isInvariant(SemanticContext context) {
		if (context.isD1()) {
			return false;
		} else {
			return (this.storage_class & STC.STCinvariant) != 0;
		}
	}

	public boolean isAuto() {
		return (this.storage_class & STC.STCauto) != 0;
	}

	public boolean isScope() {
		return (this.storage_class & (STC.STCscope | STC.STCauto)) != 0;
	}

	public boolean isStatic() {
		return (this.storage_class & (STC.STCstatic)) != 0;
	}

	public boolean isSynchronized() {
		return (storage_class & STC.STCsynchronized) != 0;
	}

	public boolean isParameter() {
		return (this.storage_class & (STC.STCparameter)) != 0;
	}

	@Override
	public boolean isDeprecated() {
		return (storage_class & STC.STCdeprecated) != 0;
	}

	public boolean isOverride() {
		return (storage_class & STC.STCoverride) != 0;
	}
	
	public boolean isIn() {
		return (storage_class & STCin) != 0;
	}

	public boolean isOut() {
		return (this.storage_class & (STC.STCout)) != 0;
	}

	public boolean isRef() {
		return (this.storage_class & (STC.STCref)) != 0;
	}

	@Override
	public String mangle(SemanticContext context) {
		boolean condition;
		if (context.isD2()) {
			condition = null == parent || parent.isModule() != null || linkage == LINK.LINKcpp;
		} else {
			condition = null == parent || parent.isModule() != null;
		}
		
		if (condition) { 
			// if at global scope
			// If it's not a D declaration, no mangling
			switch (linkage) {
			case LINKd:
				break;

			case LINKc:
			case LINKwindows:
			case LINKpascal:
			case LINKcpp:
				return ident.toChars();

			case LINKdefault:
				if (context.acceptsErrors()) {
					context.acceptProblem(Problem.newSemanticTypeError(
							IProblem.ForwardDeclaration, this));
				}
				return ident.toChars();

			default:
				throw new IllegalStateException("assert(0);");
			}
		}
		String p = mangle(this);
		OutBuffer buf = new OutBuffer();
		buf.writestring("_D");
		buf.writestring(p);
		p = buf.toChars();
		buf.data = null;
		return p;
	}
	@Override
	public Type type() {
		return type;
	}
	
	@Override
	public final PROT getProtection() {
		return protection;
	}
	
	@Override
	public final int getStorageClass() {
		return storage_class;
	}

}
