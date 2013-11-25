package com.turt2live.dumbplots.plot;

import org.bukkit.Chunk;
import org.bukkit.World;

public class ChunkLoc {

	private int x, z;

	public ChunkLoc(int x, int z) {
		this.x = x;
		this.z = z;
	}

	public ChunkLoc(String location) {
		String[] parts = location.split(",");
		try {
			x = Integer.parseInt(parts[0]);
			z = Integer.parseInt(parts[1]);
		} catch(NumberFormatException e) {
			x = 0;
			z = 0;
		} catch(IndexOutOfBoundsException e) {
			x = 0;
			z = 0;
		}
	}

	public ChunkLoc() {
		this(0, 0);
	}

	public ChunkLoc(Chunk chunk) {
		this(chunk.getX(), chunk.getZ());
	}

	public int getX() {
		return x;
	}

	public int getZ() {
		return z;
	}

	public void setX(int x) {
		this.x = x;
	}

	public void setZ(int z) {
		this.z = z;
	}

	public Chunk getChunk(World world) {
		return world.getChunkAt(x, z);
	}

	@Override
	public String toString() {
		return x + "," + z;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + x;
		result = prime * result + z;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		ChunkLoc other = (ChunkLoc) obj;
		if (x != other.x) {
			return false;
		}
		if (z != other.z) {
			return false;
		}
		return true;
	}

}
