package com.wyrdtech.parsed.lexer;

import com.wyrdtech.parsed.lexer.token.Token;
import com.wyrdtech.parsed.lexer.token.TokenFactory;
import com.wyrdtech.parsed.lexer.token.TokenType;

import java.io.IOException;

/**
 * Logic for lexing out a delimited string
 * q"(foo(xxx))"     -> foo(xxx)
 * q"[foo{]"         -> foo{
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
 */
public class LexDelimitedString {

    private final TokenFactory factory;
    private final LexerStream in_stream;

    public LexDelimitedString(TokenFactory factory, LexerStream in_stream) {
        this.factory = factory;
        this.in_stream = in_stream;
    }

    public Token read() throws IOException, LexerException
    {
        int start_index = in_stream.getPosition();
        int start_line = in_stream.getLine();
        int start_col = in_stream.getCol();

        int next = in_stream.peek();
        if (next == -1) {
            throw new LexerException(start_line, start_col, "Unexpected end of input stream when parsing delimited string literal");
        }
        if (next != 'q') {
            throw new LexerException(start_line, start_col, "Not a delimited string literal");
        }

        in_stream.read();
        next = in_stream.peek();
        if (next != '"') {
            throw new LexerException(start_line, start_col, "Not a delimited string literal");
        }
        in_stream.read(); // consume opening quote
        next = in_stream.peek();


        boolean is_identifier = false;
        boolean is_nesting = false;
        int nests = 0;

        StringBuilder delimiter = new StringBuilder();



        if (is_nesting_open(next)) {
            is_nesting = true;
            delimiter.append(Character.toChars(in_stream.read()));
            //TODO: support multi-bracket delimiters like "{{ foo }}"?
        }
        else if (Character.isLetter(next) || next == '_') {
            //TODO: use actual identifier lexing logic
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
        else {
            // non-identifier, non-nesting, just single char
            delimiter.append(Character.toChars(in_stream.read()));
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
            else if (is_nesting && next == start_delim.charAt(0)) {
                nests++;
            }
            else if (next == end_delim.charAt(0)) {
                nests--;
                if (nests < 0) {
                    // look for close followed by quote
                    if (in_stream.peek(2) == '"') {
                        in_stream.read();
                        in_stream.read();
                        break;
                    } else {
                        throw new LexerException(in_stream.getLine(), in_stream.getCol(), "Improperly delimited string");
                    }
                }
            }

            // not the end
            result.append(Character.toChars(in_stream.read()));
            next = in_stream.peek();
        }

        if (next == -1) {
            throw new LexerException(in_stream.getLine(), in_stream.getCol(), "Unexpected end of stream in delimited string literal");
        }

        return factory.create(TokenType.LiteralUtf8,
                              start_index,
                              start_line,
                              start_col,
                              in_stream.getPosition(),
                              in_stream.getLine(),
                              in_stream.getCol(),
                              result.toString());
    }


    private static boolean is_nesting_open(int c) {
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
