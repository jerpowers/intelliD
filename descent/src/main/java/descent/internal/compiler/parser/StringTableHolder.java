package descent.internal.compiler.parser;

public final class StringTableHolder implements IStringTableHolder {
	
	private StringTable stringTable;

	@Override
	public StringTable getStringTable() {
		if (stringTable == null) {
			stringTable = new StringTable();
		}
		return stringTable;
	}

	public TypeBasic tbit() {
		// TODO Auto-generated method stub
		return null;
	}

	public TypeBasic tbool() {
		// TODO Auto-generated method stub
		return null;
	}

	public TypeBasic tboolean() {
		// TODO Auto-generated method stub
		return null;
	}

	public TypeBasic tchar() {
		// TODO Auto-generated method stub
		return null;
	}

	public TypeBasic tcomplex32() {
		// TODO Auto-generated method stub
		return null;
	}

	public TypeBasic tcomplex64() {
		// TODO Auto-generated method stub
		return null;
	}

	public TypeBasic tcomplex80() {
		// TODO Auto-generated method stub
		return null;
	}

	public TypeBasic tdchar() {
		// TODO Auto-generated method stub
		return null;
	}

	public TypeBasic terror() {
		// TODO Auto-generated method stub
		return null;
	}

	public TypeBasic tfloat32() {
		// TODO Auto-generated method stub
		return null;
	}

	public TypeBasic tfloat64() {
		// TODO Auto-generated method stub
		return null;
	}

	public TypeBasic tfloat80() {
		// TODO Auto-generated method stub
		return null;
	}

	public TypeBasic timaginary32() {
		// TODO Auto-generated method stub
		return null;
	}

	public TypeBasic timaginary64() {
		// TODO Auto-generated method stub
		return null;
	}

	public TypeBasic timaginary80() {
		// TODO Auto-generated method stub
		return null;
	}

	public TypeBasic tindex() {
		// TODO Auto-generated method stub
		return null;
	}

	public TypeBasic tint16() {
		// TODO Auto-generated method stub
		return null;
	}

	public TypeBasic tint32() {
		// TODO Auto-generated method stub
		return null;
	}

	public TypeBasic tint64() {
		// TODO Auto-generated method stub
		return null;
	}

	public TypeBasic tint8() {
		// TODO Auto-generated method stub
		return null;
	}

	public TypeBasic tptrdiff_t() {
		// TODO Auto-generated method stub
		return null;
	}

	public TypeBasic tshiftcnt() {
		// TODO Auto-generated method stub
		return null;
	}

	public TypeBasic tsize_t() {
		// TODO Auto-generated method stub
		return null;
	}

	public TypeBasic tuns16() {
		// TODO Auto-generated method stub
		return null;
	}

	public TypeBasic tuns32() {
		// TODO Auto-generated method stub
		return null;
	}

	public TypeBasic tuns64() {
		// TODO Auto-generated method stub
		return null;
	}

	public TypeBasic tuns8() {
		// TODO Auto-generated method stub
		return null;
	}

	public TypeBasic tvoid() {
		// TODO Auto-generated method stub
		return null;
	}

	public TypeBasic twchar() {
		// TODO Auto-generated method stub
		return null;
	}

}
