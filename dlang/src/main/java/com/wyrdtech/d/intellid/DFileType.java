package com.wyrdtech.d.intellid;

import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.openapi.util.IconLoader;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;

/**
 *
 */
public class DFileType extends LanguageFileType {

    public static final LanguageFileType INSTANCE = new DFileType();

    public static final String DEFAULT_EXTENSION = "d";

    private DFileType() {
        super(DLanguage.INSTANCE);
    }

    @NotNull
    @Override
    public String getName() {
        return "D";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "D files";
    }

    @NotNull
    @Override
    public String getDefaultExtension() {
        return DEFAULT_EXTENSION;
    }

    @Nullable
    @Override
    public Icon getIcon() {
        return IconLoader.getIcon("/icons/d.png");
    }
}
