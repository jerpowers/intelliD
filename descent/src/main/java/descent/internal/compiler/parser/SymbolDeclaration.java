package descent.internal.compiler.parser;

import static descent.internal.compiler.parser.STC.STCconst;


public class SymbolDeclaration extends Declaration {

	public Symbol sym;
	public StructDeclaration dsym;

	public SymbolDeclaration(char[] filename, int lineNumber, Symbol s, StructDeclaration dsym) {
		super(new IdentifierExp(s.Sident));
		this.filename = filename;
		this.lineNumber = lineNumber;
		sym = s;
		this.dsym = dsym;
		storage_class |= STCconst;
	}

	@Override
	public SymbolDeclaration isSymbolDeclaration() {
		return this;
	}

	public StructDeclaration dsym() {
		return dsym;
	}

	public Symbol sym() {
		return sym;
	}

	@Override
	public String getSignature(int options) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public char getSignaturePrefix() {
		// TODO Auto-generated method stub
		return super.getSignaturePrefix();
	}

}
