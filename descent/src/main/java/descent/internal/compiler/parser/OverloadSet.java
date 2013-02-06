package descent.internal.compiler.parser;

public class OverloadSet extends Dsymbol {

	public Dsymbols a = new Dsymbols(3); // array of Dsymbols

	@Override
	public OverloadSet isOverloadSet() {
		return this;
	}

	@Override
	public String kind() {
		return "overloadset";
	}

	public void push(Dsymbol s) {
		a.add(s);
	}

}
