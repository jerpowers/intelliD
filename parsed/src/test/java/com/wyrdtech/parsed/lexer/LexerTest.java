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
        "    double sumLength = 0;\n" +
        "    foreach (line; stdin.byLine()) {\n" +
        "        ++lines;\n" +
        "        sumLength += line.length;\n" +
        "    }\n" +
        "    writeln(\"Average line length: \",\n" +
        "        lines ? sumLength / lines : 0);\n" +
        " }\n" +
        "";

        List<TokenType> expected = new ArrayList<TokenType>();
        expected.add(TokenType.LineComment);
        expected.add(TokenType.Import);
        expected.add(TokenType.Identifier);
        expected.add(TokenType.Dot);
        expected.add(TokenType.Identifier);
        expected.add(TokenType.Semicolon);
        expected.add(TokenType.Void);
        expected.add(TokenType.Identifier);
        expected.add(TokenType.OpenParenthesis);
        expected.add(TokenType.CloseParenthesis);
        expected.add(TokenType.OpenCurlyBrace);
        expected.add(TokenType.Ulong);
        expected.add(TokenType.Identifier);
        expected.add(TokenType.Assign);
        expected.add(TokenType.Literal);
        expected.add(TokenType.Semicolon);
        expected.add(TokenType.Double);
        expected.add(TokenType.Identifier);
        expected.add(TokenType.Assign);
        expected.add(TokenType.Literal);
        expected.add(TokenType.Semicolon);
        expected.add(TokenType.Foreach);
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
        expected.add(TokenType.Increment);
        expected.add(TokenType.Identifier);
        expected.add(TokenType.Semicolon);
        expected.add(TokenType.Identifier);
        expected.add(TokenType.PlusAssign);
        expected.add(TokenType.Identifier);
        expected.add(TokenType.Dot);
        expected.add(TokenType.Identifier);
        expected.add(TokenType.Semicolon);
        expected.add(TokenType.CloseCurlyBrace);
        expected.add(TokenType.Identifier);
        expected.add(TokenType.OpenParenthesis);
        expected.add(TokenType.LiteralUtf8);
        expected.add(TokenType.Comma);
        expected.add(TokenType.Identifier);
        expected.add(TokenType.Question);
        expected.add(TokenType.Identifier);
        expected.add(TokenType.Div);
        expected.add(TokenType.Identifier);
        expected.add(TokenType.Colon);
        expected.add(TokenType.Literal);
        expected.add(TokenType.CloseParenthesis);
        expected.add(TokenType.Semicolon);
        expected.add(TokenType.CloseCurlyBrace);
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

        assertEquals("std", tokens.get(2).getValue());
        assertEquals("stdio", tokens.get(4).getValue());
        assertEquals("main", tokens.get(7).getValue());
        assertEquals("lines", tokens.get(12).getValue());
        assertEquals(0L, tokens.get(14).getValue());
        assertEquals("sumLength", tokens.get(17).getValue());
        assertEquals(0L, tokens.get(19).getValue());
        assertEquals("line", tokens.get(23).getValue());
        assertEquals("stdin", tokens.get(25).getValue());
        assertEquals("byLine", tokens.get(27).getValue());
        assertEquals("lines", tokens.get(33).getValue());
        assertEquals("sumLength", tokens.get(35).getValue());
        assertEquals("line", tokens.get(37).getValue());
        assertEquals("length", tokens.get(39).getValue());
        assertEquals("writeln", tokens.get(42).getValue());
        assertEquals("Average line length: ", tokens.get(44).getValue());
        assertEquals("lines", tokens.get(46).getValue());
        assertEquals("sumLength", tokens.get(48).getValue());
        assertEquals("lines", tokens.get(50).getValue());
        assertEquals(0L, tokens.get(52).getValue());


    }
}
