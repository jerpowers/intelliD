package descent.internal.compiler.parser.ast;

import melnorme.utilbox.tree.IElement;

public interface IASTNode extends IElement {
	
	int getOffset();
	int getLength();
	
	int getStartPos();
	int getEndPos();
	
	boolean hasNoSourceRangeInfo();
	
	String toStringAsNode(boolean printRangeInfo);
	
	@Override
	public IASTNode[] getChildren(); // Redefined to refine the type of children
	
}
