package com.wyrdtech.d.intellid.lexer;

import com.intellij.lexer.Lexer;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;

/**
 * User: Elendel
 * Date: 12/7/12
 * Time: 4:58 PM
 */
public class DHighlighter extends SyntaxHighlighterBase {
    @NotNull
    @Override
    public Lexer getHighlightingLexer() {
        return new com.wyrdtech.d.intellid.lexer.DHighlightingLexer();
    }

    @NotNull
    @Override
    public TextAttributesKey[] getTokenHighlights(final IElementType tokenType) {
        return new TextAttributesKey[0];  //To change body of implemented methods use File | Settings | File Templates.
    }
}
