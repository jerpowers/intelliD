package descent.internal.compiler.parser;

import melnorme.utilbox.tree.TreeVisitor;
import descent.core.compiler.IProblem;
import descent.internal.compiler.parser.ast.IASTVisitor;

public class PostBlitDeclaration extends FuncDeclaration {
	
	public int thisStart; // where the "this" keyword starts

	public PostBlitDeclaration(char[] filename, int lineNumber) {
		this(filename, lineNumber, new IdentifierExp(Id._postblit));
	}
	
	public PostBlitDeclaration(char[] filename, int lineNumber, IdentifierExp id) {
		super(filename, lineNumber, id, STC.STCundefined, null);
	}
	
	@Override
	public Dsymbol syntaxCopy(Dsymbol s, SemanticContext context) {
		PostBlitDeclaration dd = context.newPostBlitDeclaration(filename, lineNumber, ident);
		dd.copySourceRange(this);
	    return super.syntaxCopy(dd, context);
	}
	
	@Override
	public void semantic(Scope sc, SemanticContext context) {
		parent = sc.parent;
		Dsymbol parent = toParent();
		StructDeclaration ad = parent.isStructDeclaration();
		if (null == ad) {
			if (context.acceptsErrors()) {
				context.acceptProblem(Problem.newSemanticTypeErrorLoc(IProblem.PostBlitsAreOnlyForStructUnionDefinitions, this));
			}
		} else if (equals(ident, Id._postblit)) {
			if (ad.postblits == null) {
				ad.postblits = new FuncDeclarations(1);
			}
			ad.postblits.add(this);
		}
		type = new TypeFunction(null, Type.tvoid, 0, LINK.LINKd);

		sc = sc.push();
		sc.stc &= ~STC.STCstatic; // not static
		sc.linkage = LINK.LINKd;

		super.semantic(sc, context);

		sc.pop();
	}
	
	@Override
	public boolean overloadInsert(Dsymbol s, SemanticContext context) {
		return false;
	}
	
	@Override
	public boolean addPreInvariant(SemanticContext context) {
		return false;
	}
	
	@Override
	public boolean addPostInvariant(SemanticContext context) {
		return (vthis != null && context.global.params.useInvariants);
	}
	
	@Override
	public PostBlitDeclaration isPostBlitDeclaration() {
		return this;
	}
	
	@Override
	public boolean isVirtual(SemanticContext context) {
		return false;
	}
	
	@Override
	public void toCBuffer(OutBuffer buf, HdrGenState hgs, SemanticContext context) {
		if (hgs.hdrgen) {
			return;
		}
	    buf.writestring("=this()");
	    bodyToCBuffer(buf, hgs, context);
	}
	
	@Override
	public int getErrorStart() {
		return thisStart;
	}
	
	@Override
	public int getErrorLength() {
		return 4; // "this".length()
	}
	
	@Override
	public void accept0(IASTVisitor visitor) {
		boolean children = visitor.visit(this);
		if (children) {
			TreeVisitor.acceptChildren(visitor, sourceType);
			TreeVisitor.acceptChildren(visitor, ident);
			TreeVisitor.acceptChildren(visitor, sourceFrequire);
			TreeVisitor.acceptChildren(visitor, sourceFbody);
			TreeVisitor.acceptChildren(visitor, outId);
			TreeVisitor.acceptChildren(visitor, sourceFensure);
		}
		visitor.endVisit(this);
	}
	
	@Override
	public int getNodeType() {
		return POSTBLIT_DECLARATION;
	}

}
