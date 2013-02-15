package com.wyrdtech.parsed.lexer;

/**
 *
 */
public class LexerException extends Exception {

    public final int line;
    public final int col;

    public LexerException(int line, int col, String message) {
        super(message);
        this.line = line;
        this.col = col;
    }

    public LexerException(int line, int col, String message, Throwable cause) {
        super(message, cause);
        this.line = line;
        this.col = col;
    }

}
