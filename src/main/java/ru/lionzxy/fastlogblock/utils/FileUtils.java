package ru.lionzxy.fastlogblock.utils;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;

public class FileUtils {
    public static final int BYTEBUFFERSIZE = 2048;

    public static void createFileIfNotExist(final File file) throws IOException {
        if (!file.exists()) {
            if (file.getParentFile() != null && !file.getParentFile().exists() && !file.getParentFile().mkdirs()) {
                throw new IOException("Can't create dir to file " + file.getParentFile().getAbsolutePath());
            }
            if (!file.createNewFile()) {
                throw new IOException("Can't create file " + file.getAbsolutePath());
            }
        }
    }

    public static void closeQuietly(final Closeable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (final IOException ioe) {
            // ignore
        }
    }
}
