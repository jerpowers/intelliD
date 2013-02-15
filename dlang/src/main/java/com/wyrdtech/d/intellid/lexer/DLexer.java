package com.wyrdtech.d.intellid.lexer;

import com.intellij.lexer.Lexer;
import com.intellij.lexer.LexerPosition;
import com.intellij.psi.tree.IElementType;
import com.intellij.util.text.CharSequenceReader;
import com.wyrdtech.parsed.lexer.LexerException;
import com.wyrdtech.parsed.lexer.LexerStream;
import com.wyrdtech.parsed.lexer.Token;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

/**
 * IntelliJ Lexer for D, wrapper for the ParseD lexer.
 */
public class DLexer extends Lexer {

    private CharSequence buffer;
    private LexerStream in_stream; // buffer wrapper

    private int start_offset;
    private int end_offset;
    private int cur_offset;

    private com.wyrdtech.parsed.lexer.Lexer lexer; // actual lexer
    private com.wyrdtech.parsed.lexer.Token token; // current token


    public DLexer() { }


    @Override
    public void start(final CharSequence buffer, final int startOffset, final int endOffset, final int initialState) {
        this.buffer = buffer;
        this.start_offset = startOffset;
        this.end_offset = endOffset;
        this.cur_offset = start_offset;

        this.in_stream = new LexerStream(new CharSequenceReader(buffer.subSequence(startOffset, endOffset)));
        this.lexer = new com.wyrdtech.parsed.lexer.Lexer(in_stream);

        this.token = null;
    }

    @Override
    public int getState() {
        //TODO: support state, for incremental use
        return 0;
    }

    @Nullable
    @Override
    public IElementType getTokenType() {
        return DTokenType.valueOf(token);
    }

    @Override
    public int getTokenStart() {
        if (token == null) {
            return start_offset;
        }
        return start_offset + token.start_index;
    }

    @Override
    public int getTokenEnd() {
        if (token == null) {
            return start_offset;
        }
        return start_offset + token.end_index;
    }

    @Override
    public void advance() {
        try {
            token = lexer.next();
            cur_offset = start_offset + in_stream.getPosition();
        } catch (IOException e) {
            // TODO: log/throw
        } catch (LexerException e) {
            // TODO: log/throw
        }
    }

    @Override
    public LexerPosition getCurrentPosition() {
        return new Position(cur_offset, getState());
    }

    @Override
    public void restore(final LexerPosition position) {

        this.in_stream = new LexerStream(new CharSequenceReader(buffer.subSequence(position.getOffset(), end_offset)));
        this.lexer = new com.wyrdtech.parsed.lexer.Lexer(in_stream);
    }

    @Override
    public CharSequence getBufferSequence() {
        return this.buffer;
    }

    @Override
    public int getBufferEnd() {
        return this.end_offset;
    }


    // Immutable version, in case it matters
    private static class Position implements LexerPosition {
        private final int offset;
        private final int state;

        public Position(int offset, int state) {
            this.offset = offset;
            this.state = state;
        }

        @Override
        public int getOffset() {
            return offset;
        }

        @Override
        public int getState() {
            return state;
        }
    }
}
