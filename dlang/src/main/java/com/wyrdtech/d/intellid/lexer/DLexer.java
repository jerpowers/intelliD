package com.wyrdtech.d.intellid.lexer;

import com.intellij.lexer.Lexer;
import com.intellij.lexer.LexerPosition;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.Nullable;

/**
 * Lexer for D, wrapper for the Descent lexer.
 */
public class DLexer extends Lexer {

    private CharSequence buffer; // what is being lexed
    private int endOffset;       // last position in buffer to lex

    private descent.internal.compiler.parser.Lexer lexer; // actual lexer


    public DLexer() { }


    @Override
    public void start(final CharSequence buffer, final int startOffset, final int endOffset, final int initialState) {
        this.buffer = buffer;
        this.endOffset = endOffset;

        //TODO: modify/update Decent to use CharSequence, initial state
        this.lexer = new descent.internal.compiler.parser.Lexer(buffer.toString().toCharArray(),
                                                                startOffset,
                                                                (endOffset - startOffset),
                                                                true,
                                                                true,
                                                                false,
                                                                false,
                                                                descent.internal.compiler.parser.Parser.D2);


    }

    @Override
    public int getState() {
        //TODO: support state, for incremental use
        return 0;
    }

    @Nullable
    @Override
    public IElementType getTokenType() {
        if (lexer.token == null) {
            return null;
        }
        return DTokenType.valueOf(lexer.token);
    }

    @Override
    public int getTokenStart() {
        return lexer.token.ptr;
    }

    @Override
    public int getTokenEnd() {
        return lexer.token.ptr + lexer.token.len;
    }

    @Override
    public void advance() {
        lexer.nextToken();
    }

    @Override
    public LexerPosition getCurrentPosition() {
        return new Position(lexer.p, getState());
    }

    @Override
    public void restore(final LexerPosition position) {
        int new_len = this.endOffset - position.getOffset();
        lexer.reset(position.getOffset(), new_len);
    }

    @Override
    public CharSequence getBufferSequence() {
        return this.buffer;
    }

    @Override
    public int getBufferEnd() {
        return this.endOffset;
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
