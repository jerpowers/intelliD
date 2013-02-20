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
public class LexHexLiteralTest {
    @Test
    public void simple() throws Exception {
        String str = "x\"0A\"";

        StringBuilder sb = new StringBuilder();
        sb.append(Character.toChars(0x0a));
        String expected = sb.toString();

        LexerStream ls = new LexerStream(new StringReader(str));
        LexHexLiteral lex = new LexHexLiteral(new BaseTokenFactory(), ls);

        Token tok = lex.read();
        Assert.assertEquals(TokenType.LiteralHex, tok.getType());
        assertTrue(tok.getValue() instanceof String);
        Assert.assertEquals(expected, tok.getValue());
        Assert.assertEquals(1, tok.getLine());
        Assert.assertEquals(1, tok.getCol());
        Assert.assertEquals(1, tok.getEndLine());
        Assert.assertEquals(6, tok.getEndCol());

    }


    @Test
    public void spaces() throws Exception {
        String str = "x\"00 FbcD  3 2FD 0A  \"";

        StringBuilder sb = new StringBuilder();
        sb.append(Character.toChars(0x00));
        sb.append(Character.toChars(0xfb));
        sb.append(Character.toChars(0xcd));
        sb.append(Character.toChars(0x32));
        sb.append(Character.toChars(0xfd));
        sb.append(Character.toChars(0x0a));
        String expected = sb.toString();

        LexerStream ls = new LexerStream(new StringReader(str));
        LexHexLiteral lex = new LexHexLiteral(new BaseTokenFactory(), ls);

        Token tok = lex.read();

        Assert.assertEquals(TokenType.LiteralHex, tok.getType());
        assertTrue(tok.getValue() instanceof String);
        Assert.assertEquals(expected, tok.getValue());
        Assert.assertEquals(1, tok.getLine());
        Assert.assertEquals(1, tok.getCol());
        Assert.assertEquals(1, tok.getEndLine());
        Assert.assertEquals(23, tok.getEndCol());

    }


    @Test(expected = LexerException.class)
    public void empty() throws Exception {
        String str = "";
        LexerStream ls = new LexerStream(new StringReader(str));
        LexHexLiteral lex = new LexHexLiteral(new BaseTokenFactory(), ls);

        lex.read();
    }

    @Test(expected = LexerException.class)
    public void not() throws Exception {
        String str = "hex string";
        LexerStream ls = new LexerStream(new StringReader(str));
        LexHexLiteral lex = new LexHexLiteral(new BaseTokenFactory(), ls);

        lex.read();
    }

    @Test(expected = LexerException.class)
    public void also_not() throws Exception {
        String str = "x00AB";
        LexerStream ls = new LexerStream(new StringReader(str));
        LexHexLiteral lex = new LexHexLiteral(new BaseTokenFactory(), ls);

        lex.read();
    }

    @Test(expected = LexerException.class)
    public void unterminated() throws Exception {
        String str = "x\"AB CD";
        LexerStream ls = new LexerStream(new StringReader(str));
        LexHexLiteral lex = new LexHexLiteral(new BaseTokenFactory(), ls);

        lex.read();
    }

}
