package descent.internal.compiler.parser;


public class ClassDeclarations extends Array<ClassDeclaration> {

	private static final long serialVersionUID = 1L;
	
	public ClassDeclarations() {
	}
	
	public ClassDeclarations(int capacity) {
		super(capacity);
	}
	
	public ClassDeclarations(ClassDeclarations elements) {
		super(elements);
	}

}
