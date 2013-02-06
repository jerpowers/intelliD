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

    // Used to handle end-lines gracefully (auto-converted to '\n')
    private final LineNumberReader in_stream;

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
        this.in_stream = new LineNumberReader(in_stream);

        // Count lines starting at one
        this.in_stream.setLineNumber(line);

        // TODO: Ignore any first-line '#!'
    }


    private int read() throws IOException {
        int c = in_stream.read();
        if (in_stream.getLineNumber() != line) {
            col = 1;
            line = in_stream.getLineNumber(); // should be line+1
        } else if (c >= 0) {
            col++;
        }
        return c;
    }

    //TODO: better
    private int peek() throws IOException {
        in_stream.mark(1);
        int c = in_stream.read();
        in_stream.reset();

        return c;
    }

    private void error(int line, int col, String msg) {
        //TODO: capture these
    }

    public Token next() throws IOException
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
                        token = ReadOperator('/');
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
                        token = ReadOperator(ch);
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


    private Token read_operator(char c) throws IOException
    {
        int x = col - 1;
        int y = line;
        switch (c)
        {
            case '+':
                switch (peek())
                {
                    case '+':
                        read();
                        return new Token(TokenType.Increment, x, y);
                    case '=':
                        read();
                        return new Token(TokenType.PlusAssign, x, y);
                }
                return new Token(TokenType.Plus, x, y);
            case '-':
                switch (peek())
                {
                    case '-':
                        read();
                        return new Token(TokenType.Decrement, x, y);
                    case '=':
                        read();
                        return new Token(TokenType.MinusAssign, x, y);
                    case '>':
                        read();
                        return new Token(TokenType.TildeAssign, x, y);
                }
                return new Token(TokenType.Minus, x, y);
            case '*':
                switch (peek())
                {
                    case '=':
                        read();
                        return new Token(TokenType.TimesAssign, x, y, 2);
                    default:
                        break;
                }
                return new Token(TokenType.Times, x, y);
            case '/':
                switch (peek())
                {
                    case '=':
                        read();
                        return new Token(TokenType.DivAssign, x, y, 2);
                }
                return new Token(TokenType.Div, x, y);
            case '%':
                switch (peek())
                {
                    case '=':
                        read();
                        return new Token(TokenType.ModAssign, x, y, 2);
                }
                return new Token(TokenType.Mod, x, y);
            case '&':
                switch (peek())
                {
                    case '&':
                        read();
                        return new Token(TokenType.LogicalAnd, x, y, 2);
                    case '=':
                        read();
                        return new Token(TokenType.BitwiseAndAssign, x, y, 2);
                }
                return new Token(TokenType.BitwiseAnd, x, y);
            case '|':
                switch (peek())
                {
                    case '|':
                        read();
                        return new Token(TokenType.LogicalOr, x, y, 2);
                    case '=':
                        read();
                        return new Token(TokenType.BitwiseOrAssign, x, y, 2);
                }
                return new Token(TokenType.BitwiseOr, x, y);
            case '^':
                switch (peek())
                {
                    case '=':
                        read();
                        return new Token(TokenType.XorAssign, x, y, 2);
                    case '^':
                        read();
                        if (peek() == '=')
                        {
                            read();
                            return new Token(TokenType.PowAssign, x, y, 3);
                        }
                        return new Token(TokenType.Pow, x, y, 2);
                }
                return new Token(TokenType.Xor, x, y);
            case '!':
                switch (peek())
                {
                    case '=':
                        read();
                        return new Token(TokenType.NotEqual, x, y, 2); // !=

                    case '<':
                        read();
                        switch (peek())
                        {
                            case '=':
                                read();
                                return new Token(TokenType.UnorderedOrGreater, x, y, 3); // !<=
                            case '>':
                                read();
                                switch (peek())
                                {
                                    case '=':
                                        read();
                                        return new Token(TokenType.Unordered, x, y, 4); // !<>=
                                }
                                return new Token(TokenType.UnorderedOrEqual, x, y, 3); // !<>
                        }
                        return new Token(TokenType.UnorderedGreaterOrEqual, x, y, 2); // !<

                    case '>':
                        read();
                        switch (peek())
                        {
                            case '=':
                                read();
                                return new Token(TokenType.UnorderedOrLess, x, y, 3); // !>=
                            default:
                                break;
                        }
                        return new Token(TokenType.UnorderedLessOrEqual, x, y, 2); // !>

                }
                return new Token(TokenType.Not, x, y);
            case '~':
                switch (peek())
                {
                    case '=':
                        read();
                        return new Token(TokenType.TildeAssign, x, y, 2);
                }
                return new Token(TokenType.Tilde, x, y);
            case '=':
                switch (peek())
                {
                    case '=':
                        read();
                        return new Token(TokenType.Equal, x, y, 2);
                    case '>':
                        read();
                        return new Token(TokenType.GoesTo, x, y, 2);
                }
                return new Token(TokenType.Assign, x, y);
            case '<':
                switch (peek())
                {
                    case '<':
                        read();
                        switch (peek())
                        {
                            case '=':
                                read();
                                return new Token(TokenType.ShiftLeftAssign, x, y, 3);
                            default:
                                break;
                        }
                        return new Token(TokenType.ShiftLeft, x, y, 2);
                    case '>':
                        read();
                        switch (peek())
                        {
                            case '=':
                                read();
                                return new Token(TokenType.LessEqualOrGreater, x, y, 3);
                            default:
                                break;
                        }
                        return new Token(TokenType.LessOrGreater, x, y, 2);
                    case '=':
                        read();
                        return new Token(TokenType.LessEqual, x, y, 2);
                }
                return new Token(TokenType.LessThan, x, y);
            case '>':
                switch (peek())
                {
                    case '>':
                        read();
                        if (peek() != -1)
                        {
                            switch ((char)peek())
                            {
                                case '=':
                                    read();
                                    return new Token(TokenType.ShiftRightAssign, x, y, 3);
                                case '>':
                                    read();
                                    if (peek() != -1)
                                    {
                                        switch ((char)peek())
                                        {
                                            case '=':
                                                read();
                                                return new Token(TokenType.TripleRightShiftAssign, x, y, 4);
                                        }
                                        return new Token(TokenType.ShiftRightUnsigned, x, y, 3); // >>>
                                    }
                                    break;
                            }
                        }
                        return new Token(TokenType.ShiftRight, x, y, 2);
                    case '=':
                        read();
                        return new Token(TokenType.GreaterEqual, x, y, 2);
                }
                return new Token(TokenType.GreaterThan, x, y);
            case '?':
                return new Token(TokenType.Question, x, y);
            case '$':
                return new Token(TokenType.Dollar, x, y);
            case ';':
                return new Token(TokenType.Semicolon, x, y);
            case ':':
                return new Token(TokenType.Colon, x, y);
            case ',':
                return new Token(TokenType.Comma, x, y);
            case '.':
                // Prevent OverflowException when peek returns -1
                int tmp = peek();
                if (tmp > 0 && Character.isDigit(tmp))
                    return read_digit('.', col - 1);
                else if (tmp == (int)'.')
                {
                    read();
                    if ((char)peek() == '.') // Triple dot
                    {
                        read();
                        return new Token(TokenType.TripleDot, x, y, 3);
                    }
                    return new Token(TokenType.DoubleDot, x, y, 2);
                }
                return new Token(TokenType.Dot, x, y);
            case ')':
                return new Token(TokenType.CloseParenthesis, x, y);
            case '(':
                return new Token(TokenType.OpenParenthesis, x, y);
            case ']':
                return new Token(TokenType.CloseSquareBracket, x, y);
            case '[':
                return new Token(TokenType.OpenSquareBracket, x, y);
            case '}':
                return new Token(TokenType.CloseCurlyBrace, x, y);
            case '{':
                return new Token(TokenType.OpenCurlyBrace, x, y);
            default:
                return null;
        }
    }

    //TODO: proper error/exception handling on end-of-stream peeking
    private Token read_digit(char ch, int x) throws IOException
    {
        if (!Character.isDigit(ch) && ch != '.')
        {
            error(this.line, x, "Digit literals can only start with a digit (0-9) or a dot ('.')!");
            return null;
        }

        int y = this.line;

        // Build up value string
        StringBuilder sb = new StringBuilder();
        sb.append(ch);

        String prefix = null;
        String expSuffix = "";
        String suffix = null;
        int exponent = 0;

        boolean HasDot = false;
//            LiteralSubformat subFmt = 0;
        boolean isFloat = false;
        boolean isImaginary = false;
        boolean isUnsigned = false;
        boolean isLong = false;
        int NumBase = 0; // Set it to 0 initially - it'll be set to another value later for sure

        int peek = peek();
        if (peek == -1) {
            // end of stream!
            error(this.line, x, "Unexpected end of input stream when parsing numeric literal");
            return null;
        }

        char next = (char)peek;


        // At first, check pre-comma values
        if (ch == '0')
        {
            if (next == 'x' || next == 'X') // Hex values
            {
                prefix = "0x";
                read(); // skip 'x'
                sb.setLength(0); // Remove '0' from 0x prefix from the string value
                NumBase = 16;

                next = (char)peek();
                while (is_hex(next) || next == '_')
                {
                    if (next != '_') {
                        sb.append((char)read());
                    }
                    else {
                        read();
                    }
                    next = (char)peek();
                }
            }
            else if (next == 'b' || next == 'B') // Bin values
            {
                prefix = "0b";
                read(); // skip 'b'
                sb.setLength(0);
                NumBase = 2;

                next = (char)peek();
                while (is_bin(next) || next == '_')
                {
                    if (next != '_') {
                        sb.append((char)read());
                    }
                    else {
                        read();
                    }
                    next = (char)peek();
                }
            }
            else
                NumBase = 10; // Enables pre-comma parsing .. in this case we'd 000 literals or something like that
        }

        if (NumBase == 10 || (ch != '.' && NumBase == 0)) // Only allow further digits for 10-based integers, not for binary or hex values
        {
            NumBase = 10;
            while (Character.isDigit(next) || next == '_')
            {
                if (next != '_') {
                    sb.append((char)read());
                }
                else {
                    read();
                }
                next = (char)peek();
            }
        }

        // Read digits that occur after a comma
        Token nextToken = null; // if we accidently read a 'dot'
        boolean AllowSuffixes = true;
        if ((NumBase == 0 && ch == '.') || next == '.')
        {
            if (ch != '.') read();
            else
            {
                NumBase = 10;
                sb.setLength(0);
                sb.append('0');
            }
            next = (char)peek();
            if (!is_legal_digit(next, NumBase))
            {
                if (next == '.')
                {
                    read();
                    nextToken = Token(TokenType.DoubleDot, col - 1, line, 2);
                }
                else if (IsIdentifierPart(next))
                    nextToken = Token(DTokens.Dot, Col - 1, Line, 1);

                AllowSuffixes = false;
            }
            else
            {
                HasDot = true;
                sb.Append('.');

                do
                {
                    if (next == '_')
                        ReaderRead();
                    else
                        sb.Append((char)ReaderRead());
                    next = (char)ReaderPeek();
                }
                while (IsLegalDigit(next, NumBase));
            }
        }
        #endregion

        #region Exponents
        if ((NumBase == 16) ? (next == 'p' || next == 'P') : (next == 'e' || next == 'E'))
        { // read exponent
            String suff = next.ToString();
            ReaderRead();
            next = (char)ReaderPeek();

            if (next == '-' || next == '+')
                expSuffix += (char)ReaderRead();
            next = (char)ReaderPeek();
            while ((next >= '0' && next<='9') || next == '_')
            { // read exponent value
                if (next == '_')
                    ReaderRead();
                else
                    expSuffix += (char)ReaderRead();
                next = (char)ReaderPeek();
            }

            // Exponents just can be decimal integers
            int.TryParse(expSuffix,out exponent);
            expSuffix = suff + expSuffix;
            next = (char)ReaderPeek();
        }
        #endregion

        #region Suffixes
        if (!HasDot)
        {
            unsigned:
            if (next == 'u' || next == 'U')
            {
                ReaderRead();
                suffix += "u";
                subFmt |= LiteralSubformat.Unsigned;
                isUnsigned = true;
                next = (char)ReaderPeek();
            }

            if (next == 'L')
            {
                subFmt |= LiteralSubformat.Long;
                ReaderRead();
                suffix += "L";
                isLong = true;
                next = (char)ReaderPeek();
                if (!subFmt.HasFlag(LiteralSubformat.Unsigned) && (next == 'u' || next == 'U'))
                goto unsigned;
            }
        }

        if (HasDot || AllowSuffixes)
        {
            if (next == 'f' || next == 'F')
            { // float value
                ReaderRead();
                suffix += "f";
                isFloat = true;
                subFmt |= LiteralSubformat.Float;
                next = (char)ReaderPeek();
            }
            else if (next == 'L')
            { // real value
                ReaderRead();
                isLong = true;
                suffix += 'L';
                subFmt |= LiteralSubformat.Real;
                next = (char)ReaderPeek();
            }
        }

        if (next == 'i')
        { // imaginary value
            ReaderRead();
            suffix += "i";

            subFmt |= LiteralSubformat.Imaginary;
            isImaginary = true;
        }
        #endregion

        #region Parse the digit string
        var num = ParseFloatValue(sb.ToString(), NumBase);

        if (exponent != 0)
        {
            try{
                num *= (decimal)Math.Pow(NumBase == 16 ? 2 : 10, exponent);
            }
            catch(OverflowException ox)
            {
                num = decimal.MaxValue;
                //HACK: Don't register these exceptions. The user will notice the issues at least when compiling stuff.
                //LexerErrors.Add(new ParserError(false, "Too huge exponent", DTokens.Literal, new CodeLocation(x,y)));
            }
        }
        #endregion

        var token = Token(DTokens.Literal, x, y, Col-x/*stringValue.Length*/, num,/* stringValue,*/
                          HasDot || isFloat || isImaginary ? (LiteralFormat.FloatingPoint | LiteralFormat.Scalar) : LiteralFormat.Scalar,
                          subFmt);

        if (token != null)
            token.next = nextToken;

        return token;
    }

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

    private static boolean is_oct(char digit)
    {
        return Character.isDigit(digit) && digit != '9' && digit != '8';
    }

    private static boolean is_hex(char digit)
    {
        return (digit >= '0' && digit <= '9') || ('A' <= digit && digit <= 'F') || ('a' <= digit && digit <= 'f');
    }

    private static boolean is_bin(char digit)
    {
        return digit == '0' || digit == '1';
    }

    /// <summary>
    /// Tests if digit <para>d</para> is allowed in the specified numerical base.
    /// If <para>NumBase</para> is 10, only digits from 0 to 9 would be allowed.
    /// If NumBase=2, 0 and 1 are legal.
    /// If NumBase=8, 0 to 7 are legal.
    /// If NumBase=16, 0 to 9 and a to f are allowed.
    /// Note: Underscores ('_') are legal everytime!
    /// </summary>
    /// <param name="d"></param>
    /// <param name="NumBase"></param>
    /// <returns></returns>
    private static boolean is_legal_digit(char d, int NumBase)
    {
        return (NumBase == 10 && (d >= '0' && d<='9')) || (NumBase == 2 && is_bin(d)) || (NumBase == 16 && is_hex(d)) || d == '_';
    }


}
