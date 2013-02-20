package com.wyrdtech.parsed.lexer.token;

/**
 * A token from lexing the D language.
 * Tracks the type, value, and position of the token in the lexed stream.
 *
 * TODO: Just use a base token class w/o interface?
 */
public interface Token {

    public TokenType getType();

    public int getStartIndex();
    public int getEndIndex();

    public int getLine();
    public int getCol();

    public int getEndLine();
    public int getEndCol();

    public Object getValue();

}
