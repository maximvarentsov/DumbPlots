package com.turt2live.dumbplots;

import java.io.File;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Listener;
import org.bukkit.generator.ChunkGenerator;

import com.feildmaster.lib.configuration.PluginWrapper;
import com.turt2live.dumbplots.listener.DebugListener;
import com.turt2live.dumbplots.listener.EnvironmentListener;
import com.turt2live.dumbplots.listener.PlotsListener;
import com.turt2live.dumbplots.plot.corner.CornerType;
import com.turt2live.dumbplots.plot.corner.FileMode;
import com.turt2live.dumbplots.plot.corner.PlotCorner;
import com.turt2live.dumbplots.plot.corner.PlotCornerFile;
import com.turt2live.dumbplots.plot.corner.PlotCornerManager;
import com.turt2live.dumbplots.terrain.TerrainGenerator;

public class DumbPlots extends PluginWrapper implements Listener {

	// TODO: Less creation of objects, more managers. Rewrite corners and plot data store
	// TODO: Save and load plot corners. Doesn't load/save correctly

	private static DumbPlots instance;
	public static final int CHUNKS_PER_CORNER_FILE = 128;

	private PlotManager plots;
	private PlotsCommands commands;
	private PlotCornerManager corners;
	private long lastId = 0;

	private void print(PlotCorner corner) {
		System.out.println(corner);
		for(CornerType co : CornerType.values()) {
			System.out.println("\t" + co.name() + " " + corner.getId(co));
		}
	}

	@Override
	public void onEnable() {
		instance = this;

		// Check configuration
		getConfig().loadDefaults(getResource("config.yml"));
		if (!getConfig().fileExists() || !getConfig().checkDefaults()) {
			getConfig().saveDefaults();
		}
		getConfig().load();

		lastId = getConfig().getLong("last-id", 0);

		for(File file : getCornerPath().listFiles()) {
			PlotCornerFile f = new PlotCornerFile(file, CHUNKS_PER_CORNER_FILE);
			f.open(FileMode.OPEN);
			System.out.println("File: " + file.getName());
			int empty = 0, n = 0;
			for(int x = 0; x < CHUNKS_PER_CORNER_FILE; x++) {
				for(int z = 0; z < CHUNKS_PER_CORNER_FILE; z++) {
					PlotCorner corner = f.getCorner(x, z, "world");
					if (corner == null) {
						n++;
					} else {
						boolean all = false;
						for(CornerType co : CornerType.values()) {
							if (corner.getId(co) > 0) {
								print(corner);
								all = true;
								break;
							}
						}
						if (!all) {
							empty++;
						}
					}
				}
			}
			System.out.println("empty: " + empty + " || Null: " + n);
		}

		getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
			@Override
			public void run() {
				// Setup manager
				corners = new PlotCornerManager();
				plots = new PlotManager();

				// Commands setup
				commands = new PlotsCommands();
				getCommand("plot").setExecutor(commands);

				// Listener
				getServer().getPluginManager().registerEvents(new DebugListener(), instance);
				getServer().getPluginManager().registerEvents(new PlotsListener(), instance);
				getServer().getPluginManager().registerEvents(new EnvironmentListener(), instance);

				// Load worlds for DumbPlots
				List<String> worlds = getConfig().getStringList("worlds");
				if (worlds != null) {
					getLogger().info("Loading worlds...");
					for(String worldname : worlds) {
						getLogger().info("World: " + worldname);

						WorldCreator creator = new WorldCreator(worldname);
						creator.generateStructures(false);
						creator.environment(Environment.NORMAL);
						creator.type(WorldType.FLAT);
						creator.generator(getDefaultWorldGenerator(worldname, getConfig().getInt("world-height", 16) + ""));

						World world = getServer().createWorld(creator);
						world.setSpawnLocation(0, getConfig().getInt("world-height", 16) + 2, 0);
						world.setGameRuleValue("doMobSpawning", "false");
						world.setGameRuleValue("doDaylightCycle", "false");
					}
					getLogger().info("Worlds loaded!");
				}

				plots.reload();
			}
		});

		// Spam console
		getLogger().info("Loaded! Plugin by turt2live");
	}

	@Override
	public void onDisable() {
		getServer().getScheduler().cancelTasks(this);
		commands.saveDebugModePlayers();
		plots.save();
		corners.save();
		getConfig().set("last-id", lastId);
		saveConfig();
		instance = null;
		getLogger().info("Disabled! Plugin by turt2live");
	}

	public static long nextId() {
		return ++instance.lastId;
	}

	public static DumbPlots getInstance() {
		return instance;
	}

	public static File getCornerPath() {
		File path = new File(getInstance().getDataFolder(), "plot_corners");
		if (!path.exists()) {
			path.mkdirs();
		}
		return path;
	}

	public PlotCornerManager getCornerManager() {
		return corners;
	}

	public PlotManager getPlotManager() {
		return plots;
	}

	public void sendMessage(CommandSender sender, String message) {
		sender.sendMessage(prefix() + ChatColor.translateAlternateColorCodes('&', message));
	}

	public String prefix() {
		return ChatColor.translateAlternateColorCodes('&', getConfig().getString("general.prefix")).trim() + " ";
	}

	@Override
	public ChunkGenerator getDefaultWorldGenerator(String worldName, String id) {
		return new TerrainGenerator(id, getLogger());
	}

	public boolean isInDebug(String name) {
		return commands.inDebug(name);
	}

}
