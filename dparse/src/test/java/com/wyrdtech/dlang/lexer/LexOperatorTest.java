package com.wyrdtech.dlang.lexer;

import org.junit.Test;

import java.io.StringReader;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;

/**
 *
 */
public class LexOperatorTest {

    @Test
    public void equality() throws Exception {
        String str = "a==b != c";

        LexerStream ls = new LexerStream(new StringReader(str));

        ls.read();

        Token tok = LexOperator.read(ls);

        assertEquals(TokenType.Equal, tok.type);
        assertNull(tok.literalValue);
        assertEquals(1, tok.line);
        assertEquals(2, tok.col);
        assertEquals(1, tok.end_line);
        assertEquals(4, tok.end_col);

        ls.read();
        ls.read();

        tok = LexOperator.read(ls);

        assertEquals(TokenType.NotEqual, tok.type);
        assertNull(tok.literalValue);
        assertEquals(1, tok.line);
        assertEquals(6, tok.col);
        assertEquals(1, tok.end_line);
        assertEquals(8, tok.end_col);
    }

    @Test
    public void add() throws Exception {
        String str = "+ w++ += f";

        LexerStream ls = new LexerStream(new StringReader(str));

        Token tok = LexOperator.read(ls);

        assertEquals(TokenType.Plus, tok.type);
        assertNull(tok.literalValue);
        assertEquals(1, tok.line);
        assertEquals(1, tok.col);
        assertEquals(1, tok.end_line);
        assertEquals(2, tok.end_col);

        ls.read();
        ls.read();

        tok = LexOperator.read(ls);

        assertEquals(TokenType.Increment, tok.type);
        assertNull(tok.literalValue);
        assertEquals(1, tok.line);
        assertEquals(4, tok.col);
        assertEquals(1, tok.end_line);
        assertEquals(6, tok.end_col);

        ls.read();

        tok = LexOperator.read(ls);

        assertEquals(TokenType.PlusAssign, tok.type);
        assertNull(tok.literalValue);
        assertEquals(1, tok.line);
        assertEquals(7, tok.col);
        assertEquals(1, tok.end_line);
        assertEquals(9, tok.end_col);

        assertEquals(' ', ls.read());
        assertEquals('f', ls.read());
        assertEquals(-1, ls.read());
    }

    @Test
    public void sub() throws Exception {
        String str = "a-=b-- -c";

        LexerStream ls = new LexerStream(new StringReader(str));

        ls.read();

        Token tok = LexOperator.read(ls);

        assertEquals(TokenType.MinusAssign, tok.type);
        assertNull(tok.literalValue);
        assertEquals(1, tok.line);
        assertEquals(2, tok.col);
        assertEquals(1, tok.end_line);
        assertEquals(4, tok.end_col);

        ls.read();

        tok = LexOperator.read(ls);

        assertEquals(TokenType.Decrement, tok.type);
        assertNull(tok.literalValue);
        assertEquals(1, tok.line);
        assertEquals(5, tok.col);
        assertEquals(1, tok.end_line);
        assertEquals(7, tok.end_col);

        ls.read();

        tok = LexOperator.read(ls);

        assertEquals(TokenType.Minus, tok.type);
        assertNull(tok.literalValue);
        assertEquals(1, tok.line);
        assertEquals(8, tok.col);
        assertEquals(1, tok.end_line);
        assertEquals(9, tok.end_col);

        assertEquals('c', ls.read());
        assertEquals(-1, ls.read());
    }

    @Test
    public void mul() throws Exception {
        String str = "a *=b*c;";

        LexerStream ls = new LexerStream(new StringReader(str));

        ls.read();
        ls.read();

        Token tok = LexOperator.read(ls);

        assertEquals(TokenType.TimesAssign, tok.type);
        assertNull(tok.literalValue);
        assertEquals(1, tok.line);
        assertEquals(3, tok.col);
        assertEquals(1, tok.end_line);
        assertEquals(5, tok.end_col);

        ls.read();

        tok = LexOperator.read(ls);

        assertEquals(TokenType.Times, tok.type);
        assertNull(tok.literalValue);
        assertEquals(1, tok.line);
        assertEquals(6, tok.col);
        assertEquals(1, tok.end_line);
        assertEquals(7, tok.end_col);

        assertEquals('c', ls.read());
        assertEquals(';', ls.read());
        assertEquals(-1, ls.read());
    }

    @Test
    public void div() throws Exception {
        String str = "a/=b/c;";

        LexerStream ls = new LexerStream(new StringReader(str));

        ls.read();

        Token tok = LexOperator.read(ls);

        assertEquals(TokenType.DivAssign, tok.type);
        assertNull(tok.literalValue);
        assertEquals(1, tok.line);
        assertEquals(2, tok.col);
        assertEquals(1, tok.end_line);
        assertEquals(4, tok.end_col);

        ls.read();

        tok = LexOperator.read(ls);

        assertEquals(TokenType.Div, tok.type);
        assertNull(tok.literalValue);
        assertEquals(1, tok.line);
        assertEquals(5, tok.col);
        assertEquals(1, tok.end_line);
        assertEquals(6, tok.end_col);

        assertEquals('c', ls.read());
        assertEquals(';', ls.read());
        assertEquals(-1, ls.read());
    }

    @Test
    public void mod() throws Exception {
        String str = "a%=b%c;";

        LexerStream ls = new LexerStream(new StringReader(str));

        ls.read();

        Token tok = LexOperator.read(ls);

        assertEquals(TokenType.ModAssign, tok.type);
        assertNull(tok.literalValue);
        assertEquals(1, tok.line);
        assertEquals(2, tok.col);
        assertEquals(1, tok.end_line);
        assertEquals(4, tok.end_col);

        ls.read();

        tok = LexOperator.read(ls);

        assertEquals(TokenType.Mod, tok.type);
        assertNull(tok.literalValue);
        assertEquals(1, tok.line);
        assertEquals(5, tok.col);
        assertEquals(1, tok.end_line);
        assertEquals(6, tok.end_col);

        assertEquals('c', ls.read());
        assertEquals(';', ls.read());
        assertEquals(-1, ls.read());
    }

    @Test
    public void bitwise() throws Exception {
        String str = "a&=b|c|=d&e^=f^g;";

        LexerStream ls = new LexerStream(new StringReader(str));

        ls.read();

        Token tok = LexOperator.read(ls);

        assertEquals(TokenType.BitwiseAndAssign, tok.type);
        assertNull(tok.literalValue);
        assertEquals(1, tok.line);
        assertEquals(2, tok.col);
        assertEquals(1, tok.end_line);
        assertEquals(4, tok.end_col);

        ls.read();

        tok = LexOperator.read(ls);

        assertEquals(TokenType.BitwiseOr, tok.type);
        assertNull(tok.literalValue);
        assertEquals(1, tok.line);
        assertEquals(5, tok.col);
        assertEquals(1, tok.end_line);
        assertEquals(6, tok.end_col);

        ls.read();

        tok = LexOperator.read(ls);

        assertEquals(TokenType.BitwiseOrAssign, tok.type);
        assertNull(tok.literalValue);
        assertEquals(1, tok.line);
        assertEquals(7, tok.col);
        assertEquals(1, tok.end_line);
        assertEquals(9, tok.end_col);

        ls.read();

        tok = LexOperator.read(ls);

        assertEquals(TokenType.BitwiseAnd, tok.type);
        assertNull(tok.literalValue);
        assertEquals(1, tok.line);
        assertEquals(10, tok.col);
        assertEquals(1, tok.end_line);
        assertEquals(11, tok.end_col);

        ls.read();

        tok = LexOperator.read(ls);

        assertEquals(TokenType.XorAssign, tok.type);
        assertNull(tok.literalValue);
        assertEquals(1, tok.line);
        assertEquals(12, tok.col);
        assertEquals(1, tok.end_line);
        assertEquals(14, tok.end_col);

        ls.read();

        tok = LexOperator.read(ls);

        assertEquals(TokenType.Xor, tok.type);
        assertNull(tok.literalValue);
        assertEquals(1, tok.line);
        assertEquals(15, tok.col);
        assertEquals(1, tok.end_line);
        assertEquals(16, tok.end_col);

        assertEquals('g', ls.read());
        assertEquals(';', ls.read());
        assertEquals(-1, ls.read());

    }

    @Test
    public void logical() throws Exception {
        String str = "a&&b ||!c;";

        LexerStream ls = new LexerStream(new StringReader(str));

        ls.read();

        Token tok = LexOperator.read(ls);

        assertEquals(TokenType.LogicalAnd, tok.type);
        assertNull(tok.literalValue);
        assertEquals(1, tok.line);
        assertEquals(2, tok.col);
        assertEquals(1, tok.end_line);
        assertEquals(4, tok.end_col);

        ls.read();
        ls.read();

        tok = LexOperator.read(ls);

        assertEquals(TokenType.LogicalOr, tok.type);
        assertNull(tok.literalValue);
        assertEquals(1, tok.line);
        assertEquals(6, tok.col);
        assertEquals(1, tok.end_line);
        assertEquals(8, tok.end_col);

        tok = LexOperator.read(ls);

        assertEquals(TokenType.Not, tok.type);
        assertNull(tok.literalValue);
        assertEquals(1, tok.line);
        assertEquals(8, tok.col);
        assertEquals(1, tok.end_line);
        assertEquals(9, tok.end_col);
    }

    @Test
    public void pow() throws Exception {
        String str = "a^^=b^^c;";

        LexerStream ls = new LexerStream(new StringReader(str));

        ls.read();

        Token tok = LexOperator.read(ls);

        assertEquals(TokenType.PowAssign, tok.type);
        assertNull(tok.literalValue);
        assertEquals(1, tok.line);
        assertEquals(2, tok.col);
        assertEquals(1, tok.end_line);
        assertEquals(5, tok.end_col);

        ls.read();

        tok = LexOperator.read(ls);

        assertEquals(TokenType.Pow, tok.type);
        assertNull(tok.literalValue);
        assertEquals(1, tok.line);
        assertEquals(6, tok.col);
        assertEquals(1, tok.end_line);
        assertEquals(8, tok.end_col);
    }

    @Test
    public void unordered() throws Exception {
        String str = "a !< b !<> c !<>= d !<= e !> f !>= g";

        LexerStream ls = new LexerStream(new StringReader(str));

        ls.read();
        ls.read();

        Token tok = LexOperator.read(ls);

        assertEquals(TokenType.UnorderedGreaterOrEqual, tok.type);
        assertNull(tok.literalValue);
        assertTrue(tok.line == 1 && tok.end_line == 1);
        assertEquals(3, tok.col);
        assertEquals(5, tok.end_col);

        ls.read();
        ls.read();
        ls.read();

        tok = LexOperator.read(ls);

        assertEquals(TokenType.UnorderedOrEqual, tok.type);
        assertNull(tok.literalValue);
        assertTrue(tok.line == 1 && tok.end_line == 1);
        assertEquals(8, tok.col);
        assertEquals(11, tok.end_col);

        ls.read();
        ls.read();
        ls.read();

        tok = LexOperator.read(ls);

        assertEquals(TokenType.Unordered, tok.type);
        assertNull(tok.literalValue);
        assertTrue(tok.line == 1 && tok.end_line == 1);
        assertEquals(14, tok.col);
        assertEquals(18, tok.end_col);

        ls.read();
        ls.read();
        ls.read();

        tok = LexOperator.read(ls);

        assertEquals(TokenType.UnorderedOrGreater, tok.type);
        assertNull(tok.literalValue);
        assertTrue(tok.line == 1 && tok.end_line == 1);
        assertEquals(21, tok.col);
        assertEquals(24, tok.end_col);

        ls.read();
        ls.read();
        ls.read();

        tok = LexOperator.read(ls);

        assertEquals(TokenType.UnorderedLessOrEqual, tok.type);
        assertNull(tok.literalValue);
        assertTrue(tok.line == 1 && tok.end_line == 1);
        assertEquals(27, tok.col);
        assertEquals(29, tok.end_col);

        ls.read();
        ls.read();
        ls.read();

        tok = LexOperator.read(ls);

        assertEquals(TokenType.UnorderedOrLess, tok.type);
        assertNull(tok.literalValue);
        assertTrue(tok.line == 1 && tok.end_line == 1);
        assertEquals(32, tok.col);
        assertEquals(35, tok.end_col);
    }

}
