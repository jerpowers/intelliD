package com.wyrdtech.dlang.lexer;

import java.io.IOException;

/**
 * Written for clarity, not performance.
 *
 * TODO: lex inside ddoc comments
 */
public class LexComment {

    public static Token read(final LexerStream in_stream) throws IOException, LexerException
    {
        int start_line = in_stream.getLine();
        int start_col = in_stream.getCol();

        int next = in_stream.peek();
        if (next == -1) {
            // end of stream!
            throw new LexerException(in_stream.getLine(), in_stream.getCol(), "Unexpected end of input stream when parsing comment");
        }
        if (next != '/')
        {
            throw new LexerException(in_stream.getLine(), in_stream.getCol(), "Comments must start with '/'");
        }

        TokenType type;

        // Build up contents of comment
        StringBuilder sb = new StringBuilder();
        sb.append((char)in_stream.read());

        next = in_stream.peek();
        switch (next)
        {
            case '+':
                sb.append((char)in_stream.read());
                if (in_stream.peek() == '+') {
                    // DDoc
                    sb.append((char)in_stream.read());
                    type = TokenType.DocCommentNest;
                } else {
                    type = TokenType.BlockCommentNest;
                }
                sb.append(read_multi_nested(in_stream));
                break;
            case '*':
                sb.append((char)in_stream.read());
                if (in_stream.peek() == '*') {
                    // DDoc
                    sb.append((char)in_stream.read());
                    type = TokenType.DocComment;
                } else {
                    type = TokenType.BlockComment;
                }
                sb.append(read_multi(in_stream));
                break;
            case '/':
                sb.append((char)in_stream.read());
                next = in_stream.peek();
                if (next == '/') {
                    // DDoc
                    sb.append((char)in_stream.read());
                    type = TokenType.DocLineComment;
                } else {
                    type = TokenType.LineComment;
                }
                // Read to end of line, but don't consume '\n'
                while (next != -1 && next != '\n') {
                    sb.append((char)in_stream.read());
                    next = in_stream.peek();
                }
                break;
            default:
                throw new LexerException(start_line, start_col, "Error while reading comment");
        }

        return new Token(type,
                         start_line,
                         start_col,
                         in_stream.getLine(),
                         in_stream.getCol(),
                         sb.toString());

    }

    // Read rest of multi-line comment, after '/*'
    private static String read_multi(final LexerStream in_stream) throws IOException, LexerException {
        StringBuilder sb = new StringBuilder();

        int next = in_stream.peek();
        while (next != -1) {
            char cur = (char)in_stream.read();
            sb.append(cur);

            next = in_stream.peek();

            // check for end
            if (cur == '*' && next == '/') {
                sb.append((char)in_stream.read());
                return sb.toString();
            }

        }
        throw new LexerException(in_stream.getLine(), in_stream.getCol(), "Unexpected end of stream inside multi-line comment");
    }

    private static String read_multi_nested(final LexerStream in_stream) throws IOException, LexerException
    {
        int depth = 1;
        StringBuilder sb = new StringBuilder();

        int next = in_stream.peek();
        while (next != -1) {
            char cur = (char)in_stream.read();
            sb.append(cur);

            next = in_stream.peek();

            // check for nesting
            //TODO: "/+/" case
            if (cur == '/' && next == '+') {
                depth++;
            }

            // check for end
            if (cur == '+' && next == '/') {
                depth--;
                if (depth <= 0) {
                    sb.append((char)in_stream.read());
                    return sb.toString();
                }
            }

        }
        throw new LexerException(in_stream.getLine(), in_stream.getCol(), "Unexpected end of stream inside multi-line comment");
    }

}
