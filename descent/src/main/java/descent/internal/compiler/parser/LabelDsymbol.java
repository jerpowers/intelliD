package descent.internal.compiler.parser;

import descent.internal.compiler.parser.ast.IASTVisitor;


public class LabelDsymbol extends Dsymbol {

	public LabelStatement statement;

	public LabelDsymbol(IdentifierExp ident) {
		super(ident);
	}

	public LabelDsymbol(char[] ident) {
		this(new IdentifierExp(ident));
	}

	@Override
	public void accept0(IASTVisitor visitor) {
		visitor.visit(this);
		visitor.endVisit(this);
	}

	@Override
	public int getNodeType() {
		return LABEL_DSYMBOL;
	}

	@Override
	public LabelDsymbol isLabel() {
		return this;
	}
	
	@Override
	public String getSignature(int options) {
		// TODO Auto-generated method stub
		return null;
	}

}
