package com.wyrdtech.parsed.lexer.token;

/**
 * A token from lexing the D language.
 *
 *
 */
public class BaseToken implements Token {

    private final TokenType type;

    private final int col;
    private final int line;

    private final int end_col;
    private final int end_line;

    private final int start_index;
    private final int end_index;

    private final Object literalValue;


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
    public BaseToken(TokenType type,
                     int index,
                     int line,
                     int col,
                     int end_index,
                     int end_line,
                     int end_col,
                     Object value)
    {
        this.type = type;
        this.start_index = index;
        this.end_index = end_index;
        this.line = line;
        this.col = col;
        this.end_line = end_line;
        this.end_col = end_col;
        this.literalValue = value;
    }

    @Override
    public TokenType getType() {
        return this.type;
    }

    @Override
    public int getStartIndex() {
        return this.start_index;
    }

    @Override
    public int getEndIndex() {
        return this.end_index;
    }

    @Override
    public int getLine() {
        return this.line;
    }

    @Override
    public int getCol() {
        return this.col;
    }

    @Override
    public int getEndLine() {
        return this.end_line;
    }

    @Override
    public int getEndCol() {
        return this.end_col;
    }

    @Override
    public Object getValue() {
        return this.literalValue;
    }
}
