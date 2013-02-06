package descent.internal.compiler.parser;



public abstract class TemplateParameter extends ASTDmdNode {

	public int lineNumber;
	public char[] filename;
	public IdentifierExp ident;
	public Declaration sparam;

	public TemplateParameter(char[] filename, int lineNumber, IdentifierExp ident) {
		this.filename = filename;
		this.lineNumber = lineNumber;
		this.ident = ident;
		this.sparam = null;
	}

	public abstract void declareParameter(Scope sc, SemanticContext context);

	public abstract ASTDmdNode defaultArg(char[] filename, int lineNumber, Scope sc, SemanticContext context);

	/**
	 * Create dummy argument based on parameter.
	 */
	public abstract ASTDmdNode dummyArg(SemanticContext context);

	public TemplateAliasParameter isTemplateAliasParameter() {
		return null;
	}

	public TemplateTupleParameter isTemplateTupleParameter() {
		return null;
	}

	public TemplateTypeParameter isTemplateTypeParameter() {
		return null;
	}

	public TemplateValueParameter isTemplateValueParameter() {
		return null;
	}
	
	public TemplateThisParameter isTemplateThisParameter() {
		return null;
	}
	
	/**
	 * Match actual argument against parameter.
	 */
	public final MATCH matchArg(Scope sc, Objects tiargs, int i,
			TemplateParameters parameters, Objects dedtypes,
			Declaration[] psparam, SemanticContext context) {
		return matchArg(sc, tiargs, i, parameters, dedtypes, psparam, 0, context);
	}

	/**
	 * Match actual argument against parameter.
	 */
	public abstract MATCH matchArg(Scope sc, Objects tiargs, int i,
			TemplateParameters parameters, Objects dedtypes,
			Declaration[] psparam, int flags, SemanticContext context);

	/**
	 * If TemplateParameter's match as far as overloading goes.
	 */
	public abstract int overloadMatch(TemplateParameter tp);

	public abstract void semantic(Scope sc, SemanticContext context);

	public abstract ASTDmdNode specialization();

	public abstract TemplateParameter syntaxCopy(SemanticContext context);

	public abstract void toCBuffer(OutBuffer buf, HdrGenState hgs, SemanticContext context);
	
	protected abstract void appendSignature(StringBuilder sb, int options);
	
	public String getSignature() {
		return getSignature(ISignatureOptions.Default);
	}
	
	public String getSignature(int options) {
		StringBuilder sb = new StringBuilder();
		appendSignature(sb, options);
		return sb.toString();
	}
	
	/*
	 * Descent: returns the default value of this template parameter
	 * as a char array, or null if there is no default value. 
	 */
	public abstract char[] getDefaultValue();

}
