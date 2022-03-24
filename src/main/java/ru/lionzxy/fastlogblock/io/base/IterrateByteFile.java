package ru.lionzxy.fastlogblock.io.base;

import net.minecraftforge.fml.common.FMLLog;
import ru.lionzxy.fastlogblock.utils.Constants;
import ru.lionzxy.fastlogblock.utils.FileUtils;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;

public abstract class IterrateByteFile {
    protected final File file;
    protected final AtomicBoolean markDirty = new AtomicBoolean(false);
    protected final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();

    public IterrateByteFile(final File file) {
        this.file = file;
    }

    public boolean iterateByFile(final Consumer<ByteBuffer> callback) throws IOException {
        FileUtils.createFileIfNotExist(file);

        try (FileInputStream fis = new FileInputStream(file);
             FileChannel fic = fis.getChannel();
        ) {
            readWriteLock.writeLock().lock();
            try {
                return iterateByByte(fic, callback);
            } finally {
                readWriteLock.writeLock().unlock();
            }
        }
    }

    private boolean iterateByByte(final FileChannel is, final Consumer<ByteBuffer> callback) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(Constants.SIZE_LOGLINE);

        int corrupted = 0;
        while (is.read(buffer) == Constants.SIZE_LOGLINE) {
            buffer.flip();
            if (!checkLineEnd(buffer.get(Constants.SIZE_LOGLINE - 1))) {
                ++corrupted;
            } else {
                callback.accept(buffer);
            }
            buffer.clear();
        }

        if (corrupted > 0) {
            FMLLog.log.warn("Corrupted lines: " + corrupted);
            return false;
        }
        return true;
    }

    public void sync() throws IOException {
        if (markDirty.compareAndSet(false, false)) {
            return;
        }

        FMLLog.log.debug("Sync nickToId map...");

        Files.move(file.toPath(), new File(file.getParent(), file.getName() + ".backup").toPath(),
                StandardCopyOption.REPLACE_EXISTING);

        FileUtils.createFileIfNotExist(file);

        try (final BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(file))) {
            readWriteLock.readLock().lock();
            try {
                writeToFile(bufferedOutputStream);
            } finally {
                readWriteLock.readLock().lock();
            }
        }

        FMLLog.log.debug("Sync finished!");
    }

    protected abstract boolean checkLineEnd(byte endByte);

    protected abstract void writeToFile(OutputStream outputStream) throws IOException;

}
