package com.turt2live.dumbplots.listener;

import java.util.List;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ExpBottleEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.vehicle.VehicleDamageEvent;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;

import com.turt2live.dumbplots.DumbPlots;
import com.turt2live.dumbplots.Permission;
import com.turt2live.dumbplots.plot.Plot;
import com.turt2live.dumbplots.util.DumbUtil;

public class PlotsListener implements Listener {

	private DumbPlots plugin = DumbPlots.getInstance();

	@EventHandler (priority = EventPriority.HIGHEST)
	public void onBlockBreak(BlockBreakEvent event) {
		if (event.isCancelled()) {
			return;
		}
		Block block = event.getBlock();
		Player player = event.getPlayer();
		if (block.getType() == Material.BEDROCK && block.getLocation().getBlockY() < 1) {
			event.setCancelled(true);
			return;
		}
		if (!isAllowed(block.getLocation(), player)) {
			event.setCancelled(true);
		}
	}

	@EventHandler (priority = EventPriority.HIGHEST)
	public void onBlockPlace(BlockPlaceEvent event) {
		if (event.isCancelled()) {
			return;
		}
		Block block = event.getBlock();
		Player player = event.getPlayer();
		if (!isAllowed(block.getLocation(), player)) {
			event.setCancelled(true);
		}
	}

	@EventHandler (priority = EventPriority.HIGHEST)
	public void onPlayerInteract(PlayerInteractEvent event) {
		if (event.isCancelled()) {
			return;
		}
		if (event.getClickedBlock() == null) {
			return;
		}
		Block block = event.getClickedBlock();
		Player player = event.getPlayer();
		if (!isAllowed(block.getLocation(), player)) {
			event.setCancelled(true);
		}
	}

	@EventHandler (priority = EventPriority.HIGHEST)
	public void onBlockFlow(BlockFromToEvent event) {
		if (event.isCancelled()) {
			return;
		}
		Plot p1 = DumbUtil.getPlot(event.getBlock().getLocation()), p2 = DumbUtil.getPlot(event.getToBlock().getLocation());
		if (DumbUtil.isPlotPath(event.getToBlock().getLocation())) {
			event.setCancelled(true);
		} else if ((p1 == null && p2 != null) || (p1 != null && p2 == null)) {
			event.setCancelled(true);
		} else if (p1 == null && p2 == null) {
			// Same location, ignore
		} else if (!p1.getID().equals(p2.getID())) {
			event.setCancelled(true);
		}
	}

	@EventHandler (priority = EventPriority.HIGHEST)
	public void onPistonExtend(BlockPistonExtendEvent event) {
		if (event.isCancelled()) {
			return;
		}
		List<Block> moving = event.getBlocks();
		BlockFace direction = event.getDirection();
		for(Block block : moving) {
			Block heading = block.getRelative(direction);
			Plot original = DumbUtil.getPlot(block.getLocation());
			Plot destination = DumbUtil.getPlot(heading.getLocation());
			if (DumbUtil.isPlotPath(heading.getLocation())) {
				event.setCancelled(true);
			} else if (original == null && destination == null) {
				// Allow
			} else if ((original != null && destination == null) || (original == null && destination != null)) {
				event.setCancelled(true);
			} else if (!original.getID().equals(destination.getID())) {
				event.setCancelled(true);
			}
		}
	}

	@EventHandler (priority = EventPriority.HIGHEST)
	public void onPistonRetract(BlockPistonRetractEvent event) {
		if (event.isCancelled() || !event.isSticky()) {
			return;
		}
		Block moving = event.getBlock().getRelative(event.getDirection()).getRelative(event.getDirection());
		Block heading = moving.getRelative(event.getDirection().getOppositeFace());
		Plot original = DumbUtil.getPlot(moving.getLocation());
		Plot destination = DumbUtil.getPlot(heading.getLocation());
		if (DumbUtil.isPlotPath(heading.getLocation())) {
			event.setCancelled(true);
		} else if (original == null && destination == null) {
			// Allow
		} else if ((original != null && destination == null) || (original == null && destination != null)) {
			event.setCancelled(true);
		} else if (!original.getID().equals(destination.getID())) {
			event.setCancelled(true);
		}
	}

	@EventHandler (priority = EventPriority.HIGHEST)
	public void onBlockSpread(BlockSpreadEvent event) {
		if (event.isCancelled()) {
			return;
		}
		Block source = event.getSource();
		Block heading = event.getBlock();
		Plot original = DumbUtil.getPlot(source.getLocation());
		Plot destination = DumbUtil.getPlot(heading.getLocation());
		if (DumbUtil.isPlotPath(heading.getLocation())) {
			event.setCancelled(true);
		} else if (original == null && destination == null) {
			// Allow
		} else if ((original != null && destination == null) || (original == null && destination != null)) {
			event.setCancelled(true);
		} else if (!original.getID().equals(destination.getID())) {
			event.setCancelled(true);
		}
	}

	@EventHandler (priority = EventPriority.HIGHEST)
	public void onBlockIgnite(BlockIgniteEvent event) {
		if (event.isCancelled() || event.getPlayer() != null ? event.getPlayer().hasPermission(Permission.BYPASS) : true /* Is Null */) {
			return;
		}
		Block heading = event.getBlock();
		Plot original = DumbUtil.getPlot(event.getPlayer().getLocation());
		Plot destination = DumbUtil.getPlot(heading.getLocation());
		if (DumbUtil.isPlotPath(heading.getLocation())) {
			event.setCancelled(true);
		} else if (original == null && destination == null) {
			// Allow
		} else if ((original != null && destination == null) || (original == null && destination != null)) {
			event.setCancelled(true);
		} else if (!original.getID().equals(destination.getID())) {
			event.setCancelled(true);
		}
	}

	@EventHandler (priority = EventPriority.HIGHEST)
	public void onBlockBurn(BlockBurnEvent event) {
		if (event.isCancelled()) {
			return;
		}
		if (DumbUtil.isPlotPath(event.getBlock().getLocation())) {
			event.setCancelled(true);
		}
	}

	@EventHandler (priority = EventPriority.HIGHEST)
	public void onCropTrample(PlayerInteractEvent event) {
		if (event.getAction() == Action.PHYSICAL) { // Also covers pressure plates
			if (!isAllowed(event.getPlayer().getLocation(), event.getPlayer())) {
				event.setCancelled(true);
			}
		}
	}

	@EventHandler (priority = EventPriority.HIGHEST)
	public void onPaintingPlace(HangingPlaceEvent event) {
		if (event.isCancelled()) {
			return;
		}
		if (!isAllowed(event.getBlock().getLocation(), event.getPlayer())) {
			event.setCancelled(true);
		}
	}

	@EventHandler (priority = EventPriority.HIGHEST)
	public void onBucketFill(PlayerBucketFillEvent event) {
		if (event.isCancelled()) {
			return;
		}
		if (!isAllowed(event.getBlockClicked().getLocation(), event.getPlayer())) {
			event.setCancelled(true);
		}
	}

	@EventHandler (priority = EventPriority.HIGHEST)
	public void onProjectileLaunch(ProjectileLaunchEvent event) {
		event.getEntity().setMetadata("sourceLocationX", new FixedMetadataValue(plugin, event.getEntity().getLocation().getBlockX()));
		event.getEntity().setMetadata("sourceLocationZ", new FixedMetadataValue(plugin, event.getEntity().getLocation().getBlockZ()));
	}

	@EventHandler (priority = EventPriority.HIGHEST)
	public void onProjectileLand(ProjectileHitEvent event) {
		if (!event.getEntity().hasMetadata("sourceLocationX") || !event.getEntity().hasMetadata("sourceLocationZ")) {
			return; // Ignore it
		}
		List<MetadataValue> values = event.getEntity().getMetadata("sourceLocationX");
		int x = 0, z = 0;
		for(MetadataValue value : values) {
			x = value.asInt();
		}
		values = event.getEntity().getMetadata("sourceLocationZ");
		for(MetadataValue value : values) {
			z = value.asInt();
		}
		Location from = new Location(event.getEntity().getWorld(), x, 0, z);
		Location to = event.getEntity().getLocation();
		Plot original = DumbUtil.getPlot(from);
		Plot destination = DumbUtil.getPlot(to);
		// Plot paths can shoot to other plot paths
		if (original == null && destination == null) {
			// Allow
		} else if ((original != null && destination == null) || (original == null && destination != null)) {
			event.getEntity().remove();
		} else if (!original.getID().equals(destination.getID())) {
			event.getEntity().remove();
		}
	}

	@EventHandler (priority = EventPriority.HIGHEST)
	public void onEntityDamage(EntityDamageByEntityEvent event) {
		if (event.isCancelled()) {
			return;
		}
		if (event.getDamager().hasMetadata("sourceLocationX") && event.getDamager().hasMetadata("sourceLocationZ")) {
			event.setCancelled(true);
		} else {
			if (event.getDamager() instanceof Player && event.getEntity() instanceof Player) {
				if (!plugin.getConfig().getBoolean("control.cross-plot-damage")) {
					Player injured = (Player) event.getEntity();
					Player stickWielder = (Player) event.getDamager();
					Location from = stickWielder.getLocation();
					Location to = injured.getLocation();
					Plot original = DumbUtil.getPlot(from);
					Plot destination = DumbUtil.getPlot(to);
					if (original == null && destination == null) {
						// Allow
					} else if ((original != null && destination == null) || (original == null && destination != null)) {
						event.setCancelled(true);
					} else if (!original.getID().equals(destination.getID())) {
						event.setCancelled(true);
					}
				}
			}// else mob combat
		}
	}

	@EventHandler (priority = EventPriority.HIGHEST)
	public void onItemPickup(PlayerPickupItemEvent event) {
		if (event.isCancelled()) {
			return;
		}
		Location from = event.getItem().getLocation();
		Location to = event.getPlayer().getLocation();
		Plot original = DumbUtil.getPlot(from);
		Plot destination = DumbUtil.getPlot(to);
		if (DumbUtil.isPlotPath(to) && !event.getPlayer().hasPermission(Permission.BYPASS)) {
			event.setCancelled(true);
		} else if (original == null && destination == null) {
			// Allow
		} else if ((original != null && destination == null) || (original == null && destination != null)) {
			event.setCancelled(true);
		} else if (!original.getID().equals(destination.getID())) {
			event.setCancelled(true);
		}
	}

	@EventHandler (priority = EventPriority.HIGHEST)
	public void onVehicleDamage(VehicleDamageEvent event) {
		if (event.isCancelled()) {
			return;
		}
		Location from = event.getAttacker().getLocation();
		Location to = event.getVehicle().getLocation();
		Plot original = DumbUtil.getPlot(from);
		Plot destination = DumbUtil.getPlot(to);
		if (DumbUtil.isPlotPath(to)) {
			event.setCancelled(true);
		} else if (original == null && destination == null) {
			// Allow
		} else if ((original != null && destination == null) || (original == null && destination != null)) {
			event.setCancelled(true);
		} else if (!original.getID().equals(destination.getID())) {
			event.setCancelled(true);
		}
	}

	@EventHandler (priority = EventPriority.HIGHEST)
	public void onTreeGrow(StructureGrowEvent event) {
		if (event.isCancelled()) {
			return;
		}
		Location from = event.getLocation();
		for(int i = 0; i < event.getBlocks().size(); i++) {
			Location to = event.getBlocks().get(i).getLocation();
			Plot original = DumbUtil.getPlot(from);
			Plot destination = DumbUtil.getPlot(to);
			if (original == null && destination == null) {
				// Allow
			} else if ((original != null && destination == null) || (original == null && destination != null)) {
				event.getBlocks().remove(i--);
			} else if (!original.getID().equals(destination.getID())) {
				event.getBlocks().remove(i--);
			}
		}
	}

	@EventHandler (priority = EventPriority.HIGHEST)
	public void onExpLand(ExpBottleEvent event) {
		if (!event.getEntity().hasMetadata("sourceLocationX") || !event.getEntity().hasMetadata("sourceLocationZ")) {
			return; // Ignore it
		}
		List<MetadataValue> values = event.getEntity().getMetadata("sourceLocationX");
		int x = 0, z = 0;
		for(MetadataValue value : values) {
			x = value.asInt();
		}
		values = event.getEntity().getMetadata("sourceLocationZ");
		for(MetadataValue value : values) {
			z = value.asInt();
		}
		Location from = new Location(event.getEntity().getWorld(), x, 0, z);
		Location to = event.getEntity().getLocation();
		Plot original = DumbUtil.getPlot(from);
		Plot destination = DumbUtil.getPlot(to);
		// Plot paths can shoot to other plot paths
		if (original == null && destination == null) {
			// Allow
		} else if ((original != null && destination == null) || (original == null && destination != null)) {
			event.setExperience(0);
			event.setShowEffect(false);
		} else if (!original.getID().equals(destination.getID())) {
			event.setExperience(0);
			event.setShowEffect(false);
		}
	}

	@EventHandler (priority = EventPriority.HIGHEST)
	public void onExplode(EntityExplodeEvent event) {
		if (event.isCancelled()) {
			return;
		}
		Location from = event.getLocation();
		for(int i = 0; i < event.blockList().size(); i++) {
			Location to = event.blockList().get(i).getLocation();
			Plot original = DumbUtil.getPlot(from);
			Plot destination = DumbUtil.getPlot(to);
			if (original == null && destination == null) {
				// Allow
			} else if ((original != null && destination == null) || (original == null && destination != null)) {
				event.blockList().remove(i--);
			} else if (!original.getID().equals(destination.getID())) {
				event.blockList().remove(i--);
			}
		}
	}

	@EventHandler (priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onTeleport(PlayerTeleportEvent event) {
		Player player = event.getPlayer();
		Location to = event.getTo();
		Location from = event.getFrom();
		if (!to.getWorld().getName().equalsIgnoreCase(from.getWorld().getName())) {
			if (plugin.getPlotManager().isWorldManaged(to.getWorld())) {
				if (player.getGameMode() != GameMode.CREATIVE) {
					player.setGameMode(GameMode.CREATIVE);
				}
			}
		}
	}

	@EventHandler (priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onGameModeChange(PlayerGameModeChangeEvent event) {
		Player player = event.getPlayer();
		if (plugin.getPlotManager().isWorldManaged(player.getWorld())) {
			if (event.getNewGameMode() != GameMode.CREATIVE && !player.hasPermission(Permission.BYPASS)) {
				event.setCancelled(true);
			}
		}
	}

	private boolean isAllowed(Location location, Player player) {
		if (!location.getWorld().getName().equals(player.getWorld().getName()) || !plugin.getPlotManager().isWorldManaged(location.getWorld())) {
			return true;
		}
		if (player.hasPermission(Permission.BYPASS)) {
			return true;
		}
		if (DumbUtil.isPlotPath(location)) {
			return false;
		}
		Plot plot = DumbUtil.getPlot(location);
		if (plot != null && (plot.isAllowed(player))) {
			return true;
		}
		return false;
	}

}
