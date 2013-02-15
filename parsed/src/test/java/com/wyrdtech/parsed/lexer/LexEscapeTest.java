package com.wyrdtech.parsed.lexer;

import org.junit.Test;

import java.io.StringReader;

import static junit.framework.TestCase.assertEquals;

/**
 *
 */
public class LexEscapeTest {

    @Test
    public void escape() throws Exception {
        String str = "\\'" +  // \'
                     "\\\"" + // \"
                     "\\?" +  // \?
                     "\\\\" + // \\
                     "\\a" +  // \a
                     "\\b" +  // \b
                     "\\f" +  // \f
                     "\\n" +  // \n
                     "\\r" +  // \r
                     "\\t" +  // \t
                     "\\v" +  // \v
                     "\\0" +  // \0
                     "";

        LexerStream ls = new LexerStream(new StringReader(str));

        assertEquals('\'', (char)LexEscape.read(ls));
        assertEquals('"', (char)LexEscape.read(ls));
        assertEquals('?', (char)LexEscape.read(ls));
        assertEquals('\\', (char)LexEscape.read(ls));
        assertEquals(0x07, LexEscape.read(ls));
        assertEquals('\b', (char)LexEscape.read(ls));
        assertEquals('\f', (char)LexEscape.read(ls));
        assertEquals('\n', (char)LexEscape.read(ls));
        assertEquals('\r', (char)LexEscape.read(ls));
        assertEquals('\t', (char)LexEscape.read(ls));
        assertEquals(0x0B, LexEscape.read(ls));
        assertEquals(0x00, LexEscape.read(ls));
    }


    @Test
    public void oct() throws Exception {
        String str = "\\123 \\37 \\4";

        LexerStream ls = new LexerStream(new StringReader(str));

        assertEquals(83, LexEscape.read(ls));
        ls.read();
        assertEquals(31, LexEscape.read(ls));
        ls.read();
        assertEquals(4, LexEscape.read(ls));
    }

    @Test
    public void hex() throws Exception {
        String str = "\\xA1 \\uF0a5 \\UdeaDbEef";

        LexerStream ls = new LexerStream(new StringReader(str));

        assertEquals(0xa1, LexEscape.read(ls));
        ls.read();
        assertEquals(0xf0a5, LexEscape.read(ls));
        ls.read();
        assertEquals(0xdeadbeef, LexEscape.read(ls));
    }

    @Test
    public void html() throws Exception {
        String str = "\\&gt; \\&yuml; \\&perp;";

        LexerStream ls = new LexerStream(new StringReader(str));

        assertEquals(62, LexEscape.read(ls));
        ls.read();
        assertEquals(255, LexEscape.read(ls));
        ls.read();
        assertEquals(8869, LexEscape.read(ls));
    }

    @Test(expected = LexerException.class)
    public void empty() throws Exception {
        String str = "";
        LexerStream ls = new LexerStream(new StringReader(str));

        LexEscape.read(ls);
    }

    @Test(expected = LexerException.class)
    public void not() throws Exception {
        String str = "escape";
        LexerStream ls = new LexerStream(new StringReader(str));

        LexEscape.read(ls);
    }

    @Test(expected = LexerException.class)
    public void unknown() throws Exception {
        String str = "\\z";
        LexerStream ls = new LexerStream(new StringReader(str));

        LexEscape.read(ls);
    }

}
