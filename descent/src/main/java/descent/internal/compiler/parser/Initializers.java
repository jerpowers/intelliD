package descent.internal.compiler.parser;


public class Initializers extends Array<Initializer> {

	private static final long serialVersionUID = 1L;
	
	public Initializers() {
	}
	
	public Initializers(int capacity) {
		super(capacity);
	}
	
	public Initializers(Initializers elements) {
		super(elements);
	}

}
