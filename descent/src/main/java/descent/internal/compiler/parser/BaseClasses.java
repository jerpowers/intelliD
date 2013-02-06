package descent.internal.compiler.parser;

public class BaseClasses extends Array<BaseClass> {

	private static final long serialVersionUID = 1L;
	
	public BaseClasses() {
	}
	
	public BaseClasses(int capacity) {
		super(capacity);
	}
	
	public BaseClasses(BaseClasses elements) {
		super(elements);
	}

}
