package com.turt2live.dumbplots;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Listener;
import org.bukkit.generator.ChunkGenerator;

import com.feildmaster.lib.configuration.PluginWrapper;
import com.turt2live.dumbplots.listener.DebugListener;
import com.turt2live.dumbplots.listener.EnvironmentListener;
import com.turt2live.dumbplots.listener.PlotsListener;
import com.turt2live.dumbplots.terrain.TerrainGenerator;

public class DumbPlots extends PluginWrapper implements Listener {

	private static DumbPlots instance;

	public static DumbPlots getInstance() {
		return instance;
	}

	private PlotManager plots;
	private PlotsCommands commands;

	@Override
	public void onEnable() {
		instance = this;

		// Check configuration
		getConfig().loadDefaults(getResource("resources/config.yml"));
		if (!getConfig().fileExists() || !getConfig().checkDefaults()) {
			getConfig().saveDefaults();
		}
		getConfig().load();

		// Setup manager
		plots = new PlotManager();

		// Commands setup
		commands = new PlotsCommands();
		getCommand("plot").setExecutor(commands);

		// Listener
		getServer().getPluginManager().registerEvents(new DebugListener(), this);
		getServer().getPluginManager().registerEvents(new PlotsListener(), this);
		getServer().getPluginManager().registerEvents(new EnvironmentListener(), this);

		// Spam console
		getLogger().info("Loaded! Plugin by turt2live");
	}

	@Override
	public void onDisable() {
		getServer().getScheduler().cancelTasks(this);
		commands.saveDebugModePlayers();
		commands = null;
		plots = null;
		instance = null;
		getLogger().info("Disabled! Plugin by turt2live");
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
