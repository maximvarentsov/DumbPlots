package com.turt2live.dumbplots.plot;

import java.util.HashMap;
import java.util.Map;

public class PlotCorner {

	private int cx, cz;
	private String world;
	private Map<CornerType, Long> ids = new HashMap<CornerType, Long>();

	public PlotCorner(int chunkX, int chunkZ, String world) {
		cx = chunkX;
		cz = chunkZ;
		this.world = world;

		// TODO: Move this code?
		//		File path = new File(plugin.getDataFolder(), "plot_corners");
		//		if (!path.exists()) {
		//			path.mkdirs();
		//		}

		// TODO: Load data
	}

	public String getOwner(CornerType corner) {
		return "SOME GUY"; // TODO
	}

	public void setOwner(String owner, CornerType corner) {
		// TODO
	}

	public String getID(CornerType corner) {
		return ids.get(corner).toString();
	}

	public long getInternalId(CornerType corner) {
		Long v = ids.get(corner);
		if (v == null)
			return 0;
		return ids.get(corner);
	}

	public void setCorner(long id, CornerType corner) {
		ids.put(corner, id);
	}

	public void setID(String owner, CornerType corner) {
		// TODO
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
