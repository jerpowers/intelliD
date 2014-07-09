package com.wyrdtech.d.intellid.lexer;

import com.intellij.psi.tree.IElementType;
import com.wyrdtech.d.intellid.DLanguage;
import com.wyrdtech.parsed.lexer.token.TokenType;

/**
 * An element of the D language, for tokenizing.
 * Each instance represents a type of token (as distinct from an actual
 * tokenized bit of source).  Wraps the ParseD TokenType.
 *
 * TODO: subclass for keyword tokens, ala com.intellij.psi.tree.IKeywordElementType?
 */
public class DElementType extends IElementType {

    private final TokenType type;

    public DElementType(final TokenType type) {
        super(type.name(), DLanguage.INSTANCE);

        this.type = type;
    }

    /**
     * @return ParserD TokenType of this element
     */
    public TokenType getType() {
        return type;
    }

}
