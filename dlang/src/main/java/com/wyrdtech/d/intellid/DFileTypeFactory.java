package com.wyrdtech.d.intellid;

import com.intellij.openapi.fileTypes.FileTypeConsumer;
import com.intellij.openapi.fileTypes.FileTypeFactory;
import org.jetbrains.annotations.NotNull;

/**
 * FileTypeFactory for D source file type.
 * Consumes ".d" files and sends them to DFileType
 */
public class DFileTypeFactory extends FileTypeFactory {
    @Override
    public void createFileTypes(@NotNull final FileTypeConsumer fileTypeConsumer) {
        fileTypeConsumer.consume(DFileType.INSTANCE, DFileType.DEFAULT_EXTENSION);
    }
/*

    // Check if file is of D src type
    public boolean isMyFileType(VirtualFile file) {
        if (file == null) {
            return false;
        }
        if (file.isDirectory()) {
            return false;
        }

        if (DSrcFileType.DEFAULT_EXTENSION.equals(file.getExtension())) {
            return true;
        }

        // TODO: implement contents inspection?

        return false;
    }
*/

}
