package com.turt2live.dumbplots.plot.corner;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class PlotCornerFile {

    public static final byte HEADER_BYTE = (byte) (-6);
    public static final byte FOOTER_BYTE = (byte) (-12);
    public static final int BLOCK_SIZE = 1 + 1 + (8 * CornerType.values().length) + CornerType.values().length; // Header, footer, data, data index

    private File file;
    private FileOutputStream out;
    private FileInputStream in;
    private FileChannel channel;
    private FileMode mode;
    private boolean erase = false;
    private ByteBuffer buffer = ByteBuffer.allocateDirect(BLOCK_SIZE);
    private int maxCoord;

    public PlotCornerFile(int rx, int ry, String world, File path, int max) {
        if (!path.exists()) {
            path.mkdirs();
        }
        this.file = new File(path, rx + "." + ry + "." + world + ".corner");
        this.maxCoord = max;
    }

    public PlotCornerFile(File file, int max) {
        this.file = file;
        this.maxCoord = max;
    }

    private int getFilePosition(int cx, int cz) {
        cx = Math.abs(cx);
        cz = Math.abs(cz);
        return (cx * maxCoord * BLOCK_SIZE) + (cz * BLOCK_SIZE);
    }

    public void writeCorner(PlotCorner corner) {
        if (mode == null || mode != FileMode.WRITE) {
            throw new IllegalArgumentException();
        }

        // Write
        buffer.clear();
        buffer.position(0);
        buffer.put(HEADER_BYTE);
        for (CornerType c : CornerType.values()) {
            buffer.put(c.data);
            buffer.putLong(corner.getId(c));
        }
        buffer.put(FOOTER_BYTE);
        buffer.flip();
        try {
            channel.position(getFilePosition(corner.getX(), corner.getZ()));
            channel.write(buffer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public PlotCorner getCorner(int cx, int cz, String world) {
        int position = getFilePosition(cx, cz);
        PlotCorner corner = new PlotCorner(cx, cz, world);
        try {
            channel.position(position);
            buffer.clear();
            buffer.position(0);
            channel.read(buffer);
            buffer.position(0);
            byte head = buffer.get();
            if (head == HEADER_BYTE) {
                for (int i = 0; i < CornerType.values().length; i++) {
                    byte cornerId = buffer.get();
                    long id = buffer.getLong();
                    corner.setId(id, CornerType.fromByte(cornerId));
                }
                byte tail = buffer.get();
                if (tail != FOOTER_BYTE) {
                    return null;
                }
            }
            // No head = no corner = empty corner
        } catch (IOException e) {
            e.printStackTrace();
        }
        return corner;
    }

    public void erase() {
        erase = true;
    }

    public void close() {
        try {
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }
        } catch (IOException e) {
        } // Consume error
    }

    public void open(FileMode mode) {
        close();
        try {
            this.mode = mode;
            switch (mode) {
                case OPEN:
                    if (!file.exists()) {
                        file.createNewFile();
                    }
                    in = new FileInputStream(file);
                    channel = in.getChannel();
                    break;
                case WRITE:
                    out = new FileOutputStream(file, !erase);
                    channel = out.getChannel();
                    erase = false;
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public long size() {
        return file.length();
    }

}
