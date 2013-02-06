package descent.internal.compiler.parser;


public enum MATCH {
	
	 MATCHnomatch,	// no match
	 MATCHconvert,	// match with conversions
	 MATCHconst,	// match with conversions to const
	 MATCHexact		// exact match

}