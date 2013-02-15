package com.wyrdtech.parsed.lexer;

import java.io.IOException;

/**
 * Tokenize an operator off the stream, assuming next item is an operator.
 */
public class LexOperator {

    private LexOperator() {}

    public static Token read(LexerStream in_stream) throws IOException, LexerException {
        int line = in_stream.getLine();
        int col = in_stream.getCol();

        int next = in_stream.peek();
        if (next == -1) {
            // end of stream!
            throw new LexerException(in_stream.getLine(), in_stream.getCol(), "Unexpected end of input stream when parsing operator");
        }

        // If we have a '.' may be a literal, in which case don't want to consume here
        if (next == '.') {
            int tmp = in_stream.peek(2);
            if (tmp > 0 && Character.isDigit(tmp)) {
                return LexNumericLiteral.read(in_stream);
            }
        }

        // Everything else we consume the first char, check it and next
        char cur = (char)in_stream.read();
        next = in_stream.peek();

        switch (cur)
        {
            case '+':
                switch (next)
                {
                    case '+':
                        in_stream.read();
                        return new Token(TokenType.Increment, line, col, 2);
                    case '=':
                        in_stream.read();
                        return new Token(TokenType.PlusAssign, line, col, 2);
                }
                return new Token(TokenType.Plus, line, col);
            case '-':
                switch (next)
                {
                    case '-':
                        in_stream.read();
                        return new Token(TokenType.Decrement, line, col, 2);
                    case '=':
                        in_stream.read();
                        return new Token(TokenType.MinusAssign, line, col, 2);
                }
                return new Token(TokenType.Minus, line, col);
            case '*':
                switch (next)
                {
                    case '=':
                        in_stream.read();
                        return new Token(TokenType.TimesAssign, line, col, 2);
                    default:
                        break;
                }
                return new Token(TokenType.Times, line, col);
            case '/':
                switch (next)
                {
                    case '=':
                        in_stream.read();
                        return new Token(TokenType.DivAssign, line, col, 2);
                }
                return new Token(TokenType.Div, line, col);
            case '%':
                switch (next)
                {
                    case '=':
                        in_stream.read();
                        return new Token(TokenType.ModAssign, line, col, 2);
                }
                return new Token(TokenType.Mod, line, col);
            case '&':
                switch (next)
                {
                    case '&':
                        in_stream.read();
                        return new Token(TokenType.LogicalAnd, line, col, 2);
                    case '=':
                        in_stream.read();
                        return new Token(TokenType.BitwiseAndAssign, line, col, 2);
                }
                return new Token(TokenType.BitwiseAnd, line, col);
            case '|':
                switch (next)
                {
                    case '|':
                        in_stream.read();
                        return new Token(TokenType.LogicalOr, line, col, 2);
                    case '=':
                        in_stream.read();
                        return new Token(TokenType.BitwiseOrAssign, line, col, 2);
                }
                return new Token(TokenType.BitwiseOr, line, col);
            case '^':
                switch (next)
                {
                    case '=':
                        in_stream.read();
                        return new Token(TokenType.XorAssign, line, col, 2);
                    case '^':
                        in_stream.read();
                        if (in_stream.peek() == '=')
                        {
                            in_stream.read();
                            return new Token(TokenType.PowAssign, line, col, 3);
                        }
                        return new Token(TokenType.Pow, line, col, 2);
                }
                return new Token(TokenType.Xor, line, col);
            case '!':
                switch (next)
                {
                    case '=':
                        in_stream.read();
                        return new Token(TokenType.NotEqual, line, col, 2); // !=

                    case '<':
                        in_stream.read();
                        switch (in_stream.peek())
                        {
                            case '=':
                                in_stream.read();
                                return new Token(TokenType.UnorderedOrGreater, line, col, 3); // !<=
                            case '>':
                                in_stream.read();
                                switch (in_stream.peek())
                                {
                                    case '=':
                                        in_stream.read();
                                        return new Token(TokenType.Unordered, line, col, 4); // !<>=
                                }
                                return new Token(TokenType.UnorderedOrEqual, line, col, 3); // !<>
                        }
                        return new Token(TokenType.UnorderedGreaterOrEqual, line, col, 2); // !<

                    case '>':
                        in_stream.read();
                        switch (in_stream.peek())
                        {
                            case '=':
                                in_stream.read();
                                return new Token(TokenType.UnorderedOrLess, line, col, 3); // !>=
                            default:
                                break;
                        }
                        return new Token(TokenType.UnorderedLessOrEqual, line, col, 2); // !>

                }
                return new Token(TokenType.Not, line, col);
            case '~':
                switch (next)
                {
                    case '=':
                        in_stream.read();
                        return new Token(TokenType.TildeAssign, line, col, 2);
                }
                return new Token(TokenType.Tilde, line, col);
            case '=':
                switch (next)
                {
                    case '=':
                        in_stream.read();
                        return new Token(TokenType.Equal, line, col, 2);
                    case '>':
                        in_stream.read();
                        return new Token(TokenType.GoesTo, line, col, 2);
                }
                return new Token(TokenType.Assign, line, col);
            case '<':
                switch (next)
                {
                    case '<':
                        in_stream.read();
                        switch (in_stream.peek())
                        {
                            case '=':
                                in_stream.read();
                                return new Token(TokenType.ShiftLeftAssign, line, col, 3);
                            default:
                                break;
                        }
                        return new Token(TokenType.ShiftLeft, line, col, 2);
                    case '>':
                        in_stream.read();
                        switch (in_stream.peek())
                        {
                            case '=':
                                in_stream.read();
                                return new Token(TokenType.LessEqualOrGreater, line, col, 3);
                            default:
                                break;
                        }
                        return new Token(TokenType.LessOrGreater, line, col, 2);
                    case '=':
                        in_stream.read();
                        return new Token(TokenType.LessEqual, line, col, 2);
                }
                return new Token(TokenType.LessThan, line, col);
            case '>':
                switch (next)
                {
                    case '>':
                        in_stream.read();
                        int p = in_stream.peek();
                        if (p != -1)
                        {
                            switch ((char)p)
                            {
                                case '=':
                                    in_stream.read();
                                    return new Token(TokenType.ShiftRightAssign, line, col, 3);
                                case '>':
                                    in_stream.read();
                                    int q = in_stream.peek();
                                    if (q != -1 && q == '=')
                                    {
                                        in_stream.read();
                                        return new Token(TokenType.TripleRightShiftAssign, line, col, 4);
                                    }
                                    return new Token(TokenType.ShiftRightUnsigned, line, col, 3); // >>>
                            }
                        }
                        return new Token(TokenType.ShiftRight, line, col, 2);
                    case '=':
                        in_stream.read();
                        return new Token(TokenType.GreaterEqual, line, col, 2);
                }
                return new Token(TokenType.GreaterThan, line, col);
            case '?':
                return new Token(TokenType.Question, line, col);
            case '$':
                return new Token(TokenType.Dollar, line, col);
            case ';':
                return new Token(TokenType.Semicolon, line, col);
            case ':':
                return new Token(TokenType.Colon, line, col);
            case ',':
                return new Token(TokenType.Comma, line, col);
            case '.':
                if (next == '.')
                {
                    in_stream.read();
                    int p = in_stream.peek();
                    if (p != -1 && p == '.') {
                        in_stream.read();
                        return new Token(TokenType.TripleDot, line, col, 3);
                    }
                    return new Token(TokenType.DoubleDot, line, col, 2);
                }
                return new Token(TokenType.Dot, line, col);
            case ')':
                return new Token(TokenType.CloseParenthesis, line, col);
            case '(':
                return new Token(TokenType.OpenParenthesis, line, col);
            case ']':
                return new Token(TokenType.CloseSquareBracket, line, col);
            case '[':
                return new Token(TokenType.OpenSquareBracket, line, col);
            case '}':
                return new Token(TokenType.CloseCurlyBrace, line, col);
            case '{':
                return new Token(TokenType.OpenCurlyBrace, line, col);
            case '#':
                return new Token(TokenType.Hash, line, col);
            default:
                return null;
        }
    }

}
