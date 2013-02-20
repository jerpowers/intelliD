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
public class LexCharLiteralTest {

    @Test
    public void chars() throws Exception {
        String str = "'a' '\\n' '' '\\t'";
        LexerStream ls = new LexerStream(new StringReader(str));

        LexCharLiteral lex = new LexCharLiteral(new BaseTokenFactory(), ls);

        Token tok = lex.read();

        assertEquals(TokenType.LiteralChar, tok.getType());
        assertEquals("a", tok.getValue());
        assertEquals(1, tok.getLine());
        assertEquals(1, tok.getCol());
        assertEquals(1, tok.getEndLine());
        assertEquals(4, tok.getEndCol());

        ls.read();
        tok = lex.read();

        assertEquals(TokenType.LiteralChar, tok.getType());
        assertEquals("\n", tok.getValue());
        assertEquals(1, tok.getLine());
        assertEquals(5, tok.getCol());
        assertEquals(1, tok.getEndLine());
        assertEquals(9, tok.getEndCol());

        ls.read();
        tok = lex.read();

        assertEquals(TokenType.LiteralChar, tok.getType());
        assertEquals("''", tok.getValue());
        assertEquals(1, tok.getLine());
        assertEquals(10, tok.getCol());
        assertEquals(1, tok.getEndLine());
        assertEquals(12, tok.getEndCol());

        ls.read();
        tok = lex.read();

        assertEquals(TokenType.LiteralChar, tok.getType());
        assertEquals("\t", tok.getValue());
        assertEquals(1, tok.getLine());
        assertEquals(13, tok.getCol());
        assertEquals(1, tok.getEndLine());
        assertEquals(17, tok.getEndCol());
    }
}
