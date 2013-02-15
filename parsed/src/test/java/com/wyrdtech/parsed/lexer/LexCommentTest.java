package com.wyrdtech.parsed.lexer;

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

    @Test
    public void block() throws Exception {
        String str = "a/*f\n *b\n *\n*/\ncde";
        LexerStream ls = new LexerStream(new StringReader(str));

        ls.read();

        Token tok = LexComment.read(ls);

        assertEquals(TokenType.BlockComment, tok.type);
        assertEquals("/*f\n" +
                     " *b\n" +
                     " *\n" +
                     "*/", tok.literalValue);
        assertEquals(1, tok.line);
        assertEquals(2, tok.col);
        assertEquals(4, tok.end_line);
        assertEquals(3, tok.end_col);

    }

    @Test
    public void block_inline() throws Exception {
        String str = "a=/*b*/c";
        LexerStream ls = new LexerStream(new StringReader(str));

        ls.read();
        ls.read();

        Token tok = LexComment.read(ls);

        assertEquals(TokenType.BlockComment, tok.type);
        assertEquals("/*b*/", tok.literalValue);
        assertEquals(1, tok.line);
        assertEquals(3, tok.col);
        assertEquals(1, tok.end_line);
        assertEquals(8, tok.end_col);

    }

    @Test
    public void nest() throws Exception {
        String str = "a/+ /+b\n+/ /+c /+\nd\n +/ e+// +/f;";
        LexerStream ls = new LexerStream(new StringReader(str));

        ls.read();
        Token tok = LexComment.read(ls);

        assertEquals(TokenType.BlockCommentNest, tok.type);
        assertEquals("/+ /+b\n" +
                     "+/ /+c /+\n" +
                     "d\n" +
                     " +/ e+// +/", tok.literalValue);
        assertEquals(1, tok.line);
        assertEquals(2, tok.col);
        assertEquals(4, tok.end_line);
        assertEquals(12, tok.end_col);
    }

    @Test
    public void nest_inline() throws Exception {
        String str = "a = /+ // +/ 1;\n" +
                     "a = /+ \"+/\" +/ 1\";\n" +
                     "a = /+ /* +/ */ 3;";
        LexerStream ls = new LexerStream(new StringReader(str));

        ls.read();
        ls.read();
        ls.read();
        ls.read();

        Token tok = LexComment.read(ls);

        assertEquals(TokenType.BlockCommentNest, tok.type);
        assertEquals("/+ // +/", tok.literalValue);
        assertEquals(1, tok.line);
        assertEquals(5, tok.col);
        assertEquals(1, tok.end_line);
        assertEquals(13, tok.end_col);

        ls.readLine();
        ls.read();
        ls.read();
        ls.read();
        ls.read();

        tok = LexComment.read(ls);

        assertEquals(TokenType.BlockCommentNest, tok.type);
        assertEquals("/+ \"+/", tok.literalValue);
        assertEquals(2, tok.line);
        assertEquals(5, tok.col);
        assertEquals(2, tok.end_line);
        assertEquals(11, tok.end_col);

        ls.readLine();
        ls.read();
        ls.read();
        ls.read();
        ls.read();

        tok = LexComment.read(ls);

        assertEquals(TokenType.BlockCommentNest, tok.type);
        assertEquals("/+ /* +/", tok.literalValue);
        assertEquals(3, tok.line);
        assertEquals(5, tok.col);
        assertEquals(3, tok.end_line);
        assertEquals(13, tok.end_col);
    }

}
