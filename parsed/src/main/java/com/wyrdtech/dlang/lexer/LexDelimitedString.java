package com.wyrdtech.dlang.lexer;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Logic for lexing out a delimited string or token string
 * q"(foo(xxx))"     -> foo(xxx)
 * q"[foo{]"         -> foo{
 * q{foo(q{hello});} -> foo(q{hello});
 *
 * q"END
 * Multi-line string literals
 * also supported.
 * END";
 *
 * As with all other lexer logic, written for clarity not performance.
 *
 * TODO: support single character delimiters other than nesting ones?
 * q"$foo$"  -> $foo$
 * The published docs (http://dlang.org/lex.html) are ambiguous on this.
 *
 * TODO: proper support for token strings, with actual tokenization
 */
public class LexDelimitedString {

    public static Token read(final LexerStream in_stream) throws IOException, LexerException
    {
        int start_line = in_stream.getLine();
        int start_col = in_stream.getCol();

        int next = in_stream.peek();
        if (next == -1) {
            throw new LexerException(start_line, start_col, "Unexpected end of input stream when parsing string literal");
        }
        if (next != 'q') {
            throw new LexerException(start_line, start_col, "Not a literal hex string");
        }

        in_stream.read();
        next = in_stream.peek();
        if (next != '"' && next != '{') {
            throw new LexerException(start_line, start_col, "Not a delimited string");
        }


        boolean is_quoted = false;
        boolean is_identifier = false;

        StringBuilder delimiter = new StringBuilder();

        if (next == '{') {
            delimiter.append(Character.toChars(in_stream.read()));
        }
        else {
            is_quoted = true;

            in_stream.read(); // consume opening quote
            next = in_stream.peek();

            if (is_valid_open(next)) {
                delimiter.append(Character.toChars(in_stream.read()));
                //TODO: support multi-bracket delimiters like "{{ foo }}"?
            }
            else if (Character.isLetter(next) || next == '_') {
                is_identifier = true;

                delimiter.append(Character.toChars(in_stream.read()));
                next = in_stream.peek();
                while (Character.isLetterOrDigit(next) || next == '_') {
                    delimiter.append(Character.toChars(in_stream.read()));
                    next = in_stream.peek();
                }
                if (next != '\n') {
                    throw new LexerException(in_stream.getLine(), in_stream.getCol(), "String delimiter must be followed by newline");
                }

                in_stream.read(); // consume newline
            }
        }
        next = in_stream.peek();


        String start_delim = delimiter.toString();
        String end_delim;
        if (is_identifier) {
            end_delim = start_delim;
        }
        else {
            end_delim = String.valueOf((char)valid_close(start_delim.charAt(0)));
        }


        StringBuilder result = new StringBuilder();

        while (next != -1) {

            //TODO: This.  Tokenize, but preserve original for string
            // For now, just treat as a regular non-token string
/*
            if (!is_quoted) {
                // token string, only tokens are valid inside
                Token token = null;
                try {
                    token = Lexer.next(in_stream);
                } catch (LexerException e) {
                    //
                }
            }
*/

            if (next == '\n' && is_identifier) {
                // whether end delimiter or not, newline is part of string
                result.append((char)in_stream.read());
                next = in_stream.peek();

                // see if the delimiter is sitting on the stream
                StringBuilder sb = new StringBuilder();
                for (char c : end_delim.toCharArray()) {
                    if (next == -1) {
                        throw new LexerException(in_stream.getLine(), in_stream.getCol(), "Unexpected end of stream in delimited string literal");
                    } else if (next != c) {
                        break;
                    }
                    sb.append(Character.toChars(in_stream.read()));
                    next = in_stream.peek();
                }
                if (next == '"' && sb.toString().equals(end_delim)) {
                    in_stream.read();
                    break; // end of string
                }

                // false alarm
                result.append(sb);
            }
            else if (next == end_delim.charAt(0)) {
                if (is_quoted) {
                    // check for end nesting delimiter - cheat, doesn't track nesting
                    // but rather looks for close followed by quote
                    if (in_stream.peek(2) == '"') {
                        in_stream.read();
                        in_stream.read();
                        break;
                    }
                } else {
                    in_stream.read();
                    break;
                    //TODO:
                    // check for end of token string (!is_quoted implies is tokens)
                }
            }

            // not the end
            result.append(Character.toChars(in_stream.read()));
            next = in_stream.peek();
        }

        if (next == -1) {
            throw new LexerException(in_stream.getLine(), in_stream.getCol(), "Unexpected end of stream in delimited string literal");
        }

        return new Token(TokenType.LiteralUtf8, start_line, start_col, in_stream.getLine(), in_stream.getCol(), result.toString());
    }


    private static boolean is_valid_open(int c) {
        if (c == -1) {
            return false;
        }
        return c == '[' || c == '(' || c == '<' || c == '{';
    }

    private static int valid_close(int open) {
        if (open == '[') {
            return ']';
        }
        if (open == '(') {
            return ')';
        }
        if (open == '<') {
            return '>';
        }
        if (open == '{') {
            return '}';
        }
        return open;
    }

}
