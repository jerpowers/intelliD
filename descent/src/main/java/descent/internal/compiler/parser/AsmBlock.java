package descent.internal.compiler.parser;

import melnorme.utilbox.tree.TreeVisitor;
import descent.internal.compiler.parser.ast.IASTVisitor;

public class AsmBlock extends CompoundStatement {

	public AsmBlock(char[] filename, int lineNumber, Statements statements) {
		super(filename, lineNumber, statements);
	}
	
	@Override
	public int getNodeType() {
		return ASM_BLOCK;
	}
	
	@Override
	public void accept0(IASTVisitor visitor) {
		boolean children = visitor.visit(this);
		if (children) {
			TreeVisitor.acceptChildren(visitor, sourceStatements);
		}
		visitor.endVisit(this);
	}

}
