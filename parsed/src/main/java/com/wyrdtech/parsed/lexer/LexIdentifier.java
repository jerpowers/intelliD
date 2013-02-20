package com.wyrdtech.parsed.lexer;

import com.wyrdtech.parsed.lexer.token.Token;
import com.wyrdtech.parsed.lexer.token.TokenFactory;
import com.wyrdtech.parsed.lexer.token.TokenType;

import java.io.IOException;

/**
 * Tokenize an identifier or keyword.
 */
public class LexIdentifier {

    private final TokenFactory factory;
    private final LexerStream in_stream;

    public LexIdentifier(TokenFactory factory, LexerStream in_stream) {
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
            throw new LexerException(start_line, start_col, "Unexpected end of input stream when parsing identifier");
        }
        if (!Character.isLetter(next) && next != '_') {
            throw new LexerException(start_line, start_col, "Invalid character for start of identifier: "+(char)next);
        }


        StringBuilder sb = new StringBuilder();

        while (valid_ident_char(next)) {
            sb.append(Character.toChars(in_stream.read()));
            next = in_stream.peek();
        }

        String result = sb.toString();

        // Check if is a known/reserved keyword
        TokenType type = TokenType.forValue(result);
        if (type == null) {
            type = TokenType.Identifier;
        }

        // Check for reserved identifier
        if (type == TokenType.Identifier && result.startsWith("__")) {
            throw new LexerException(start_line, start_col, "Unknown reserved identifier");
        }


        return factory.create(type,
                              start_index,
                              start_line,
                              start_col,
                              in_stream.getPosition(),
                              in_stream.getLine(),
                              in_stream.getCol(),
                              result);
    }

    private static boolean valid_ident_char(int c) {
        if (c == -1) {
            return false;
        }
        if (c == '_') {
            return true;
        }
        return Character.isLetterOrDigit(c);
    }
}
