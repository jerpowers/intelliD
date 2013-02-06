package descent.internal.compiler.parser.ast;

import melnorme.utilbox.tree.IVisitable;

/** 
 * An abstract visitor that visits nodes in a homogeneous way, 
 * i.e., without any type-specific methods. Uses the accept0 mechanism and
 * not getChildren().
 */
public abstract class ASTHomoVisitor extends AstVisitorAdapter {
	
	public <T extends IASTVisitor> void traverse(IVisitable<? super IASTVisitor> elem) {
		elem.accept(this);
	}
	@Override
	public boolean preVisit(ASTNode elem) {
		return enterNode(elem);
	}
	@Override
	public void postVisit(ASTNode elem) {
		leaveNode(elem);
	}
	
	abstract boolean enterNode(ASTNode elem);
	abstract void leaveNode(ASTNode elem);
	
}