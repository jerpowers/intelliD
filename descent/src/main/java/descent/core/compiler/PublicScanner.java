package descent.core.compiler;

import descent.internal.compiler.parser.TOK;

/**
 * Implementation of an IScanner for the D language.
 * 
 * It wraps the internal Lexer class.
 */
public class PublicScanner implements IScanner {
	
	private final boolean tokenizeComments;
	private final boolean tokenizePragmas;
	private final boolean tokenizeWhiteSpace;	
	private final boolean recordLineSeparator;
	private final int apiLevel;
	private char[] source;
	public descent.internal.compiler.parser.Lexer lexer;

	public PublicScanner(boolean tokenizeComments, boolean tokenizePragmas, boolean tokenizeWhiteSpace, boolean recordLineSeparator, int apiLevel) {
		this.tokenizeComments = tokenizeComments;
		this.tokenizePragmas = tokenizePragmas;
		this.tokenizeWhiteSpace = tokenizeWhiteSpace;		
		this.recordLineSeparator = recordLineSeparator;
		this.apiLevel = apiLevel;
	}
	
	@Override
	public int getCurrentTokenEndPosition() {
		if (lexer.token.value == TOK.TOKeof) {
			return lexer.token.ptr + lexer.token.sourceLen;
		} else {
			return lexer.token.ptr + lexer.token.sourceLen - 1;
		}
	}
	
	@Override
	public int getCurrentTokenStartPosition() {
		return lexer.token.ptr;
	}
	
	// This method is here to make DefaultCommentMapper look much more like
	// the original one
	public int getCurrentPosition() {
		return getCurrentTokenEndPosition() + 1;
	}
	
	@Override
	public int getLineEnd(int lineNumber) {
		return lexer.getLineEnd(lineNumber);
	}
	
	@Override
	public int[] getLineEnds() {
		return lexer.getLineEnds();
	}
	
	@Override
	public int getLineNumber(int position) {
		return lexer.getLineNumber(position);
	}
	
	@Override
	public int getLineStart(int lineNumber) {
		return lexer.getLineStart(lineNumber);
	}
	
	@Override
	public int getNextToken() throws InvalidInputException {
		return lexer.nextToken().terminalSymbol;
	}
	
	@Override
	public char[] getRawTokenSource() {
		return lexer.token.getRawTokenSource();
	}
	
	@Override
	public char[] getCurrentTokenSource() {
		// TODO replace unicode sequences
		return lexer.token.getRawTokenSource();
	}
	
	@Override
	public String getRawTokenSourceAsString() {
		return lexer.token.getRawTokenSourceAsString();
	}
	
	@Override
	public char[] getSource() {
		return source;
	}
	
	@Override
	public void resetTo(int startPosition, int endPosition) {
		if (this.lexer == null) {
			this.lexer = new descent.internal.compiler.parser.Lexer(source, startPosition, endPosition - startPosition, tokenizeComments, tokenizePragmas, tokenizeWhiteSpace, recordLineSeparator, apiLevel);
		} else {
			this.lexer.reset(startPosition, endPosition - startPosition);
		}
	}
	
	@Override
	public void setSource(char[] source) {
		this.source = source;
		if (this.lexer == null) {
			this.lexer = new descent.internal.compiler.parser.Lexer(source, 0, source.length, tokenizeComments, tokenizePragmas, tokenizeWhiteSpace, recordLineSeparator, apiLevel);
		} else {
			this.lexer.reset(source, 0, source.length, tokenizeComments, tokenizePragmas, tokenizeWhiteSpace, recordLineSeparator);
		}
		resetTo(0, source.length);
	}
	
	/**
	 * This method allows this scanner to reuse the lexer that is used
	 * to parse a compilation unit.
	 */
	public void setLexerAndSource(descent.internal.compiler.parser.Lexer lexer, char[] source) {
		this.lexer = lexer;
		this.source = source;
	}

}
