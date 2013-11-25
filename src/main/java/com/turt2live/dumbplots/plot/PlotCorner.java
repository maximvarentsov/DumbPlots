package com.turt2live.dumbplots.plot;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import com.turt2live.dumbplots.DumbPlots;
import com.turt2live.dumbplots.io.FileMode;
import com.turt2live.dumbplots.io.PlotCornerFile;

public class PlotCorner {

	private int cx, cz;
	private String world;
	private Map<CornerType, Long> ids = new HashMap<CornerType, Long>();
	private Map<CornerType, String> names = new HashMap<CornerType, String>();
	private Map<CornerType, String> owners = new HashMap<CornerType, String>();

	public PlotCorner(int chunkX, int chunkZ, String world) {
		cx = chunkX;
		cz = chunkZ;
		this.world = world;

		DumbPlots plugin = DumbPlots.getInstance();
		File path = new File(plugin.getDataFolder(), "plot_corners");
		if (!path.exists()) {
			path.mkdirs();
		}

		PlotCornerFile file = new PlotCornerFile((int) Math.floor(chunkX / DumbPlots.CHUNKS_PER_CORNER_FILE), (int) Math.floor(chunkZ / DumbPlots.CHUNKS_PER_CORNER_FILE), world, path, DumbPlots.CHUNKS_PER_CORNER_FILE);
		file.open(FileMode.OPEN);
	}

	public String getOwner(CornerType corner) {
		return owners.get(corner);
	}

	public void setOwner(String owner, CornerType corner) {
		owners.put(corner, owner);
	}

	public long getId(CornerType corner) {
		return ids.get(corner);
	}

	public void setId(long id, CornerType corner) {
		ids.put(corner, id);
	}

	public void setName(String owner, CornerType corner) {
		names.put(corner, owner);
	}

	public int getX() {
		return cx;
	}

	public int getZ() {
		return cz;
	}

	public String getWorld() {
		return world;
	}

	@Override
	public String toString() {
		return cx + "," + cz;
	}

}
