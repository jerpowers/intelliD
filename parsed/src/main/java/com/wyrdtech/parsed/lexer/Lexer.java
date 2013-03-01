package com.wyrdtech.parsed.lexer;

import com.wyrdtech.parsed.lexer.token.Token;
import com.wyrdtech.parsed.lexer.token.TokenFactory;
import com.wyrdtech.parsed.lexer.token.TokenType;

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
 * Tokens are generated using a TokenFactory, injected at lexer creation time.
 * This allows for users of the lexer to have their own custom token types, for
 * easier integration with other lexer/parser code.
 *
 *
 * Originally based on the Mono-D lexer:
 * http://mono-d.alexanderbothe.com/
 *
 */
public class Lexer {

    private final TokenFactory factory;
    private final LexerStream in_stream;

    private boolean eof;


    // Sub-lexers for tokenizing once the next token type is determined
    private final LexCharLiteral lexChar;
    private final LexComment lexComment;
    private final LexDelimitedString lexDelimitedString;
    private final LexHexLiteral lexHexLiteral;
    private final LexIdentifier lexIdentifier;
    private final LexNumericLiteral lexNumber;
    private final LexOperator lexOperator;
    private final LexStringLiteral lexString;
    private final LexTokenString lexTokenString;

    public Lexer(TokenFactory tokenFactory, Reader inStream) {
        this(tokenFactory, new LexerStream(inStream));
    }

    private Lexer(TokenFactory tokenFactory, LexerStream inStream) {
        this.factory = tokenFactory;
        this.in_stream = inStream;
        this.eof = false;


        //TODO: inject sub-lexers for more control
        lexChar = new LexCharLiteral(this.factory, this.in_stream);
        lexComment = new LexComment(this.factory, this.in_stream);
        lexDelimitedString = new LexDelimitedString(this.factory, this.in_stream);
        lexHexLiteral = new LexHexLiteral(this.factory, this.in_stream);
        lexIdentifier = new LexIdentifier(this.factory, this.in_stream);
        lexNumber = new LexNumericLiteral(this.factory, this.in_stream);
        lexOperator = new LexOperator(this.factory, this.in_stream);
        lexString = new LexStringLiteral(this.factory, this.in_stream);
        lexTokenString = new LexTokenString(this.factory, this.in_stream, this);


        // TODO: Ignore any first-line '#!'
    }

    /**
     * @return Current position on the Reader stream, relative to
     *         position of stream when lexer was created.
     */
    public int position() {
        return in_stream.getPosition();
    }

    /**
     * Read the next token off the stream and return it.
     * If there is an error tokenizing, will return a token with type
     * TokenType.Unknown, with the stream having consumed as much as could
     * be made sense of before encountering the error.
     * Subsequent calls to next() may continue to return an Unknown token,
     * until a recognized token is encountered.
     *
     * @return Next token on the stream
     * @throws IOException
     */
    public Token next() throws IOException
    {
        Token token;
        try {
            token = _next();
        } catch (LexerException e) {
            token = factory.create(TokenType.Unknown, in_stream.getPosition(), in_stream.getLine(), in_stream.getCol());
            in_stream.read();
        }

        // TODO: Catch and release special tokens: meta tokens and '#line'

        return token;
    }

    public Token _next() throws IOException, LexerException
    {

        int n = in_stream.peek();

        // End of stream
        if (n == -1) {
            if (eof) {
                // already reported EOF, no more tokens
                return null;
            }
            eof = true;
            return factory.create(TokenType.EOF,
                                  in_stream.getLine(),
                                  in_stream.getCol(),
                                  0);
        }

        // Whitespace
        if (Character.isWhitespace(n)) {
            StringBuilder whites = new StringBuilder();
            int pos = in_stream.getPosition();
            int line = in_stream.getLine();
            int col = in_stream.getCol();
            do {
                whites.append(Character.toChars(in_stream.read()));
                n = in_stream.peek();
            }
            while (Character.isWhitespace(n));

            return factory.create(TokenType.Whitespace,
                                  pos,
                                  line,
                                  col,
                                  in_stream.getPosition(),
                                  in_stream.getLine(),
                                  in_stream.getCol(),
                                  whites.toString());
        }


        // All other tokens
        Token token;
        int ahead;
        switch (n)
        {
            case '/':
                ahead = in_stream.peek(2);
                if (ahead == '/' || ahead == '*' || ahead == '+') {
                    token = lexComment.read();
                }
                else {
                    token = lexOperator.read(); // '/'
                }
                break;
            case 'r':
                ahead = in_stream.peek(2);
                if (ahead == '"') {
                    token = lexString.read();
                    break;
                }
                // else default
            case 'x':
                ahead = in_stream.peek(2);
                if (ahead == '"') {
                    token = lexHexLiteral.read();
                    break;
                }
                // else default
            case 'q':
                ahead = in_stream.peek(2);
                if (ahead == '"') {
                    token = lexDelimitedString.read();
                    break;
                }
                else if (ahead == '{') {
                    token = lexTokenString.read();
                    break;
                }
                // else default
            case '`':
            case '"':
                token = lexString.read();
                break;
            case '\\':
                throw new LexerException(in_stream.getLine(), in_stream.getCol(), "Escape sequence strings are deprecated");
            case '\'':
                token = lexChar.read();
                break;
            case '@':
                token = factory.create(TokenType.At, in_stream.getLine(), in_stream.getCol(), 1);
                in_stream.read();
                break;
            case '.':
                ahead = in_stream.peek(2);
                if (ahead != -1 && Character.isDigit(ahead)) {
                    token = lexNumber.read();
                    break;
                }
                // else default
            default:
                if (Character.isLetter(n) || n == '_') // || n == '\\'
                {
                    token = lexIdentifier.read();
                }
                else if (Character.isDigit(n)) {
                    token = lexNumber.read();
                }
                else {
                    token = lexOperator.read();
                }
                break;
        } // end switch

        if (token == null) {
            throw new LexerException(in_stream.getLine(), in_stream.getCol(), "Invalid character");
        }

        return token;
    }


}
