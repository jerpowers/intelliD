package com.wyrdtech.dlang.lexer;


import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Reader;

/**
 * Wrapper for a stream reader, providing context for lexing the contents.
 *
 * All access to the underlying stream during lexing should be done through
 * this, to ensure consistency.
 */
public class LexerStream extends LineNumberReader {

    private int line;
    private int col;

    public LexerStream(Reader in_stream) {
        super(in_stream);
        super.setLineNumber(1);
        this.line = 1;
        this.col = 1;
    }

    /**
     * @return Line count for the next character on the stream, starts at 1.
     */
    public int getLine() {
        return super.getLineNumber();
    }

    /**
     * @return Column for the next character on the stream, starting at 1.
     */
    public int getCol() {
        return this.col;
    }

    /**
     * Pull the next character off the stream and return it.
     * Will keep track of current line and column in the stream.
     * @return Next character in stream
     * @throws IOException
     */
    @Override
    public int read() throws IOException {
        int c = super.read();
        if (super.getLineNumber() != line) {
            //TODO: check '\n' instead?
            col = 1;
            line = super.getLineNumber(); // should be line+1
        } else if (c >= 0) {
            col++;
        }
        return c;
    }

    /**
     * Read the stream up to and including the next line terminator, return
     * read contents (not including line terminator).
     *
     * @return  A String containing the contents of the line, not including
     *          any line termination characters, or null if the end of the
     *          stream has been reached
     *
     * @throws  IOException
     *          If an I/O error occurs
     */
    @Override
    public String readLine() throws IOException {
        String ln = super.readLine();

        line = super.getLineNumber();
        col = 1;

        return ln;
    }

    /**
     * Peek at the next character without taking it off the stream.
     * The same as calling peek(1)
     * @return Next character in stream
     * @throws IOException
     */
    public int peek() throws IOException {
        super.mark(1);
        int c = super.read();
        super.reset();

        return c;
    }

    /**
     * Peek at the Nth next character on the stream, without taking anything
     * off the stream.  If the stream ends at or before the specified location,
     * returns -1.
     * @return Nth character in stream
     * @throws IOException
     */
    public int peek(int ahead) throws IOException {
        super.mark(ahead);

        int c = -1;
        for (int i = 0; i < ahead; i++) {
            c = super.read();
        }
        super.reset();

        return c;
    }

    @Override
    public int read(char[] cbuf, int off, int len) throws IOException {
        int cnt = super.read(cbuf, off, len);
        if (super.getLineNumber() != line) {
            //TODO: check '\n' instead?
            col = 1;
            line = super.getLineNumber(); // should be line+1
        } else if (cnt >= 0) {
            col+=cnt;
        }
        return cnt;
    }

    @Override
    public long skip(long n) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public void mark(int readAheadLimit) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public void reset() {
        throw new UnsupportedOperationException("Not supported");
    }

}
