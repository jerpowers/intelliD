package com.wyrdtech.dlang.lexer;

import org.junit.Test;

import java.io.StringReader;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;

/**
 *
 */
public class LexCommentTest {

    @Test
    public void line() throws Exception {
        String str = "a//bcd\n// //def";
        LexerStream ls = new LexerStream(new StringReader(str));

        ls.read();

        Token tok = LexComment.read(ls);

        assertEquals(TokenType.LineComment, tok.type);
        assertEquals("//bcd", tok.literalValue);
        assertEquals(1, tok.line);
        assertEquals(2, tok.col);
        assertEquals(1, tok.end_line);
        assertEquals(7, tok.end_col);

        ls.read();
        tok = LexComment.read(ls);

        assertEquals(TokenType.LineComment, tok.type);
        assertEquals("// //def", tok.literalValue);
        assertEquals(2, tok.line);
        assertEquals(1, tok.col);
        assertEquals(2, tok.end_line);
        assertEquals(9, tok.end_col);

    }
}
