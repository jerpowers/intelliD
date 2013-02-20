package com.wyrdtech.parsed.lexer;

import com.wyrdtech.parsed.lexer.token.Token;
import com.wyrdtech.parsed.lexer.token.TokenFactory;
import com.wyrdtech.parsed.lexer.token.TokenType;

import java.io.IOException;

/**
 * Tokenize an operator off the stream, assuming next item is an operator.
 */
public class LexOperator {

    private final TokenFactory factory;
    private final LexerStream in_stream;

    public LexOperator(TokenFactory factory, LexerStream in_stream) {
        this.factory = factory;
        this.in_stream = in_stream;
    }

    public Token read() throws IOException, LexerException
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
                throw new LexerException(in_stream.getLine(), in_stream.getCol(), "Literal float encountered when operator '.' expected");
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
                        return factory.create(TokenType.Increment, index, line, col, 2);
                    case '=':
                        in_stream.read();
                        return factory.create(TokenType.PlusAssign, index, line, col, 2);
                }
                return factory.create(TokenType.Plus, index, line, col);
            case '-':
                switch (next)
                {
                    case '-':
                        in_stream.read();
                        return factory.create(TokenType.Decrement, index, line, col, 2);
                    case '=':
                        in_stream.read();
                        return factory.create(TokenType.MinusAssign, index, line, col, 2);
                }
                return factory.create(TokenType.Minus, index, line, col);
            case '*':
                switch (next)
                {
                    case '=':
                        in_stream.read();
                        return factory.create(TokenType.TimesAssign, index, line, col, 2);
                    default:
                        break;
                }
                return factory.create(TokenType.Times, index, line, col);
            case '/':
                switch (next)
                {
                    case '=':
                        in_stream.read();
                        return factory.create(TokenType.DivAssign, index, line, col, 2);
                }
                return factory.create(TokenType.Div, index, line, col);
            case '%':
                switch (next)
                {
                    case '=':
                        in_stream.read();
                        return factory.create(TokenType.ModAssign, index, line, col, 2);
                }
                return factory.create(TokenType.Mod, index, line, col);
            case '&':
                switch (next)
                {
                    case '&':
                        in_stream.read();
                        return factory.create(TokenType.LogicalAnd, index, line, col, 2);
                    case '=':
                        in_stream.read();
                        return factory.create(TokenType.BitwiseAndAssign, index, line, col, 2);
                }
                return factory.create(TokenType.BitwiseAnd, index, line, col);
            case '|':
                switch (next)
                {
                    case '|':
                        in_stream.read();
                        return factory.create(TokenType.LogicalOr, index, line, col, 2);
                    case '=':
                        in_stream.read();
                        return factory.create(TokenType.BitwiseOrAssign, index, line, col, 2);
                }
                return factory.create(TokenType.BitwiseOr, index, line, col);
            case '^':
                switch (next)
                {
                    case '=':
                        in_stream.read();
                        return factory.create(TokenType.XorAssign, index, line, col, 2);
                    case '^':
                        in_stream.read();
                        if (in_stream.peek() == '=')
                        {
                            in_stream.read();
                            return factory.create(TokenType.PowAssign, index, line, col, 3);
                        }
                        return factory.create(TokenType.Pow, index, line, col, 2);
                }
                return factory.create(TokenType.Xor, index, line, col);
            case '!':
                switch (next)
                {
                    case '=':
                        in_stream.read();
                        return factory.create(TokenType.NotEqual, index, line, col, 2); // !=

                    case '<':
                        in_stream.read();
                        switch (in_stream.peek())
                        {
                            case '=':
                                in_stream.read();
                                return factory.create(TokenType.UnorderedOrGreater, index, line, col, 3); // !<=
                            case '>':
                                in_stream.read();
                                switch (in_stream.peek())
                                {
                                    case '=':
                                        in_stream.read();
                                        return factory.create(TokenType.Unordered, index, line, col, 4); // !<>=
                                }
                                return factory.create(TokenType.UnorderedOrEqual, index, line, col, 3); // !<>
                        }
                        return factory.create(TokenType.UnorderedGreaterOrEqual, index, line, col, 2); // !<

                    case '>':
                        in_stream.read();
                        switch (in_stream.peek())
                        {
                            case '=':
                                in_stream.read();
                                return factory.create(TokenType.UnorderedOrLess, index, line, col, 3); // !>=
                            default:
                                break;
                        }
                        return factory.create(TokenType.UnorderedLessOrEqual, index, line, col, 2); // !>

                }
                return factory.create(TokenType.Not, index, line, col);
            case '~':
                switch (next)
                {
                    case '=':
                        in_stream.read();
                        return factory.create(TokenType.TildeAssign, index, line, col, 2);
                }
                return factory.create(TokenType.Tilde, index, line, col);
            case '=':
                switch (next)
                {
                    case '=':
                        in_stream.read();
                        return factory.create(TokenType.Equal, index, line, col, 2);
                    case '>':
                        in_stream.read();
                        return factory.create(TokenType.GoesTo, index, line, col, 2);
                }
                return factory.create(TokenType.Assign, index, line, col);
            case '<':
                switch (next)
                {
                    case '<':
                        in_stream.read();
                        switch (in_stream.peek())
                        {
                            case '=':
                                in_stream.read();
                                return factory.create(TokenType.ShiftLeftAssign, index, line, col, 3);
                            default:
                                break;
                        }
                        return factory.create(TokenType.ShiftLeft, index, line, col, 2);
                    case '>':
                        in_stream.read();
                        switch (in_stream.peek())
                        {
                            case '=':
                                in_stream.read();
                                return factory.create(TokenType.LessEqualOrGreater, index, line, col, 3);
                            default:
                                break;
                        }
                        return factory.create(TokenType.LessOrGreater, index, line, col, 2);
                    case '=':
                        in_stream.read();
                        return factory.create(TokenType.LessEqual, index, line, col, 2);
                }
                return factory.create(TokenType.LessThan, index, line, col);
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
                                    return factory.create(TokenType.ShiftRightAssign, index, line, col, 3);
                                case '>':
                                    in_stream.read();
                                    int q = in_stream.peek();
                                    if (q != -1 && q == '=')
                                    {
                                        in_stream.read();
                                        return factory.create(TokenType.TripleRightShiftAssign, index, line, col, 4);
                                    }
                                    return factory.create(TokenType.ShiftRightUnsigned, index, line, col, 3); // >>>
                            }
                        }
                        return factory.create(TokenType.ShiftRight, index, line, col, 2);
                    case '=':
                        in_stream.read();
                        return factory.create(TokenType.GreaterEqual, index, line, col, 2);
                }
                return factory.create(TokenType.GreaterThan, index, line, col);
            case '?':
                return factory.create(TokenType.Question, index, line, col);
            case '$':
                return factory.create(TokenType.Dollar, index, line, col);
            case ';':
                return factory.create(TokenType.Semicolon, index, line, col);
            case ':':
                return factory.create(TokenType.Colon, index, line, col);
            case ',':
                return factory.create(TokenType.Comma, index, line, col);
            case '.':
                if (next == '.')
                {
                    in_stream.read();
                    int p = in_stream.peek();
                    if (p != -1 && p == '.') {
                        in_stream.read();
                        return factory.create(TokenType.TripleDot, index, line, col, 3);
                    }
                    return factory.create(TokenType.DoubleDot, index, line, col, 2);
                }
                return factory.create(TokenType.Dot, index, line, col);
            case ')':
                return factory.create(TokenType.CloseParenthesis, index, line, col);
            case '(':
                return factory.create(TokenType.OpenParenthesis, index, line, col);
            case ']':
                return factory.create(TokenType.CloseSquareBracket, index, line, col);
            case '[':
                return factory.create(TokenType.OpenSquareBracket, index, line, col);
            case '}':
                return factory.create(TokenType.CloseCurlyBrace, index, line, col);
            case '{':
                return factory.create(TokenType.OpenCurlyBrace, index, line, col);
            case '#':
                return factory.create(TokenType.Hash, index, line, col);
            default:
                return null;
        }
    }

}
