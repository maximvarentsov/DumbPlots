package com.turt2live.dumbplots.listener;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import com.turt2live.dumbplots.DumbPlots;
import com.turt2live.dumbplots.plot.ChunkType;
import com.turt2live.dumbplots.plot.CornerType;
import com.turt2live.dumbplots.plot.LinearSide;
import com.turt2live.dumbplots.plot.Plot;
import com.turt2live.dumbplots.plot.PlotCorner;
import com.turt2live.dumbplots.util.DumbUtil;
import com.turt2live.dumbplots.util.ResetPlot;

public class DebugListener implements Listener {

	private DumbPlots plugin = DumbPlots.getInstance();

	@EventHandler
	public void onJoin(PlayerJoinEvent event) {
		if (plugin.isInDebug(event.getPlayer().getName())) {
			plugin.sendMessage(event.getPlayer(), "You are currently " + ChatColor.GREEN + "in" + ChatColor.WHITE + " debug mode.");
		}
	}

	@EventHandler (priority = EventPriority.HIGHEST)
	public void onDebugInteract(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		if (!plugin.isInDebug(player.getName())) {
			return;
		}
		if (player.getItemInHand() != null && player.getItemInHand().getType() == Material.BLAZE_ROD) {
			Block block = event.getClickedBlock();
			if (block != null) {
				Plot plot = DumbUtil.getPlot(block.getLocation());
				if (plot != null) {
					player.sendMessage("Plot");
				} else {
					player.sendMessage("Not a plot");
				}
				event.setCancelled(true);
			}
		} else if (player.getItemInHand() != null && player.getItemInHand().getType() == Material.BLAZE_POWDER) {
			Block block = event.getClickedBlock();
			if (block != null) {
				int x = DumbUtil.getXInChunk(block.getLocation());
				int z = DumbUtil.getZInChunk(block.getLocation());
				player.sendMessage(x + "," + z);
				event.setCancelled(true);
			}
		} else if (player.getItemInHand() != null && player.getItemInHand().getType() == Material.COAL) {
			Block block = event.getClickedBlock();
			if (block != null) {
				if (DumbUtil.getChunkType(block.getChunk().getX(), block.getChunk().getZ()) == ChunkType.LINEAR_X
						|| DumbUtil.getChunkType(block.getChunk().getX(), block.getChunk().getZ()) == ChunkType.LINEAR_Z) {
					if (LinearSide.getLinearSide(block.getLocation()) == LinearSide.LEFT) {
						block.setType(Material.GOLD_BLOCK);
					} else if (LinearSide.getLinearSide(block.getLocation()) == LinearSide.RIGHT) {
						block.setType(Material.DIAMOND_BLOCK);
					}
					player.sendMessage(LinearSide.getLinearSide(block.getLocation()) + " " + ((DumbUtil.getChunkType(block.getChunk().getX(), block.getChunk().getZ()) == ChunkType.LINEAR_X) ? "X" : "Z"));
				}
				event.setCancelled(true);
			}
		} else if (player.getItemInHand() != null && player.getItemInHand().getType() == Material.ARROW) {
			Block block = event.getClickedBlock();
			if (block != null) {
				Plot plot = DumbUtil.getPlot(block.getLocation());
				if (plot == null) {
					player.sendMessage("Null Plot");
				} else {
					player.sendMessage("PID = " + plot.getName());
				}
				event.setCancelled(true);
			}
		} else if (player.getItemInHand() != null && player.getItemInHand().getType() == Material.GOLD_NUGGET) {
			Block block = event.getClickedBlock();
			if (block != null) {
				Plot foundIn = null;
				for(Plot plot : plugin.getPlotManager().getAllPlots()) {
					if (plot.has(block.getLocation())) {
						foundIn = plot;
						player.sendMessage("FOUND IN " + plot.getName());
					} else {
						player.sendMessage("NOT IN " + plot.getName());
					}
				}
				if (foundIn != null) {
					player.sendMessage(">> FOUND IN " + foundIn.getName());
				} else {
					player.sendMessage(">> Not found anywhere");
				}
				event.setCancelled(true);
			}
		} else if (player.getItemInHand() != null && player.getItemInHand().getType() == Material.GHAST_TEAR) {
			Block block = event.getClickedBlock();
			if (block != null) {
				ChunkType type = DumbUtil.getChunkType(block.getChunk().getX(), block.getChunk().getZ());
				if (type == ChunkType.CORNER) {
					player.sendMessage("CORNER " + CornerType.getCornerType(block.getLocation()));
				} else if (type == ChunkType.LINEAR_X || type == ChunkType.LINEAR_Z) {
					player.sendMessage(type.name() + " " + LinearSide.getLinearSide(block.getLocation()));
				} else {
					player.sendMessage(type.name());
				}
				event.setCancelled(true);
			}
		} else if (player.getItemInHand() != null && player.getItemInHand().getType() == Material.PAPER) {
			Block block = event.getClickedBlock();
			if (block != null) {
				ChunkType type = DumbUtil.getChunkType(block.getChunk().getX(), block.getChunk().getZ());
				if (type == ChunkType.CORNER) {
					PlotCorner corner = new PlotCorner(block.getChunk().getX(), block.getChunk().getZ(), block.getWorld().getName());
					for(CornerType ct : CornerType.values()) {
						player.sendMessage(ct.name() + " is owned by " + corner.getOwner(ct) + ". PID = " + corner.getId(ct));
					}
				} else {
					player.sendMessage("Not a corner");
				}
				event.setCancelled(true);
			}
		} else if (player.getItemInHand() != null && player.getItemInHand().getType() == Material.PORK) {
			Block block = event.getClickedBlock();
			if (block != null) {
				Plot plot = DumbUtil.getPlot(block.getLocation());
				if (plot != null) {
					player.sendMessage("Resetting plot");
					long start = System.currentTimeMillis();
					ResetPlot.reset(plot);
					long end = System.currentTimeMillis();
					player.sendMessage("Plot is reset");
					player.sendMessage("Time = " + (end - start) + "ms");
				} else {
					player.sendMessage("Plot is null");
				}
				event.setCancelled(true);
			}
		}
	}

}
