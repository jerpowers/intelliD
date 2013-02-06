package descent.internal.compiler.parser;



public class DsymbolTable {
	
	private final HashtableOfCharArrayAndObject map;
	
	public DsymbolTable() {
		map = new HashtableOfCharArrayAndObject();
	}
	
	public DsymbolTable(DsymbolTable table) {
		this.map = new HashtableOfCharArrayAndObject(table.map);
	}

	public Dsymbol insert(Dsymbol dsymbol) {
		return insert(dsymbol.ident, dsymbol);
	}
	
	public Dsymbol insert(IdentifierExp ident, Dsymbol dsymbol) {
		return insert(ident.ident, dsymbol);
	}
	
	public Dsymbol insert(char[] ident, Dsymbol dsymbol) {
		if (map.containsKey(ident)) {
			return null;
		}
		map.put(ident, dsymbol);
		return dsymbol;
	}

	public Dsymbol lookup(IdentifierExp ident) {
		return (Dsymbol) map.get(ident.ident);
	}
	
	public Dsymbol lookup(char[] ident) {
		return (Dsymbol) map.get(ident);
	}
	
	public char[][] keys() {
		return map.keys();
	}
	
	public Object[] values() {
		return map.getValues();
	}
	
	@Override
	public String toString() {
		return map.toString();
	}

}
