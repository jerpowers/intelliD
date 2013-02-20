package com.wyrdtech.parsed.lexer;

import com.wyrdtech.parsed.lexer.token.BaseTokenFactory;
import com.wyrdtech.parsed.lexer.token.Token;
import com.wyrdtech.parsed.lexer.token.TokenType;
import org.junit.Test;

import java.io.StringReader;

import static junit.framework.Assert.assertNull;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;

/**
 * TODO: error cases
 */
public class LexNumericaLiteralTest {

    @Test
    public void integer() throws Exception {
        String str = "1_33_7 with 123456 after";

        LexerStream ls = new LexerStream(new StringReader(str));
        LexNumericLiteral lex = new LexNumericLiteral(new BaseTokenFactory(), ls);

        Token tok = lex.read();

        assertEquals(TokenType.Literal, tok.getType());
        assertTrue(tok.getValue() instanceof Long);
        assertEquals(1337L, tok.getValue());
        assertEquals("1337", tok.getValue().toString());
        assertEquals(1, tok.getLine());
        assertEquals(1, tok.getCol());
        assertEquals(1, tok.getEndLine());
        assertEquals(7, tok.getEndCol());

        assertEquals(' ', ls.read());
        assertEquals('w', ls.read());
        ls.read();
        ls.read();
        assertEquals('h', ls.read());
        ls.read();

        tok = lex.read();

        assertEquals(TokenType.Literal, tok.getType());
        assertTrue(tok.getValue() instanceof Long);
        assertEquals(123456L, tok.getValue());
        assertEquals(1, tok.getLine());
        assertEquals(13, tok.getCol());
        assertEquals(1, tok.getEndLine());
        assertEquals(19, tok.getEndCol());

        assertEquals(' ', ls.read());
        assertEquals('a', ls.read());
    }

    @Test
    public void integer_dots() throws Exception {
        String str = "9..5";

        LexerStream ls = new LexerStream(new StringReader(str));
        LexNumericLiteral lex = new LexNumericLiteral(new BaseTokenFactory(), ls);

        Token tok = lex.read();

        assertEquals(TokenType.Literal, tok.getType());
        assertTrue(tok.getValue() instanceof Long);
        assertEquals(9L, tok.getValue());
        assertEquals(1, tok.getLine());
        assertEquals(1, tok.getCol());
        assertEquals(1, tok.getEndLine());
        assertEquals(2, tok.getEndCol());

        assertEquals('.', ls.read());
        assertEquals('.', ls.read());

        tok = lex.read();

        assertEquals(TokenType.Literal, tok.getType());
        assertTrue(tok.getValue() instanceof Long);
        assertEquals(5L, tok.getValue());
        assertEquals(1, tok.getLine());
        assertEquals(4, tok.getCol());
        assertEquals(1, tok.getEndLine());
        assertEquals(5, tok.getEndCol());
    }

    @Test
    public void hex_int() throws Exception {
        String str = "f = 0xdeadBeEf_1234;";

        LexerStream ls = new LexerStream(new StringReader(str));
        LexNumericLiteral lex = new LexNumericLiteral(new BaseTokenFactory(), ls);

        ls.read();
        ls.read();
        ls.read();
        ls.read();

        Token tok = lex.read();

        assertEquals(TokenType.Literal, tok.getType());
        assertTrue(tok.getValue() instanceof Long);
        assertEquals(Long.parseLong("deadBeEf1234", 16), tok.getValue());
        assertEquals(1, tok.getLine());
        assertEquals(5, tok.getCol());
        assertEquals(1, tok.getEndLine());
        assertEquals(20, tok.getEndCol());
    }

    @Test
    public void bin_int() throws Exception {
        String str = "0b0101";

        LexerStream ls = new LexerStream(new StringReader(str));
        LexNumericLiteral lex = new LexNumericLiteral(new BaseTokenFactory(), ls);

        Token tok = lex.read();

        assertEquals(TokenType.Literal, tok.getType());
        assertTrue(tok.getValue() instanceof Long);
        assertEquals(Long.parseLong("0101", 2), tok.getValue());
        assertEquals(1, tok.getLine());
        assertEquals(1, tok.getCol());
        assertEquals(1, tok.getEndLine());
        assertEquals(7, tok.getEndCol());
    }

    @Test
    public void int_long() throws Exception {
        String str = "987654321L";

        LexerStream ls = new LexerStream(new StringReader(str));
        LexNumericLiteral lex = new LexNumericLiteral(new BaseTokenFactory(), ls);

        Token tok = lex.read();

        assertEquals(TokenType.Literal, tok.getType());
        assertTrue(tok.getValue() instanceof Long);
        assertEquals(987654321L, tok.getValue());
        assertEquals(1, tok.getLine());
        assertEquals(1, tok.getCol());
        assertEquals(1, tok.getEndLine());
        assertEquals(11, tok.getEndCol());
    }

    @Test
    public void ulong() throws Exception {
        String str = "867_5309uL";

        LexerStream ls = new LexerStream(new StringReader(str));
        LexNumericLiteral lex = new LexNumericLiteral(new BaseTokenFactory(), ls);

        Token tok = lex.read();

        assertEquals(TokenType.Literal, tok.getType());
        assertTrue(tok.getValue() instanceof Long);
        assertEquals(8675309L, tok.getValue());
        assertEquals(1, tok.getLine());
        assertEquals(1, tok.getCol());
        assertEquals(1, tok.getEndLine());
        assertEquals(11, tok.getEndCol());
    }

    @Test
    public void float_dot() throws Exception {
        String str = "123_456.567_8.";

        LexerStream ls = new LexerStream(new StringReader(str));
        LexNumericLiteral lex = new LexNumericLiteral(new BaseTokenFactory(), ls);

        Token tok = lex.read();

        assertEquals(TokenType.Literal, tok.getType());
        assertTrue(tok.getValue() instanceof Double);
        assertEquals(123456.5678, tok.getValue());
        assertEquals(1, tok.getLine());
        assertEquals(1, tok.getCol());
        assertEquals(1, tok.getEndLine());
        assertEquals(14, tok.getEndCol());

        // Shouldn't have consumed last dot
        assertEquals('.', ls.read());
    }

    @Test
    public void float_suffix() throws Exception {
        String str = "987654321f";

        LexerStream ls = new LexerStream(new StringReader(str));
        LexNumericLiteral lex = new LexNumericLiteral(new BaseTokenFactory(), ls);

        Token tok = lex.read();

        assertEquals(TokenType.Literal, tok.getType());
        assertTrue(tok.getValue() instanceof Double);
        assertEquals(Double.valueOf("987654321"), tok.getValue());
        assertEquals(1, tok.getLine());
        assertEquals(1, tok.getCol());
        assertEquals(1, tok.getEndLine());
        assertEquals(11, tok.getEndCol());
    }

    @Test
    public void float_exp() throws Exception {
        String str = "6.022E23 10e10";

        LexerStream ls = new LexerStream(new StringReader(str));
        LexNumericLiteral lex = new LexNumericLiteral(new BaseTokenFactory(), ls);

        Token tok = lex.read();

        assertEquals(TokenType.Literal, tok.getType());
        assertTrue(tok.getValue() instanceof Double);
        assertEquals(Double.valueOf("6.022e23"), tok.getValue());
        assertEquals(1, tok.getLine());
        assertEquals(1, tok.getCol());
        assertEquals(1, tok.getEndLine());
        assertEquals(9, tok.getEndCol());

        assertEquals(' ', ls.read());

        tok = lex.read();

        assertEquals(TokenType.Literal, tok.getType());
        assertTrue(tok.getValue() instanceof Double);
        assertEquals(Double.valueOf("10e10"), tok.getValue());
        assertEquals(1, tok.getLine());
        assertEquals(10, tok.getCol());
        assertEquals(1, tok.getEndLine());
        assertEquals(15, tok.getEndCol());
    }

    @Test
    public void float_neg_exp() throws Exception {
        String str = "s\n\n 1_2_3_4_5_6_._5e-6_ blah\n\n";

        LexerStream ls = new LexerStream(new StringReader(str));
        LexNumericLiteral lex = new LexNumericLiteral(new BaseTokenFactory(), ls);

        ls.read();
        ls.read();
        ls.read();
        ls.read();

        Token tok = lex.read();

        assertEquals(TokenType.Literal, tok.getType());
        assertTrue(tok.getValue() instanceof Double);
        assertEquals(Double.valueOf("123456.5e-6"), tok.getValue());
        assertEquals(3, tok.getLine());
        assertEquals(2, tok.getCol());
        assertEquals(3, tok.getEndLine());
        assertEquals(21, tok.getEndCol());
    }

    @Test
    public void imaginary() throws Exception {
        String str = "2i";

        LexerStream ls = new LexerStream(new StringReader(str));
        LexNumericLiteral lex = new LexNumericLiteral(new BaseTokenFactory(), ls);

        Token tok = lex.read();

        assertEquals(TokenType.Literal, tok.getType());
        assertTrue(tok.getValue() instanceof Double);
        assertEquals(Double.valueOf("2"), tok.getValue());
        assertEquals(1, tok.getLine());
        assertEquals(1, tok.getCol());
        assertEquals(1, tok.getEndLine());
        assertEquals(3, tok.getEndCol());
    }

    @Test
    public void hex_float() throws Exception {
        String str = "0x1.beefp3";

        LexerStream ls = new LexerStream(new StringReader(str));
        LexNumericLiteral lex = new LexNumericLiteral(new BaseTokenFactory(), ls);

        Token tok = lex.read();

        assertEquals(TokenType.Literal, tok.getType());
        assertTrue(tok.getValue() instanceof Double);
//        assertEquals(Double.parseDouble("1.beef", 16) * 2*2*2, tok.getValue());
        assertEquals(1, tok.getLine());
        assertEquals(1, tok.getCol());
        assertEquals(1, tok.getEndLine());
        assertEquals(11, tok.getEndCol());
    }

    @Test
    public void max_int() throws Exception {
        String str = String.valueOf(Long.MAX_VALUE) + "0";

        LexerStream ls = new LexerStream(new StringReader(str));
        LexNumericLiteral lex = new LexNumericLiteral(new BaseTokenFactory(), ls);

        Token tok = lex.read();

        assertEquals(TokenType.Literal, tok.getType());
        assertTrue(tok.getValue() instanceof Long);
        assertEquals(Long.MAX_VALUE, tok.getValue());
    }

    @Test(expected = LexerException.class)
    public void empty() throws Exception {
        String str = "";
        LexerStream ls = new LexerStream(new StringReader(str));
        LexNumericLiteral lex = new LexNumericLiteral(new BaseTokenFactory(), ls);

        lex.read();
    }

    @Test(expected = LexerException.class)
    public void not() throws Exception {
        String str = "nine";
        LexerStream ls = new LexerStream(new StringReader(str));
        LexNumericLiteral lex = new LexNumericLiteral(new BaseTokenFactory(), ls);

        lex.read();
    }

}
