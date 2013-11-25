package com.turt2live.dumbplots.util;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.block.Block;

import com.turt2live.dumbplots.DumbPlots;
import com.turt2live.dumbplots.plot.ChunkLoc;
import com.turt2live.dumbplots.plot.ChunkType;
import com.turt2live.dumbplots.plot.LinearSide;
import com.turt2live.dumbplots.plot.Plot;
import com.turt2live.dumbplots.plot.corner.CornerType;
import com.turt2live.dumbplots.plot.corner.PlotCorner;

public class ResetPlot {

	// TODO: wat
	public static void reset(Plot plot) {
		for(ChunkLoc chunkl : plot.getChunks()) {
			ChunkType ctype = DumbUtil.getChunkType(chunkl);
			Chunk chunk = chunkl.getChunk(plot.getWorld());
			Conditional condition = null;
			DumbPlots plugin = DumbPlots.getInstance();
			switch (ctype) {
			case FLAT:
				condition = Conditional.NO_CONDITION;
				break;
			case LINEAR_X:
				int cz = chunkl.getZ();
				int t = 0;
				while(DumbUtil.getChunkType(chunkl.getX(), cz) != ChunkType.CORNER) {
					cz--;
					t++;
					if (t > 10) {
						break;
					}
				}
				PlotCorner plotCorner = plugin.getCornerManager().getCorner(chunkl.getX(), cz, plot.getWorld().getName());
				CornerType corner = CornerType.UNKNOWN;
				for(CornerType c : CornerType.values()) {
					if (plotCorner.getId(c) > 0 && plotCorner.getId(c) == plot.getId()) {
						corner = c;
						break;
					}
				}
				LinearSide side = (corner == CornerType.A || corner == CornerType.D) ? LinearSide.LEFT : LinearSide.RIGHT;
				condition = new Conditional(side);
				break;
			case LINEAR_Z:
				int cx = chunkl.getX();
				t = 0;
				while(DumbUtil.getChunkType(cx, chunkl.getZ()) != ChunkType.CORNER) {
					cx--;
					t++;
					if (t > 10) {
						break;
					}
				}
				plotCorner = plugin.getCornerManager().getCorner(cx, chunkl.getZ(), plot.getWorld().getName());
				corner = CornerType.UNKNOWN;
				for(CornerType c : CornerType.values()) {
					if (plotCorner.getId(c) > 0 && plotCorner.getId(c) == plot.getId()) {
						corner = c;
						break;
					}
				}
				side = (corner == CornerType.A || corner == CornerType.B) ? LinearSide.LEFT : LinearSide.RIGHT;
				condition = new Conditional(side);
				break;
			case CORNER:
				plotCorner = plugin.getCornerManager().getCorner(chunkl.getX(), chunkl.getZ(), plot.getWorld().getName());
				corner = CornerType.UNKNOWN;
				for(CornerType c : CornerType.values()) {
					if (plotCorner.getId(c) > 0 && plotCorner.getId(c) == plot.getId()) {
						corner = c;
						break;
					}
				}
				condition = new Conditional(corner);
				break;
			default:
				break;
			}
			// Save
			DumbBlock[][][] blocks = null;
			if (condition != Conditional.NO_CONDITION) {
				blocks = new DumbBlock[16][plot.getWorld().getMaxHeight() + 1][16];
				for(int x = 0; x < 16; x++) {
					for(int z = 0; z < 16; z++) {
						for(int y = 0; y < plot.getWorld().getMaxHeight() + 1; y++) {
							Block block = chunk.getBlock(x, y, z);
							blocks[x][y][z] = new DumbBlock(block);
							if (y + 1 > block.getWorld().getHighestBlockYAt(x, z)) {
								break;
							}
						}
					}
				}
			}
			// Regenerate
			try {
				plot.getWorld().regenerateChunk(chunkl.getX(), chunkl.getZ());
			} catch(Throwable e) {
				e.printStackTrace();
			}
			// Restore
			if (condition != Conditional.NO_CONDITION) {
				for(int x = 0; x < 16; x++) {
					for(int z = 0; z < 16; z++) {
						for(int y = 0; y < plot.getWorld().getMaxHeight() + 1; y++) {
							if (blocks[x][y][z] == null) {
								break;
							}
							Location location = chunk.getBlock(x, y, z).getLocation();
							switch (condition.getType()) {
							case CORNER:
								if (!condition.match(CornerType.getCornerType(location))) {
									blocks[x][y][z].resetBlock();
								}
								break;
							case SIDE:
								if (!condition.match(LinearSide.getLinearSide(location))) {
									blocks[x][y][z].resetBlock();
								}
								break;
							default:
								break;
							}
						}
					}
				}
			}
			// Refresh
			try {
				plot.getWorld().refreshChunk(chunkl.getX(), chunkl.getZ());
			} catch(Throwable e) {
				e.printStackTrace();
			}
		}
	}

}
