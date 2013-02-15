package com.wyrdtech.parsed.lexer;

/**
 * A token from lexing the D language.
 * Tracks the type, value, and position of the token in the lexed stream.
 *
 */
public class Token {

    public final TokenType type;

    public final int col;
    public final int line;

    public final int end_col;
    public final int end_line;

    public final Object literalValue;

    /**
     * Create single-character token.  Literal value is taken from
     * the TokenType.
     *
     * @param type TokenType of this token
     * @param line Line token appears on in stream
     * @param col  Column token appears on in stream
     */
    public Token(TokenType type, int line, int col) {
        this.type = type;
        this.line = line;
        this.col = col;

        this.end_line = line;
        this.end_col = col+1;
        this.literalValue = type.value;
    }

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
    public Token(TokenType type, int line, int col, int length)
    {
        this.type = type;
        this.line = line;
        this.col = col;
        this.end_col = col + length;

        this.end_line = line;
        this.literalValue = type.value;
    }

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
    public Token(TokenType type,
                 int line,
                 int col,
                 int length,
                 Object value)
    {
        this.type = type;
        this.line = line;
        this.col = col;
        this.end_col = col + length;
        this.literalValue = value;

        this.end_line = line;
    }

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
    public Token(TokenType type,
                 int line,
                 int col,
                 int end_line,
                 int end_col,
                 Object value)
    {
        this.type = type;
        this.line = line;
        this.col = col;
        this.end_line = end_line;
        this.end_col = end_col;
        this.literalValue = value;
    }

}
