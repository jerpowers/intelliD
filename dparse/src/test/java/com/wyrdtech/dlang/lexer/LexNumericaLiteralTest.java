package com.wyrdtech.dlang.lexer;

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

        Token tok = LexNumericLiteral.read(ls);

        assertEquals(TokenType.Literal, tok.type);
        assertTrue(tok.literalValue instanceof Long);
        assertEquals(1337L, tok.literalValue);
        assertEquals("1337", tok.literalValue.toString());
        assertEquals(1, tok.line);
        assertEquals(1, tok.col);
        assertEquals(1, tok.end_line);
        assertEquals(7, tok.end_col);

        assertEquals(' ', ls.read());
        assertEquals('w', ls.read());
        ls.read();
        ls.read();
        assertEquals('h', ls.read());
        ls.read();

        tok = LexNumericLiteral.read(ls);

        assertEquals(TokenType.Literal, tok.type);
        assertTrue(tok.literalValue instanceof Long);
        assertEquals(123456L, tok.literalValue);
        assertEquals(1, tok.line);
        assertEquals(13, tok.col);
        assertEquals(1, tok.end_line);
        assertEquals(19, tok.end_col);

        assertEquals(' ', ls.read());
        assertEquals('a', ls.read());
    }

    @Test
    public void integer_dots() throws Exception {
        String str = "9..5";

        LexerStream ls = new LexerStream(new StringReader(str));

        Token tok = LexNumericLiteral.read(ls);

        assertEquals(TokenType.Literal, tok.type);
        assertTrue(tok.literalValue instanceof Long);
        assertEquals(9L, tok.literalValue);
        assertEquals(1, tok.line);
        assertEquals(1, tok.col);
        assertEquals(1, tok.end_line);
        assertEquals(2, tok.end_col);

        assertEquals(TokenType.DoubleDot, tok.next.type);

        tok = LexNumericLiteral.read(ls);

        assertEquals(TokenType.Literal, tok.type);
        assertTrue(tok.literalValue instanceof Long);
        assertEquals(5L, tok.literalValue);
        assertEquals(1, tok.line);
        assertEquals(4, tok.col);
        assertEquals(1, tok.end_line);
        assertEquals(5, tok.end_col);
    }

    @Test
    public void hex_int() throws Exception {
        String str = "f = 0xdeadBeEf_1234;";

        LexerStream ls = new LexerStream(new StringReader(str));

        ls.read();
        ls.read();
        ls.read();
        ls.read();

        Token tok = LexNumericLiteral.read(ls);

        assertEquals(TokenType.Literal, tok.type);
        assertTrue(tok.literalValue instanceof Long);
        assertEquals(Long.parseLong("deadBeEf1234", 16), tok.literalValue);
        assertEquals(1, tok.line);
        assertEquals(5, tok.col);
        assertEquals(1, tok.end_line);
        assertEquals(20, tok.end_col);
    }

    @Test
    public void ulong() throws Exception {
        String str = "867_5309uL";

        LexerStream ls = new LexerStream(new StringReader(str));

        Token tok = LexNumericLiteral.read(ls);

        assertEquals(TokenType.Literal, tok.type);
        assertTrue(tok.literalValue instanceof Long);
        assertEquals(8675309L, tok.literalValue);
        assertEquals(1, tok.line);
        assertEquals(1, tok.col);
        assertEquals(1, tok.end_line);
        assertEquals(11, tok.end_col);
    }

    @Test
    public void float_dot() throws Exception {
        String str = "123_456.567_8.";

        LexerStream ls = new LexerStream(new StringReader(str));

        Token tok = LexNumericLiteral.read(ls);

        assertEquals(TokenType.Literal, tok.type);
        assertTrue(tok.literalValue instanceof Double);
        assertEquals(123456.5678, tok.literalValue);
        assertEquals(1, tok.line);
        assertEquals(1, tok.col);
        assertEquals(1, tok.end_line);
        assertEquals(14, tok.end_col);

        // Shouldn't have consumed last dot
        assertNull(tok.next);
    }

    @Test
    public void float_suffix() throws Exception {
        String str = "987654321f";

        LexerStream ls = new LexerStream(new StringReader(str));

        Token tok = LexNumericLiteral.read(ls);

        assertEquals(TokenType.Literal, tok.type);
        assertTrue(tok.literalValue instanceof Double);
        assertEquals(Double.valueOf("987654321"), tok.literalValue);
        assertEquals(1, tok.line);
        assertEquals(1, tok.col);
        assertEquals(1, tok.end_line);
        assertEquals(11, tok.end_col);
    }

    @Test
    public void float_exp() throws Exception {
        String str = "6.022E23 10e10";

        LexerStream ls = new LexerStream(new StringReader(str));

        Token tok = LexNumericLiteral.read(ls);

        assertEquals(TokenType.Literal, tok.type);
        assertTrue(tok.literalValue instanceof Double);
        assertEquals(Double.valueOf("6.022e23"), tok.literalValue);
        assertEquals(1, tok.line);
        assertEquals(1, tok.col);
        assertEquals(1, tok.end_line);
        assertEquals(9, tok.end_col);

        assertEquals(' ', ls.read());

        tok = LexNumericLiteral.read(ls);

        assertEquals(TokenType.Literal, tok.type);
        assertTrue(tok.literalValue instanceof Double);
        assertEquals(Double.valueOf("10e10"), tok.literalValue);
        assertEquals(1, tok.line);
        assertEquals(10, tok.col);
        assertEquals(1, tok.end_line);
        assertEquals(15, tok.end_col);
    }

    @Test
    public void float_neg_exp() throws Exception {
        String str = "s\n\n 1_2_3_4_5_6_._5e-6_ blah\n\n";

        LexerStream ls = new LexerStream(new StringReader(str));
        ls.read();
        ls.read();
        ls.read();
        ls.read();

        Token tok = LexNumericLiteral.read(ls);

        assertEquals(TokenType.Literal, tok.type);
        assertTrue(tok.literalValue instanceof Double);
        assertEquals(Double.valueOf("123456.5e-6"), tok.literalValue);
        assertEquals(3, tok.line);
        assertEquals(2, tok.col);
        assertEquals(3, tok.end_line);
        assertEquals(21, tok.end_col);
    }

    @Test
    public void hex_float() throws Exception {
        String str = "0x1.beefp3";

        LexerStream ls = new LexerStream(new StringReader(str));

        Token tok = LexNumericLiteral.read(ls);

        assertEquals(TokenType.Literal, tok.type);
        assertTrue(tok.literalValue instanceof Double);
//        assertEquals(Double.parseDouble("1.beef", 16) * 2*2*2, tok.literalValue);
        assertEquals(1, tok.line);
        assertEquals(1, tok.col);
        assertEquals(1, tok.end_line);
        assertEquals(11, tok.end_col);
    }
}
