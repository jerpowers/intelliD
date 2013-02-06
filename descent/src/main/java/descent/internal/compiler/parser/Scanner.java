package descent.internal.compiler.parser;

public class Scanner {
	
	public static final String INVALID_DIGIT = "Invalid_Digit"; //$NON-NLS-1$
	
	// extended unicode support
	public static final int LOW_SURROGATE_MIN_VALUE = 0xDC00;
	public static final int HIGH_SURROGATE_MIN_VALUE = 0xD800;
	public static final int HIGH_SURROGATE_MAX_VALUE = 0xDBFF;
	public static final int LOW_SURROGATE_MAX_VALUE = 0xDFFF;

}
