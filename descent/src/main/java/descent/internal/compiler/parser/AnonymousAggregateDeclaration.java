package descent.internal.compiler.parser;

import descent.internal.compiler.parser.ast.IASTVisitor;

public class AnonymousAggregateDeclaration extends AggregateDeclaration {

	public AnonymousAggregateDeclaration(char[] filename, int lineNumber) {
		super(filename, lineNumber, null);
	}
	
	@Override
	public void accept0(IASTVisitor visitor) {
		visitor.visit(this);
		visitor.endVisit(this);
	}
	
	@Override
	public AnonymousAggregateDeclaration isAnonymousAggregateDeclaration() {
		return this;
	}
	@Override
	public char getSignaturePrefix() {
		// TODO Descent signature
		return 0;
	}

}
