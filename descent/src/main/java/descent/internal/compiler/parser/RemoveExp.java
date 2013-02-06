package descent.internal.compiler.parser;

import descent.internal.compiler.parser.ast.IASTVisitor;


public class RemoveExp extends BinExp {

	public RemoveExp(char[] filename, int lineNumber, Expression e1, Expression e2) {
		super(filename, lineNumber, TOK.TOKremove, e1, e2);
	}

	@Override
	public int getNodeType() {
		return 0;
	}
	
	@Override
	public void accept0(IASTVisitor visitor) {
		visitor.visit(this);
		visitor.endVisit(this);
	}
}
