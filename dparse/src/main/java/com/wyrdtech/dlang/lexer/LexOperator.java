package com.wyrdtech.dlang.lexer;

import java.io.IOException;

/**
 *
 */
public class LexOperator {

    private LexOperator() {}

    public static Token read(LexerStream in_stream) throws IOException, LexerException {
        int start_line = in_stream.getLine();
        int start_col = in_stream.getCol();

        int x = start_col;
        int y = start_line;

        int c = in_stream.read();
        if (c == -1) {
            // end of stream!
            throw new LexerException(in_stream.getLine(), in_stream.getCol(), "Unexpected end of input stream when parsing operator");
        }

        int n = in_stream.peek();
        if (n == -1) {
            // end of stream!
            throw new LexerException(in_stream.getLine(), in_stream.getCol(), "Unexpected end of input stream when parsing operator");
        }

        char cur = (char)c;

        // Parsing decisions are made on the not-yet-consumed next value, to
        // avoid consuming character after end of literal
//        char next = (char)n;
        int next = n;

        switch (cur)
        {
            case '+':
                switch (next)
                {
                    case '+':
                        in_stream.read();
                        return new Token(TokenType.Increment, x, y);
                    case '=':
                        in_stream.read();
                        return new Token(TokenType.PlusAssign, x, y);
                }
                return new Token(TokenType.Plus, x, y);
            case '-':
                switch (next)
                {
                    case '-':
                        in_stream.read();
                        return new Token(TokenType.Decrement, x, y);
                    case '=':
                        in_stream.read();
                        return new Token(TokenType.MinusAssign, x, y);
                    case '>':
                        in_stream.read();
                        return new Token(TokenType.TildeAssign, x, y);
                }
                return new Token(TokenType.Minus, x, y);
            case '*':
                switch (next)
                {
                    case '=':
                        in_stream.read();
                        return new Token(TokenType.TimesAssign, x, y, 2);
                    default:
                        break;
                }
                return new Token(TokenType.Times, x, y);
            case '/':
                switch (next)
                {
                    case '=':
                        in_stream.read();
                        return new Token(TokenType.DivAssign, x, y, 2);
                }
                return new Token(TokenType.Div, x, y);
            case '%':
                switch (next)
                {
                    case '=':
                        in_stream.read();
                        return new Token(TokenType.ModAssign, x, y, 2);
                }
                return new Token(TokenType.Mod, x, y);
            case '&':
                switch (next)
                {
                    case '&':
                        in_stream.read();
                        return new Token(TokenType.LogicalAnd, x, y, 2);
                    case '=':
                        in_stream.read();
                        return new Token(TokenType.BitwiseAndAssign, x, y, 2);
                }
                return new Token(TokenType.BitwiseAnd, x, y);
            case '|':
                switch (next)
                {
                    case '|':
                        in_stream.read();
                        return new Token(TokenType.LogicalOr, x, y, 2);
                    case '=':
                        in_stream.read();
                        return new Token(TokenType.BitwiseOrAssign, x, y, 2);
                }
                return new Token(TokenType.BitwiseOr, x, y);
            case '^':
                switch (next)
                {
                    case '=':
                        in_stream.read();
                        return new Token(TokenType.XorAssign, x, y, 2);
                    case '^':
                        in_stream.read();
                        if (in_stream.peek() == '=')
                        {
                            in_stream.read();
                            return new Token(TokenType.PowAssign, x, y, 3);
                        }
                        return new Token(TokenType.Pow, x, y, 2);
                }
                return new Token(TokenType.Xor, x, y);
            case '!':
                switch (next)
                {
                    case '=':
                        in_stream.read();
                        return new Token(TokenType.NotEqual, x, y, 2); // !=

                    case '<':
                        in_stream.read();
                        switch (in_stream.peek())
                        {
                            case '=':
                                in_stream.read();
                                return new Token(TokenType.UnorderedOrGreater, x, y, 3); // !<=
                            case '>':
                                in_stream.read();
                                switch (in_stream.peek())
                                {
                                    case '=':
                                        in_stream.read();
                                        return new Token(TokenType.Unordered, x, y, 4); // !<>=
                                }
                                return new Token(TokenType.UnorderedOrEqual, x, y, 3); // !<>
                        }
                        return new Token(TokenType.UnorderedGreaterOrEqual, x, y, 2); // !<

                    case '>':
                        in_stream.read();
                        switch (in_stream.peek())
                        {
                            case '=':
                                in_stream.read();
                                return new Token(TokenType.UnorderedOrLess, x, y, 3); // !>=
                            default:
                                break;
                        }
                        return new Token(TokenType.UnorderedLessOrEqual, x, y, 2); // !>

                }
                return new Token(TokenType.Not, x, y);
            case '~':
                switch (next)
                {
                    case '=':
                        in_stream.read();
                        return new Token(TokenType.TildeAssign, x, y, 2);
                }
                return new Token(TokenType.Tilde, x, y);
            case '=':
                switch (next)
                {
                    case '=':
                        in_stream.read();
                        return new Token(TokenType.Equal, x, y, 2);
                    case '>':
                        in_stream.read();
                        return new Token(TokenType.GoesTo, x, y, 2);
                }
                return new Token(TokenType.Assign, x, y);
            case '<':
                switch (next)
                {
                    case '<':
                        in_stream.read();
                        switch (in_stream.peek())
                        {
                            case '=':
                                in_stream.read();
                                return new Token(TokenType.ShiftLeftAssign, x, y, 3);
                            default:
                                break;
                        }
                        return new Token(TokenType.ShiftLeft, x, y, 2);
                    case '>':
                        in_stream.read();
                        switch (in_stream.peek())
                        {
                            case '=':
                                in_stream.read();
                                return new Token(TokenType.LessEqualOrGreater, x, y, 3);
                            default:
                                break;
                        }
                        return new Token(TokenType.LessOrGreater, x, y, 2);
                    case '=':
                        in_stream.read();
                        return new Token(TokenType.LessEqual, x, y, 2);
                }
                return new Token(TokenType.LessThan, x, y);
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
                                    return new Token(TokenType.ShiftRightAssign, x, y, 3);
                                case '>':
                                    in_stream.read();
                                    int q = in_stream.peek();
                                    if (q != -1)
                                    {
                                        switch ((char)q)
                                        {
                                            case '=':
                                                in_stream.read();
                                                return new Token(TokenType.TripleRightShiftAssign, x, y, 4);
                                        }
                                        return new Token(TokenType.ShiftRightUnsigned, x, y, 3); // >>>
                                    }
                                    break;
                            }
                        }
                        return new Token(TokenType.ShiftRight, x, y, 2);
                    case '=':
                        in_stream.read();
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
                int tmp = in_stream.peek();
                if (tmp > 0 && Character.isDigit(tmp))
                    return LexNumericLiteral.read(in_stream); //('.', col - 1);
                else if (tmp == (int)'.')
                {
                    in_stream.read();
                    if ((char)in_stream.peek() == '.') // Triple dot
                    {
                        in_stream.read();
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

}
