package com.wyrdtech.parsed.lexer;

import java.io.IOException;

/**
 * Tokenize literal strings.
 * "foo"
 * r"foo"
 * `foo`
 *
 */
public class LexStringLiteral {

    public static Token read(final LexerStream in_stream)
    throws IOException, LexerException
    {
        int start_index = in_stream.getPosition();
        int start_line = in_stream.getLine();
        int start_col = in_stream.getCol();

        boolean is_literal = false;

        int next = in_stream.peek();
        if (next == -1) {
            throw new LexerException(start_line, start_col, "Unexpected end of input stream when parsing string literal");
        }

        if (next == 'r') {
            is_literal = true;
            in_stream.read();
            next = in_stream.peek();
        }

        int initial = next;
        if (initial == '`') {
            is_literal = true;
        }

        TokenType type = TokenType.LiteralUtf8;

        StringBuilder sb = new StringBuilder();
        in_stream.read(); // consume opening quote

        next = in_stream.peek();
        while (next != -1) {

            if (next == initial) {
                in_stream.read(); // consume closing quote
                next = in_stream.peek();

                // end of literal, check for suffix
                if (next == 'c') {
                    type = TokenType.LiteralUtf8;
                    in_stream.read();
                }
                else if (next == 'w') {
                    type = TokenType.LiteralUtf16;
                    in_stream.read();
                }
                else if (next == 'd') {
                    type = TokenType.LiteralUtf32;
                    in_stream.read();
                }

                break; // exit loop
            }

            if (next == '\\' && !is_literal) {
                sb.append(Character.toChars(LexEscape.read(in_stream)));
            } else {
                sb.append(Character.toChars(in_stream.read()));
            }

            next = in_stream.peek();
        }

        return new Token(type,
                         start_index,
                         start_line,
                         start_col,
                         in_stream.getPosition(),
                         in_stream.getLine(),
                         in_stream.getCol(),
                         sb.toString());
    }

}
