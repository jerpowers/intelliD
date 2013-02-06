package descent.internal.compiler.parser;

public class TemplateVarDeclaration extends VarDeclaration {

	public TemplateVarDeclaration(char[] filename, int lineNumber, Type type, char[] ident, Initializer init) {
		super(filename, lineNumber, type, ident, init);
	}

	public TemplateVarDeclaration(char[] filename, int lineNumber, Type type, IdentifierExp id, Initializer init) {
		super(filename, lineNumber, type, id, init);
	}
	
	@Override
	public boolean isTemplateArgument() {
		return true;
	}

}
