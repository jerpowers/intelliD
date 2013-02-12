package com.wyrdtech.dlang.lexer;

import java.io.IOException;

/**
 * Logic for lexing out character literals.
 * Created tokens have literal character value stored as a String type,
 * to support multi-char utf-32 characters.  For any non-utf-32 character,
 * will be a String with length 1.
 */
public class LexCharLiteral {

    public static Token read(final LexerStream in_stream)
    throws IOException, LexerException
    {
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
            return new Token(TokenType.LiteralChar,
                             start_line,
                             start_col,
                             2, "''");
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
        return new Token(TokenType.LiteralChar,
                         start_line,
                         start_col,
                         in_stream.getLine(),
                         in_stream.getCol(),
                         ch_str);
    }

}
