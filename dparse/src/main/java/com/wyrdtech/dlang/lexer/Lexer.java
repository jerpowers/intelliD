package com.wyrdtech.dlang.lexer;

import java.io.IOException;
import java.io.Reader;

/**
 * Lexer for the D language.
 *
 * Written to be easy to understand/test, not for performance.
 * Various lexing parts are broken up into separate classes by token type,
 * these may do some non-optimal look ahead when finding start/end of token.
 *
 * Originally based on the Mono-D lexer:
 * http://mono-d.alexanderbothe.com/
 */
public class Lexer {

    private final LexerStream in_stream;

    private int line = 1;
    private int col = 1;

    private static enum State {
        kWhite,
        kBlockComment,
        kNestedComment,
        kStringCStyle,
        kStringWysiwyg,
        kStringAltWysiwyg,
        kStringDelimited,
        kStringDelimitedNestedBracket,
        kStringDelimitedNestedParen,
        kStringDelimitedNestedBrace,
        kStringDelimitedNestedAngle,
        kStringTokenFirst,  // after 'q', but before '{' to pass '{' as single operator
        kStringToken,  // encoded by tokenStringLevel > 0
        kStringHex,    // for now, treated as State.kStringWysiwyg
        kStringEscape, // removed in D2.026, not supported
    }


    public Lexer(Reader in_stream) {
        this.in_stream = new LexerStream(in_stream);

        // TODO: Ignore any first-line '#!'
    }


    private void error(int line, int col, String msg) {
        //TODO: this, instead of exceptions?
    }

    public Token next() throws IOException, LexerException
    {
        return next(this.in_stream);
    }

    public static Token next(LexerStream in_stream) throws IOException, LexerException
    {
        int n = in_stream.peek();

        // End of stream
        if (n == -1) {
            return new Token(TokenType.EOF, in_stream.getLine(), in_stream.getCol(), 0);
        }

        while (Character.isWhitespace(n)) {
            in_stream.read();
            n = in_stream.peek();
        }

        Token token = null;

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
                int next = in_stream.peek(2);
                if (next == '/' || next == '*' || next == '+') {
                    token = LexComment.read(in_stream);
                }
                else {
                    token = LexOperator.read(in_stream); // '/'
                }
                break;
            case 'r':
                if (in_stream.peek(2) == '"') {
                    token = LexStringLiteral.read(in_stream);
                    break;
                }
                // else default
            case 'x':
                if (in_stream.peek(2) == '"') {
                    token = LexHexLiteral.read(in_stream);
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
/*
                    x = Col - 1; // Col was incremented above, but we want the start of the identifier
                    y = Line;
                    bool canBeKeyword;
                    var s = ReadIdent(ch, out canBeKeyword);
                    if (canBeKeyword)
                    {
                        // A micro-optimization..
                        if (s.Length >= 8 && s[0] == '_' && s[1] == '_')
                        {
                            LiteralFormat literalFormat = 0;
                            var subFormat = LiteralSubformat.Utf8;
                            object literalValue = null;

                            // Fill in static string surrogates directly
                            if (s == "__DATE__")
                            {
                                literalFormat = LiteralFormat.StringLiteral;
                                literalValue = DateTime.Now.ToString("MMM dd yyyy", System.Globalization.CultureInfo.InvariantCulture.DateTimeFormat);
                            }
                            else if (s == "__TIME__")
                            {
                                literalFormat = LiteralFormat.StringLiteral;
                                literalValue = DateTime.Now.ToString("HH:mm:ss", System.Globalization.CultureInfo.InvariantCulture.DateTimeFormat);
                            }
                            else if (s == "__TIMESTAMP__")
                            {
                                literalFormat = LiteralFormat.StringLiteral;
                                literalValue = DateTime.Now.ToString("ddd MMM dd HH:mm:ss yyyy", System.Globalization.CultureInfo.InvariantCulture.DateTimeFormat);
                            }
                            else if (s == "__VENDOR__")
                            {
                                literalFormat = LiteralFormat.StringLiteral;
                                literalValue = "D Parser v" + System.Reflection.Assembly.GetExecutingAssembly().GetName().Version.ToString(3) + " by Alexander Bothe";
                            }
                            else if (s == "__VERSION__")
                            {
                                subFormat = LiteralSubformat.Integer;
                                var lexerVersion = System.Reflection.Assembly.GetExecutingAssembly().GetName().Version;
                                literalFormat = LiteralFormat.Scalar;
                                literalValue = lexerVersion.Major * 1000 + lexerVersion.Minor;
                            }

                            if (literalFormat != 0)
                                return Token(TokenType.Literal, x, y, s.Length,
                                             literalValue,
                                             //literalValue is string ? (string)literalValue : literalValue.ToString(),
                                             literalFormat,
                                             subFormat);
                        }

                        byte key;
                        if(TokenType.Keywords_Lookup.TryGetValue(s,out key))
                            return Token(key, x, y, s.Length);
                    }
                    return Token(TokenType.Identifier, x, y, s);
*/
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





/*
    private static boolean is_identifier_part(int ch)
    {
        if((ch >= 'a' && ch<='z') ||
           (ch >= 'A' && ch<='Z') ||
           (ch >= '0' && ch<='9') ||
           ch == '_')
            return true;

        switch(ch)
        {
            case ' ':
            case '@':
            case '/':
            case '(':
            case ')':
            case '[':
            case ']':
            case '{':
            case '}':
            case '=':
            case '\"':
            case '\'':
            case -1:
                return false;
            default:
                return char.IsLetterOrDigit((char)ch); // accept unicode letters
        }
    }
*/

}
