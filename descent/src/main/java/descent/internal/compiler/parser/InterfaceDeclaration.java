package descent.internal.compiler.parser;

import static descent.internal.compiler.parser.LINK.LINKcpp;
import static descent.internal.compiler.parser.LINK.LINKwindows;
import static descent.internal.compiler.parser.STC.STC_TYPECTOR;
import static descent.internal.compiler.parser.STC.STCabstract;
import static descent.internal.compiler.parser.STC.STCauto;
import static descent.internal.compiler.parser.STC.STCdeprecated;
import static descent.internal.compiler.parser.STC.STCfinal;
import static descent.internal.compiler.parser.STC.STCgshared;
import static descent.internal.compiler.parser.STC.STCscope;
import static descent.internal.compiler.parser.STC.STCstatic;
import static descent.internal.compiler.parser.STC.STCtls;
import static descent.internal.compiler.parser.TY.Tclass;
import static descent.internal.compiler.parser.TY.Ttuple;
import melnorme.utilbox.tree.TreeVisitor;

import melnorme.utilbox.core.Assert;

import descent.core.Flags;
import descent.core.Signature;
import descent.core.compiler.IProblem;
import descent.internal.compiler.parser.ast.IASTVisitor;


public class InterfaceDeclaration extends ClassDeclaration {

	public boolean cpp; // true if this is a C++ interface

	public InterfaceDeclaration(char[] filename, int lineNumber, IdentifierExp id,
			BaseClasses baseclasses) {
		super(filename, lineNumber, id, baseclasses);
		com = false;
		if (id != null && equals(id, Id.IUnknown)) { // IUnknown is the root
			// of all COM
			// objects
			com = true;
			cpp = true;
		}
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
	public int getNodeType() {
		return INTERFACE_DECLARATION;
	}

	public boolean isBaseOf(BaseClass bc, int[] poffset) {
		for (int j = 0; j < ASTDmdNode.size(bc.baseInterfaces); j++) {
			BaseClass b = bc.baseInterfaces.get(j);

			if (SemanticMixin.equals(this, b.base)) {
				if (poffset != null) {
					poffset[0] = b.offset;
					if (j != 0 && bc.base.isInterfaceDeclaration() != null)
					    poffset[0] = OFFSET_RUNTIME;
				}
				return true;
			}
			if (this.isBaseOf(b, poffset)) {
				if (j != 0 && poffset != null && bc.base.isInterfaceDeclaration() != null)
					poffset[0] = OFFSET_RUNTIME;
				return true;
			}
		}
		if (poffset != null) {
			poffset[0] = 0;
		}
		return false;
	}

	@Override
	public boolean isBaseOf(ClassDeclaration cd, int[] poffset,
			SemanticContext context) {
		int j;

		Assert.isTrue(baseClass == null);

		if (cd != null && cd.interfaces != null) {
			for (j = 0; j < cd.interfaces.size(); j++) {
				BaseClass b = cd.interfaces.get(j);

				if (SemanticMixin.equals(this, b.base)) {
					if (poffset != null) {
						poffset[0] = b.offset;
						if (j != 0 && cd.isInterfaceDeclaration() != null) {
							poffset[0] = OFFSET_RUNTIME;
						}
					}
					return true;
				}
				if (isBaseOf(b, poffset)) {
					if (j != 0 && poffset != null
							&& cd.isInterfaceDeclaration() != null) {
						poffset[0] = OFFSET_RUNTIME;
					}
					return true;
				}
			}
		}

		if (cd.baseClass != null && isBaseOf(cd.baseClass, poffset, context)) {
			return true;
		}

		if (poffset != null) {
			poffset[0] = 0;
		}
		return false;
	}

	@Override
	public InterfaceDeclaration isInterfaceDeclaration() {
		return this;
	}

	@Override
	public String kind() {
		return "interface";
	}

	@Override
	public void semantic(Scope sc, SemanticContext context) {
		int i;

		if (scope == null) {
			type = type.semantic(filename, lineNumber, sc, context);
			handle = handle.semantic(filename, lineNumber, sc, context);
		}
		if (members == null) // if forward reference
		{
			return;
		}
		if (symtab != null) // if already done
		{
			if (scope == null) {
				return;
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

	    if ((sc.stc & STC.STCdeprecated) != 0) {
			isdeprecated = true;
		}

		// Expand any tuples in baseclasses[]
		for (i = 0; i < baseclasses.size();) {
			BaseClass b = baseclasses.get(0);

			b.type = b.type.semantic(filename, lineNumber, sc, context);

			if (getModule() == context.Module_rootModule) {
				unlazy(b, context);
			}

			if (getModule() == context.Module_rootModule && b.type instanceof TypeClass) {
				unlazy(b, context);
			}

			Type tb = b.type.toBasetype(context);

			if (tb.ty == Ttuple) {
				TypeTuple tup = (TypeTuple) tb;
				PROT protection = b.protection;
				baseclasses.remove(i);
				int dim = Argument.dim(tup.arguments, context);
				for (int j = 0; j < dim; j++) {
					Argument arg = Argument.getNth(tup.arguments, j, context);
					b = new BaseClass(arg.type, protection);
					baseclasses.set(i + j, b);
				}
			} else {
				i++;
			}
		}

		if (!context.isD1()) {
		    if (0 == size(baseclasses) && sc.linkage == LINKcpp) {
		    	cpp = true;
		    }
		}

		// Check for errors, handle forward references
		for (i = 0; i < baseclasses.size();) {
			TypeClass tc;
			BaseClass b;
			Type tb;

			b = baseclasses.get(i);
			b.type = b.type.semantic(filename, lineNumber, sc, context);
			tb = b.type.toBasetype(context);
			if (tb.ty == Tclass) {
				tc = (TypeClass) tb;
			} else {
				tc = null;
			}
			if (tc == null || tc.sym.isInterfaceDeclaration() == null) {
				if (context.acceptsErrors()) {
					context.acceptProblem(Problem.newSemanticTypeError(
							IProblem.BaseTypeMustBeInterface, b.sourceType));
				}
				baseclasses.remove(i);
				continue;
			} else {
				// Check for duplicate interfaces
				for (int j = 0; j < i; j++) {
					BaseClass b2 = baseclasses.get(j);
					if (b2.base == tc.sym) {
						if (context.acceptsErrors()) {
							context.acceptProblem(Problem.newSemanticTypeErrorLoc(
									IProblem.InterfaceInheritsFromDuplicateInterface, b2, new String[] { toChars(context), b2.base.toChars(context) }));
						}
					}
				}

				b.base = tc.sym;
				if (b.base == this || isBaseOf2(b.base)) {
					if (context.acceptsErrors()) {
						context.acceptProblem(Problem.newSemanticTypeErrorLoc(IProblem.CircularInheritanceOfInterface, b));
					}
					baseclasses.remove(i);
					continue;
				}
				if (b.base.symtab == null || b.base.scope != null) {
					// Forward reference of base, try again later
					scope = scx != null ? scx : new Scope(sc);
					scope.setNoFree();
					scope.module.addDeferredSemantic(this, context);
					return;
				}
			}
			i++;
		}

		interfaces = new BaseClasses(baseclasses);

		interfaceSemantic(sc, context);

		if (vtblOffset(context) != 0) {
			vtbl.add(this); // leave room at vtbl[0] for classinfo
		}

		// Cat together the vtbl[]'s from base interfaces
		Lcontinue: for (i = 0; i < interfaces.size(); i++) {
			BaseClass b = interfaces.get(i);

			// Skip if b has already appeared
			for (int k = 0; k < i; k++) {
				if (b == interfaces.get(i)) {
					// goto Lcontinue;
					continue Lcontinue;
				}
			}

			// Copy vtbl[] from base class
			if (b.base.vtblOffset(context) != 0) {

				// Descent: to get "override" errors ok
				b.base = (ClassDeclaration) b.base.unlazy(context);

				int d = b.base.vtbl.size();
				if (d > 1) {
					for (int j = 1; j < d; j++) {
						vtbl.add(b.base.vtbl.get(j));
					}
				}
			} else {
				vtbl.add(b.base.vtbl);
			}

			// Lcontinue: ;
		}

		if (context.isD2()) {
		    protection = sc.protection;
		    storage_class |= sc.stc & STC_TYPECTOR;
		}

		for (Dsymbol s : members) {
			s.addMember(sc, this, 1, context);
		}

		sc = sc.push(this);

		if (context.isD2()) {
		    sc.stc &= ~(STCfinal | STCauto | STCscope | STCstatic |
	                 STCabstract | STCdeprecated | STC_TYPECTOR | STCtls | STCgshared);
		    sc.stc |= storage_class & STC_TYPECTOR;
		}

		sc.parent = this;
		if (isCOMinterface()) {
			sc.linkage = LINKwindows;
		} else if (context.isD2() && isCPPinterface()) {
	    	sc.linkage = LINK.LINKcpp;
		}
		sc.structalign = 8;
		structalign = sc.structalign;
		sc.offset = Type.PTRSIZE * 2;

		semanticScope(sc);

		for (int j = 0; j < size(members); j++) {
			Dsymbol s = members.get(j);
			s.semantic(sc, context);
		}
		sc.pop();
	}

	@Override
	public Dsymbol syntaxCopy(Dsymbol s, SemanticContext context) {
		InterfaceDeclaration id;

		if (s != null) {
			id = (InterfaceDeclaration) s;
		} else {
			id = context.newInterfaceDeclaration(filename, lineNumber, ident, null);
		}

		super.syntaxCopy(id, context);

		id.copySourceRange(this);
		id.javaElement = javaElement;
		id.templated = templated;

		return id;
	}

	@Override
	public int vtblOffset(SemanticContext context) {
		if (context.isD1()) {
			if (isCOMinterface()) {
				return 0;
			}
		} else {
			if (isCOMinterface() || isCPPinterface()) {
				return 0;
			}
		}
		return 1;
	}

	@Override
	public boolean isCOMinterface() {
		return com;
	}

	@Override
	public boolean isCPPinterface() {
		return cpp;
	}

	@Override
	public char getSignaturePrefix() {
		if (templated) {
			return Signature.C_TEMPLATED_INTERFACE;
		} else {
			return Signature.C_INTERFACE;
		}
	}

	@Override
	public long getFlags() {
		return super.getFlags() | Flags.AccInterface;
	}

	@Override
	public InterfaceDeclaration unlazy(char[] prefix, SemanticContext context) {
		return this;
	}

}
