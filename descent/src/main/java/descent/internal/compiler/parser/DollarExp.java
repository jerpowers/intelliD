package descent.internal.compiler.parser;

import descent.internal.compiler.parser.ast.IASTVisitor;


public class DollarExp extends IdentifierExp {

	public DollarExp(char[] filename, int lineNumber) {
		super(filename, lineNumber, Id.dollar);
	}
	
	@Override
	public void accept0(IASTVisitor visitor) {
		visitor.visit(this);
		visitor.endVisit(this);
	}
	
	@Override
	public int getNodeType() {
		return DOLLAR_EXP;
	}

}
