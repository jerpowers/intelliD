package descent.internal.compiler.parser;


public class Statements extends Array<Statement> {

	private static final long serialVersionUID = 1L;
	
	public Statements() {
	}
	
	public Statements(int capacity) {
		super(capacity);
	}
	
	public Statements(Statements elements) {
		super(elements);
	}
	
	@Override
	public boolean add(Statement e) {
		return super.add(e);
	}
	
	@Override
	public void add(int index, Statement element) {
		if (element == null) {
			throw new IllegalStateException();
		}
		super.add(index, element);
	}

}
