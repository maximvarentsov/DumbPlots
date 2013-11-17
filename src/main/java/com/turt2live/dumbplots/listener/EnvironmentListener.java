package com.turt2live.dumbplots.listener;

import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.weather.ThunderChangeEvent;
import org.bukkit.event.weather.WeatherChangeEvent;

import com.turt2live.dumbplots.DumbPlots;
import com.turt2live.dumbplots.util.DumbUtil;

public class EnvironmentListener implements Listener {

	private DumbPlots plugin = DumbPlots.getInstance();

	@EventHandler (priority = EventPriority.HIGHEST)
	public void onEntitySpawn(CreatureSpawnEvent event) {
		Location location = event.getLocation();
		if (DumbUtil.doControl(event.getEntityType()) && plugin.getPlotManager().isWorldManaged(location.getWorld())) {
			if (DumbUtil.isPeacefulMob(event.getEntityType()) && plugin.getConfig().getBoolean("control.no-animals")) {
				event.setCancelled(true);
			} else if (!DumbUtil.isPeacefulMob(event.getEntityType()) && plugin.getConfig().getBoolean("control.no-mobs")) {
				event.setCancelled(true);
			}
		}
	}

	@EventHandler (priority = EventPriority.HIGHEST)
	public void onWeatherChange(WeatherChangeEvent event) {
		if (event.toWeatherState() && plugin.getConfig().getBoolean("control.no-weather") && plugin.getPlotManager().isWorldManaged(event.getWorld())) {
			event.setCancelled(true);
		}
	}

	@EventHandler (priority = EventPriority.HIGHEST)
	public void onWeatherChange(ThunderChangeEvent event) {
		if (event.toThunderState() && plugin.getConfig().getBoolean("control.no-weather") && plugin.getPlotManager().isWorldManaged(event.getWorld())) {
			event.setCancelled(true);
		}
	}

}
