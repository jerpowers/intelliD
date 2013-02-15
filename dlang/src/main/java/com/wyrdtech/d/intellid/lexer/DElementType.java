package com.wyrdtech.d.intellid.lexer;

import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import com.wyrdtech.d.intellid.DLanguage;

/**
 * An element of the D language, for tokenizing.
 *
 * TODO: subclass for keyword tokens, ala com.intellij.psi.tree.IKeywordElementType?
 */
public class DElementType extends IElementType {
    public DElementType(@NotNull @NonNls final String debugName)
    {
        super(debugName, DLanguage.INSTANCE);
    }

}
