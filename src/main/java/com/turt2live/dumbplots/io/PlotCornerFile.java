package com.turt2live.dumbplots.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import com.turt2live.dumbplots.plot.CornerType;
import com.turt2live.dumbplots.plot.PlotCorner;

public class PlotCornerFile {

	public static final byte HEADER_BYTE = 0xF;
	public static final byte FOOTER_BYTE = 0x9;
	public static final int BLOCK_SIZE = 1 + 1 + (8 * CornerType.values().length) + CornerType.values().length; // Header, footer, data, data index

	private File file;
	private FileOutputStream out;
	private FileInputStream in;
	private FileChannel channel;
	private FileMode mode;
	private boolean erase = false;
	private ByteBuffer buffer = ByteBuffer.allocateDirect(BLOCK_SIZE);

	public PlotCornerFile(int rx, int ry, String world, File path) {
		if (!path.exists()) {
			path.mkdirs();
		}
		this.file = new File(path, rx + "." + ry + "." + world + ".corner");
	}

	public void writeCorner(PlotCorner corner) {
		if (mode == null || mode != FileMode.WRITE) {
			throw new IllegalArgumentException();
		}
		// Write
		buffer.clear();
		buffer.position(0);
		buffer.put(HEADER_BYTE);
		for(CornerType c : CornerType.values()) {
			buffer.put(c.data);
			buffer.putLong(corner.getInternalId(c));
		}
		buffer.put(FOOTER_BYTE);
		buffer.flip();
		try {
			channel.position((corner.getX() * BLOCK_SIZE) + corner.getZ());
			channel.write(buffer);
		} catch(IOException e) {
			e.printStackTrace();
		}
	}

	public PlotCorner getCorner(int cx, int cz, String world) {
		int position = (cx * BLOCK_SIZE) + cz;
		PlotCorner corner = new PlotCorner(cx, cz, world);
		try {
			channel.position(position);
			buffer.clear();
			buffer.position(0);
			channel.read(buffer);
			buffer.position(0);
			byte head = buffer.get();
			if (head == HEADER_BYTE) {
				for(int i = 0; i < CornerType.values().length; i++) {
					byte cornerId = buffer.get();
					long id = buffer.getLong();
					corner.setCorner(id, CornerType.fromByte(cornerId));
				}
				byte tail = buffer.get();
				if (tail != FOOTER_BYTE) {
					// TODO: DEBUG WHY THIS HAPPENS
					System.out.println("NO FOOT " + tail + " " + FOOTER_BYTE + " " + buffer.position() + " " + buffer.limit());
					return null;
				}
			} else {
				// TODO: DEBUG WHY THIS HAPPENS
				System.out.println("NO HEAD " + head + " " + HEADER_BYTE + " " + buffer.position() + " " + buffer.limit());
				return null;
			}
		} catch(IOException e) {
			e.printStackTrace();
		}
		return corner;
	}

	public void erase() {
		erase = true;
	}

	public void close() {
		try {
			if (in != null)
				in.close();
			if (out != null)
				out.close();
		} catch(IOException e) {} // Consume error
	}

	public void open(FileMode mode) {
		close();
		try {
			this.mode = mode;
			switch (mode) {
			case OPEN:
				in = new FileInputStream(file);
				channel = in.getChannel();
				break;
			case WRITE:
				out = new FileOutputStream(file, !erase);
				channel = out.getChannel();
				erase = false;
				break;
			}
		} catch(IOException e) {
			e.printStackTrace();
		}
	}

	public long size() {
		return file.length();
	}

}
