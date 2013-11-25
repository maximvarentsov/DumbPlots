package com.turt2live.dumbplots.plot.corner;

import org.bukkit.Location;

import com.turt2live.dumbplots.plot.ChunkType;
import com.turt2live.dumbplots.util.DumbUtil;

public enum CornerType {

	A((byte) 0x5),
	B((byte) 0x6),
	C((byte) 0x7),
	D((byte) 0x8),
	UNKNOWN((byte) (-1));

	public final byte data;

	private CornerType(byte data) {
		this.data = data;
	}

	public static CornerType getCornerType(Location location) {
		if (DumbUtil.getChunkType(location.getChunk().getX(), location.getChunk().getZ()) == ChunkType.CORNER) {
			int x = DumbUtil.getXInChunk(location);
			int z = DumbUtil.getZInChunk(location);
			if (x < 8 && z < 8) {
				return A;
			} else if (x >= 8 && z < 8) {
				return B;
			} else if (x >= 8 && z >= 8) {
				return C;
			} else if (x < 8 && z >= 8) {
				return D;
			}
		}
		return UNKNOWN;
	}

	public CornerType opposite() {
		switch (this) {
		case A:
			return C;
		case B:
			return D;
		case C:
			return A;
		case D:
			return B;
		default:
			return UNKNOWN;
		}
	}

	public static CornerType fromByte(byte bite) {
		for(CornerType t : values()) {
			if (t.data == bite) {
				return t;
			}
		}
		return UNKNOWN;
	}

}
