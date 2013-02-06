package descent.internal.compiler.parser;

import melnorme.utilbox.tree.TreeVisitor;
import descent.core.compiler.IProblem;
import descent.internal.compiler.parser.ast.IASTVisitor;


public class DtorDeclaration extends FuncDeclaration {

	public int thisStart;

	public DtorDeclaration(char[] filename, int lineNumber) {
		this(filename, lineNumber, new IdentifierExp(Id.dtor));
	}
	
	public DtorDeclaration(char[] filename, int lineNumber, IdentifierExp id) {
		super(filename, lineNumber, id, STC.STCundefined, null);
	}

	@Override
	public void accept0(IASTVisitor visitor) {
		boolean children = visitor.visit(this);
		if (children) {
			TreeVisitor.acceptChildren(visitor, type);
			TreeVisitor.acceptChildren(visitor, ident);
			TreeVisitor.acceptChildren(visitor, sourceFrequire);
			TreeVisitor.acceptChildren(visitor, sourceFbody);
			TreeVisitor.acceptChildren(visitor, outId);
			TreeVisitor.acceptChildren(visitor, sourceFensure);
		}
		visitor.endVisit(this);
	}

	@Override
	public boolean addPostInvariant(SemanticContext context) {
		return false;
	}

	@Override
	public boolean addPreInvariant(SemanticContext context) {
		return (isThis() != null && vthis != null && context.global.params.useInvariants);
	}

	@Override
	public int getNodeType() {
		return DTOR_DECLARATION;
	}

	@Override
	public DtorDeclaration isDtorDeclaration() {
		return this;
	}

	@Override
	public boolean isVirtual(SemanticContext context) {
		if (context.BREAKABI) {
			return false;
		} else {
			return super.isVirtual(context);
		}
	}

	@Override
	public boolean overloadInsert(Dsymbol s, SemanticContext context) {
		return false; // cannot overload destructors
	}

	@Override
	public void semantic(Scope sc, SemanticContext context) {
		AggregateDeclaration cd;

		parent = sc.parent;
		Dsymbol parent = toParent();
		
		if (context.isD1()) {
			cd = parent.isClassDeclaration();
		} else {
			cd = parent.isAggregateDeclaration();
		}
		if (cd == null) {
			if (context.acceptsErrors()) {
				context.acceptProblem(Problem.newSemanticTypeErrorLoc(
						IProblem.DestructorsOnlyForClass, this));
			}
		} else if (context.isD1() || (context.isD2() && equals(ident, Id.dtor))){
			if (cd.dtors == null) {
				cd.dtors = new FuncDeclarations(1);
			}
			cd.dtors.add(this);
		}
		type = new TypeFunction(null, Type.tvoid, 0, LINK.LINKd);

		sc = sc.push();
		sc.stc &= ~STC.STCstatic; // not a static destructor
		sc.linkage = LINK.LINKd;

		super.semantic(sc, context);

		sc.pop();
	}
	
	@Override
	public String kind() {
		return "destructor";
	}
	
	@Override
	public String toChars(SemanticContext context) {
		return "~this";
	}

	@Override
	public Dsymbol syntaxCopy(Dsymbol s, SemanticContext context) {
		if (s != null) {
			throw new IllegalStateException("assert(!s);");
		}

		DtorDeclaration dd = context.newDtorDeclaration(filename, lineNumber, ident);
		dd.javaElement = javaElement;
		dd.copySourceRange(this);
		return super.syntaxCopy(dd, context);
	}

	@Override
	public void toCBuffer(OutBuffer buf, HdrGenState hgs,
			SemanticContext context) {
		if (hgs.hdrgen) {
			return;
		}
		buf.writestring("~this()");
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

}
