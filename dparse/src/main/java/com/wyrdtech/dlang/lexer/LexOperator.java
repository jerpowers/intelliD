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
                        return new Token(TokenType.Increment, y, x, 2);
                    case '=':
                        in_stream.read();
                        return new Token(TokenType.PlusAssign, y, x, 2);
                }
                return new Token(TokenType.Plus, y, x);
            case '-':
                switch (next)
                {
                    case '-':
                        in_stream.read();
                        return new Token(TokenType.Decrement, y, x, 2);
                    case '=':
                        in_stream.read();
                        return new Token(TokenType.MinusAssign, y, x, 2);
/*
                    case '>':
                        in_stream.read();
                        return new Token(TokenType.TildeAssign, x, y, 2);
*/
                }
                return new Token(TokenType.Minus, y, x);
            case '*':
                switch (next)
                {
                    case '=':
                        in_stream.read();
                        return new Token(TokenType.TimesAssign, y, x, 2);
                    default:
                        break;
                }
                return new Token(TokenType.Times, y, x);
            case '/':
                switch (next)
                {
                    case '=':
                        in_stream.read();
                        return new Token(TokenType.DivAssign, y, x, 2);
                }
                return new Token(TokenType.Div, y, x);
            case '%':
                switch (next)
                {
                    case '=':
                        in_stream.read();
                        return new Token(TokenType.ModAssign, y, x, 2);
                }
                return new Token(TokenType.Mod, y, x);
            case '&':
                switch (next)
                {
                    case '&':
                        in_stream.read();
                        return new Token(TokenType.LogicalAnd, y, x, 2);
                    case '=':
                        in_stream.read();
                        return new Token(TokenType.BitwiseAndAssign, y, x, 2);
                }
                return new Token(TokenType.BitwiseAnd, y, x);
            case '|':
                switch (next)
                {
                    case '|':
                        in_stream.read();
                        return new Token(TokenType.LogicalOr, y, x, 2);
                    case '=':
                        in_stream.read();
                        return new Token(TokenType.BitwiseOrAssign, y, x, 2);
                }
                return new Token(TokenType.BitwiseOr, y, x);
            case '^':
                switch (next)
                {
                    case '=':
                        in_stream.read();
                        return new Token(TokenType.XorAssign, y, x, 2);
                    case '^':
                        in_stream.read();
                        if (in_stream.peek() == '=')
                        {
                            in_stream.read();
                            return new Token(TokenType.PowAssign, y, x, 3);
                        }
                        return new Token(TokenType.Pow, y, x, 2);
                }
                return new Token(TokenType.Xor, y, x);
            case '!':
                switch (next)
                {
                    case '=':
                        in_stream.read();
                        return new Token(TokenType.NotEqual, y, x, 2); // !=

                    case '<':
                        in_stream.read();
                        switch (in_stream.peek())
                        {
                            case '=':
                                in_stream.read();
                                return new Token(TokenType.UnorderedOrGreater,
                                                 y,
                                                 x,
                                                 3); // !<=
                            case '>':
                                in_stream.read();
                                switch (in_stream.peek())
                                {
                                    case '=':
                                        in_stream.read();
                                        return new Token(TokenType.Unordered,
                                                         y,
                                                         x,
                                                         4); // !<>=
                                }
                                return new Token(TokenType.UnorderedOrEqual,
                                                 y,
                                                 x,
                                                 3); // !<>
                        }
                        return new Token(TokenType.UnorderedGreaterOrEqual,
                                         y,
                                         x,
                                         2); // !<

                    case '>':
                        in_stream.read();
                        switch (in_stream.peek())
                        {
                            case '=':
                                in_stream.read();
                                return new Token(TokenType.UnorderedOrLess,
                                                 y,
                                                 x,
                                                 3); // !>=
                            default:
                                break;
                        }
                        return new Token(TokenType.UnorderedLessOrEqual, y, x, 2); // !>

                }
                return new Token(TokenType.Not, y, x);
            case '~':
                switch (next)
                {
                    case '=':
                        in_stream.read();
                        return new Token(TokenType.TildeAssign, y, x, 2);
                }
                return new Token(TokenType.Tilde, y, x);
            case '=':
                switch (next)
                {
                    case '=':
                        in_stream.read();
                        return new Token(TokenType.Equal, y, x, 2);
                    case '>':
                        in_stream.read();
                        return new Token(TokenType.GoesTo, y, x, 2);
                }
                return new Token(TokenType.Assign, y, x);
            case '<':
                switch (next)
                {
                    case '<':
                        in_stream.read();
                        switch (in_stream.peek())
                        {
                            case '=':
                                in_stream.read();
                                return new Token(TokenType.ShiftLeftAssign,
                                                 y,
                                                 x,
                                                 3);
                            default:
                                break;
                        }
                        return new Token(TokenType.ShiftLeft, y, x, 2);
                    case '>':
                        in_stream.read();
                        switch (in_stream.peek())
                        {
                            case '=':
                                in_stream.read();
                                return new Token(TokenType.LessEqualOrGreater,
                                                 y,
                                                 x,
                                                 3);
                            default:
                                break;
                        }
                        return new Token(TokenType.LessOrGreater, y, x, 2);
                    case '=':
                        in_stream.read();
                        return new Token(TokenType.LessEqual, y, x, 2);
                }
                return new Token(TokenType.LessThan, y, x);
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
                                    return new Token(TokenType.ShiftRightAssign,
                                                     y,
                                                     x,
                                                     3);
                                case '>':
                                    in_stream.read();
                                    int q = in_stream.peek();
                                    if (q != -1 && q == '=')
                                    {
                                                in_stream.read();
                                                return new Token(TokenType.TripleRightShiftAssign,
                                                                 y,
                                                                 x,
                                                                 4);
                                    }
                                    return new Token(TokenType.ShiftRightUnsigned,
                                                     y,
                                                     x,
                                                     3); // >>>
                            }
                        }
                        return new Token(TokenType.ShiftRight, y, x, 2);
                    case '=':
                        in_stream.read();
                        return new Token(TokenType.GreaterEqual, y, x, 2);
                }
                return new Token(TokenType.GreaterThan, y, x);
            case '?':
                return new Token(TokenType.Question, y, x);
            case '$':
                return new Token(TokenType.Dollar, y, x);
            case ';':
                return new Token(TokenType.Semicolon, y, x);
            case ':':
                return new Token(TokenType.Colon, y, x);
            case ',':
                return new Token(TokenType.Comma, y, x);
            case '.':
                if (next == '.')
                {
                    in_stream.read();
                    int p = in_stream.peek();
                    if (p != -1 && p == '.') {
                        in_stream.read();
                        return new Token(TokenType.TripleDot, y, x, 3);
                    }
                    return new Token(TokenType.DoubleDot, y, x, 2);
                }
                return new Token(TokenType.Dot, y, x);
            case ')':
                return new Token(TokenType.CloseParenthesis, y, x);
            case '(':
                return new Token(TokenType.OpenParenthesis, y, x);
            case ']':
                return new Token(TokenType.CloseSquareBracket, y, x);
            case '[':
                return new Token(TokenType.OpenSquareBracket, y, x);
            case '}':
                return new Token(TokenType.CloseCurlyBrace, y, x);
            case '{':
                return new Token(TokenType.OpenCurlyBrace, y, x);
            default:
                return null;
        }
    }

}
