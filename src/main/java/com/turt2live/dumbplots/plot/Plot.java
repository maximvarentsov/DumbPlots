package com.turt2live.dumbplots.plot;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;

import com.feildmaster.lib.configuration.EnhancedConfiguration;
import com.turt2live.dumbplots.DumbPlots;
import com.turt2live.dumbplots.util.DumbUtil;

public class Plot {

	public static enum PlotType {
		UNCLAIMED, CLAIMED, INVALID;
	}

	public static Plot generateUnclaimedPlot(Location location) {
		Plot plot = null;
		if (DumbUtil.isPlotPath(location)) {
			return null;
		}
		String plotid = DumbUtil.generateUnclaimedID();
		List<String> corners = new ArrayList<String>();
		List<String> chunks = new ArrayList<String>();
		// Find 4 corners
		ChunkLoc[] abcdCorners = {null, null, null, null};
		// CornerPlotCorner
		int chunkX = location.getChunk().getX();
		int chunkZ = location.getChunk().getZ();
		ChunkType initType = DumbUtil.getChunkType(chunkX, chunkZ);
		Location newLocation = location.clone();
		Location startLocation = location.clone(); // Used in correction values
		int c = 0;
		switch (initType) {
		case CORNER:
			abcdCorners = DumbUtil.findCorners(location, chunkX, chunkZ);
			break;
		case LINEAR_Z:
			c = 0;
			while(DumbUtil.getChunkType(chunkX, chunkZ) != ChunkType.CORNER) {
				chunkX--;
				newLocation.setX(newLocation.getX() - 16);
				c++;
				if (c > 10) {
					break;
				}
			}
			// Correct location
			if (LinearSide.getLinearSide(startLocation) == LinearSide.LEFT) {
				newLocation.setX((chunkX * 16) + 15);
				newLocation.setZ((chunkZ * 16));
			} else if (LinearSide.getLinearSide(startLocation) == LinearSide.RIGHT) {
				newLocation.setX((chunkX * 16) + 15);
				newLocation.setZ((chunkZ * 16) + 15);
			}
			break;
		case LINEAR_X:
			c = 0;
			while(DumbUtil.getChunkType(chunkX, chunkZ) != ChunkType.CORNER) {
				chunkZ--;
				newLocation.setZ(newLocation.getZ() - 16);
				c++;
				if (c > 10) {
					break;
				}
			}
			// Correct location
			if (LinearSide.getLinearSide(startLocation) == LinearSide.LEFT) {
				newLocation.setX((chunkX * 16) + 0);
				newLocation.setZ((chunkZ * 16) + 15);
			} else if (LinearSide.getLinearSide(startLocation) == LinearSide.RIGHT) {
				newLocation.setX((chunkX * 16) + 15);
				newLocation.setZ((chunkZ * 16) + 15);
			}
			break;
		case FLAT:
			c = 0;
			boolean edgeFound = false;
			while(!edgeFound) {
				chunkX--;
				newLocation.setX(newLocation.getX() - 16);
				ChunkType ctype = DumbUtil.getChunkType(chunkX, chunkZ);
				if (ctype == ChunkType.LINEAR_X || ctype == ChunkType.LINEAR_Z) {
					edgeFound = true;
				}
				c++;
				if (c > 10) {
					break;
				}
			}
			// Correct location (move to relative 15,15 in chunk)
			newLocation.setX((chunkX * 16) + 15);
			newLocation.setZ((chunkZ * 16) + 15);
			switch (DumbUtil.getChunkType(chunkX, chunkZ)) {
			case LINEAR_Z:
				c = 0;
				while(DumbUtil.getChunkType(chunkX, chunkZ) != ChunkType.CORNER) {
					chunkX--;
					newLocation.setX(newLocation.getX() - 16);
					if (c > 10) {
						break;
					}
				}
				break;
			case LINEAR_X:
				c = 0;
				while(DumbUtil.getChunkType(chunkX, chunkZ) != ChunkType.CORNER) {
					chunkZ--;
					newLocation.setZ(newLocation.getZ() - 16);
					if (c > 10) {
						break;
					}
				}
				break;
			default:
				break;
			}
			break;
		default:
			break;
		}
		abcdCorners = DumbUtil.findCorners(newLocation, chunkX, chunkZ);
		// Create corners
		for(int i = 0; i < abcdCorners.length; i++) {
			corners.add((i == 0 ? "A" : (i == 1 ? "B" : (i == 2 ? "C" : "D"))) + "=" + abcdCorners[i].toString());
		}
		for(int x = abcdCorners[1].getX(); x <= abcdCorners[0].getX(); x++) {
			for(int z = abcdCorners[3].getZ(); z <= abcdCorners[1].getZ(); z++) {
				chunks.add(new ChunkLoc(x, z).toString());
			}
		}
		// Save
		File path = new File(DumbPlots.getInstance().getDataFolder(), "plots");
		if (!path.exists()) {
			path.mkdirs();
		}
		File file = new File(path, plotid + ".yml");
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch(IOException e) {} // Consume
		}
		EnhancedConfiguration config = new EnhancedConfiguration(file, DumbPlots.getInstance());
		config.load();
		config.set("owner", "CONSOLE");
		config.set("id", plotid);
		config.set("claimed", false);
		config.set("corners", corners);
		config.set("chunks", chunks);
		config.set("world", location.getWorld().getName());
		config.save();
		plot = new Plot(file);
		plot.setPlotType(PlotType.UNCLAIMED);
		return plot;
	}

	private EnhancedConfiguration config;
	private List<CornerPlotCorner> corners = new ArrayList<CornerPlotCorner>();
	private List<ChunkLoc> chunks = new ArrayList<ChunkLoc>();

	public Plot(File plotfile) {
		config = new EnhancedConfiguration(plotfile, DumbPlots.getInstance());
		config.load();
		// Load corners and chunks
		List<String> ccorners = config.getStringList("corners");
		for(String corner : ccorners) {
			corners.add(new CornerPlotCorner(corner));
		}
		List<String> cchunks = config.getStringList("chunks");
		for(String chunk : cchunks) {
			ChunkLoc loc = new ChunkLoc(chunk);
			chunks.add(loc);
		}
	}

	public boolean addAllowedMember(OfflinePlayer player) {
		config.load();
		List<String> members = config.getStringList("allowed");
		boolean added = members.add(player.getName());
		config.set("allowed", members);
		config.save();
		return added;
	}

	public boolean removeAllowedMember(OfflinePlayer player) {
		config.load();
		List<String> members = config.getStringList("allowed");
		boolean removed = members.remove(player.getName());
		config.set("allowed", members);
		config.save();
		return removed;
	}

	public boolean isAllowed(OfflinePlayer player) {
		config.load();
		return getAllowedMembers().contains(player.getName());
	}

	public List<String> getAllowedMembers() {
		config.load();
		List<String> members = config.getStringList("allowed");
		if (members == null) {
			members = new ArrayList<String>();
		}
		members.add(getOwner());
		return Collections.unmodifiableList(members);
	}

	public void setWorld(World world) {
		config.set("world", world.getName());
		config.save();
		assignCorners();
	}

	public World getWorld() {
		String name = config.getString("world");
		return DumbPlots.getInstance().getServer().getWorld(name);
	}

	public void setOwner(String name) {
		config.set("owner", name);
		config.save();
		assignCorners();
	}

	public void setID(String id) {
		config.set("id", id);
		config.save();
		assignCorners();
	}

	public String getOwner() {
		return config.getString("owner");
	}

	public String getID() {
		return config.getString("id");
	}

	/**
	 * Warning: This method does not check for paths and only returns true if it's found.
	 * Therefore looping plots may return weird results.
	 * 
	 * @param location the location
	 * @return true if this plot has the point
	 */
	public boolean has(Location location) {
		boolean has = false;
		ChunkLoc chunk = new ChunkLoc(location.getChunk());
		for(ChunkLoc ch : getChunks()) {
			if (ch.equals(chunk)) {
				has = true;
				break;
			}
		}
		return has && location.getWorld().getName().equals(getWorld().getName());
	}

	public void setPlotType(PlotType type) {
		switch (type) {
		case CLAIMED:
			config.set("claimed", true);
			config.save();
			break;
		case UNCLAIMED:
			config.set("claimed", false);
			config.save();
			break;
		default:
			break;
		}
		assignCorners();
	}

	public PlotType getPlotType() {
		return config.getBoolean("claimed") ? PlotType.CLAIMED : PlotType.UNCLAIMED;
	}

	public List<CornerPlotCorner> getCorners() {
		return Collections.unmodifiableList(corners);
	}

	public List<ChunkLoc> getChunks() {
		return Collections.unmodifiableList(chunks);
	}

	public void assignCorners() {
		for(CornerPlotCorner corner : getCorners()) {
			corner.getCorner().setID(getID(), corner.getType(), getWorld().getName());
			corner.getCorner().setOwner(getOwner(), corner.getType(), getWorld().getName());
		}
	}

}
