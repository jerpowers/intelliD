package descent.internal.compiler.parser;

import descent.internal.compiler.parser.ast.IASTVisitor;


public class ThisDeclaration extends VarDeclaration {

	public ThisDeclaration(char[] filename, int lineNumber, Type type) {
		super(filename, lineNumber, type, Id.This, null);
	}
	
	@Override
	public ThisDeclaration isThisDeclaration() {
		return this;
	}
	
	@Override
	public void accept0(IASTVisitor visitor) {
		visitor.visit(this);
		visitor.endVisit(this);
	}
	
	@Override
	public Dsymbol syntaxCopy(Dsymbol s, SemanticContext context) {
		throw new IllegalStateException("assert(0);");
	}

}
