package com.wyrdtech.dlang.intellid.com.wyrdtech.dlang.intellid.lexer;

import com.intellij.lexer.Lexer;
import com.intellij.lexer.LexerPosition;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.Nullable;

/**
 *
 */
public class DLexer extends Lexer {
    @Override
    public void start(final CharSequence charSequence,
                      final int i,
                      final int i2,
                      final int i3)
    {
    }

    @Override
    public int getState() {
        return 0;
    }

    @Nullable
    @Override
    public IElementType getTokenType() {
        return null;
    }

    @Override
    public int getTokenStart() {
        return 0;
    }

    @Override
    public int getTokenEnd() {
        return 0;
    }

    @Override
    public void advance() {
    }

    @Override
    public LexerPosition getCurrentPosition() {
        return null;
    }

    @Override
    public void restore(final LexerPosition lexerPosition) {
    }

    @Override
    public CharSequence getBufferSequence() {
        return null;
    }

    @Override
    public int getBufferEnd() {
        return 0;
    }
}
