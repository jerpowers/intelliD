package com.wyrdtech.dlang.lexer;

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

        Token tok = LexHexLiteral.read(ls);
        Assert.assertEquals(TokenType.LiteralHex, tok.type);
        assertTrue(tok.literalValue instanceof String);
        Assert.assertEquals(expected, tok.literalValue);
        Assert.assertEquals(1, tok.line);
        Assert.assertEquals(1, tok.col);
        Assert.assertEquals(1, tok.end_line);
        Assert.assertEquals(6, tok.end_col);

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

        Token tok = LexHexLiteral.read(ls);

        Assert.assertEquals(TokenType.LiteralHex, tok.type);
        assertTrue(tok.literalValue instanceof String);
        Assert.assertEquals(expected, tok.literalValue);
        Assert.assertEquals(1, tok.line);
        Assert.assertEquals(1, tok.col);
        Assert.assertEquals(1, tok.end_line);
        Assert.assertEquals(23, tok.end_col);

    }
}
