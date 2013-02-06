package descent.internal.compiler.parser;

import melnorme.utilbox.tree.TreeVisitor;
import descent.internal.compiler.parser.ast.IASTVisitor;


public class ModuleDeclaration extends ASTDmdNode {

	public IdentifierExp id;
	public Identifiers packages;
	public boolean safe;

	public ModuleDeclaration(Identifiers packages, IdentifierExp id, boolean safe) {
		this.packages = packages;
		this.id = id;
		this.safe = safe;
	}

	@Override
	public void accept0(IASTVisitor visitor) {
		boolean children = visitor.visit(this);
		if (children) {
			TreeVisitor.acceptChildren(visitor, packages);
			TreeVisitor.acceptChildren(visitor, id);
		}
		visitor.endVisit(this);
	}

	@Override
	public int getNodeType() {
		return MODULE_DECLARATION;
	}

	@Override
	public String toChars(SemanticContext context) {
		OutBuffer buf = new OutBuffer();
		if (this.packages() != null && this.packages().size() > 0) {
			for (int i = 0; i < this.packages().size(); i++) {
				IdentifierExp pid = this.packages().get(i);
				buf.writestring(pid.toChars());
				buf.writeByte('.');
			}
		}
		buf.writestring(this.id().toChars());
		return buf.extractData();
	}
	
	public char[] getFQN() {
		return getFQN(packages, id);
	}
	
	public IdentifierExp id() {
		return id;
	}
	
	public Identifiers packages() {
		return packages;
	}

}
