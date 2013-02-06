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
        if (this.getLineNumber() != line) {
            //TODO: check '\n' instead?
            col = 1;
            line = this.getLineNumber(); // should be line+1
        } else if (c >= 0) {
            col++;
        }
        return c;
    }

    /**
     * Peek at the next character without taking it off the stream.
     * @return Next character in stream
     * @throws IOException
     */
    public int peek() throws IOException {
        this.mark(1);
        int c = super.read();
        this.reset();

        return c;
    }

    @Override
    public int read(char[] cbuf, int off, int len) {
        throw new UnsupportedOperationException("Multi-char read not supported");
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
