package com.wyrdtech.dlang.lexer;

import org.junit.Test;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.assertEquals;

/**
 *
 */
public class LexIdentifierTest {

    //TODO: discover from TokenType definitions rather than having in two places
    private static List<String> keywords = new ArrayList<String>();
    static {
        keywords.add("abstract");
        keywords.add("alias");
        keywords.add("align");
        keywords.add("asm");
        keywords.add("assert");
        keywords.add("auto");
        keywords.add("body");
        keywords.add("bool");
        keywords.add("break");
        keywords.add("byte");
        keywords.add("case");
        keywords.add("cast");
        keywords.add("catch");
        keywords.add("cdouble");
        keywords.add("cent");
        keywords.add("cfloat");
        keywords.add("char");
        keywords.add("class");
        keywords.add("const");
        keywords.add("continue");
        keywords.add("creal");
        keywords.add("dchar");
        keywords.add("debug");
        keywords.add("default");
        keywords.add("delegate");
        keywords.add("delete");
        keywords.add("deprecated");
        keywords.add("do");
        keywords.add("double");
        keywords.add("else");
        keywords.add("enum");
        keywords.add("export");
        keywords.add("extern");
        keywords.add("false");
        keywords.add("final");
        keywords.add("finally");
        keywords.add("float");
        keywords.add("for");
        keywords.add("foreach");
        keywords.add("foreach_reverse");
        keywords.add("function");
        keywords.add("goto");
        keywords.add("idouble");
        keywords.add("if");
        keywords.add("ifloat");
        keywords.add("immutable");
        keywords.add("import");
        keywords.add("in");
        keywords.add("inout");
        keywords.add("int");
        keywords.add("interface");
        keywords.add("invariant");
        keywords.add("ireal");
        keywords.add("is");
        keywords.add("lazy");
        keywords.add("long");
        keywords.add("macro");
        keywords.add("mixin");
        keywords.add("module");
        keywords.add("new");
        keywords.add("nothrow");
        keywords.add("null");
        keywords.add("out");
        keywords.add("override");
        keywords.add("package");
        keywords.add("pragma");
        keywords.add("private");
        keywords.add("protected");
        keywords.add("public");
        keywords.add("pure");
        keywords.add("real");
        keywords.add("ref");
        keywords.add("return");
        keywords.add("scope");
        keywords.add("shared");
        keywords.add("short");
        keywords.add("static");
        keywords.add("struct");
        keywords.add("super");
        keywords.add("switch");
        keywords.add("synchronized");
        keywords.add("template");
        keywords.add("this");
        keywords.add("throw");
        keywords.add("true");
        keywords.add("try");
        keywords.add("typedef");
        keywords.add("typeid");
        keywords.add("typeof");
        keywords.add("ubyte");
        keywords.add("ucent");
        keywords.add("uint");
        keywords.add("ulong");
        keywords.add("union");
        keywords.add("unittest");
        keywords.add("ushort");
        keywords.add("version");
        keywords.add("void");
        keywords.add("volatile");
        keywords.add("wchar");
        keywords.add("while");
        keywords.add("with");
        keywords.add("__FILE__");
        keywords.add("__LINE__");
        keywords.add("__gshared");
        keywords.add("__traits");
        keywords.add("__vector");
        keywords.add("__parameters");
    }


    @Test
    public void keywords() throws Exception {
        StringBuilder words = new StringBuilder();
        for (String word : keywords) {
            words.append(word).append(" ");
        }

        LexerStream ls = new LexerStream(new StringReader(words.toString()));

        for (String word : keywords) {
            Token tok = LexIdentifier.read(ls);

            assertEquals(word, tok.literalValue);
            assertEquals(TokenType.forValue(word), tok.type);

            ls.read(); // following space
        }

        assertEquals(-1, ls.read());
    }


    @Test
    public void identifier() throws Exception {
        String str = "foo _bar a_2";

        LexerStream ls = new LexerStream(new StringReader(str));

        Token tok = LexIdentifier.read(ls);

        assertEquals(TokenType.Identifier, tok.type);
        assertEquals("foo", tok.literalValue);
        assertEquals(1, tok.line);
        assertEquals(1, tok.col);
        assertEquals(1, tok.end_line);
        assertEquals(4, tok.end_col);

        ls.read();
        tok = LexIdentifier.read(ls);

        assertEquals(TokenType.Identifier, tok.type);
        assertEquals("_bar", tok.literalValue);
        assertEquals(1, tok.line);
        assertEquals(5, tok.col);
        assertEquals(1, tok.end_line);
        assertEquals(9, tok.end_col);

        ls.read();
        tok = LexIdentifier.read(ls);

        assertEquals(TokenType.Identifier, tok.type);
        assertEquals("a_2", tok.literalValue);
        assertEquals(1, tok.line);
        assertEquals(10, tok.col);
        assertEquals(1, tok.end_line);
        assertEquals(13, tok.end_col);
    }

    @Test
    public void dotted() throws Exception {
        String str = "foo.bar";

        LexerStream ls = new LexerStream(new StringReader(str));

        Token tok = LexIdentifier.read(ls);

        assertEquals(TokenType.Identifier, tok.type);
        assertEquals("foo", tok.literalValue);
        assertEquals(1, tok.line);
        assertEquals(1, tok.col);
        assertEquals(1, tok.end_line);
        assertEquals(4, tok.end_col);

        ls.read();
        tok = LexIdentifier.read(ls);

        assertEquals(TokenType.Identifier, tok.type);
        assertEquals("bar", tok.literalValue);
        assertEquals(1, tok.line);
        assertEquals(5, tok.col);
        assertEquals(1, tok.end_line);
        assertEquals(8, tok.end_col);

    }

    @Test(expected = LexerException.class)
    public void reserved() throws Exception {
        // Reserved identifier not in keyword list
        String str = "__FOO__";
        LexerStream ls = new LexerStream(new StringReader(str));

        LexIdentifier.read(ls);
    }

    @Test(expected = LexerException.class)
    public void empty() throws Exception {
        String str = "";
        LexerStream ls = new LexerStream(new StringReader(str));

        LexIdentifier.read(ls);
    }

    @Test(expected = LexerException.class)
    public void not() throws Exception {
        String str = "9d";
        LexerStream ls = new LexerStream(new StringReader(str));

        LexIdentifier.read(ls);
    }

}
