package com.wyrdtech.dlang.lexer;

import junit.framework.Assert;
import org.junit.Test;

import java.io.StringReader;

import static junit.framework.Assert.assertTrue;

/**
 *
 */
public class LexDelimitedStringTest {

    @Test
    public void bracketed() throws Exception {
        String str = "q\"{foo][}f}\"bar";

        LexerStream ls = new LexerStream(new StringReader(str));

        Token tok = LexDelimitedString.read(ls);
        Assert.assertEquals(TokenType.LiteralUtf8, tok.type);
        assertTrue(tok.literalValue instanceof String);
        Assert.assertEquals("foo][}f", tok.literalValue);
        Assert.assertEquals(1, tok.line);
        Assert.assertEquals(1, tok.col);
        Assert.assertEquals(1, tok.end_line);
        Assert.assertEquals(13, tok.end_col);

    }

    @Test
    public void parens() throws Exception {
        String str = "q\"((f/*)*/)\"";

        LexerStream ls = new LexerStream(new StringReader(str));

        Token tok = LexDelimitedString.read(ls);
        Assert.assertEquals(TokenType.LiteralUtf8, tok.type);
        assertTrue(tok.literalValue instanceof String);
        Assert.assertEquals("(f/*)*/", tok.literalValue);
        Assert.assertEquals(1, tok.line);
        Assert.assertEquals(1, tok.col);
        Assert.assertEquals(1, tok.end_line);
        Assert.assertEquals(13, tok.end_col);

    }

    @Test
    public void identifier() throws Exception {
        String str = "q\"END\nsome\nmultiline\nstring\nEND\" ";

        LexerStream ls = new LexerStream(new StringReader(str));

        Token tok = LexDelimitedString.read(ls);
        Assert.assertEquals(TokenType.LiteralUtf8, tok.type);
        assertTrue(tok.literalValue instanceof String);
        Assert.assertEquals("some\nmultiline\nstring\n", tok.literalValue);
        Assert.assertEquals(1, tok.line);
        Assert.assertEquals(1, tok.col);
        Assert.assertEquals(5, tok.end_line);
        Assert.assertEquals(5, tok.end_col);

    }

    @Test(expected = LexerException.class)
    public void empty() throws Exception {
        String str = "";
        LexerStream ls = new LexerStream(new StringReader(str));

        LexDelimitedString.read(ls);
    }

    @Test(expected = LexerException.class)
    public void not() throws Exception {
        String str = "q[]";
        LexerStream ls = new LexerStream(new StringReader(str));

        LexDelimitedString.read(ls);
    }

    @Test(expected = LexerException.class)
    public void also_not() throws Exception {
        String str = "\"q{}\"";
        LexerStream ls = new LexerStream(new StringReader(str));

        LexDelimitedString.read(ls);
    }

    @Test(expected = LexerException.class)
    public void unterminated() throws Exception {
        String str = "q\"{lalala}";
        LexerStream ls = new LexerStream(new StringReader(str));

        LexDelimitedString.read(ls);
    }

}
