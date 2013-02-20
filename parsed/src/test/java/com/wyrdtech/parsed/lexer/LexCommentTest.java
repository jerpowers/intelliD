package com.wyrdtech.parsed.lexer;

import com.wyrdtech.parsed.lexer.token.BaseTokenFactory;
import com.wyrdtech.parsed.lexer.token.Token;
import com.wyrdtech.parsed.lexer.token.TokenType;
import org.junit.Test;

import java.io.StringReader;

import static junit.framework.Assert.assertEquals;

/**
 *
 */
public class LexCommentTest {

    @Test
    public void line() throws Exception {
        String str = "a//bcd\n// //def";
        LexerStream ls = new LexerStream(new StringReader(str));
        LexComment lex = new LexComment(new BaseTokenFactory(), ls);

        ls.read();

        Token tok = lex.read();

        assertEquals(TokenType.LineComment, tok.getType());
        assertEquals("//bcd", tok.getValue());
        assertEquals(1, tok.getLine());
        assertEquals(2, tok.getCol());
        assertEquals(1, tok.getEndLine());
        assertEquals(7, tok.getEndCol());

        ls.read();
        tok = lex.read();

        assertEquals(TokenType.LineComment, tok.getType());
        assertEquals("// //def", tok.getValue());
        assertEquals(2, tok.getLine());
        assertEquals(1, tok.getCol());
        assertEquals(2, tok.getEndLine());
        assertEquals(9, tok.getEndCol());

    }

    @Test
    public void block() throws Exception {
        String str = "a/*f\n *b\n *\n*/\ncde";
        LexerStream ls = new LexerStream(new StringReader(str));
        LexComment lex = new LexComment(new BaseTokenFactory(), ls);

        ls.read();

        Token tok = lex.read();

        assertEquals(TokenType.BlockComment, tok.getType());
        assertEquals("/*f\n" +
                     " *b\n" +
                     " *\n" +
                     "*/", tok.getValue());
        assertEquals(1, tok.getLine());
        assertEquals(2, tok.getCol());
        assertEquals(4, tok.getEndLine());
        assertEquals(3, tok.getEndCol());

    }

    @Test
    public void block_inline() throws Exception {
        String str = "a=/*b*/c";
        LexerStream ls = new LexerStream(new StringReader(str));
        LexComment lex = new LexComment(new BaseTokenFactory(), ls);

        ls.read();
        ls.read();

        Token tok = lex.read();

        assertEquals(TokenType.BlockComment, tok.getType());
        assertEquals("/*b*/", tok.getValue());
        assertEquals(1, tok.getLine());
        assertEquals(3, tok.getCol());
        assertEquals(1, tok.getEndLine());
        assertEquals(8, tok.getEndCol());

    }

    @Test
    public void nest() throws Exception {
        String str = "a/+ /+b\n+/ /+c /+\nd\n +/ e+// +/f;";
        LexerStream ls = new LexerStream(new StringReader(str));
        LexComment lex = new LexComment(new BaseTokenFactory(), ls);

        ls.read();
        Token tok = lex.read();

        assertEquals(TokenType.BlockCommentNest, tok.getType());
        assertEquals("/+ /+b\n" +
                     "+/ /+c /+\n" +
                     "d\n" +
                     " +/ e+// +/", tok.getValue());
        assertEquals(1, tok.getLine());
        assertEquals(2, tok.getCol());
        assertEquals(4, tok.getEndLine());
        assertEquals(12, tok.getEndCol());
    }

    @Test
    public void nest_inline() throws Exception {
        String str = "a = /+ // +/ 1;\n" +
                     "a = /+ \"+/\" +/ 1\";\n" +
                     "a = /+ /* +/ */ 3;";
        LexerStream ls = new LexerStream(new StringReader(str));
        LexComment lex = new LexComment(new BaseTokenFactory(), ls);

        ls.read();
        ls.read();
        ls.read();
        ls.read();

        Token tok = lex.read();

        assertEquals(TokenType.BlockCommentNest, tok.getType());
        assertEquals("/+ // +/", tok.getValue());
        assertEquals(1, tok.getLine());
        assertEquals(5, tok.getCol());
        assertEquals(1, tok.getEndLine());
        assertEquals(13, tok.getEndCol());

        ls.readLine();
        ls.read();
        ls.read();
        ls.read();
        ls.read();

        tok = lex.read();

        assertEquals(TokenType.BlockCommentNest, tok.getType());
        assertEquals("/+ \"+/", tok.getValue());
        assertEquals(2, tok.getLine());
        assertEquals(5, tok.getCol());
        assertEquals(2, tok.getEndLine());
        assertEquals(11, tok.getEndCol());

        ls.readLine();
        ls.read();
        ls.read();
        ls.read();
        ls.read();

        tok = lex.read();

        assertEquals(TokenType.BlockCommentNest, tok.getType());
        assertEquals("/+ /* +/", tok.getValue());
        assertEquals(3, tok.getLine());
        assertEquals(5, tok.getCol());
        assertEquals(3, tok.getEndLine());
        assertEquals(13, tok.getEndCol());
    }

}
