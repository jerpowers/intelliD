package com.wyrdtech.parsed.lexer;

import org.junit.Test;

import java.io.StringReader;

import static junit.framework.Assert.assertEquals;

/**
 *
 */
public class LexCharLiteralTest {

    @Test
    public void chars() throws Exception {
        String str = "'a' '\\n' '' '\\t'";
        LexerStream ls = new LexerStream(new StringReader(str));

        Token tok = LexCharLiteral.read(ls);

        assertEquals(TokenType.LiteralChar, tok.type);
        assertEquals("a", tok.literalValue);
        assertEquals(1, tok.line);
        assertEquals(1, tok.col);
        assertEquals(1, tok.end_line);
        assertEquals(4, tok.end_col);

        ls.read();
        tok = LexCharLiteral.read(ls);

        assertEquals(TokenType.LiteralChar, tok.type);
        assertEquals("\n", tok.literalValue);
        assertEquals(1, tok.line);
        assertEquals(5, tok.col);
        assertEquals(1, tok.end_line);
        assertEquals(9, tok.end_col);

        ls.read();
        tok = LexCharLiteral.read(ls);

        assertEquals(TokenType.LiteralChar, tok.type);
        assertEquals("''", tok.literalValue);
        assertEquals(1, tok.line);
        assertEquals(10, tok.col);
        assertEquals(1, tok.end_line);
        assertEquals(12, tok.end_col);

        ls.read();
        tok = LexCharLiteral.read(ls);

        assertEquals(TokenType.LiteralChar, tok.type);
        assertEquals("\t", tok.literalValue);
        assertEquals(1, tok.line);
        assertEquals(13, tok.col);
        assertEquals(1, tok.end_line);
        assertEquals(17, tok.end_col);
    }
}
