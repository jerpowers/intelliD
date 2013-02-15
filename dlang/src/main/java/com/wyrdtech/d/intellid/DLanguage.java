package com.wyrdtech.d.intellid;

import com.intellij.lang.Language;
import com.intellij.openapi.fileTypes.SingleLazyInstanceSyntaxHighlighterFactory;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.fileTypes.SyntaxHighlighterFactory;
import com.wyrdtech.d.intellid.lexer.DHighlighter;
import org.jetbrains.annotations.NotNull;

/**
 *
 */
public class DLanguage extends Language {
    public static final DLanguage INSTANCE = new DLanguage();

    public DLanguage() {
        super("D", "text/x-dsrc");

        SyntaxHighlighterFactory.LANGUAGE_FACTORY.addExplicitExtension(this, new DHighlighterFactory());
    }

    private static class DHighlighterFactory extends SingleLazyInstanceSyntaxHighlighterFactory {
        @NotNull
        @Override
        protected SyntaxHighlighter createHighlighter() {
            return new DHighlighter();
        }
    }
}
