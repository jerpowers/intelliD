package com.wyrdtech.parsed.lexer.token;

/**
 * Basic/standard implementation of TokenFactory.
 * Generates tokens of type BaseToken, using basic TokenType.
 */
public class BaseTokenFactory implements TokenFactory {

    @Override
    public Token create(final TokenType type,
                            final int index,
                            final int line,
                            final int col)
    {
        return create(type,
                             index,
                             line,
                             col,
                             index+1,
                             line,
                             col+1,
                             type.value);
    }

    @Override
    public Token create(final TokenType type,
                            final int index,
                            final int line,
                            final int col,
                            final int length)
    {
        return create(type,
                      index,
                      line,
                      col,
                      index+length,
                      line,
                      col+length,
                      type.value);

    }

    @Override
    public Token create(final TokenType type,
                            final int index,
                            final int line,
                            final int col,
                            final int length,
                            final Object value)
    {
        return create(type,
                      index,
                      line,
                      col,
                      index+length,
                      line,
                      col+length,
                      value);
    }

    @Override
    public Token create(final TokenType type,
                            final int index,
                            final int line,
                            final int col,
                            final int end_index,
                            final int end_line,
                            final int end_col,
                            final Object value)
    {
        return new BaseToken(type,
                             index,
                             line,
                             col,
                             end_index,
                             end_line,
                             end_col,
                             value);
    }
}
