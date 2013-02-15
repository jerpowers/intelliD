package com.wyrdtech.dlang.lexer;

import java.io.IOException;

/**
 * Logic for lexing out a token string
 *
 * q{foo}            -> "foo"
 * q{foo(q{hello}); }-> "foo(q{hello});"
 * q{ __TIME__ }     -> " __TIME__ "
 *
 * TODO: separate TokenType for literal token strings?
 */
public class LexTokenString {

    public static Token read(final LexerStream in_stream) throws IOException, LexerException
    {
        int start_line = in_stream.getLine();
        int start_col = in_stream.getCol();

        int next = in_stream.peek();
        if (next == -1) {
            throw new LexerException(start_line, start_col, "Unexpected end of stream when parsing token string literal");
        }
        if (next != 'q') {
            throw new LexerException(start_line, start_col, "Not a literal hex string");
        }

        in_stream.read();
        next = in_stream.peek();
        if (next != '{') {
            throw new LexerException(start_line, start_col, "Not a token string");
        }

        in_stream.read(); // consume '{'

        StringBuilder result = new StringBuilder();


        //TODO: preserve original string

        // token string, only tokens are valid inside
        // recursive on token strings for nesting
        Token token = Lexer.next(in_stream);
        while (token != null && token.type != TokenType.EOF) {
            if (token.type == TokenType.CloseCurlyBrace) {
                // end of token string
                break;
            }

            result.append(token.literalValue);
            try {
                token = Lexer.next(in_stream);
            } catch (LexerException e) {
                throw new LexerException(in_stream.getLine(), in_stream.getCol(), "Invalid token inside token string");
            }

        }

        if (token == null || token.type != TokenType.CloseCurlyBrace) {
            throw new LexerException(start_line, start_col, "Unexpected end of stream when parsing token string literal");
        }

        return new Token(TokenType.LiteralUtf8, start_line, start_col, in_stream.getLine(), in_stream.getCol(), result.toString());
    }

}
