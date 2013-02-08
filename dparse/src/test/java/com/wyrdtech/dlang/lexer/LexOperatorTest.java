package com.wyrdtech.dlang.lexer;

import org.junit.Test;

import java.io.StringReader;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

/**
 *
 */
public class LexOperatorTest {

    @Test
    public void not_op() throws Exception {
        String str = "foo";

        LexerStream ls = new LexerStream(new StringReader(str));

        Token tok = LexOperator.read(ls);
        assertNull(tok);

        ls = new LexerStream(new StringReader(""));

        try {
            tok = LexOperator.read(ls);
        } catch (LexerException e) {
            // expected
            return;
        }
        fail();
    }

    @Test
    public void assign() throws Exception {
        String str = "a=b";

        LexerStream ls = new LexerStream(new StringReader(str));

        ls.read();

        Token tok = LexOperator.read(ls);

        assertEquals(TokenType.Assign, tok.type);
        assertNull(tok.literalValue);
        assertEquals(1, tok.line);
        assertEquals(2, tok.col);
        assertEquals(1, tok.end_line);
        assertEquals(3, tok.end_col);
    }

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
    public void inequality() throws Exception {
        String str = "a<b>c<=d>=e<>d<>=e";

        LexerStream ls = new LexerStream(new StringReader(str));

        ls.read();

        Token tok = LexOperator.read(ls);

        assertEquals(TokenType.LessThan, tok.type);
        assertNull(tok.literalValue);
        assertTrue(tok.line == 1 && tok.end_line == 1);
        assertEquals(2, tok.col);
        assertEquals(3, tok.end_col);

        ls.read();

        tok = LexOperator.read(ls);

        assertEquals(TokenType.GreaterThan, tok.type);
        assertNull(tok.literalValue);
        assertTrue(tok.line == 1 && tok.end_line == 1);
        assertEquals(4, tok.col);
        assertEquals(5, tok.end_col);

        ls.read();

        tok = LexOperator.read(ls);

        assertEquals(TokenType.LessEqual, tok.type);
        assertNull(tok.literalValue);
        assertTrue(tok.line == 1 && tok.end_line == 1);
        assertEquals(6, tok.col);
        assertEquals(8, tok.end_col);

        ls.read();

        tok = LexOperator.read(ls);

        assertEquals(TokenType.GreaterEqual, tok.type);
        assertNull(tok.literalValue);
        assertTrue(tok.line == 1 && tok.end_line == 1);
        assertEquals(9, tok.col);
        assertEquals(11, tok.end_col);

        ls.read();

        tok = LexOperator.read(ls);

        assertEquals(TokenType.LessOrGreater, tok.type);
        assertNull(tok.literalValue);
        assertTrue(tok.line == 1 && tok.end_line == 1);
        assertEquals(12, tok.col);
        assertEquals(14, tok.end_col);

        ls.read();

        tok = LexOperator.read(ls);

        assertEquals(TokenType.LessEqualOrGreater, tok.type);
        assertNull(tok.literalValue);
        assertTrue(tok.line == 1 && tok.end_line == 1);
        assertEquals(15, tok.col);
        assertEquals(18, tok.end_col);
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

        tok = LexOperator.read(ls);
        assertEquals(TokenType.Semicolon, tok.type);
        assertNull(tok.literalValue);

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

        tok = LexOperator.read(ls);
        assertEquals(TokenType.Semicolon, tok.type);
        assertNull(tok.literalValue);

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

        tok = LexOperator.read(ls);
        assertEquals(TokenType.Semicolon, tok.type);
        assertNull(tok.literalValue);

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

        tok = LexOperator.read(ls);
        assertEquals(TokenType.Semicolon, tok.type);
        assertNull(tok.literalValue);

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

        ls.read();

        tok = LexOperator.read(ls);
        assertEquals(TokenType.Semicolon, tok.type);
        assertNull(tok.literalValue);
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

        ls.read();

        tok = LexOperator.read(ls);
        assertEquals(TokenType.Semicolon, tok.type);
        assertNull(tok.literalValue);
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

    @Test
    public void tilde() throws Exception {
        String str = "a~=b~c;";

        LexerStream ls = new LexerStream(new StringReader(str));

        ls.read();

        Token tok = LexOperator.read(ls);

        assertEquals(TokenType.TildeAssign, tok.type);
        assertNull(tok.literalValue);
        assertTrue(tok.line == 1 && tok.end_line == 1);
        assertEquals(2, tok.col);
        assertEquals(4, tok.end_col);

        ls.read();

        tok = LexOperator.read(ls);

        assertEquals(TokenType.Tilde, tok.type);
        assertNull(tok.literalValue);
        assertTrue(tok.line == 1 && tok.end_line == 1);
        assertEquals(5, tok.col);
        assertEquals(6, tok.end_col);
    }

    @Test
    public void eleven() throws Exception {
        String str = "v=>11;";

        LexerStream ls = new LexerStream(new StringReader(str));

        ls.read();

        Token tok = LexOperator.read(ls);

        assertEquals(TokenType.GoesTo, tok.type);
        assertNull(tok.literalValue);
        assertTrue(tok.line == 1 && tok.end_line == 1);
        assertEquals(2, tok.col);
        assertEquals(4, tok.end_col);
    }

    @Test
    public void shift() throws Exception {
        String str = "a<<b>>c<<=d>>=e";

        LexerStream ls = new LexerStream(new StringReader(str));

        ls.read();

        Token tok = LexOperator.read(ls);

        assertEquals(TokenType.ShiftLeft, tok.type);
        assertNull(tok.literalValue);
        assertTrue(tok.line == 1 && tok.end_line == 1);
        assertEquals(2, tok.col);
        assertEquals(4, tok.end_col);

        ls.read();

        tok = LexOperator.read(ls);

        assertEquals(TokenType.ShiftRight, tok.type);
        assertNull(tok.literalValue);
        assertTrue(tok.line == 1 && tok.end_line == 1);
        assertEquals(5, tok.col);
        assertEquals(7, tok.end_col);

        ls.read();

        tok = LexOperator.read(ls);

        assertEquals(TokenType.ShiftLeftAssign, tok.type);
        assertNull(tok.literalValue);
        assertTrue(tok.line == 1 && tok.end_line == 1);
        assertEquals(8, tok.col);
        assertEquals(11, tok.end_col);

        ls.read();

        tok = LexOperator.read(ls);

        assertEquals(TokenType.ShiftRightAssign, tok.type);
        assertNull(tok.literalValue);
        assertTrue(tok.line == 1 && tok.end_line == 1);
        assertEquals(12, tok.col);
        assertEquals(15, tok.end_col);
    }

    @Test
    public void u_shift() throws Exception {
        String str = "a>>>=b>>>"; // unsigned is only for right shift

        LexerStream ls = new LexerStream(new StringReader(str));

        ls.read();

        Token tok = LexOperator.read(ls);

        assertEquals(TokenType.TripleRightShiftAssign, tok.type);
        assertNull(tok.literalValue);
        assertTrue(tok.line == 1 && tok.end_line == 1);
        assertEquals(2, tok.col);
        assertEquals(6, tok.end_col);

        ls.read();

        tok = LexOperator.read(ls);

        assertEquals(TokenType.ShiftRightUnsigned, tok.type);
        assertNull(tok.literalValue);
        assertTrue(tok.line == 1 && tok.end_line == 1);
        assertEquals(7, tok.col);
        assertEquals(10, tok.end_col);

    }

    @Test
    public void tri() throws Exception {
        String str = "a?b:c$d";

        LexerStream ls = new LexerStream(new StringReader(str));

        ls.read();

        Token tok = LexOperator.read(ls);

        assertEquals(TokenType.Question, tok.type);
        assertNull(tok.literalValue);
        assertTrue(tok.line == 1 && tok.end_line == 1);
        assertEquals(2, tok.col);
        assertEquals(3, tok.end_col);

        ls.read();

        tok = LexOperator.read(ls);

        assertEquals(TokenType.Colon, tok.type);
        assertNull(tok.literalValue);
        assertTrue(tok.line == 1 && tok.end_line == 1);
        assertEquals(4, tok.col);
        assertEquals(5, tok.end_col);

        ls.read();

        tok = LexOperator.read(ls);

        assertEquals(TokenType.Dollar, tok.type);
        assertNull(tok.literalValue);
        assertTrue(tok.line == 1 && tok.end_line == 1);
        assertEquals(6, tok.col);
        assertEquals(7, tok.end_col);
    }


    @Test
    public void dot() throws Exception {
        String str = "a.b .3";

        LexerStream ls = new LexerStream(new StringReader(str));

        ls.read();

        Token tok = LexOperator.read(ls);

        assertEquals(TokenType.Dot, tok.type);
        assertNull(tok.literalValue);
        assertTrue(tok.line == 1 && tok.end_line == 1);
        assertEquals(2, tok.col);
        assertEquals(3, tok.end_col);

        ls.read();
        ls.read();

        tok = LexOperator.read(ls);

        assertEquals(TokenType.Literal, tok.type);
        assertEquals(Double.valueOf("0.3"), tok.literalValue);
        assertTrue(tok.line == 1 && tok.end_line == 1);
        assertEquals(5, tok.col);
        assertEquals(7, tok.end_col);
    }

    @Test
    public void range() throws Exception {
        String str = "1..2...3,4";

        LexerStream ls = new LexerStream(new StringReader(str));

        ls.read();

        Token tok = LexOperator.read(ls);

        assertEquals(TokenType.DoubleDot, tok.type);
        assertNull(tok.literalValue);
        assertTrue(tok.line == 1 && tok.end_line == 1);
        assertEquals(2, tok.col);
        assertEquals(4, tok.end_col);

        ls.read();

        tok = LexOperator.read(ls);

        assertEquals(TokenType.TripleDot, tok.type);
        assertNull(tok.literalValue);
        assertTrue(tok.line == 1 && tok.end_line == 1);
        assertEquals(5, tok.col);
        assertEquals(8, tok.end_col);

        ls.read();

        tok = LexOperator.read(ls);

        assertEquals(TokenType.Comma, tok.type);
        assertNull(tok.literalValue);
        assertTrue(tok.line == 1 && tok.end_line == 1);
        assertEquals(9, tok.col);
        assertEquals(10, tok.end_col);
    }

    @Test
    public void paren() throws Exception {
        String str = "(1)[2]{3}";

        LexerStream ls = new LexerStream(new StringReader(str));

        Token tok = LexOperator.read(ls);

        assertEquals(TokenType.OpenParenthesis, tok.type);
        assertNull(tok.literalValue);
        assertTrue(tok.line == 1 && tok.end_line == 1);
        assertEquals(1, tok.col);
        assertEquals(2, tok.end_col);

        ls.read();

        tok = LexOperator.read(ls);

        assertEquals(TokenType.CloseParenthesis, tok.type);
        assertNull(tok.literalValue);
        assertTrue(tok.line == 1 && tok.end_line == 1);
        assertEquals(3, tok.col);
        assertEquals(4, tok.end_col);

        tok = LexOperator.read(ls);

        assertEquals(TokenType.OpenSquareBracket, tok.type);
        assertNull(tok.literalValue);
        assertTrue(tok.line == 1 && tok.end_line == 1);
        assertEquals(4, tok.col);
        assertEquals(5, tok.end_col);

        ls.read();

        tok = LexOperator.read(ls);

        assertEquals(TokenType.CloseSquareBracket, tok.type);
        assertNull(tok.literalValue);
        assertTrue(tok.line == 1 && tok.end_line == 1);
        assertEquals(6, tok.col);
        assertEquals(7, tok.end_col);

        tok = LexOperator.read(ls);

        assertEquals(TokenType.OpenCurlyBrace, tok.type);
        assertNull(tok.literalValue);
        assertTrue(tok.line == 1 && tok.end_line == 1);
        assertEquals(7, tok.col);
        assertEquals(8, tok.end_col);

        ls.read();

        tok = LexOperator.read(ls);

        assertEquals(TokenType.CloseCurlyBrace, tok.type);
        assertNull(tok.literalValue);
        assertTrue(tok.line == 1 && tok.end_line == 1);
        assertEquals(9, tok.col);
        assertEquals(10, tok.end_col);
    }

}
