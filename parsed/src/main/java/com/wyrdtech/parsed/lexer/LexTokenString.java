package com.wyrdtech.parsed.lexer;

import com.wyrdtech.parsed.lexer.token.Token;
import com.wyrdtech.parsed.lexer.token.TokenFactory;
import com.wyrdtech.parsed.lexer.token.TokenType;

import java.io.IOException;

/**
 * Logic for lexing out a token string.  String must consist
 * of valid tokens.
 *
 * q{foo}            -> "foo"
 * q{foo(q{hello}); }-> "foo(q{hello});"
 * q{ __TIME__ }     -> " __TIME__ "
 *
 * TODO: separate TokenType for literal token strings?
 */
public class LexTokenString {

    private final TokenFactory factory;
    private final LexerStream in_stream;
    private final Lexer parent;

    public LexTokenString(TokenFactory factory, LexerStream in_stream, Lexer parent) {
        this.factory = factory;
        this.in_stream = in_stream;
        this.parent = parent;
    }

    public Token read() throws IOException, LexerException
    {
        int start_index = in_stream.getPosition();
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

        // Recurse on the lexer, reading valid tokens
        //TODO: preserve original string for token value
        Token token = parent.next();
        while (token != null && token.getType() != TokenType.EOF) {
            if (token.getType() == TokenType.CloseCurlyBrace) {
                // end of token string
                break;
            }

            result.append(token.getValue());
            try {
                token = parent.next();
            } catch (LexerException e) {
                throw new LexerException(in_stream.getLine(), in_stream.getCol(), "Invalid token inside token string");
            }

        }

        if (token == null || token.getType() != TokenType.CloseCurlyBrace) {
            throw new LexerException(start_line, start_col, "Unexpected end of stream when parsing token string literal");
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

}
