package com.wyrdtech.parsed.lexer.token;

/**
 * Abstracts creation of Tokens, to allow injection of custom types as required
 * by users of the lexer.
 *
 * TODO: builder interface?
 */
public interface TokenFactory {

    /**
     * Create single-character token.  Literal value is taken from
     * the TokenType.
     *
     * @param type TokenType of this token
     * @param line Line token appears on in stream
     * @param col  Column token appears on in stream
     */
    public Token create(TokenType type, int index, int line, int col);

    /**
     * Create multi-character token.  Length is the number of characters
     * taken up by token in the stream, assumes token is contained on one line.
     * Literal value is taken from the TokenType.
     *
     * @param type TokenType of this token
     * @param line Line token appears on in stream
     * @param col  Column token appears on in stream
     * @param length Number of characters in the token
     */
    public Token create(TokenType type, int index, int line, int col, int length);

    /**
     * Create multi-character token with a corresponding literal value.
     * Length is the number of characters taken up by token in the stream,
     * assumes token is contained on one line.
     *
     * @param type TokenType of this token
     * @param line Line token appears on in stream
     * @param col  Column token appears on in stream
     * @param length Number of characters in the token
     * @param value Typed value of the token.  For string literals, String
     *              representing contents; for numeric literals, numeric value.
     */
    public Token create(TokenType type,
                        int index,
                        int line,
                        int col,
                        int length,
                        Object value);

    /**
     * Create multi-character token with a corresponding literal value and
     * explicit end location, allowing for tokens that span multiple lines.
     *
     * @param type TokenType of this token
     * @param line Line token appears on in stream
     * @param col  Column token appears on in stream
     * @param end_line Line in stream token ends on
     * @param end_col  Column in stream token ends on
     * @param value Typed value of the token.  For string literals, String
     *              representing contents; for numeric literals, numeric value.
     */
    public Token create(TokenType type,
                        int index,
                        int line,
                        int col,
                        int end_index,
                        int end_line,
                        int end_col,
                        Object value);

}
