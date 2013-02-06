package descent.internal.compiler.parser;

import melnorme.utilbox.tree.TreeVisitor;
import descent.core.Flags;
import descent.core.Signature;
import descent.internal.compiler.parser.ast.IASTVisitor;


public class UnionDeclaration extends StructDeclaration {

	public UnionDeclaration(char[] filename, int lineNumber, IdentifierExp id) {
		super(filename, lineNumber, id);
	}

	@Override
	public void accept0(IASTVisitor visitor) {
		boolean children = visitor.visit(this);
		if (children) {
			TreeVisitor.acceptChildren(visitor, ident);
			TreeVisitor.acceptChildren(visitor, members);
			
//			acceptSynthetic(visitor);
		}
		visitor.endVisit(this);
	}

	@Override
	public int getNodeType() {
		return UNION_DECLARATION;
	}

	@Override
	public UnionDeclaration isUnionDeclaration() {
		return this;
	}

	@Override
	public String kind() {
		return "union";
	}

	@Override
	public Dsymbol syntaxCopy(Dsymbol s, SemanticContext context) {
		UnionDeclaration ud;

		if (s != null) {
			ud = (UnionDeclaration) s;
		} else {
			ud = context.newUnionDeclaration(filename, lineNumber, ident);
		}
		super.syntaxCopy(ud, context);
		
		ud.copySourceRange(this);
		ud.javaElement = javaElement;
		ud.templated = templated;
		
		return ud;
	}
	
	@Override
	public int getErrorStart() {
		if (ident != null) {
			return ident.start;
		}
		return start;
	}
	
	@Override
	public int getErrorLength() {
		if (ident != null) {
			return ident.length;
		}
		return 5; // "union".length()
	}
	
	@Override
	public char getSignaturePrefix() {
		if (templated) {
			return Signature.C_TEMPLATED_UNION;
		} else {
			return Signature.C_UNION;
		}
	}
	
	@Override
	public long getFlags() {
		return (super.getFlags() | Flags.AccUnion) - Flags.AccStruct;
	}

}
