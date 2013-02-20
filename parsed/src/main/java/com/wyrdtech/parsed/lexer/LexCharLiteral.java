package com.wyrdtech.parsed.lexer;

import com.wyrdtech.parsed.lexer.token.Token;
import com.wyrdtech.parsed.lexer.token.TokenFactory;
import com.wyrdtech.parsed.lexer.token.TokenType;

import java.io.IOException;

/**
 * Logic for lexing out character literals.
 * Created tokens have literal character value stored as a String type,
 * to support multi-char utf-32 characters.  For any non-utf-32 character,
 * will be a String with length 1.
 */
public class LexCharLiteral {

    private final TokenFactory factory;
    private final LexerStream in_stream;

    public LexCharLiteral(TokenFactory factory, LexerStream in_stream) {
        this.factory = factory;
        this.in_stream = in_stream;
    }

    /**
     * @return next token off the stream as a character literal.
     * @throws IOException on error reading the stream.
     * @throws LexerException if stream is not at start of a char literal.
     */
    public Token read()
    throws IOException, LexerException
    {
        int start_index = in_stream.getPosition();
        int start_line = in_stream.getLine();
        int start_col = in_stream.getCol();

        int code_point;

        int next = in_stream.peek();
        if (next != '\'') {
            throw new LexerException(start_line, start_col, "Not a character literal");
        }

        in_stream.read(); // consume opening quote

        next = in_stream.peek();
        if (next == '\'') {
            // empty '' character
            in_stream.read(); // consume closing quote
            return factory.create(TokenType.LiteralChar,
                                  start_index,
                                  start_line,
                                  start_col,
                                  2,
                                  "''");
        }

        if (next == '\\') {
            code_point = LexEscape.read(in_stream);
        } else {
            code_point = in_stream.read();
        }

        // Check (and consume) end quote
        if (in_stream.read() != '\'') {
            throw new LexerException(start_line, start_col, "Unterminated character literal");
        }

        String ch_str = String.valueOf(Character.toChars(code_point));

        //TODO: store code point in token instead?
        return factory.create(TokenType.LiteralChar,
                              start_index,
                              start_line,
                              start_col,
                              in_stream.getPosition(),
                              in_stream.getLine(),
                              in_stream.getCol(),
                              ch_str);
    }

}
