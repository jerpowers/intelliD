package descent.internal.compiler.parser;

/**
 * Flags to determine what to include when invoking
 * {@link Dsymbol#getSignature()} or {@link Type#getSignature()}
 */
public interface ISignatureOptions {
	
	int None = 0;
	int AliasResolution = 1;
	int TemplateInstanceParameters = 2;
	int Default = AliasResolution | TemplateInstanceParameters;

}
