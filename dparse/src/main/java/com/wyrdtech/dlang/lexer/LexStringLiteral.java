package com.wyrdtech.dlang.lexer;

import java.io.IOException;

/**
 *
 */
public class LexStringLiteral {

    public static Token read(final LexerStream in_stream)
    throws IOException, LexerException
    {
        int start_line = in_stream.getLine();
        int start_col = in_stream.getCol();

        int next = in_stream.peek();
        if (next == -1) {
            throw new LexerException(start_line, start_col, "Unexpected end of input stream when parsing string literal");
        }

        char initial = (char)next;

        TokenType type = TokenType.LiteralUtf8;

        StringBuilder sb = new StringBuilder();
        sb.append((char)in_stream.read());

        next = in_stream.peek();
        while (next != -1) {

            if (next == initial) {
                sb.append((char)in_stream.read());
                next = in_stream.peek();

                // end of literal, check for suffix
                if (next == 'c') {
                    type = TokenType.LiteralUtf8;
                    sb.append((char)in_stream.read());
                }
                else if (next == 'w') {
                    type = TokenType.LiteralUtf16;
                    sb.append((char)in_stream.read());
                }
                else if (next == 'd') {
                    type = TokenType.LiteralUtf32;
                    sb.append((char)in_stream.read());
                }

                break; // exit loop
            }

            if (next == '\\') {
                sb.append(Character.toChars(LexEscape.read(in_stream)));
            } else {
                sb.append((char)in_stream.read());
            }

        }

        return new Token(type, start_col, start_line, in_stream.getCol(), in_stream.getLine(), sb.toString());
    }

}
