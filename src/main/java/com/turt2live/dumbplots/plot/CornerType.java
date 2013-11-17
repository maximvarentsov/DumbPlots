package com.turt2live.dumbplots.plot;

import org.bukkit.Location;

import com.turt2live.dumbplots.util.DumbUtil;

public enum CornerType {

	A, B, C, D, UNKNOWN;

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

}
