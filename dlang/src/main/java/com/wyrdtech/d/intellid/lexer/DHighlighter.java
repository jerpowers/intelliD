package com.wyrdtech.d.intellid.lexer;

import com.intellij.lexer.Lexer;
import com.intellij.openapi.editor.SyntaxHighlighterColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

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
        TextAttributesKey key = keys.get(tokenType);
        if (key != null) {
            return pack(keys.get(tokenType));
        }
        return EMPTY;
    }



    public static final TextAttributesKey LINE_COMMENT = TextAttributesKey.createTextAttributesKey("LINE_COMMENT",
                                                                                                    SyntaxHighlighterColors.LINE_COMMENT);
    public static final TextAttributesKey BLOCK_COMMENT = TextAttributesKey.createTextAttributesKey("BLOCK_COMMENT",
                                                                                                     SyntaxHighlighterColors.JAVA_BLOCK_COMMENT);
    public static final TextAttributesKey DOC_COMMENT = TextAttributesKey.createTextAttributesKey("DOC_COMMENT",
                                                                                                   SyntaxHighlighterColors.DOC_COMMENT);

    private static final TextAttributesKey[] COMMENTS = new TextAttributesKey[]{LINE_COMMENT,BLOCK_COMMENT,DOC_COMMENT};


    public static final TextAttributesKey STRING_LITERAL = TextAttributesKey.createTextAttributesKey("STRING_LITERAL",
                                                                                                      SyntaxHighlighterColors.STRING);

    public static final TextAttributesKey NUM_LITERAL = TextAttributesKey.createTextAttributesKey("NUM_LITERAL",
                                                                                                     SyntaxHighlighterColors.NUMBER);


    public static final TextAttributesKey OPERATOR = TextAttributesKey.createTextAttributesKey("OPERATOR",
                                                                                                SyntaxHighlighterColors.OPERATION_SIGN);

    public static final TextAttributesKey KEYWORD = TextAttributesKey.createTextAttributesKey("KEYWORD",
                                                                                              SyntaxHighlighterColors.KEYWORD);

    public static final TextAttributesKey PARENS = TextAttributesKey.createTextAttributesKey("PARENS",
                                                                                             SyntaxHighlighterColors.PARENTHS);
    public static final TextAttributesKey BRACES = TextAttributesKey.createTextAttributesKey("BRACES",
                                                                                             SyntaxHighlighterColors.BRACES);
    public static final TextAttributesKey BRACKETS = TextAttributesKey.createTextAttributesKey("BRACKETS",
                                                                                               SyntaxHighlighterColors.BRACKETS);

/*
    public static final TextAttributesKey IDENTIFIER = TextAttributesKey.createTextAttributesKey("IDENTIFIER",
                                                                                                 SyntaxHighlighterColors.KEYWORD);
*/

    private static final Map<IElementType, TextAttributesKey> keys;
    static {
        keys = new HashMap<IElementType, TextAttributesKey>();

        keys.put(DTokenType.LINE_COMMENT, LINE_COMMENT);
        keys.put(DTokenType.BLOCK_COMMENT, BLOCK_COMMENT);
        keys.put(DTokenType.BLOCK_COMMENT_NEST, BLOCK_COMMENT);
        keys.put(DTokenType.DOC_COMMENT, DOC_COMMENT);
        keys.put(DTokenType.DOC_COMMENT_NEST, DOC_COMMENT);
        keys.put(DTokenType.DOC_LINE_COMMENT, DOC_COMMENT);

        keys.put(DTokenType.STRING_LITERAL, STRING_LITERAL);
        keys.put(DTokenType.DSTR_LITERAL, STRING_LITERAL);
        keys.put(DTokenType.WSTR_LITERAL, STRING_LITERAL);
        keys.put(DTokenType.CHAR_LITERAL, STRING_LITERAL);

        keys.put(DTokenType.LITERAL, NUM_LITERAL);

        keys.put(DTokenType.PARENS, PARENS);
        keys.put(DTokenType.BRACE, BRACES);
        keys.put(DTokenType.BRACKET, BRACKETS);

        keys.put(DTokenType.OPERATOR, OPERATOR);
        keys.put(DTokenType.KEYWORD, KEYWORD);
//        keys.put(DTokenType.IDENTIFIER, IDENTIFIER);
    }

}
