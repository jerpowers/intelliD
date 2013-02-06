package descent.internal.compiler.parser;


public class Dsymbols extends Array<Dsymbol> {

	private static final long serialVersionUID = 1L;
	
	public Dsymbols() {
	}
	
	public Dsymbols(int capacity) {
		super(capacity);
	}
	
	public Dsymbols(Dsymbols elements) {
		super(elements);
	}

}
