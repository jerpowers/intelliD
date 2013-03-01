package com.wyrdtech.parsed.lexer;

import com.wyrdtech.parsed.lexer.token.BaseTokenFactory;
import com.wyrdtech.parsed.lexer.token.Token;
import com.wyrdtech.parsed.lexer.token.TokenType;
import org.junit.Test;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

/**
 *
 */
public class LexerTest {

    @Test
    public void lex() throws Exception {
        // Example from dlang.org
        String code = "" +
        "// Computes average line length for standard input.\n" +
        "import std.stdio;\n" +
        "\n" +
        "void main() {\n" +
        "    ulong lines = 0;\n" +
        "    double sumLength=0;\n" +
        "    foreach (line;stdin.byLine()){\n" +
        "        ++lines;\n" +
        "        sumLength += line.length;\n" +
        "    }\n" +
        "    writeln(\"Average line length: \",\n" +
        "        lines ? sumLength/lines : 0);\n" +
        " }\n" +
        "";

        List<TokenType> expected = new ArrayList<TokenType>();
        expected.add(TokenType.LineComment);
        expected.add(TokenType.Whitespace);
        expected.add(TokenType.Import);
        expected.add(TokenType.Whitespace);
        expected.add(TokenType.Identifier);
        expected.add(TokenType.Dot);
        expected.add(TokenType.Identifier);
        expected.add(TokenType.Semicolon);
        expected.add(TokenType.Whitespace);
        expected.add(TokenType.Void);
        expected.add(TokenType.Whitespace);
        expected.add(TokenType.Identifier);
        expected.add(TokenType.OpenParenthesis);
        expected.add(TokenType.CloseParenthesis);
        expected.add(TokenType.Whitespace);
        expected.add(TokenType.OpenCurlyBrace);
        expected.add(TokenType.Whitespace);
        expected.add(TokenType.Ulong);
        expected.add(TokenType.Whitespace);
        expected.add(TokenType.Identifier);
        expected.add(TokenType.Whitespace);
        expected.add(TokenType.Assign);
        expected.add(TokenType.Whitespace);
        expected.add(TokenType.Literal);
        expected.add(TokenType.Semicolon);
        expected.add(TokenType.Whitespace);
        expected.add(TokenType.Double);
        expected.add(TokenType.Whitespace);
        expected.add(TokenType.Identifier);
        expected.add(TokenType.Assign);
        expected.add(TokenType.Literal);
        expected.add(TokenType.Semicolon);
        expected.add(TokenType.Whitespace);
        expected.add(TokenType.Foreach);
        expected.add(TokenType.Whitespace);
        expected.add(TokenType.OpenParenthesis);
        expected.add(TokenType.Identifier);
        expected.add(TokenType.Semicolon);
        expected.add(TokenType.Identifier);
        expected.add(TokenType.Dot);
        expected.add(TokenType.Identifier);
        expected.add(TokenType.OpenParenthesis);
        expected.add(TokenType.CloseParenthesis);
        expected.add(TokenType.CloseParenthesis);
        expected.add(TokenType.OpenCurlyBrace);
        expected.add(TokenType.Whitespace);
        expected.add(TokenType.Increment);
        expected.add(TokenType.Identifier);
        expected.add(TokenType.Semicolon);
        expected.add(TokenType.Whitespace);
        expected.add(TokenType.Identifier);
        expected.add(TokenType.Whitespace);
        expected.add(TokenType.PlusAssign);
        expected.add(TokenType.Whitespace);
        expected.add(TokenType.Identifier);
        expected.add(TokenType.Dot);
        expected.add(TokenType.Identifier);
        expected.add(TokenType.Semicolon);
        expected.add(TokenType.Whitespace);
        expected.add(TokenType.CloseCurlyBrace);
        expected.add(TokenType.Whitespace);
        expected.add(TokenType.Identifier);
        expected.add(TokenType.OpenParenthesis);
        expected.add(TokenType.LiteralUtf8);
        expected.add(TokenType.Comma);
        expected.add(TokenType.Whitespace);
        expected.add(TokenType.Identifier);
        expected.add(TokenType.Whitespace);
        expected.add(TokenType.Question);
        expected.add(TokenType.Whitespace);
        expected.add(TokenType.Identifier);
        expected.add(TokenType.Div);
        expected.add(TokenType.Identifier);
        expected.add(TokenType.Whitespace);
        expected.add(TokenType.Colon);
        expected.add(TokenType.Whitespace);
        expected.add(TokenType.Literal);
        expected.add(TokenType.CloseParenthesis);
        expected.add(TokenType.Semicolon);
        expected.add(TokenType.Whitespace);
        expected.add(TokenType.CloseCurlyBrace);
        expected.add(TokenType.Whitespace);
        expected.add(TokenType.EOF);

        Lexer lexer = new Lexer(new BaseTokenFactory(), new StringReader(code));

        List<Token> tokens = new ArrayList<Token>();
        Token tok;
        do {
            tok = lexer.next();
            tokens.add(tok);
        } while (tok != null && tok.getType() != TokenType.EOF);

        // There are no tokens after EOF
        assertNull(lexer.next());

        for (int i = 0; i < expected.size(); i++) {
            assertEquals(expected.get(i), tokens.get(i).getType());
        }

        assertEquals("std", tokens.get(4).getValue());
        assertEquals("stdio", tokens.get(6).getValue());
        assertEquals("main", tokens.get(11).getValue());
        assertEquals("lines", tokens.get(19).getValue());
        assertEquals(0L, tokens.get(23).getValue());
        assertEquals("sumLength", tokens.get(28).getValue());
        assertEquals(0L, tokens.get(30).getValue());
        assertEquals("line", tokens.get(36).getValue());
        assertEquals("stdin", tokens.get(38).getValue());
        assertEquals("byLine", tokens.get(40).getValue());
        assertEquals("lines", tokens.get(47).getValue());
        assertEquals("sumLength", tokens.get(50).getValue());
        assertEquals("line", tokens.get(54).getValue());
        assertEquals("length", tokens.get(56).getValue());
        assertEquals("writeln", tokens.get(61).getValue());
        assertEquals("Average line length: ", tokens.get(63).getValue());
        assertEquals("lines", tokens.get(66).getValue());
        assertEquals("sumLength", tokens.get(70).getValue());
        assertEquals("lines", tokens.get(72).getValue());
        assertEquals(0L, tokens.get(76).getValue());

    }

    @Test
    public void error() throws Exception {
        String code = "" +
                      "/* Source with tokenizing errors */\n" +
                      "q\"/abc/def/\"\n" +
//                      "q{ __EOF__ }\n" +
                      "";

        Lexer lexer = new Lexer(new BaseTokenFactory(), new StringReader(code));

        assertEquals(TokenType.BlockComment, lexer.next().getType());
        assertEquals(TokenType.Whitespace, lexer.next().getType());

        assertEquals(TokenType.Unknown, lexer.next().getType());

        // Should be sitting at point right after 'abc/'

        Token tok = lexer.next();
        assertEquals(TokenType.Identifier, tok.getType());
        assertEquals("def", tok.getValue());

        assertEquals(TokenType.Div, lexer.next().getType());

        // Unterminated string
        assertEquals(TokenType.Unknown, lexer.next().getType());

        assertEquals(TokenType.EOF, lexer.next().getType());
        assertNull(lexer.next());

    }

}
