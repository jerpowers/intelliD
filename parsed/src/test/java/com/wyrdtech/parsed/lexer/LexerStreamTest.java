package com.wyrdtech.parsed.lexer;

import org.junit.Test;

import java.io.Reader;
import java.io.StringReader;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;

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

        c = ls.read();
        assertEquals('w', (char)c);
        assertEquals(2, ls.getLine());
        assertEquals(2, ls.getCol());

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

    @Test
    public void peek() throws Exception {

        Reader in = new StringReader("I see you");

        LexerStream ls = new LexerStream(in);

        assertEquals(1, ls.getLine());
        assertEquals(1, ls.getCol());

        int c = ls.peek();
        assertEquals('I', (char)c);
        assertTrue(1 == ls.getLine() && 1 == ls.getCol());

        c = ls.peek(2);
        assertEquals(' ', (char)c);
        assertTrue(1 == ls.getLine() && 1 == ls.getCol());

        c = ls.peek(4);
        assertEquals('e', (char)c);
        assertTrue(1 == ls.getLine() && 1 == ls.getCol());

        c = ls.peek(8);
        assertEquals('o', (char)c);
        assertTrue(1 == ls.getLine() && 1 == ls.getCol());

        c = ls.read();
        assertEquals('I', (char)c);
        assertTrue(1 == ls.getLine() && 2 == ls.getCol());
    }


    @Test
    public void readLine() throws Exception {

        Reader in = new StringReader("line1\nline2\n3line\n\nline5");

        LexerStream ls = new LexerStream(in);

        assertEquals(1, ls.getLine());
        assertEquals(1, ls.getCol());

        assertEquals('l', (char)ls.read());
        assertEquals(1, ls.getLine());
        assertEquals(2, ls.getCol());

        assertEquals("ine1", ls.readLine());
        assertEquals(2, ls.getLine());
        assertEquals(1, ls.getCol());

        assertEquals("line2", ls.readLine());
        assertEquals(3, ls.getLine());
        assertEquals(1, ls.getCol());

        assertEquals('3', (char)ls.read());
        assertEquals(3, ls.getLine());
        assertEquals(2, ls.getCol());

        assertEquals("line", ls.readLine());
        assertEquals(4, ls.getLine());
        assertEquals(1, ls.getCol());

        assertEquals("", ls.readLine());
        assertEquals(5, ls.getLine());
        assertEquals(1, ls.getCol());

        assertEquals("line5", ls.readLine());
        assertEquals(6, ls.getLine());
        assertEquals(1, ls.getCol());

        assertNull(ls.readLine());
        assertEquals(-1, ls.read());
    }
}
