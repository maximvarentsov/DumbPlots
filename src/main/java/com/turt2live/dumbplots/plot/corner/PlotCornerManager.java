package com.turt2live.dumbplots.plot.corner;

import java.util.LinkedHashMap;
import java.util.Map;

import com.turt2live.dumbplots.DumbPlots;
import com.turt2live.dumbplots.plot.ChunkLoc;

public class PlotCornerManager {

	private Map<String, Map<ChunkLoc, PlotCorner>> corners = new LinkedHashMap<String, Map<ChunkLoc, PlotCorner>>();

	public void save() {
		for(String world : corners.keySet()) {
			Map<ChunkLoc, PlotCorner> map = corners.get(world);
			for(ChunkLoc loc : map.keySet()) {
				PlotCorner corner = map.get(loc);
				PlotCornerFile file = new PlotCornerFile((int) Math.floor(loc.getX() / DumbPlots.CHUNKS_PER_CORNER_FILE), (int) Math.floor(loc.getZ() / DumbPlots.CHUNKS_PER_CORNER_FILE), world, DumbPlots.getCornerPath(), DumbPlots.CHUNKS_PER_CORNER_FILE);
				file.open(FileMode.WRITE);
				file.writeCorner(corner);
				file.close();
			}
		}
	}

	public PlotCorner getCorner(int chunkX, int chunkZ, String world) {
		Map<ChunkLoc, PlotCorner> map = corners.get(world);
		if (map == null || map.isEmpty()) {
			load(chunkX, chunkZ, world);
			map = corners.get(world);
			if (map == null) {
				map = new LinkedHashMap<ChunkLoc, PlotCorner>();
				corners.put(world, map);
			}
		}
		ChunkLoc cl = new ChunkLoc(chunkX, chunkZ);
		PlotCorner corner = map.get(cl);
		if (corner == null) {
			corner = new PlotCorner(chunkX, chunkZ, world);
			map.put(cl, corner);
		}
		return corner;
	}

	private void load(int chunkX, int chunkZ, String world) {
		PlotCornerFile file = new PlotCornerFile((int) Math.floor(chunkX / DumbPlots.CHUNKS_PER_CORNER_FILE), (int) Math.floor(chunkZ / DumbPlots.CHUNKS_PER_CORNER_FILE), world, DumbPlots.getCornerPath(), DumbPlots.CHUNKS_PER_CORNER_FILE);
		file.open(FileMode.OPEN);
		PlotCorner corner = file.getCorner(chunkX, chunkZ, world);
		if (corner != null) {
			Map<ChunkLoc, PlotCorner> map = corners.get(world);
			if (map == null) {
				map = new LinkedHashMap<ChunkLoc, PlotCorner>();
				corners.put(world, map);
			}
			map.put(new ChunkLoc(chunkX, chunkZ), corner);
		}
		file.close();
	}

}
