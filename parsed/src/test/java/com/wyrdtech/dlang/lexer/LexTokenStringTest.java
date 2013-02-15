package com.wyrdtech.dlang.lexer;

import junit.framework.Assert;
import org.junit.Test;

import java.io.StringReader;

import static junit.framework.Assert.assertTrue;

/**
 *
 */
public class LexTokenStringTest {

    @Test
    public void simple() throws Exception {
        String str = "q{foo}";

        LexerStream ls = new LexerStream(new StringReader(str));

        Token tok = LexTokenString.read(ls);
        Assert.assertEquals(TokenType.LiteralUtf8, tok.type);
        assertTrue(tok.literalValue instanceof String);
        Assert.assertEquals("foo", tok.literalValue);
        Assert.assertEquals(1, tok.line);
        Assert.assertEquals(1, tok.col);
        Assert.assertEquals(1, tok.end_line);
        Assert.assertEquals(7, tok.end_col);

    }

    @Test
    public void nested() throws Exception {
        String str = "q{/*}*/ q{foo}; }";

        LexerStream ls = new LexerStream(new StringReader(str));

        Token tok = LexTokenString.read(ls);
        Assert.assertEquals(TokenType.LiteralUtf8, tok.type);
        assertTrue(tok.literalValue instanceof String);
//        Assert.assertEquals("/*}*/ q{foo}; ", tok.literalValue);
        Assert.assertEquals(1, tok.line);
        Assert.assertEquals(1, tok.col);
        Assert.assertEquals(1, tok.end_line);
        Assert.assertEquals(18, tok.end_col);

    }

    @Test(expected = LexerException.class)
    public void empty() throws Exception {
        String str = "";
        LexerStream ls = new LexerStream(new StringReader(str));

        LexTokenString.read(ls);
    }

    @Test(expected = LexerException.class)
    public void not() throws Exception {
        String str = "q[]";
        LexerStream ls = new LexerStream(new StringReader(str));

        LexTokenString.read(ls);
    }

    @Test(expected = LexerException.class)
    public void also_not() throws Exception {
        String str = "\"q{}\"";
        LexerStream ls = new LexerStream(new StringReader(str));

        LexTokenString.read(ls);
    }

    @Test(expected = LexerException.class)
    public void unterminated() throws Exception {
        String str = "q{lalala";
        LexerStream ls = new LexerStream(new StringReader(str));

        LexTokenString.read(ls);
    }

}
