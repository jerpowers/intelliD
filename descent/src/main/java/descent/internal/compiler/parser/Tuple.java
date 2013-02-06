package descent.internal.compiler.parser;

import descent.internal.compiler.parser.ast.IASTVisitor;


public class Tuple extends ASTDmdNode {

	public Objects objects;
	
	public Tuple()
	{
		objects = new Objects(3);
	}
	
	@Override
	public void accept0(IASTVisitor visitor) {
		visitor.visit(this);
		visitor.endVisit(this);
	}

	@Override
	public DYNCAST dyncast() {
		return DYNCAST.DYNCAST_TUPLE;
	}

	@Override
	public int getNodeType() {
		return 0;
	}

}
