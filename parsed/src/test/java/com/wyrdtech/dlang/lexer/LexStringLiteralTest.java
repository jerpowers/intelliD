package com.wyrdtech.dlang.lexer;

import org.junit.Test;

import java.io.StringReader;

import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.assertEquals;

/**
 *
 */
public class LexStringLiteralTest {

    @Test
    public void simple() throws Exception {
        String str = "\"Here is a string\"";

        LexerStream ls = new LexerStream(new StringReader(str));

        Token tok = LexStringLiteral.read(ls);
        assertEquals(TokenType.LiteralUtf8, tok.type);
        assertTrue(tok.literalValue instanceof String);
        assertEquals("Here is a string", tok.literalValue);
        assertEquals(1, tok.line);
        assertEquals(1, tok.col);
        assertEquals(1, tok.end_line);
        assertEquals(19, tok.end_col);

        assertEquals(-1, ls.read());
    }


    @Test
    public void escaped() throws Exception {
        String str = "\"\\n\\\"\\tfoo\\\" \\x7c\"";

        LexerStream ls = new LexerStream(new StringReader(str));

        Token tok = LexStringLiteral.read(ls);

        assertEquals(TokenType.LiteralUtf8, tok.type);
        assertTrue(tok.literalValue instanceof String);
        assertEquals("\n\"\tfoo\" |", tok.literalValue);
        assertEquals(1, tok.line);
        assertEquals(1, tok.col);
        assertEquals(1, tok.end_line);
        assertEquals(19, tok.end_col);

        assertEquals(-1, ls.read());
    }

    @Test
    public void wysiwyg() throws Exception {
        String str = "r\"a\\b\\nc\"";

        LexerStream ls = new LexerStream(new StringReader(str));

        Token tok = LexStringLiteral.read(ls);

        assertEquals(TokenType.LiteralUtf8, tok.type);
        assertTrue(tok.literalValue instanceof String);
        assertEquals("a\\b\\nc", tok.literalValue);
        assertEquals(1, tok.line);
        assertEquals(1, tok.col);
        assertEquals(1, tok.end_line);
        assertEquals(10, tok.end_col);

        assertEquals(-1, ls.read());
    }

    @Test
    public void tick() throws Exception {
        String str = "`a\\b\\nc`";

        LexerStream ls = new LexerStream(new StringReader(str));

        Token tok = LexStringLiteral.read(ls);

        assertEquals(TokenType.LiteralUtf8, tok.type);
        assertTrue(tok.literalValue instanceof String);
        assertEquals("a\\b\\nc", tok.literalValue);
        assertEquals(1, tok.line);
        assertEquals(1, tok.col);
        assertEquals(1, tok.end_line);
        assertEquals(9, tok.end_col);

        assertEquals(-1, ls.read());
    }

    @Test
    public void explicit_utf8() throws Exception {
        String str = "\"foo\"c";

        LexerStream ls = new LexerStream(new StringReader(str));

        Token tok = LexStringLiteral.read(ls);
        assertEquals(TokenType.LiteralUtf8, tok.type);
        assertTrue(tok.literalValue instanceof String);
        assertEquals("foo", tok.literalValue);
        assertEquals(1, tok.line);
        assertEquals(1, tok.col);
        assertEquals(1, tok.end_line);
        assertEquals(7, tok.end_col);
    }

    @Test
    public void explicit_utf16() throws Exception {
        String str = "\"foo\"w";

        LexerStream ls = new LexerStream(new StringReader(str));

        Token tok = LexStringLiteral.read(ls);
        assertEquals(TokenType.LiteralUtf16, tok.type);
        assertTrue(tok.literalValue instanceof String);
        assertEquals("foo", tok.literalValue);
        assertEquals(1, tok.line);
        assertEquals(1, tok.col);
        assertEquals(1, tok.end_line);
        assertEquals(7, tok.end_col);
    }

    @Test
    public void explicit_utf32() throws Exception {
        String str = "\"foo\"d";

        LexerStream ls = new LexerStream(new StringReader(str));

        Token tok = LexStringLiteral.read(ls);
        assertEquals(TokenType.LiteralUtf32, tok.type);
        assertTrue(tok.literalValue instanceof String);
        assertEquals("foo", tok.literalValue);
        assertEquals(1, tok.line);
        assertEquals(1, tok.col);
        assertEquals(1, tok.end_line);
        assertEquals(7, tok.end_col);
    }


    @Test(expected = LexerException.class)
    public void empty() throws Exception {
        String str = "";
        LexerStream ls = new LexerStream(new StringReader(str));

        LexStringLiteral.read(ls);
    }
}
