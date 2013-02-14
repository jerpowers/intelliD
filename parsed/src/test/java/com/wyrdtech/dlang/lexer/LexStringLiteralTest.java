package com.wyrdtech.dlang.lexer;

import junit.framework.Assert;
import org.junit.Test;

import java.io.StringReader;

import static junit.framework.Assert.assertTrue;

/**
 *
 */
public class LexStringLiteralTest {

    @Test
    public void simple() throws Exception {
        String str = "\"Here is a string\"";

        LexerStream ls = new LexerStream(new StringReader(str));

        Token tok = LexStringLiteral.read(ls);
        Assert.assertEquals(TokenType.LiteralUtf8, tok.type);
        assertTrue(tok.literalValue instanceof String);
        Assert.assertEquals("Here is a string", tok.literalValue);
        Assert.assertEquals(1, tok.line);
        Assert.assertEquals(1, tok.col);
        Assert.assertEquals(1, tok.end_line);
        Assert.assertEquals(19, tok.end_col);

    }


    @Test
    public void escaped() throws Exception {
        String str = "\"\\n\\\"\\tfoo\\\" \\x7c\"";

        LexerStream ls = new LexerStream(new StringReader(str));

        Token tok = LexStringLiteral.read(ls);

        Assert.assertEquals(TokenType.LiteralUtf8, tok.type);
        assertTrue(tok.literalValue instanceof String);
        Assert.assertEquals("\n\"\tfoo\" |", tok.literalValue);
        Assert.assertEquals(1, tok.line);
        Assert.assertEquals(1, tok.col);
        Assert.assertEquals(1, tok.end_line);
        Assert.assertEquals(19, tok.end_col);

    }

    @Test
    public void wysiwyg() throws Exception {
        String str = "r\"a\\b\\nc\"";

        LexerStream ls = new LexerStream(new StringReader(str));

        Token tok = LexStringLiteral.read(ls);

        Assert.assertEquals(TokenType.LiteralUtf8, tok.type);
        assertTrue(tok.literalValue instanceof String);
        Assert.assertEquals("a\\b\\nc", tok.literalValue);
        Assert.assertEquals(1, tok.line);
        Assert.assertEquals(1, tok.col);
        Assert.assertEquals(1, tok.end_line);
        Assert.assertEquals(10, tok.end_col);

    }
}
