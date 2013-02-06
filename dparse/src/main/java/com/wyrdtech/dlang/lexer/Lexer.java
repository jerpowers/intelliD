package com.wyrdtech.dlang.lexer;

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Reader;

/**
 * Lexer for the D language.
 *
 * Heavily inspired by the Visual D lexer:
 * http://www.dsource.org/projects/visuald
 * and Mono-D lexer:
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


    private int read() throws IOException {
        int c = in_stream.read();
        this.line = in_stream.getLine();
        this.col = in_stream.getCol();
        return c;
    }

    private int peek() throws IOException {
        return in_stream.peek();
    }

    private void error(int line, int col, String msg) {
        //TODO: capture these
    }

    public Token next() throws IOException, LexerException
    {
        boolean hadLineEnd = false;
/*
        int next;
        char ch;
        int x = col - 1;
        int y = line;
*/

        if (line == 1 && col == 1) hadLineEnd = true; // beginning of document


        int c = read();
        while (c != -1) {
            Token token;

            switch (c)
            {
                case ' ':
                case '\t':
                    continue;
//                case '\r':
                case '\n':
                    hadLineEnd = true;
                    continue;
                case '/':
                    int next = peek();
                    if (next == '/' || next == '*' || next == '+')
                    {
                        switch (read())
                        {
                            case '+':
/*
                                if (peek() == '+')// DDoc
                                    ReadMultiLineComment(Comment.Type.Documentation | Comment.Type.Block, true);
                                else
                                    ReadMultiLineComment(Comment.Type.Block, true);
*/
                                break;
                            case '*':
/*
                                if (peek() == '*')// DDoc
                                    ReadMultiLineComment(Comment.Type.Documentation | Comment.Type.Block, false);
                                else
                                    ReadMultiLineComment(Comment.Type.Block, false);
*/
                                break;
                            case '/':
/*
                                if (peek() == '/')// DDoc
                                    ReadSingleLineComment(Comment.Type.Documentation | Comment.Type.SingleLine);
                                else
                                    ReadSingleLineComment(Comment.Type.SingleLine);
*/
                                break;
                            default:
                                error(line, col, "Error while reading comment");
                                break;
                        }
                        continue;
                    }
                    else
                    {
                        token = LexOperator.read(in_stream); // '/'
                    }
                    break;
                case 'r':
                    peek = peek();
                    if (peek == '"')
                    {
                        read();
                        token = ReadVerbatimString(peek);
                        break;
                    }
                    else
                    goto default;
                case '`':
                    token = ReadVerbatimString(nextChar);
                    break;
                case '"':
                    token = ReadString(nextChar);
                    break;
                case '\\':
                    // http://digitalmars.com/d/1.0/lex.html#EscapeSequence
                    // - It's actually deprecated, but parse such literals anyway
                    String surr = "";
                    x = Col - 1;
                    y = Line;
                    var lit = ReadEscapeSequence(out ch, out surr);
                    token = Token(TokenType.Literal, x, y, lit.Length + 1, lit/*, ch.ToString()*/, LiteralFormat.StringLiteral);
                    token.RawCodeRepresentation = ch.ToString();
                    OnError(y, x, "Escape sequence strings are deprecated!");
                    break;
                case '\'':
                    token = ReadChar();
                    break;
                case '@':
                    token = Token(TokenType.At, Col-1, Line, 1);
                    break;
                default:
                    ch = (char)nextChar;

                    if (ch == 'x')
                    {
                        peek = peek();
                        if (peek == '"') // HexString
                        {
                            read(); // Skip the "

                            String numString = "";

                            while ((next = read()) != -1)
                            {
                                ch = (char)next;

                                if (is_hex(ch))
                                    numString += ch;
                                else if (!Char.IsWhiteSpace(ch))
                                    break;
                            }

                            return Token(TokenType.Literal, Col - 1, Line, numString.Length + 1, ParseFloatValue(numString, 16), /*numString,*/ LiteralFormat.Scalar);
                        }
                    }
                    else if (ch == 'q') // Token strings
                    {
                        peek = peek();
                        if (peek == '{'/*q{ ... }*/ || peek == '"'/* q"{{ ...}}   }}"*/)
                        {
                            x = Col - 1;
                            y = Line;
                            String initDelim = "";
                            String endDelim = "";
                            String tokenString = "";
                            initDelim += (char)read();
                            bool IsQuoted = false;
                            int BracketLevel = 0; // Only needed if IsQuoted is false

                            // Read out initializer
                            if (initDelim == "\"")
                            {
                                IsQuoted = true;
                                initDelim = "";

                                int pk = peek();
                                ch = (char)pk;
                                if (Char.IsLetterOrDigit(ch)) // q"EOS EOS"
                                    while ((next = read()) != -1)
                                    {
                                        ch = (char)next;
                                        if (!Char.IsWhiteSpace(ch))
                                            initDelim += ch;
                                        else
                                            break;
                                    }
                                else if (ch == '(' || ch == '<' || ch == '[' || ch == '{')
                                {
                                    var firstBracket = ch;
                                    while ((next = read()) != -1)
                                    {
                                        ch = (char)next;
                                        if (ch == firstBracket)
                                            initDelim += ch;
                                        else
                                            break;
                                    }
                                }
                            }
                            else if (initDelim == "{")
                                BracketLevel = 1;

                            // Build end delimiter
                            endDelim = initDelim.Replace('{', '}').Replace('[', ']').Replace('(', ')').Replace('<', '>');
                            if (IsQuoted) endDelim += "\"";

                            // Read tokens
                            bool inSuperComment = false,
                                    inNestedComment = false;

                            while ((next = read()) != -1)
                            {
                                ch = (char)next;

                                tokenString += ch;

                                // comments are treated as part of the string inside of tokenized string. curly braces inside the comments are ignored. WEIRD!
                                if (!inSuperComment && tokenString.EndsWith("/+")) inSuperComment = true;
                                else if (inSuperComment && tokenString.EndsWith("+/")) inSuperComment = false;
                                if (!inSuperComment)
                                {
                                    if (!inNestedComment && tokenString.EndsWith("/*")) inNestedComment = true;
                                    else if (inNestedComment && tokenString.EndsWith("*/")) inNestedComment = false;
                                }

                                if (!inNestedComment && !inSuperComment)
                                {
                                    if (!IsQuoted && ch == '{')
                                        BracketLevel++;
                                    if (!IsQuoted && ch == '}')
                                        BracketLevel--;
                                }

                                if (tokenString.EndsWith(endDelim) && (IsQuoted || BracketLevel < 1))
                                {
                                    tokenString = tokenString.Remove(tokenString.Length - endDelim.Length);
                                    break;
                                }
                            }

                            return Token(TokenType.Literal, x, y, Col, Line, tokenString, /*tokenString,*/ LiteralFormat.VerbatimStringLiteral);
                        }
                    }

                    if (Char.IsLetter(ch) || ch == '_' || ch == '\\')
                    {
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
                    }
                    else if (Char.IsDigit(ch))
                        token = ReadDigit(ch, Col - 1);
                    else
                        token = read_operator(ch);
                    break;
            }

            // try error recovery (token = null -> continue with next char)
            if (token != null)
            {
                //token.prev = base.curToken;
                return token;
            }
            else
            {
                OnError(Line, Col, "Invalid character");
                //StopLexing();
                break;
            }
        }

        return Token(TokenType.EOF, Col, Line, 0);
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
