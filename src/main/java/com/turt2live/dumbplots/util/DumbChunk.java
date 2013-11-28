package com.turt2live.dumbplots.util;

import org.bukkit.Chunk;
import org.bukkit.World;

import com.turt2live.dumbplots.plot.ChunkLoc;

public class DumbChunk {

	private Conditional conditional;
	private ChunkLoc location;
	private World world;

	public DumbChunk(ChunkLoc location, Conditional conditional, World world) {
		this.location = location;
		this.conditional = conditional;
		this.world = world;
	}

	public ChunkLoc getLocation() {
		return location;
	}

	public Chunk getChunk() {
		return location.getChunk(world);
	}

	public boolean hasCondition() {
		return conditional != Conditional.NO_CONDITION;
	}

	public Conditional getCondition() {
		return conditional;
	}

}
