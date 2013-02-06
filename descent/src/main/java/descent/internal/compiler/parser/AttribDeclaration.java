package descent.internal.compiler.parser;

import java.util.ArrayList;
import java.util.List;

public abstract class AttribDeclaration extends Dsymbol {

	public Dsymbols decl;

	public AttribDeclaration(Dsymbols decl) {
		this.decl = decl;
	}

	@Override
	public void addLocalClass(ClassDeclarations aclasses,
			SemanticContext context) {
		Dsymbols d = include(null, null, context);

		if (d != null) {
			for (Dsymbol s : d) {
				s.addLocalClass(aclasses, context);
			}
		}
	}

	@Override
	public int addMember(Scope sc, ScopeDsymbol sd, int memnum,
			SemanticContext context) {
		int m = 0;
		Dsymbols d = include(sc, sd, context);

		if (d != null) {
			for (Dsymbol s : d) {
				m |= s.addMember(sc, sd, m | memnum, context);
			}
		}
		return m;
	}

	@Override
	public void checkCtorConstInit(SemanticContext context) {
		Dsymbols d = include(null, null, context);

		if (d != null) {
			for (Dsymbol s : d) {
				s.checkCtorConstInit(context);
			}
		}
	}

	@Override
	public boolean hasPointers(SemanticContext context) {
		Dsymbols d = include(null, null, context);

		if (d != null) {
			for (Dsymbol s : d) {
				if (s.hasPointers(context)) {
					return true;
				}
			}
		}
		return false;
	}

	public Dsymbols include(Scope sc, ScopeDsymbol sd, SemanticContext context) {
		return decl;
	}

	@Override
	public AttribDeclaration isAttribDeclaration() {
		return this;
	}

	@Override
	public String kind() {
		return "attribute";
	}

	@Override
	public boolean oneMember(Dsymbol[] ps, SemanticContext context) {
		Dsymbols d = include(null, null, context);

		return Dsymbol.oneMembers(d, ps, context);
	}

	@Override
	public void semantic(Scope sc, SemanticContext context) {
		Dsymbols d = include(sc, null, context);

		if (d != null && d.size() > 0) {
			for (Dsymbol s : d) {
				s.semantic(sc, context);
			}
		}
	}

	@Override
	public void semantic2(Scope sc, SemanticContext context) {
		Dsymbols d = include(sc, null, context);

		if (d != null && d.size() > 0) {
			for (Dsymbol s : d) {
				try {
					s.semantic2(sc, context);
				} catch (StackOverflowError e) {
					System.out.println(e);
				}
			}
		}
	}

	@Override
	public void semantic3(Scope sc, SemanticContext context) {
		Dsymbols d = include(sc, null, context);

		if (d != null && d.size() > 0) {
			for (Dsymbol s : d) {
				s.semantic3(sc, context);
			}
		}
	}

	@Override
	public void toCBuffer(OutBuffer buf, HdrGenState hgs,
			SemanticContext context) {
		if (decl != null) {
			buf.writenl();
			buf.writeByte('{');
			buf.writenl();
			for (Dsymbol s : decl) {
				buf.writestring("    ");
				s.toCBuffer(buf, hgs, context);
			}
			buf.writeByte('}');
		} else {
			buf.writeByte(';');
		}
		buf.writenl();
	}
	
	protected final void semanticWithExtraModifiers(Dsymbol s, Modifier modifier, Scope sc, SemanticContext context) {
		// Send extra modifiers to our children, so that they can report better problems
		List<Modifier> thisModifiers = context.Module_rootModule.getModifiers(this);
		List<Modifier> thisExtraModifiers = context.getExtraModifiers(this);
		List<Modifier> sExtraModifiers = thisModifiers;
		
		if (sExtraModifiers == null) {
			sExtraModifiers = new ArrayList<Modifier>();
		}
		if (thisExtraModifiers != null) {
			sExtraModifiers.addAll(thisExtraModifiers);
		}
		sExtraModifiers.add(modifier);
		
		context.setExtraModifiers(s, sExtraModifiers);
		s.semantic(sc, context);
		context.setExtraModifiers(s, null);
	}

}
