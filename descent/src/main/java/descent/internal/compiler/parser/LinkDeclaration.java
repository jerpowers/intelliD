package descent.internal.compiler.parser;

import melnorme.utilbox.tree.TreeVisitor;

import melnorme.utilbox.core.Assert;

import descent.internal.compiler.parser.ast.IASTVisitor;


public class LinkDeclaration extends AttribDeclaration {

	public LINK linkage;

	public LinkDeclaration(LINK linkage, Dsymbols decl) {
		super(decl);
		this.linkage = linkage;
	}

	@Override
	public void accept0(IASTVisitor visitor) {
		boolean children = visitor.visit(this);
		if (children) {
			//TreeVisitor.acceptChildren(visitor, linkage);
			TreeVisitor.acceptChildren(visitor, decl);
		}
		visitor.endVisit(this);
	}

	@Override
	public int getNodeType() {
		return LINK_DECLARATION;
	}

	@Override
	public void semantic(Scope sc, SemanticContext context) {
		if (decl != null && decl.size() > 0) {
			LINK linkage_save = sc.linkage;

			sc.linkage = linkage;
			for (Dsymbol s : decl) {
				s.semantic(sc, context);
			}
			sc.linkage = linkage_save;
		} else {
			sc.linkage = linkage;
		}
	}

	@Override
	public void semantic3(Scope sc, SemanticContext context) {
		if (decl != null && decl.size() > 0) {
			LINK linkage_save = sc.linkage;

			sc.linkage = linkage;
			for (Dsymbol s : decl) {
				s.semantic3(sc, context);
			}
			sc.linkage = linkage_save;
		} else {
			sc.linkage = linkage;
		}
	}

	@Override
	public Dsymbol syntaxCopy(Dsymbol s, SemanticContext context) {
		LinkDeclaration ld;

		Assert.isNotNull(s);
		ld = context.newLinkDeclaration(linkage, Dsymbol.arraySyntaxCopy(decl, context));
		ld.copySourceRange(this);
		return ld;
	}

	@Override
	public void toCBuffer(OutBuffer buf, HdrGenState hgs,
			SemanticContext context) {
		String p = null;

		switch (linkage) {
		case LINKd:
			p = "D";
			break;
		case LINKc:
			p = "C";
			break;
		case LINKcpp:
			p = "C++";
			break;
		case LINKwindows:
			p = "Windows";
			break;
		case LINKpascal:
			p = "Pascal";
			break;
		default:
			Assert.isTrue(false);
			break;
		}
		buf.writestring("extern (");
		buf.writestring(p);
		buf.writestring(") ");
		super.toCBuffer(buf, hgs, context);
	}

	@Override
	public String toChars(SemanticContext context) {
		return "extern ()";
	}

	@Override
	public String getSignature(int options) {
		return parent.getSignature(options);
	}

}
