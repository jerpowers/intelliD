package com.wyrdtech.parsed.lexer;

import java.io.IOException;

/**
 * Tokenize an operator off the stream, assuming next item is an operator.
 */
public class LexOperator {

    private LexOperator() {}

    public static Token read(LexerStream in_stream) throws IOException, LexerException
    {
        int index = in_stream.getPosition();
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
                        return new Token(TokenType.Increment, index, line, col, 2);
                    case '=':
                        in_stream.read();
                        return new Token(TokenType.PlusAssign, index, line, col, 2);
                }
                return new Token(TokenType.Plus, index, line, col);
            case '-':
                switch (next)
                {
                    case '-':
                        in_stream.read();
                        return new Token(TokenType.Decrement, index, line, col, 2);
                    case '=':
                        in_stream.read();
                        return new Token(TokenType.MinusAssign, index, line, col, 2);
                }
                return new Token(TokenType.Minus, index, line, col);
            case '*':
                switch (next)
                {
                    case '=':
                        in_stream.read();
                        return new Token(TokenType.TimesAssign, index, line, col, 2);
                    default:
                        break;
                }
                return new Token(TokenType.Times, index, line, col);
            case '/':
                switch (next)
                {
                    case '=':
                        in_stream.read();
                        return new Token(TokenType.DivAssign, index, line, col, 2);
                }
                return new Token(TokenType.Div, index, line, col);
            case '%':
                switch (next)
                {
                    case '=':
                        in_stream.read();
                        return new Token(TokenType.ModAssign, index, line, col, 2);
                }
                return new Token(TokenType.Mod, index, line, col);
            case '&':
                switch (next)
                {
                    case '&':
                        in_stream.read();
                        return new Token(TokenType.LogicalAnd, index, line, col, 2);
                    case '=':
                        in_stream.read();
                        return new Token(TokenType.BitwiseAndAssign, index, line, col, 2);
                }
                return new Token(TokenType.BitwiseAnd, index, line, col);
            case '|':
                switch (next)
                {
                    case '|':
                        in_stream.read();
                        return new Token(TokenType.LogicalOr, index, line, col, 2);
                    case '=':
                        in_stream.read();
                        return new Token(TokenType.BitwiseOrAssign, index, line, col, 2);
                }
                return new Token(TokenType.BitwiseOr, index, line, col);
            case '^':
                switch (next)
                {
                    case '=':
                        in_stream.read();
                        return new Token(TokenType.XorAssign, index, line, col, 2);
                    case '^':
                        in_stream.read();
                        if (in_stream.peek() == '=')
                        {
                            in_stream.read();
                            return new Token(TokenType.PowAssign, index, line, col, 3);
                        }
                        return new Token(TokenType.Pow, index, line, col, 2);
                }
                return new Token(TokenType.Xor, index, line, col);
            case '!':
                switch (next)
                {
                    case '=':
                        in_stream.read();
                        return new Token(TokenType.NotEqual, index, line, col, 2); // !=

                    case '<':
                        in_stream.read();
                        switch (in_stream.peek())
                        {
                            case '=':
                                in_stream.read();
                                return new Token(TokenType.UnorderedOrGreater, index, line, col, 3); // !<=
                            case '>':
                                in_stream.read();
                                switch (in_stream.peek())
                                {
                                    case '=':
                                        in_stream.read();
                                        return new Token(TokenType.Unordered, index, line, col, 4); // !<>=
                                }
                                return new Token(TokenType.UnorderedOrEqual, index, line, col, 3); // !<>
                        }
                        return new Token(TokenType.UnorderedGreaterOrEqual, index, line, col, 2); // !<

                    case '>':
                        in_stream.read();
                        switch (in_stream.peek())
                        {
                            case '=':
                                in_stream.read();
                                return new Token(TokenType.UnorderedOrLess, index, line, col, 3); // !>=
                            default:
                                break;
                        }
                        return new Token(TokenType.UnorderedLessOrEqual, index, line, col, 2); // !>

                }
                return new Token(TokenType.Not, index, line, col);
            case '~':
                switch (next)
                {
                    case '=':
                        in_stream.read();
                        return new Token(TokenType.TildeAssign, index, line, col, 2);
                }
                return new Token(TokenType.Tilde, index, line, col);
            case '=':
                switch (next)
                {
                    case '=':
                        in_stream.read();
                        return new Token(TokenType.Equal, index, line, col, 2);
                    case '>':
                        in_stream.read();
                        return new Token(TokenType.GoesTo, index, line, col, 2);
                }
                return new Token(TokenType.Assign, index, line, col);
            case '<':
                switch (next)
                {
                    case '<':
                        in_stream.read();
                        switch (in_stream.peek())
                        {
                            case '=':
                                in_stream.read();
                                return new Token(TokenType.ShiftLeftAssign, index, line, col, 3);
                            default:
                                break;
                        }
                        return new Token(TokenType.ShiftLeft, index, line, col, 2);
                    case '>':
                        in_stream.read();
                        switch (in_stream.peek())
                        {
                            case '=':
                                in_stream.read();
                                return new Token(TokenType.LessEqualOrGreater, index, line, col, 3);
                            default:
                                break;
                        }
                        return new Token(TokenType.LessOrGreater, index, line, col, 2);
                    case '=':
                        in_stream.read();
                        return new Token(TokenType.LessEqual, index, line, col, 2);
                }
                return new Token(TokenType.LessThan, index, line, col);
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
                                    return new Token(TokenType.ShiftRightAssign, index, line, col, 3);
                                case '>':
                                    in_stream.read();
                                    int q = in_stream.peek();
                                    if (q != -1 && q == '=')
                                    {
                                        in_stream.read();
                                        return new Token(TokenType.TripleRightShiftAssign, index, line, col, 4);
                                    }
                                    return new Token(TokenType.ShiftRightUnsigned, index, line, col, 3); // >>>
                            }
                        }
                        return new Token(TokenType.ShiftRight, index, line, col, 2);
                    case '=':
                        in_stream.read();
                        return new Token(TokenType.GreaterEqual, index, line, col, 2);
                }
                return new Token(TokenType.GreaterThan, index, line, col);
            case '?':
                return new Token(TokenType.Question, index, line, col);
            case '$':
                return new Token(TokenType.Dollar, index, line, col);
            case ';':
                return new Token(TokenType.Semicolon, index, line, col);
            case ':':
                return new Token(TokenType.Colon, index, line, col);
            case ',':
                return new Token(TokenType.Comma, index, line, col);
            case '.':
                if (next == '.')
                {
                    in_stream.read();
                    int p = in_stream.peek();
                    if (p != -1 && p == '.') {
                        in_stream.read();
                        return new Token(TokenType.TripleDot, index, line, col, 3);
                    }
                    return new Token(TokenType.DoubleDot, index, line, col, 2);
                }
                return new Token(TokenType.Dot, index, line, col);
            case ')':
                return new Token(TokenType.CloseParenthesis, index, line, col);
            case '(':
                return new Token(TokenType.OpenParenthesis, index, line, col);
            case ']':
                return new Token(TokenType.CloseSquareBracket, index, line, col);
            case '[':
                return new Token(TokenType.OpenSquareBracket, index, line, col);
            case '}':
                return new Token(TokenType.CloseCurlyBrace, index, line, col);
            case '{':
                return new Token(TokenType.OpenCurlyBrace, index, line, col);
            case '#':
                return new Token(TokenType.Hash, index, line, col);
            default:
                return null;
        }
    }

}
