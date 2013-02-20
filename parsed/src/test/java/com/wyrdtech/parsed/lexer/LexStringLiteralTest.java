package com.wyrdtech.parsed.lexer;

import com.wyrdtech.parsed.lexer.token.BaseTokenFactory;
import com.wyrdtech.parsed.lexer.token.Token;
import com.wyrdtech.parsed.lexer.token.TokenType;
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
        LexStringLiteral lex = new LexStringLiteral(new BaseTokenFactory(), ls);

        Token tok = lex.read();
        assertEquals(TokenType.LiteralUtf8, tok.getType());
        assertTrue(tok.getValue() instanceof String);
        assertEquals("Here is a string", tok.getValue());
        assertEquals(1, tok.getLine());
        assertEquals(1, tok.getCol());
        assertEquals(1, tok.getEndLine());
        assertEquals(19, tok.getEndCol());

        assertEquals(-1, ls.read());
    }


    @Test
    public void escaped() throws Exception {
        String str = "\"\\n\\\"\\tfoo\\\" \\x7c\"";

        LexerStream ls = new LexerStream(new StringReader(str));
        LexStringLiteral lex = new LexStringLiteral(new BaseTokenFactory(), ls);

        Token tok = lex.read();

        assertEquals(TokenType.LiteralUtf8, tok.getType());
        assertTrue(tok.getValue() instanceof String);
        assertEquals("\n\"\tfoo\" |", tok.getValue());
        assertEquals(1, tok.getLine());
        assertEquals(1, tok.getCol());
        assertEquals(1, tok.getEndLine());
        assertEquals(19, tok.getEndCol());

        assertEquals(-1, ls.read());
    }

    @Test
    public void wysiwyg() throws Exception {
        String str = "r\"a\\b\\nc\"";

        LexerStream ls = new LexerStream(new StringReader(str));
        LexStringLiteral lex = new LexStringLiteral(new BaseTokenFactory(), ls);

        Token tok = lex.read();

        assertEquals(TokenType.LiteralUtf8, tok.getType());
        assertTrue(tok.getValue() instanceof String);
        assertEquals("a\\b\\nc", tok.getValue());
        assertEquals(1, tok.getLine());
        assertEquals(1, tok.getCol());
        assertEquals(1, tok.getEndLine());
        assertEquals(10, tok.getEndCol());

        assertEquals(-1, ls.read());
    }

    @Test
    public void tick() throws Exception {
        String str = "`a\\b\\nc`";

        LexerStream ls = new LexerStream(new StringReader(str));
        LexStringLiteral lex = new LexStringLiteral(new BaseTokenFactory(), ls);

        Token tok = lex.read();

        assertEquals(TokenType.LiteralUtf8, tok.getType());
        assertTrue(tok.getValue() instanceof String);
        assertEquals("a\\b\\nc", tok.getValue());
        assertEquals(1, tok.getLine());
        assertEquals(1, tok.getCol());
        assertEquals(1, tok.getEndLine());
        assertEquals(9, tok.getEndCol());

        assertEquals(-1, ls.read());
    }

    @Test
    public void explicit_utf8() throws Exception {
        String str = "\"foo\"c";

        LexerStream ls = new LexerStream(new StringReader(str));
        LexStringLiteral lex = new LexStringLiteral(new BaseTokenFactory(), ls);

        Token tok = lex.read();
        assertEquals(TokenType.LiteralUtf8, tok.getType());
        assertTrue(tok.getValue() instanceof String);
        assertEquals("foo", tok.getValue());
        assertEquals(1, tok.getLine());
        assertEquals(1, tok.getCol());
        assertEquals(1, tok.getEndLine());
        assertEquals(7, tok.getEndCol());
    }

    @Test
    public void explicit_utf16() throws Exception {
        String str = "\"foo\"w";

        LexerStream ls = new LexerStream(new StringReader(str));
        LexStringLiteral lex = new LexStringLiteral(new BaseTokenFactory(), ls);

        Token tok = lex.read();
        assertEquals(TokenType.LiteralUtf16, tok.getType());
        assertTrue(tok.getValue() instanceof String);
        assertEquals("foo", tok.getValue());
        assertEquals(1, tok.getLine());
        assertEquals(1, tok.getCol());
        assertEquals(1, tok.getEndLine());
        assertEquals(7, tok.getEndCol());
    }

    @Test
    public void explicit_utf32() throws Exception {
        String str = "\"foo\"d";

        LexerStream ls = new LexerStream(new StringReader(str));
        LexStringLiteral lex = new LexStringLiteral(new BaseTokenFactory(), ls);

        Token tok = lex.read();
        assertEquals(TokenType.LiteralUtf32, tok.getType());
        assertTrue(tok.getValue() instanceof String);
        assertEquals("foo", tok.getValue());
        assertEquals(1, tok.getLine());
        assertEquals(1, tok.getCol());
        assertEquals(1, tok.getEndLine());
        assertEquals(7, tok.getEndCol());
    }


    @Test(expected = LexerException.class)
    public void empty() throws Exception {
        String str = "";
        LexerStream ls = new LexerStream(new StringReader(str));
        LexStringLiteral lex = new LexStringLiteral(new BaseTokenFactory(), ls);

        lex.read();
    }
}
