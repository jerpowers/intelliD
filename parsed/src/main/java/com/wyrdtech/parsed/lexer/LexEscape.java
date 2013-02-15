package com.wyrdtech.parsed.lexer;

import org.apache.commons.lang3.StringEscapeUtils;

import java.io.IOException;

/**
 * No actual tokens, rather reads an escape sequence and returns an integer
 * containing the utf code point of the escaped value.
 *
 * TODO: return character codepoint instead (as int)
 */
public class LexEscape {

    public static int read(final LexerStream in_stream) throws IOException, LexerException
    {
        int next = in_stream.peek();
        if (next == -1) {
            throw new LexerException(in_stream.getLine(), in_stream.getCol(), "Unexpected end of input stream when inside escape sequence");
        }
        if (next != '\\') {
            throw new LexerException(in_stream.getLine(), in_stream.getCol(), "Not an escape sequence");
        }

        int result; // int instead of 'char' to support higher-value utf-32

        in_stream.read(); // backslash '\' char

        // Consume next, as char after escape will always lex as part of escape
        // or result in error
        int cur = in_stream.read();
        next = in_stream.peek();
        switch (cur)
        {
            case '\'':
                result = '\'';
                break;
            case '\"':
                result = '\"';
                break;
            case '?':
                result = '?';
//                return "\\?"; // Literal question mark
                break;
            case '\\':
                result = '\\';
                break;
            case '0':
                result = '\0';
                break;
            case 'a':
                result = 0x07; // '\a', BEL alert
                break;
            case 'b':
                result = '\b'; // Backspace
                break;
            case 'f':
                result = '\f'; // Form feed
                break;
            case 'n':
                result = '\n';
                break;
            case 'r':
                result = '\r';
                break;
            case 't':
                result = '\t';
                break;
            case 'v':
                result = 0x0B; // '\v', Vertical tab
                break;
            case 'x':
                //TODO: refactor commonality
                // 8 bit unicode character, read next two chars as hex
                result = 0;
                for (int i = 0; i < 2; i++) {
                    if (next == -1) {
                        throw new LexerException(in_stream.getLine(), in_stream.getCol(), "Unexpected end of stream");
                    }
                    try {
                        int n = Integer.parseInt(String.valueOf((char)next), 16);
                        result = (16 * result) + n;
                    } catch (NumberFormatException e) {
                        throw new LexerException(in_stream.getLine(), in_stream.getCol(), "Invalid character in literal: "+next, e);
                    }

                    in_stream.read();
                    next = in_stream.peek();
                }

                break;
            case 'u':
                // 16 bit unicode character
                // read next four chars, as 2 bytes of literal
                result = 0;
                for (int i = 0; i < 4; i++) {
                    if (next == -1) {
                        throw new LexerException(in_stream.getLine(), in_stream.getCol(), "Unexpected end of stream");
                    }
                    try {
                        int n = Integer.parseInt(String.valueOf((char)next), 16);
                        result = (16 * result) + n;
                    } catch (NumberFormatException e) {
                        throw new LexerException(in_stream.getLine(), in_stream.getCol(), "Invalid character in literal: "+next, e);
                    }

                    in_stream.read();
                    next = in_stream.peek();
                }

                break;
            case 'U':
                // 32 bit unicode character
                // read next eight chars, as 4 bytes of literal
                result = 0;
                for (int i = 0; i < 8; i++) {
                    if (next == -1) {
                        throw new LexerException(in_stream.getLine(), in_stream.getCol(), "Unexpected end of stream");
                    }
                    try {
                        int n = Integer.parseInt(String.valueOf((char)next), 16);
                        result = (16 * result) + n;
                    } catch (NumberFormatException e) {
                        throw new LexerException(in_stream.getLine(), in_stream.getCol(), "Invalid character in literal: "+next, e);
                    }

                    in_stream.read();
                    next = in_stream.peek();
                }

                break;

            case '&':
                // HTML named character entity
                StringBuilder entity = new StringBuilder();
                entity.append((char)cur);
                while (true)
                {
                    if (next == -1) {
                        throw new LexerException(in_stream.getLine(), in_stream.getCol(), "Unexpected end of stream");
                    }

                    if (next == ';') {
                        entity.append((char)in_stream.read());
                        break;
                    }

                    if (Character.isLetter(next)) {
                        entity.append((char)in_stream.read());
                    } else {
                        throw new LexerException(in_stream.getLine(), in_stream.getCol(), "Unexpected character found in named entity: "+next);
                    }

                    next = in_stream.peek();
                }

                if (entity.length() < 1) {
                    throw new LexerException(in_stream.getLine(), in_stream.getCol(), "Empty named character entity encountered");
                }

                String val = StringEscapeUtils.unescapeHtml4(entity.toString());
                result = Character.codePointAt(val, 0);
                break;

            default:
                if (!is_oct(cur)) {
                    throw new LexerException(in_stream.getLine(), in_stream.getCol(), "Unrecognized escape sequence: "+next);
                }

                result = Integer.parseInt(String.valueOf((char)cur), 8);

                int cnt = 1;
                while (is_oct(next) && cnt < 3) {
                    // maximum of three octal digits
                    cnt++;

                    try {
                        int n = Integer.parseInt(String.valueOf((char)next), 8);
                        result = (8 * result) + n;
                    } catch (NumberFormatException e) {
                        throw new LexerException(in_stream.getLine(), in_stream.getCol(), "Invalid character in octal literal: "+next, e);
                    }

                    in_stream.read();
                    next = in_stream.peek();
                }
                break;
        }

        return result;
    }

    private static boolean is_oct(int digit)
    {
        return Character.isDigit(digit) && digit != '9' && digit != '8';
    }

}
