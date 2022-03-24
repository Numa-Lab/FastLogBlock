package ru.lionzxy.fastlogblock.io.log;

import ru.lionzxy.fastlogblock.io.mappers.BlockMapper;
import ru.lionzxy.fastlogblock.io.mappers.NickMapper;
import ru.lionzxy.fastlogblock.models.BlockChangeEventModel;
import ru.lionzxy.fastlogblock.utils.Constants;
import ru.lionzxy.fastlogblock.utils.FileUtils;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class LogWritter implements Closeable {
    private final BlockMapper blockMapper;
    private final NickMapper nickMapper;
    private final File file;
    private FileOutputStream os;
    private FileChannel fc;

    public LogWritter(final File file, final BlockMapper blockMapper, final NickMapper nickMapper) throws IOException {
        this.file = file;
        this.blockMapper = blockMapper;
        this.nickMapper = nickMapper;

        init();
    }

    private void init() throws IOException {
        FileUtils.createFileIfNotExist(file);

        os = new FileOutputStream(file, true);
        fc = os.getChannel();
        long size = fc.size();
        long pad = size % Constants.SIZE_LOGLINE;
        if (pad != 0) {
            fc.truncate(size - pad);
            fc.position(size - pad);
        }
    }

    /**
     * Name	posX posY posZ typeaction playerid blockid timestamp
     *
     * @param blockChangeEventModel
     */
    public void putEvent(final BlockChangeEventModel blockChangeEventModel) {
        final ByteBuffer byteBuffer = ByteBuffer.allocate(Constants.SIZE_LOGLINE);
        byteBuffer.putInt(blockChangeEventModel.getPosX());
        byteBuffer.putInt(blockChangeEventModel.getPosY());
        byteBuffer.putInt(blockChangeEventModel.getPosZ());
        byteBuffer.put(blockChangeEventModel.getBlockChangeType().getTypeId());
        byteBuffer.putInt(nickMapper.getOrPutUser(blockChangeEventModel.getPlayernick()));
        byteBuffer.putLong(blockMapper.getOrPutBlock(blockChangeEventModel.getNameblock()));
        byteBuffer.putLong(blockChangeEventModel.getTimestamp().getTime());
        byteBuffer.put(Constants.DEVIDER_SYMBOL);

        try {
            fc.write(byteBuffer);
        } catch (final IOException e) {
            try {
                sync();
                fc.write(byteBuffer);
            } catch (final IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }

    public void sync() throws IOException {
        try {
            fc.force(false);
        } catch (final IOException e) {
            FileUtils.closeQuietly(fc);
            init();
        }
    }

    @Override
    public void close() throws IOException {
        fc.close();
        os.close();
    }
}
