package com.turt2live.dumbplots;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bukkit.OfflinePlayer;
import org.bukkit.World;

import com.turt2live.dumbplots.plot.Plot;

public class PlotManager {

	private DumbPlots plugin = DumbPlots.getInstance();
	private List<Plot> plots = new ArrayList<Plot>();

	public PlotManager() {
		reload();
	}

	public String getPlotID(OfflinePlayer player) {
		return player.getName() + "_" + getOwnedPlots(player).size();
		// First plot will be turt2live_0, next turt2live_1, etc
	}

	public List<Plot> getOwnedPlots(OfflinePlayer player) {
		List<Plot> owned = new ArrayList<Plot>();
		for(Plot plot : plots) {
			if (plot.getOwner().equals(player.getName())) {
				owned.add(plot);
			}
		}
		return owned;
	}

	public List<Plot> getAllowedPlots(OfflinePlayer player) {
		List<Plot> allowed = new ArrayList<Plot>();
		for(Plot plot : plots) {
			if (plot.isAllowed(player)) {
				allowed.add(plot);
			}
		}
		return allowed;
	}

	public boolean isWorldManaged(World world) {
		return plugin.getConfig().getStringList("worlds").contains(world.getName());
	}

	public Plot getPlot(String id) {
		for(Plot plot : plots) {
			if (plot.getName().equals(id)) {
				return plot;
			}
		}
		return null;
	}

	public void reload() {
		plots.clear();
		File path = new File(plugin.getDataFolder(), "plots");
		if (!path.exists()) {
			path.mkdirs();
		}
		File[] listing = path.listFiles();
		if (listing != null) {
			for(File file : listing) {
				Plot plot = new Plot(file);
				plots.add(plot);
			}
		}
	}

	public void addPlot(Plot plot) {
		plots.add(plot);
	}

	public List<Plot> getAllPlots() {
		return Collections.unmodifiableList(plots);
	}

	public Plot getDefaultPlot(OfflinePlayer player) {
		// TODO Auto-generated method stub
		return null;
	}

}
