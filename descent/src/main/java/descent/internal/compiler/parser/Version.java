package descent.internal.compiler.parser;

import descent.internal.compiler.parser.ast.IASTVisitor;


public class Version extends Dsymbol {
	
	public char[] value;
	
	public Version(char[] filename, int lineNumber, char[] value) {
		this.filename = filename;
		this.lineNumber = lineNumber;
		this.value = value;
	}
	
	@Override
	public int getNodeType() {
		return VERSION;
	}
	
	@Override
	public void accept0(IASTVisitor visitor) {
		boolean children = visitor.visit(this);
		if (children) {
		}
		visitor.endVisit(this);
	}
	
	@Override
	public String getSignature(int options) {
		// TODO Auto-generated method stub
		return null;
	}

}
