package descent.internal.compiler.parser;


public class FuncDeclarations extends Array<FuncDeclaration> {

	private static final long serialVersionUID = 1L;
	
	public FuncDeclarations() {
	}
	
	public FuncDeclarations(int capacity) {
		super(capacity);
	}
	
	public FuncDeclarations(FuncDeclarations elements) {
		super(elements);
	}

}
