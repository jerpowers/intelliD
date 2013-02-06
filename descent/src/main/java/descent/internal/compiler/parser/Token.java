package descent.internal.compiler.parser;

import descent.core.compiler.CharOperation;


public class Token {
	
	public final static int SPECIAL__FILE__ = 1;
	public final static int SPECIAL__LINE__ = 2;
	public final static int SPECIAL__DATE__ = 3;
	public final static int SPECIAL__TIME__ = 4;
	public final static int SPECIAL__TIMESTAMP__ = 5;
	public final static int SPECIAL__VENDOR__ = 6;
	public final static int SPECIAL__VERSION__ = 7;
	
	public Token next;
	public int ptr; // The start position of the token
	public TOK value;
	
	// These three are for string tokens
	public char[] ustring;
	public int len;
	public int postfix;
	
	// These two are for range information
	public char[] sourceString;
	public int sourceLen;
	
	public integer_t intValue;
	public real_t floatValue;
	public int lineNumber;
	public Comment leadingComment;
	public int special;
	
//	private static int instances = 0;
	
	public Token() {
//		System.out.println("Token: " + ++instances);
	}
	
	public Token(Token other) {
		assign(this, other);
//		System.out.println("Token: " + ++instances);
	}
	
	public void reset() {
		next = null;
		ptr = 0;
		value = TOK.TOKreserved;
		sourceString = null;
		sourceLen = 0;
		postfix = 0;
		lineNumber = 0;
		leadingComment = null;
		intValue = null;
		floatValue = null;
		ustring = null;
		len = 0;
	}
	
	public static void assign(Token to, Token from) {
		to.next = from.next;
		to.ptr = from.ptr;
		to.value = from.value;
		to.ustring = from.ustring;
		to.len = from.len;
		to.postfix = from.postfix;
		to.intValue = from.intValue;
		to.floatValue = from.floatValue;
		
		to.lineNumber = from.lineNumber;
		to.sourceString = from.sourceString;
		to.sourceLen = from.sourceLen;
		to.leadingComment = from.leadingComment;
	}
	
	public void setString(char[] input, int start, int length) {
		if (start + length >= input.length) {
			length = input.length - start;
		}
		
		this.sourceString = new char[length];
		System.arraycopy(input, start, this.sourceString, 0, length);
	}
	
	@Override
	public String toString() {
		return getRawTokenSourceAsString();
	}
	
	public char[] getRawTokenSource() {
		if (value == null) {
			return CharOperation.NO_CHAR;
		}
		switch(value) {
			case TOKeof:
				return CharOperation.NO_CHAR;
			case TOKint32v:
			case TOKuns32v:
			case TOKint64v:
			case TOKuns64v:
			case TOKfloat32v:
			case TOKfloat64v:
			case TOKfloat80v:
			case TOKimaginary32v:
			case TOKimaginary64v:
			case TOKimaginary80v:
			case TOKcharv:
			case TOKwcharv:
			case TOKdcharv:
			case TOKstring:
			case TOKlinecomment:
			case TOKdoclinecomment:
			case TOKblockcomment:
			case TOKdocblockcomment:
			case TOKpluscomment:
			case TOKdocpluscomment:
			case TOKwhitespace:
			case TOKPRAGMA:
			case TOKidentifier:
				return sourceString;
			default:
				return value.charArrayValue;
		}		
	}
	
	public String getRawTokenSourceAsString() {
		if (value == null) {
			return "";
		}
		switch(value) {
		case TOKeof:
			return "";
		case TOKint32v:
		case TOKuns32v:
		case TOKint64v:
		case TOKuns64v:
		case TOKfloat32v:
		case TOKfloat64v:
		case TOKfloat80v:
		case TOKimaginary32v:
		case TOKimaginary64v:
		case TOKimaginary80v:
		case TOKcharv:
		case TOKwcharv:
		case TOKdcharv:
		case TOKstring:
		case TOKlinecomment:
		case TOKdoclinecomment:
		case TOKblockcomment:
		case TOKdocblockcomment:
		case TOKpluscomment:
		case TOKdocpluscomment:
		case TOKwhitespace:
		case TOKPRAGMA:
			return null != sourceString ? new String(sourceString) : "";
		case TOKidentifier:
			return null != sourceString ? new String(sourceString) : "";
		default:
			return value.value;
	}	
	}

}

