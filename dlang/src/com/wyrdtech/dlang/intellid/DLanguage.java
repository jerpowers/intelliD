package com.wyrdtech.dlang.intellid;

import com.intellij.lang.Language;

/**
 *
 */
public class DLanguage extends Language {
    public static final DLanguage INSTANCE = new DLanguage();

    public DLanguage() {
        super("D", "text/x-dsrc");


    }
}
