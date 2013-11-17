package com.turt2live.dumbplots.plot;

import java.io.File;
import java.io.IOException;

import com.feildmaster.lib.configuration.EnhancedConfiguration;
import com.turt2live.dumbplots.DumbPlots;

public class PlotCorner {

	private int cx, cz;
	private EnhancedConfiguration config;

	public PlotCorner(int chunkX, int chunkZ) {
		cx = chunkX;
		cz = chunkZ;
		DumbPlots plugin = DumbPlots.getInstance();
		File path = new File(plugin.getDataFolder(), "plot_corners");
		if (!path.exists()) {
			path.mkdirs();
		}
		File file = new File(path, cx + "." + cz + ".yml");
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch(IOException e) {} // Consume
		}
		config = new EnhancedConfiguration(file, plugin);
		config.load();
	}

	public String getOwner(CornerType corner, String world) {
		config.load();
		return config.getString(world + ".owner." + corner.name());
	}

	public void setOwner(String owner, CornerType corner, String world) {
		config.set(world + ".owner." + corner.name(), owner);
		config.save();
	}

	public String getID(CornerType corner, String world) {
		config.load();
		return config.getString(world + ".id." + corner.name());
	}

	public void setID(String owner, CornerType corner, String world) {
		config.set(world + ".id." + corner.name(), owner);
		config.save();
	}

	public int getX() {
		return cx;
	}

	public int getZ() {
		return cz;
	}

	@Override
	public String toString() {
		return cx + "," + cz;
	}

}
