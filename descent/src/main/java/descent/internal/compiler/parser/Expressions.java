package descent.internal.compiler.parser;


public class Expressions extends Array<Expression> {

	private static final long serialVersionUID = 1L;
	
	public Expressions() {
	}
	
	public Expressions(int capacity) {
		super(capacity);
	}
	
	public Expressions(Expressions objects) {
		super(objects);
	}
	
	public Expressions copy() {
		Expressions other = new Expressions();
		for(Expression exp : this) {
			if (exp != null) {
				other.add(exp.copy());
			} else {
				other.add(null);
			}
		}
		return other;
	}

}
