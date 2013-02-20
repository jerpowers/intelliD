package com.wyrdtech.parsed.lexer;

import com.wyrdtech.parsed.lexer.token.BaseTokenFactory;
import com.wyrdtech.parsed.lexer.token.Token;
import com.wyrdtech.parsed.lexer.token.TokenType;
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
        LexDelimitedString lex = new LexDelimitedString(new BaseTokenFactory(), ls);

        Token tok = lex.read();
        Assert.assertEquals(TokenType.LiteralUtf8, tok.getType());
        assertTrue(tok.getValue() instanceof String);
        Assert.assertEquals("foo][}f", tok.getValue());
        Assert.assertEquals(1, tok.getLine());
        Assert.assertEquals(1, tok.getCol());
        Assert.assertEquals(1, tok.getEndLine());
        Assert.assertEquals(13, tok.getEndCol());

    }

    @Test
    public void parens() throws Exception {
        String str = "q\"((f/*)*/)\"";

        LexerStream ls = new LexerStream(new StringReader(str));
        LexDelimitedString lex = new LexDelimitedString(new BaseTokenFactory(), ls);

        Token tok = lex.read();
        Assert.assertEquals(TokenType.LiteralUtf8, tok.getType());
        assertTrue(tok.getValue() instanceof String);
        Assert.assertEquals("(f/*)*/", tok.getValue());
        Assert.assertEquals(1, tok.getLine());
        Assert.assertEquals(1, tok.getCol());
        Assert.assertEquals(1, tok.getEndLine());
        Assert.assertEquals(13, tok.getEndCol());

    }

    @Test
    public void identifier() throws Exception {
        String str = "q\"END\nsome\nmultiline\nstring\nEND\" ";

        LexerStream ls = new LexerStream(new StringReader(str));
        LexDelimitedString lex = new LexDelimitedString(new BaseTokenFactory(), ls);

        Token tok = lex.read();
        Assert.assertEquals(TokenType.LiteralUtf8, tok.getType());
        assertTrue(tok.getValue() instanceof String);
        Assert.assertEquals("some\nmultiline\nstring\n", tok.getValue());
        Assert.assertEquals(1, tok.getLine());
        Assert.assertEquals(1, tok.getCol());
        Assert.assertEquals(5, tok.getEndLine());
        Assert.assertEquals(5, tok.getEndCol());

    }

    @Test(expected = LexerException.class)
    public void empty() throws Exception {
        String str = "";
        LexerStream ls = new LexerStream(new StringReader(str));
        LexDelimitedString lex = new LexDelimitedString(new BaseTokenFactory(), ls);

        lex.read();
    }

    @Test(expected = LexerException.class)
    public void not() throws Exception {
        String str = "q[]";
        LexerStream ls = new LexerStream(new StringReader(str));
        LexDelimitedString lex = new LexDelimitedString(new BaseTokenFactory(), ls);

        lex.read();
    }

    @Test(expected = LexerException.class)
    public void also_not() throws Exception {
        String str = "\"q{}\"";
        LexerStream ls = new LexerStream(new StringReader(str));
        LexDelimitedString lex = new LexDelimitedString(new BaseTokenFactory(), ls);

        lex.read();
    }

    @Test(expected = LexerException.class)
    public void unterminated() throws Exception {
        String str = "q\"{lalala}";
        LexerStream ls = new LexerStream(new StringReader(str));
        LexDelimitedString lex = new LexDelimitedString(new BaseTokenFactory(), ls);

        lex.read();
    }

}
