package descent.internal.compiler.parser;

import melnorme.utilbox.tree.TreeVisitor;

import melnorme.utilbox.core.Assert;

import descent.internal.compiler.parser.ast.IASTVisitor;

public class AlignDeclaration extends AttribDeclaration {

	public int salign;

	public AlignDeclaration(int sa, Dsymbols decl) {
		super(decl);
		this.salign = sa;
	}

	@Override
	public void accept0(IASTVisitor visitor) {
		boolean children = visitor.visit(this);
		if (children) {
			TreeVisitor.acceptChildren(visitor, decl);
		}
		visitor.endVisit(this);
	}

	@Override
	public int getNodeType() {
		return ALIGN_DECLARATION;
	}

	@Override
	public void semantic(Scope sc, SemanticContext context) {
		if (decl != null) {
			int salign_save = sc.structalign;

			sc.structalign = salign;
			for (Dsymbol s : decl) {
				s.semantic(sc, context);
			}
			sc.structalign = salign_save;
		} else {
			sc.structalign = salign;
		}
	}

	@Override
	public Dsymbol syntaxCopy(Dsymbol s, SemanticContext context) {
		AlignDeclaration ad;

		Assert.isTrue(s == null);
		ad = context.newAlignDeclaration(salign, Dsymbol.arraySyntaxCopy(decl, context));
		ad.copySourceRange(this);
		return ad;
	}

	@Override
	public void toCBuffer(OutBuffer buf, HdrGenState hgs,
			SemanticContext context) {
		buf.data.append("align (" + salign + ")");
		super.toCBuffer(buf, hgs, context);
	}

	@Override
	public String getSignature(int options) {
		return parent.getSignature(options);
	}

}
