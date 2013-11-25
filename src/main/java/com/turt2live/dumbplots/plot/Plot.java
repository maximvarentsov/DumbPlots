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
		long id = DumbUtil.nextId();
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
		// TODO: wat
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
		config.set("plot-name", plotid);
		config.set("id", id);
		config.set("claimed", false);
		config.set("corners", corners);
		config.set("chunks", chunks);
		config.set("world", location.getWorld().getName());
		config.save();
		plot = new Plot(file);
		plot.setPlotType(PlotType.UNCLAIMED);
		return plot;
	}

	private long sysId;
	private String name, owner, world;
	private List<CornerPlotCorner> corners = new ArrayList<CornerPlotCorner>();
	private List<ChunkLoc> chunks = new ArrayList<ChunkLoc>();
	private List<String> allowed = new ArrayList<String>();
	private PlotType state = PlotType.UNCLAIMED;
	private File file;

	public Plot(File file) {
		this.file = file;
		DumbPlots plugin = DumbPlots.getInstance();
		EnhancedConfiguration config = new EnhancedConfiguration(file, plugin);
		config.load();
		name = config.getString("plot-name", "UNCLAIMED");
		owner = config.getString("owner", "CONSOLE");
		world = config.getString("world", "world");
		state = config.getBoolean("claimed", false) ? PlotType.CLAIMED : PlotType.UNCLAIMED;
		// Load corners and chunks
		List<String> ccorners = config.getStringList("corners");
		if (ccorners == null)
			ccorners = new ArrayList<String>();
		for(String corner : ccorners) {
			corners.add(new CornerPlotCorner(corner, getWorld().getName()));
		}
		List<String> cchunks = config.getStringList("chunks");
		if (cchunks == null)
			cchunks = new ArrayList<String>();
		for(String chunk : cchunks) {
			ChunkLoc loc = new ChunkLoc(chunk);
			chunks.add(loc);
		}
		this.sysId = config.getLong("id", 0);
	}

	public void save() {
		DumbPlots plugin = DumbPlots.getInstance();
		EnhancedConfiguration config = new EnhancedConfiguration(file, plugin);
		config.load();
		config.set("owner", "CONSOLE");
		config.set("plot-name", name);
		config.set("id", sysId);
		config.set("claimed", state == PlotType.CLAIMED);
		List<String> c = new ArrayList<String>();
		for(CornerPlotCorner co : corners) {
			c.add(co.toString());
		}
		config.set("corners", c);
		c.clear();
		for(ChunkLoc lo : chunks) {
			c.add(lo.toString());
		}
		config.set("chunks", c);
		config.set("world", world);
		config.save();
	}

	public long getId() {
		return sysId;
	}

	public void setId(long id) {
		this.sysId = id;
	}

	public boolean addAllowedMember(OfflinePlayer player) {
		return allowed.add(player.getName());
	}

	public boolean removeAllowedMember(OfflinePlayer player) {
		return allowed.remove(player.getName());
	}

	public boolean isAllowed(OfflinePlayer player) {
		return allowed.contains(player.getName());
	}

	public List<String> getAllowedMembers() {
		return Collections.unmodifiableList(allowed);
	}

	public void setWorld(World world) {
		this.world = world.getName();
		assignCorners();
	}

	public World getWorld() {
		return DumbPlots.getInstance().getServer().getWorld(world);
	}

	public void setOwner(String name) {
		this.owner = name;
		assignCorners();
	}

	public void setName(String id) {
		this.name = id;
		assignCorners();
	}

	public String getOwner() {
		return owner;
	}

	public String getName() {
		return name;
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
		this.state = type;
		assignCorners();
	}

	public PlotType getPlotType() {
		return state;
	}

	public List<CornerPlotCorner> getCorners() {
		return Collections.unmodifiableList(corners);
	}

	public List<ChunkLoc> getChunks() {
		return Collections.unmodifiableList(chunks);
	}

	public void assignCorners() {
		for(CornerPlotCorner corner : getCorners()) {
			corner.getCorner().setName(getName(), corner.getType());
			corner.getCorner().setOwner(getOwner(), corner.getType());
			corner.getCorner().setId(getId(), corner.getType());
		}
	}

}
