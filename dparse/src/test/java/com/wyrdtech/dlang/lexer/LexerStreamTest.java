package com.wyrdtech.dlang.lexer;

import org.junit.Test;

import java.io.Reader;
import java.io.StringReader;

import static junit.framework.Assert.assertEquals;

/**
 *
 */
public class LexerStreamTest {

    @Test
    public void simple() throws Exception {

        Reader in = new StringReader("Something\nwith 2 lines.\n");

        LexerStream ls = new LexerStream(in);

        assertEquals(1, ls.getLine());
        assertEquals(1, ls.getCol());

        int c = ls.read();
        assertEquals('S', (char)c);
        assertEquals(1, ls.getLine());
        assertEquals(2, ls.getCol());

        c = ls.read();
        assertEquals('o', (char)c);
        assertEquals(1, ls.getLine());
        assertEquals(3, ls.getCol());

        c = ls.peek();
        assertEquals('m', (char)c);
        assertEquals(1, ls.getLine());
        assertEquals(3, ls.getCol());

        c = ls.read();
        assertEquals('m', (char)c);
        assertEquals(1, ls.getLine());
        assertEquals(4, ls.getCol());

        ls.read(); // e
        ls.read(); // t
        ls.read(); // h
        ls.read(); // i
        ls.read(); // n
        ls.read(); // g

        c = ls.peek();
        assertEquals('\n', (char)c);
        assertEquals(1, ls.getLine());
        assertEquals(10, ls.getCol());

        c = ls.read();
        assertEquals('\n', (char)c);
        assertEquals(2, ls.getLine());
        assertEquals(1, ls.getCol());

        ls.read(); // w
        ls.read(); // i
        ls.read(); // t
        ls.read(); // h
        ls.read(); //
        ls.read(); // 2
        ls.read(); //
        ls.read(); // l
        ls.read(); // i
        ls.read(); // n
        ls.read(); // e
        ls.read(); // s

        c = ls.read();
        assertEquals('.', (char)c);
        assertEquals(2, ls.getLine());
        assertEquals(14, ls.getCol());

        c = ls.read();
        assertEquals('\n', (char)c);
        assertEquals(3, ls.getLine());
        assertEquals(1, ls.getCol());

        c = ls.read();
        assertEquals(-1, c);
    }

}
