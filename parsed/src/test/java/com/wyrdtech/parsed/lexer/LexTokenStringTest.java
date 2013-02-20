package com.wyrdtech.parsed.lexer;

import com.wyrdtech.parsed.lexer.token.BaseTokenFactory;
import com.wyrdtech.parsed.lexer.token.Token;
import com.wyrdtech.parsed.lexer.token.TokenType;
import junit.framework.Assert;
import org.junit.Test;

import java.io.StringReader;

import static junit.framework.Assert.assertTrue;

/**
 * Tests depend on Lexer for tokenizing contents of the token string.
 *
 */
public class LexTokenStringTest {

    @Test
    public void simple() throws Exception {
        String str = "q{foo}";

        Lexer lexer = new Lexer(new BaseTokenFactory(), new StringReader(str));

        Token tok = lexer.next();

        Assert.assertEquals(TokenType.LiteralUtf8, tok.getType());
        assertTrue(tok.getValue() instanceof String);
        Assert.assertEquals("foo", tok.getValue());
        Assert.assertEquals(1, tok.getLine());
        Assert.assertEquals(1, tok.getCol());
        Assert.assertEquals(1, tok.getEndLine());
        Assert.assertEquals(7, tok.getEndCol());

    }

    @Test
    public void nested() throws Exception {
        String str = "q{/*}*/ q{foo}; }";

        Lexer lexer = new Lexer(new BaseTokenFactory(), new StringReader(str));

        Token tok = lexer.next();

        Assert.assertEquals(TokenType.LiteralUtf8, tok.getType());
        assertTrue(tok.getValue() instanceof String);
//        Assert.assertEquals("/*}*/ q{foo}; ", tok.getValue());
        Assert.assertEquals(1, tok.getLine());
        Assert.assertEquals(1, tok.getCol());
        Assert.assertEquals(1, tok.getEndLine());
        Assert.assertEquals(18, tok.getEndCol());

    }

    @Test(expected = LexerException.class)
    public void empty() throws Exception {
        String str = "";
        LexerStream ls = new LexerStream(new StringReader(str));
        LexTokenString lex = new LexTokenString(new BaseTokenFactory(), ls, null);

        lex.read();
    }

    @Test(expected = LexerException.class)
    public void not() throws Exception {
        String str = "q[]";
        LexerStream ls = new LexerStream(new StringReader(str));
        LexTokenString lex = new LexTokenString(new BaseTokenFactory(), ls, null);

        lex.read();
    }

    @Test(expected = LexerException.class)
    public void also_not() throws Exception {
        String str = "\"q{}\"";
        LexerStream ls = new LexerStream(new StringReader(str));
        LexTokenString lex = new LexTokenString(new BaseTokenFactory(), ls, null);

        lex.read();
    }

    @Test(expected = LexerException.class)
    public void unterminated() throws Exception {
        String str = "q{lalala";
        Lexer lexer = new Lexer(new BaseTokenFactory(), new StringReader(str));

        lexer.next();
    }

}
