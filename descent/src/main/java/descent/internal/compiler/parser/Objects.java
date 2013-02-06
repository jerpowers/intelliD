package descent.internal.compiler.parser;


public class Objects extends Array<ASTDmdNode> {

	private static final long serialVersionUID = 1L;

	public Objects() {
	}

	public Objects(int capacity) {
		super(capacity);
	}

	public Objects(Objects elements) {
		super(elements);
	}
}
