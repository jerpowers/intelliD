package descent.internal.compiler.parser;

import melnorme.utilbox.tree.TreeVisitor;

import melnorme.utilbox.core.Assert;

import descent.internal.compiler.parser.ast.IASTVisitor;


public class ProtDeclaration extends AttribDeclaration {

	public Modifier modifier;
	public boolean single;
	public PROT protection;
	public boolean colon;

	public ProtDeclaration(PROT p, Dsymbols decl,
			Modifier modifier, boolean single, boolean colon) {
		super(decl);
		this.protection = p;
		this.modifier = modifier;
		this.single = single;
		this.colon = colon;
	}

	@Override
	public void accept0(IASTVisitor visitor) {
		boolean children = visitor.visit(this);
		if (children) {
			TreeVisitor.acceptChildren(visitor, modifier);
			TreeVisitor.acceptChildren(visitor, decl);
		}
		visitor.endVisit(this);
	}

	@Override
	public int getNodeType() {
		return PROT_DECLARATION;
	}

	@Override
	public void semantic(Scope sc, SemanticContext context) {
		if (decl != null && decl.size() > 0) {
			PROT protection_save = sc.protection;
			int explicitProtection_save = sc.explicitProtection;

			sc.protection = protection;
			sc.explicitProtection = 1;

			for (Dsymbol s : decl) {
				// Send extra modifiers to out children, so that they can report better problems
				semanticWithExtraModifiers(s, modifier, sc, context);
			}

			sc.protection = protection_save;
			sc.explicitProtection = explicitProtection_save;
		} else {
			sc.protection = protection;
			sc.explicitProtection = 1;
		}
	}

	@Override
	public Dsymbol syntaxCopy(Dsymbol s, SemanticContext context) {
		ProtDeclaration pd;

		if (s != null) {
			throw new IllegalStateException("assert(s);");
		}
		pd = new ProtDeclaration(protection,
				Dsymbol.arraySyntaxCopy(decl, context), modifier, single, colon);
		return pd;
	}

	@Override
	public void toCBuffer(OutBuffer buf, HdrGenState hgs,
			SemanticContext context) {
		String p = null;

		switch (protection) {
		case PROTprivate:
			p = "private";
			break;
		case PROTpackage:
			p = "package";
			break;
		case PROTprotected:
			p = "protected";
			break;
		case PROTpublic:
			p = "public";
			break;
		case PROTexport:
			p = "export";
			break;
		default:
			Assert.isTrue(false);
			break;
		}
		buf.writestring(p);
		super.toCBuffer(buf, hgs, context);
	}

	@Override
	public String getSignature(int options) {
		return parent.getSignature(options);
	}

	@Override
	public PROT getProtection() {
		return protection;
	}

}
