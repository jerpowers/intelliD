package com.wyrdtech.parsed.lexer;

import java.io.IOException;
import java.io.Reader;

/**
 * Lexer for the D language.
 *
 * Written to be easy to understand/test, not for performance.
 * Various lexing parts are broken up into separate classes by token type,
 * these may do some non-optimal look ahead when finding start/end of token.
 *
 * The Lexer will look at the next part of the stream far enough ahead to
 * determine what type of token is next, then call the appropriate sub-lexer
 * to read and tokenize.  Each sub-lexer throws an exception if what it was
 * asked to read is not the type of token it knows.
 *
 * Originally based on the Mono-D lexer:
 * http://mono-d.alexanderbothe.com/
 */
public class Lexer {

    private final LexerStream in_stream;

    public Lexer(Reader in_stream) {
        this.in_stream = new LexerStream(in_stream);

        // TODO: Ignore any first-line '#!'
    }

    public Token next() throws IOException, LexerException
    {
        return next(this.in_stream);
    }

    public static Token next(LexerStream in_stream) throws IOException, LexerException
    {
        int n = in_stream.peek();

        while (Character.isWhitespace(n)) {
            in_stream.read();
            n = in_stream.peek();
        }

        // End of stream
        if (n == -1) {
            return new Token(TokenType.EOF, in_stream.getLine(), in_stream.getCol(), 0);
        }


        Token token;

        int ahead;
        switch (n)
        {
/*
            case ' ':
            case '\t':
                continue;
//          case '\r':
            case '\n':
                continue;
*/
            case '/':
                ahead = in_stream.peek(2);
                if (ahead == '/' || ahead == '*' || ahead == '+') {
                    token = LexComment.read(in_stream);
                }
                else {
                    token = LexOperator.read(in_stream); // '/'
                }
                break;
            case 'r':
                ahead = in_stream.peek(2);
                if (ahead == '"') {
                    token = LexStringLiteral.read(in_stream);
                    break;
                }
                // else default
            case 'x':
                ahead = in_stream.peek(2);
                if (ahead == '"') {
                    token = LexHexLiteral.read(in_stream);
                    break;
                }
                // else default
            case 'q':
                ahead = in_stream.peek(2);
                if (ahead == '"') {
                    token = LexDelimitedString.read(in_stream);
                    break;
                }
                else if (ahead == '{') {
                    token = LexTokenString.read(in_stream);
                    break;
                }
                // else default
            case '`':
            case '"':
                token = LexStringLiteral.read(in_stream);
                break;
            case '\\':
                throw new LexerException(in_stream.getLine(), in_stream.getCol(), "Escape sequence strings are deprecated");
            case '\'':
                token = LexCharLiteral.read(in_stream);
                break;
            case '@':
                token = new Token(TokenType.At, in_stream.getLine(), in_stream.getCol(), 1);
                in_stream.read();
                break;
            default:
                if (Character.isLetter(n) || n == '_') // || n == '\\'
                {
                    token = LexIdentifier.read(in_stream);
                }
                else if (Character.isDigit(n)) {
                    token = LexNumericLiteral.read(in_stream);
                }
                else {
                    token = LexOperator.read(in_stream);
                }
                break;
        } // end switch

        if (token == null) {
            throw new LexerException(in_stream.getLine(), in_stream.getCol(), "Invalid character");
        }

        return token;
    }


}
