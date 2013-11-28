package com.turt2live.dumbplots.plot;

import org.bukkit.Location;

import com.turt2live.dumbplots.util.DumbUtil;

public enum LinearSide {
	LEFT,
	RIGHT,
	UNKNOWN;

	public static LinearSide getLinearSide(Location location) {
		if (DumbUtil.getChunkType(location.getChunk().getX(), location.getChunk().getZ()) == ChunkType.LINEAR_X) {
			int x = DumbUtil.getXInChunk(location);
			if (x < 8) {
				return LEFT;
			} else if (x >= 8) {
				return RIGHT;
			}
		} else if (DumbUtil.getChunkType(location.getChunk().getX(), location.getChunk().getZ()) == ChunkType.LINEAR_Z) {
			int z = DumbUtil.getZInChunk(location);
			if (z < 8) {
				return LEFT;
			} else if (z >= 8) {
				return RIGHT;
			}
		}
		return UNKNOWN;
	}

	public LinearSide opposite() {
		switch (this) {
		case LEFT:
			return RIGHT;
		case RIGHT:
			return LEFT;
		default:
			return UNKNOWN;
		}
	}
}
