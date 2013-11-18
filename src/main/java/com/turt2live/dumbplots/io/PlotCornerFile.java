package com.turt2live.dumbplots.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

import com.turt2live.dumbplots.plot.PlotCorner;

public class PlotCornerFile {

	private File file;
	private FileOutputStream out;
	private FileInputStream in;
	private FileChannel channel;
	private FileMode mode;
	private boolean erase = false;

	public PlotCornerFile(File file) {
		this.file = file;
	}

	public void writeCorner(PlotCorner corner) {
		if (mode == null || mode != FileMode.WRITE) {
			throw new IllegalArgumentException();
		}
		// First, calculate total length
		// TODO
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

}
