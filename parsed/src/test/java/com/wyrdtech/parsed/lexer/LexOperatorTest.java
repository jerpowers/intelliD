package com.wyrdtech.parsed.lexer;

import com.wyrdtech.parsed.lexer.token.BaseTokenFactory;
import com.wyrdtech.parsed.lexer.token.Token;
import com.wyrdtech.parsed.lexer.token.TokenType;
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
        LexOperator lex = new LexOperator(new BaseTokenFactory(), ls);

        Token tok = lex.read();
        assertNull(tok);

        lex = new LexOperator(new BaseTokenFactory(), new LexerStream(new StringReader("")));
        try {
            tok = lex.read();
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
        LexOperator lex = new LexOperator(new BaseTokenFactory(), ls);

        ls.read();

        Token tok = lex.read();

        assertEquals(TokenType.Assign, tok.getType());
        assertEquals("=", tok.getValue());
        assertEquals(1, tok.getLine());
        assertEquals(2, tok.getCol());
        assertEquals(1, tok.getEndLine());
        assertEquals(3, tok.getEndCol());
    }

    @Test
    public void equality() throws Exception {
        String str = "a==b != c";

        LexerStream ls = new LexerStream(new StringReader(str));
        LexOperator lex = new LexOperator(new BaseTokenFactory(), ls);

        ls.read();

        Token tok = lex.read();

        assertEquals(TokenType.Equal, tok.getType());
        assertEquals("==", tok.getValue());
        assertEquals(1, tok.getLine());
        assertEquals(2, tok.getCol());
        assertEquals(1, tok.getEndLine());
        assertEquals(4, tok.getEndCol());

        ls.read();
        ls.read();

        tok = lex.read();

        assertEquals(TokenType.NotEqual, tok.getType());
        assertEquals("!=", tok.getValue());
        assertEquals(1, tok.getLine());
        assertEquals(6, tok.getCol());
        assertEquals(1, tok.getEndLine());
        assertEquals(8, tok.getEndCol());
    }

    @Test
    public void inequality() throws Exception {
        String str = "a<b>c<=d>=e<>d<>=e";

        LexerStream ls = new LexerStream(new StringReader(str));
        LexOperator lex = new LexOperator(new BaseTokenFactory(), ls);

        ls.read();

        Token tok = lex.read();

        assertEquals(TokenType.LessThan, tok.getType());
        assertEquals("<", tok.getValue());
        assertTrue(tok.getLine() == 1 && tok.getEndLine() == 1);
        assertEquals(2, tok.getCol());
        assertEquals(3, tok.getEndCol());

        ls.read();

        tok = lex.read();

        assertEquals(TokenType.GreaterThan, tok.getType());
        assertEquals(">", tok.getValue());
        assertTrue(tok.getLine() == 1 && tok.getEndLine() == 1);
        assertEquals(4, tok.getCol());
        assertEquals(5, tok.getEndCol());

        ls.read();

        tok = lex.read();

        assertEquals(TokenType.LessEqual, tok.getType());
        assertTrue(tok.getLine() == 1 && tok.getEndLine() == 1);
        assertEquals(6, tok.getCol());
        assertEquals(8, tok.getEndCol());

        ls.read();

        tok = lex.read();

        assertEquals(TokenType.GreaterEqual, tok.getType());
        assertTrue(tok.getLine() == 1 && tok.getEndLine() == 1);
        assertEquals(9, tok.getCol());
        assertEquals(11, tok.getEndCol());

        ls.read();

        tok = lex.read();

        assertEquals(TokenType.LessOrGreater, tok.getType());
        assertTrue(tok.getLine() == 1 && tok.getEndLine() == 1);
        assertEquals(12, tok.getCol());
        assertEquals(14, tok.getEndCol());

        ls.read();

        tok = lex.read();

        assertEquals(TokenType.LessEqualOrGreater, tok.getType());
        assertTrue(tok.getLine() == 1 && tok.getEndLine() == 1);
        assertEquals(15, tok.getCol());
        assertEquals(18, tok.getEndCol());
    }

    @Test
    public void add() throws Exception {
        String str = "+ w++ += f";

        LexerStream ls = new LexerStream(new StringReader(str));
        LexOperator lex = new LexOperator(new BaseTokenFactory(), ls);

        Token tok = lex.read();

        assertEquals(TokenType.Plus, tok.getType());
        assertEquals(1, tok.getLine());
        assertEquals(1, tok.getCol());
        assertEquals(1, tok.getEndLine());
        assertEquals(2, tok.getEndCol());

        ls.read();
        ls.read();

        tok = lex.read();

        assertEquals(TokenType.Increment, tok.getType());
        assertEquals(1, tok.getLine());
        assertEquals(4, tok.getCol());
        assertEquals(1, tok.getEndLine());
        assertEquals(6, tok.getEndCol());

        ls.read();

        tok = lex.read();

        assertEquals(TokenType.PlusAssign, tok.getType());
        assertEquals(1, tok.getLine());
        assertEquals(7, tok.getCol());
        assertEquals(1, tok.getEndLine());
        assertEquals(9, tok.getEndCol());

        assertEquals(' ', ls.read());
        assertEquals('f', ls.read());
        assertEquals(-1, ls.read());
    }

    @Test
    public void sub() throws Exception {
        String str = "a-=b-- -c";

        LexerStream ls = new LexerStream(new StringReader(str));
        LexOperator lex = new LexOperator(new BaseTokenFactory(), ls);

        ls.read();

        Token tok = lex.read();

        assertEquals(TokenType.MinusAssign, tok.getType());
        assertEquals(1, tok.getLine());
        assertEquals(2, tok.getCol());
        assertEquals(1, tok.getEndLine());
        assertEquals(4, tok.getEndCol());

        ls.read();

        tok = lex.read();

        assertEquals(TokenType.Decrement, tok.getType());
        assertEquals(1, tok.getLine());
        assertEquals(5, tok.getCol());
        assertEquals(1, tok.getEndLine());
        assertEquals(7, tok.getEndCol());

        ls.read();

        tok = lex.read();

        assertEquals(TokenType.Minus, tok.getType());
        assertEquals(1, tok.getLine());
        assertEquals(8, tok.getCol());
        assertEquals(1, tok.getEndLine());
        assertEquals(9, tok.getEndCol());

        assertEquals('c', ls.read());
        assertEquals(-1, ls.read());
    }

    @Test
    public void mul() throws Exception {
        String str = "a *=b*c;";

        LexerStream ls = new LexerStream(new StringReader(str));
        LexOperator lex = new LexOperator(new BaseTokenFactory(), ls);

        ls.read();
        ls.read();

        Token tok = lex.read();

        assertEquals(TokenType.TimesAssign, tok.getType());
        assertEquals(1, tok.getLine());
        assertEquals(3, tok.getCol());
        assertEquals(1, tok.getEndLine());
        assertEquals(5, tok.getEndCol());

        ls.read();

        tok = lex.read();

        assertEquals(TokenType.Times, tok.getType());
        assertEquals(1, tok.getLine());
        assertEquals(6, tok.getCol());
        assertEquals(1, tok.getEndLine());
        assertEquals(7, tok.getEndCol());

        assertEquals('c', ls.read());

        tok = lex.read();
        assertEquals(TokenType.Semicolon, tok.getType());

        assertEquals(-1, ls.read());
    }

    @Test
    public void div() throws Exception {
        String str = "a/=b/c;";

        LexerStream ls = new LexerStream(new StringReader(str));
        LexOperator lex = new LexOperator(new BaseTokenFactory(), ls);

        ls.read();

        Token tok = lex.read();

        assertEquals(TokenType.DivAssign, tok.getType());
        assertEquals(1, tok.getLine());
        assertEquals(2, tok.getCol());
        assertEquals(1, tok.getEndLine());
        assertEquals(4, tok.getEndCol());

        ls.read();

        tok = lex.read();

        assertEquals(TokenType.Div, tok.getType());
        assertEquals(1, tok.getLine());
        assertEquals(5, tok.getCol());
        assertEquals(1, tok.getEndLine());
        assertEquals(6, tok.getEndCol());

        assertEquals('c', ls.read());

        tok = lex.read();
        assertEquals(TokenType.Semicolon, tok.getType());

        assertEquals(-1, ls.read());
    }

    @Test
    public void mod() throws Exception {
        String str = "a%=b%c;";

        LexerStream ls = new LexerStream(new StringReader(str));
        LexOperator lex = new LexOperator(new BaseTokenFactory(), ls);

        ls.read();

        Token tok = lex.read();

        assertEquals(TokenType.ModAssign, tok.getType());
        assertEquals(1, tok.getLine());
        assertEquals(2, tok.getCol());
        assertEquals(1, tok.getEndLine());
        assertEquals(4, tok.getEndCol());

        ls.read();

        tok = lex.read();

        assertEquals(TokenType.Mod, tok.getType());
        assertEquals(1, tok.getLine());
        assertEquals(5, tok.getCol());
        assertEquals(1, tok.getEndLine());
        assertEquals(6, tok.getEndCol());

        assertEquals('c', ls.read());

        tok = lex.read();
        assertEquals(TokenType.Semicolon, tok.getType());

        assertEquals(-1, ls.read());
    }

    @Test
    public void bitwise() throws Exception {
        String str = "a&=b|c|=d&e^=f^g;";

        LexerStream ls = new LexerStream(new StringReader(str));
        LexOperator lex = new LexOperator(new BaseTokenFactory(), ls);

        ls.read();

        Token tok = lex.read();

        assertEquals(TokenType.BitwiseAndAssign, tok.getType());
        assertEquals(1, tok.getLine());
        assertEquals(2, tok.getCol());
        assertEquals(1, tok.getEndLine());
        assertEquals(4, tok.getEndCol());

        ls.read();

        tok = lex.read();

        assertEquals(TokenType.BitwiseOr, tok.getType());
        assertEquals(1, tok.getLine());
        assertEquals(5, tok.getCol());
        assertEquals(1, tok.getEndLine());
        assertEquals(6, tok.getEndCol());

        ls.read();

        tok = lex.read();

        assertEquals(TokenType.BitwiseOrAssign, tok.getType());
        assertEquals(1, tok.getLine());
        assertEquals(7, tok.getCol());
        assertEquals(1, tok.getEndLine());
        assertEquals(9, tok.getEndCol());

        ls.read();

        tok = lex.read();

        assertEquals(TokenType.BitwiseAnd, tok.getType());
        assertEquals(1, tok.getLine());
        assertEquals(10, tok.getCol());
        assertEquals(1, tok.getEndLine());
        assertEquals(11, tok.getEndCol());

        ls.read();

        tok = lex.read();

        assertEquals(TokenType.XorAssign, tok.getType());
        assertEquals(1, tok.getLine());
        assertEquals(12, tok.getCol());
        assertEquals(1, tok.getEndLine());
        assertEquals(14, tok.getEndCol());

        ls.read();

        tok = lex.read();

        assertEquals(TokenType.Xor, tok.getType());
        assertEquals(1, tok.getLine());
        assertEquals(15, tok.getCol());
        assertEquals(1, tok.getEndLine());
        assertEquals(16, tok.getEndCol());

        assertEquals('g', ls.read());

        tok = lex.read();
        assertEquals(TokenType.Semicolon, tok.getType());

        assertEquals(-1, ls.read());

    }

    @Test
    public void logical() throws Exception {
        String str = "a&&b ||!c;";

        LexerStream ls = new LexerStream(new StringReader(str));
        LexOperator lex = new LexOperator(new BaseTokenFactory(), ls);

        ls.read();

        Token tok = lex.read();

        assertEquals(TokenType.LogicalAnd, tok.getType());
        assertEquals(1, tok.getLine());
        assertEquals(2, tok.getCol());
        assertEquals(1, tok.getEndLine());
        assertEquals(4, tok.getEndCol());

        ls.read();
        ls.read();

        tok = lex.read();

        assertEquals(TokenType.LogicalOr, tok.getType());
        assertEquals(1, tok.getLine());
        assertEquals(6, tok.getCol());
        assertEquals(1, tok.getEndLine());
        assertEquals(8, tok.getEndCol());

        tok = lex.read();

        assertEquals(TokenType.Not, tok.getType());
        assertEquals(1, tok.getLine());
        assertEquals(8, tok.getCol());
        assertEquals(1, tok.getEndLine());
        assertEquals(9, tok.getEndCol());

        ls.read();

        tok = lex.read();
        assertEquals(TokenType.Semicolon, tok.getType());
    }

    @Test
    public void pow() throws Exception {
        String str = "a^^=b^^c;";

        LexerStream ls = new LexerStream(new StringReader(str));
        LexOperator lex = new LexOperator(new BaseTokenFactory(), ls);

        ls.read();

        Token tok = lex.read();

        assertEquals(TokenType.PowAssign, tok.getType());
        assertEquals(1, tok.getLine());
        assertEquals(2, tok.getCol());
        assertEquals(1, tok.getEndLine());
        assertEquals(5, tok.getEndCol());

        ls.read();

        tok = lex.read();

        assertEquals(TokenType.Pow, tok.getType());
        assertEquals(1, tok.getLine());
        assertEquals(6, tok.getCol());
        assertEquals(1, tok.getEndLine());
        assertEquals(8, tok.getEndCol());

        ls.read();

        tok = lex.read();
        assertEquals(TokenType.Semicolon, tok.getType());
    }

    @Test
    public void unordered() throws Exception {
        String str = "a !< b !<> c !<>= d !<= e !> f !>= g";

        LexerStream ls = new LexerStream(new StringReader(str));
        LexOperator lex = new LexOperator(new BaseTokenFactory(), ls);

        ls.read();
        ls.read();

        Token tok = lex.read();

        assertEquals(TokenType.UnorderedGreaterOrEqual, tok.getType());
        assertTrue(tok.getLine() == 1 && tok.getEndLine() == 1);
        assertEquals(3, tok.getCol());
        assertEquals(5, tok.getEndCol());

        ls.read();
        ls.read();
        ls.read();

        tok = lex.read();

        assertEquals(TokenType.UnorderedOrEqual, tok.getType());
        assertTrue(tok.getLine() == 1 && tok.getEndLine() == 1);
        assertEquals(8, tok.getCol());
        assertEquals(11, tok.getEndCol());

        ls.read();
        ls.read();
        ls.read();

        tok = lex.read();

        assertEquals(TokenType.Unordered, tok.getType());
        assertTrue(tok.getLine() == 1 && tok.getEndLine() == 1);
        assertEquals(14, tok.getCol());
        assertEquals(18, tok.getEndCol());

        ls.read();
        ls.read();
        ls.read();

        tok = lex.read();

        assertEquals(TokenType.UnorderedOrGreater, tok.getType());
        assertTrue(tok.getLine() == 1 && tok.getEndLine() == 1);
        assertEquals(21, tok.getCol());
        assertEquals(24, tok.getEndCol());

        ls.read();
        ls.read();
        ls.read();

        tok = lex.read();

        assertEquals(TokenType.UnorderedLessOrEqual, tok.getType());
        assertTrue(tok.getLine() == 1 && tok.getEndLine() == 1);
        assertEquals(27, tok.getCol());
        assertEquals(29, tok.getEndCol());

        ls.read();
        ls.read();
        ls.read();

        tok = lex.read();

        assertEquals(TokenType.UnorderedOrLess, tok.getType());
        assertTrue(tok.getLine() == 1 && tok.getEndLine() == 1);
        assertEquals(32, tok.getCol());
        assertEquals(35, tok.getEndCol());
    }

    @Test
    public void tilde() throws Exception {
        String str = "a~=b~c;";

        LexerStream ls = new LexerStream(new StringReader(str));
        LexOperator lex = new LexOperator(new BaseTokenFactory(), ls);

        ls.read();

        Token tok = lex.read();

        assertEquals(TokenType.TildeAssign, tok.getType());
        assertTrue(tok.getLine() == 1 && tok.getEndLine() == 1);
        assertEquals(2, tok.getCol());
        assertEquals(4, tok.getEndCol());

        ls.read();

        tok = lex.read();

        assertEquals(TokenType.Tilde, tok.getType());
        assertTrue(tok.getLine() == 1 && tok.getEndLine() == 1);
        assertEquals(5, tok.getCol());
        assertEquals(6, tok.getEndCol());
    }

    @Test
    public void eleven() throws Exception {
        String str = "v=>11;";

        LexerStream ls = new LexerStream(new StringReader(str));
        LexOperator lex = new LexOperator(new BaseTokenFactory(), ls);

        ls.read();

        Token tok = lex.read();

        assertEquals(TokenType.GoesTo, tok.getType());
        assertTrue(tok.getLine() == 1 && tok.getEndLine() == 1);
        assertEquals(2, tok.getCol());
        assertEquals(4, tok.getEndCol());
    }

    @Test
    public void shift() throws Exception {
        String str = "a<<b>>c<<=d>>=e";

        LexerStream ls = new LexerStream(new StringReader(str));
        LexOperator lex = new LexOperator(new BaseTokenFactory(), ls);

        ls.read();

        Token tok = lex.read();

        assertEquals(TokenType.ShiftLeft, tok.getType());
        assertTrue(tok.getLine() == 1 && tok.getEndLine() == 1);
        assertEquals(2, tok.getCol());
        assertEquals(4, tok.getEndCol());

        ls.read();

        tok = lex.read();

        assertEquals(TokenType.ShiftRight, tok.getType());
        assertTrue(tok.getLine() == 1 && tok.getEndLine() == 1);
        assertEquals(5, tok.getCol());
        assertEquals(7, tok.getEndCol());

        ls.read();

        tok = lex.read();

        assertEquals(TokenType.ShiftLeftAssign, tok.getType());
        assertTrue(tok.getLine() == 1 && tok.getEndLine() == 1);
        assertEquals(8, tok.getCol());
        assertEquals(11, tok.getEndCol());

        ls.read();

        tok = lex.read();

        assertEquals(TokenType.ShiftRightAssign, tok.getType());
        assertTrue(tok.getLine() == 1 && tok.getEndLine() == 1);
        assertEquals(12, tok.getCol());
        assertEquals(15, tok.getEndCol());
    }

    @Test
    public void u_shift() throws Exception {
        String str = "a>>>=b>>>"; // unsigned is only for right shift

        LexerStream ls = new LexerStream(new StringReader(str));
        LexOperator lex = new LexOperator(new BaseTokenFactory(), ls);

        ls.read();

        Token tok = lex.read();

        assertEquals(TokenType.TripleRightShiftAssign, tok.getType());
        assertTrue(tok.getLine() == 1 && tok.getEndLine() == 1);
        assertEquals(2, tok.getCol());
        assertEquals(6, tok.getEndCol());

        ls.read();

        tok = lex.read();

        assertEquals(TokenType.ShiftRightUnsigned, tok.getType());
        assertTrue(tok.getLine() == 1 && tok.getEndLine() == 1);
        assertEquals(7, tok.getCol());
        assertEquals(10, tok.getEndCol());

    }

    @Test
    public void tri() throws Exception {
        String str = "a?b:c$d";

        LexerStream ls = new LexerStream(new StringReader(str));
        LexOperator lex = new LexOperator(new BaseTokenFactory(), ls);

        ls.read();

        Token tok = lex.read();

        assertEquals(TokenType.Question, tok.getType());
        assertTrue(tok.getLine() == 1 && tok.getEndLine() == 1);
        assertEquals(2, tok.getCol());
        assertEquals(3, tok.getEndCol());

        ls.read();

        tok = lex.read();

        assertEquals(TokenType.Colon, tok.getType());
        assertTrue(tok.getLine() == 1 && tok.getEndLine() == 1);
        assertEquals(4, tok.getCol());
        assertEquals(5, tok.getEndCol());

        ls.read();

        tok = lex.read();

        assertEquals(TokenType.Dollar, tok.getType());
        assertTrue(tok.getLine() == 1 && tok.getEndLine() == 1);
        assertEquals(6, tok.getCol());
        assertEquals(7, tok.getEndCol());
    }


    @Test
    public void dot() throws Exception {
        String str = "a.b .3";

        LexerStream ls = new LexerStream(new StringReader(str));
        LexOperator lex = new LexOperator(new BaseTokenFactory(), ls);

        ls.read();

        Token tok = lex.read();

        assertEquals(TokenType.Dot, tok.getType());
        assertTrue(tok.getLine() == 1 && tok.getEndLine() == 1);
        assertEquals(2, tok.getCol());
        assertEquals(3, tok.getEndCol());

        ls.read();
        ls.read();

        try {
            tok = lex.read();
        } catch (LexerException e) {
            // expected
            return;
        }
        fail();
    }

    @Test
    public void range() throws Exception {
        String str = "1..2...3,4";

        LexerStream ls = new LexerStream(new StringReader(str));
        LexOperator lex = new LexOperator(new BaseTokenFactory(), ls);

        ls.read();

        Token tok = lex.read();

        assertEquals(TokenType.DoubleDot, tok.getType());
        assertTrue(tok.getLine() == 1 && tok.getEndLine() == 1);
        assertEquals(2, tok.getCol());
        assertEquals(4, tok.getEndCol());

        ls.read();

        tok = lex.read();

        assertEquals(TokenType.TripleDot, tok.getType());
        assertTrue(tok.getLine() == 1 && tok.getEndLine() == 1);
        assertEquals(5, tok.getCol());
        assertEquals(8, tok.getEndCol());

        ls.read();

        tok = lex.read();

        assertEquals(TokenType.Comma, tok.getType());
        assertTrue(tok.getLine() == 1 && tok.getEndLine() == 1);
        assertEquals(9, tok.getCol());
        assertEquals(10, tok.getEndCol());
    }

    @Test
    public void paren() throws Exception {
        String str = "(1)[2]{3}";

        LexerStream ls = new LexerStream(new StringReader(str));
        LexOperator lex = new LexOperator(new BaseTokenFactory(), ls);

        Token tok = lex.read();

        assertEquals(TokenType.OpenParenthesis, tok.getType());
        assertTrue(tok.getLine() == 1 && tok.getEndLine() == 1);
        assertEquals(1, tok.getCol());
        assertEquals(2, tok.getEndCol());

        ls.read();

        tok = lex.read();

        assertEquals(TokenType.CloseParenthesis, tok.getType());
        assertTrue(tok.getLine() == 1 && tok.getEndLine() == 1);
        assertEquals(3, tok.getCol());
        assertEquals(4, tok.getEndCol());

        tok = lex.read();

        assertEquals(TokenType.OpenSquareBracket, tok.getType());
        assertTrue(tok.getLine() == 1 && tok.getEndLine() == 1);
        assertEquals(4, tok.getCol());
        assertEquals(5, tok.getEndCol());

        ls.read();

        tok = lex.read();

        assertEquals(TokenType.CloseSquareBracket, tok.getType());
        assertTrue(tok.getLine() == 1 && tok.getEndLine() == 1);
        assertEquals(6, tok.getCol());
        assertEquals(7, tok.getEndCol());

        tok = lex.read();

        assertEquals(TokenType.OpenCurlyBrace, tok.getType());
        assertTrue(tok.getLine() == 1 && tok.getEndLine() == 1);
        assertEquals(7, tok.getCol());
        assertEquals(8, tok.getEndCol());

        ls.read();

        tok = lex.read();

        assertEquals(TokenType.CloseCurlyBrace, tok.getType());
        assertTrue(tok.getLine() == 1 && tok.getEndLine() == 1);
        assertEquals(9, tok.getCol());
        assertEquals(10, tok.getEndCol());
    }

    @Test
    public void hash() throws Exception {
        String str = "##";

        LexerStream ls = new LexerStream(new StringReader(str));
        LexOperator lex = new LexOperator(new BaseTokenFactory(), ls);

        Token tok = lex.read();

        assertEquals(TokenType.Hash, tok.getType());
        assertTrue(tok.getLine() == 1 && tok.getEndLine() == 1);
        assertEquals(1, tok.getCol());
        assertEquals(2, tok.getEndCol());

        tok = lex.read();

        assertEquals(TokenType.Hash, tok.getType());
        assertTrue(tok.getLine() == 1 && tok.getEndLine() == 1);
        assertEquals(2, tok.getCol());
        assertEquals(3, tok.getEndCol());
    }

    @Test(expected = LexerException.class)
    public void empty() throws Exception {
        String str = "";
        LexerStream ls = new LexerStream(new StringReader(str));
        LexOperator lex = new LexOperator(new BaseTokenFactory(), ls);

        lex.read();
    }
}
