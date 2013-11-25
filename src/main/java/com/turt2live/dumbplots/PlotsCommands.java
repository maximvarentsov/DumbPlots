package com.turt2live.dumbplots;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.turt2live.dumbplots.plot.ChunkLoc;
import com.turt2live.dumbplots.plot.CornerPlotCorner;
import com.turt2live.dumbplots.plot.CornerType;
import com.turt2live.dumbplots.plot.Plot;
import com.turt2live.dumbplots.plot.Plot.PlotType;
import com.turt2live.dumbplots.util.DumbUtil;
import com.turt2live.dumbplots.util.ResetPlot;

public class PlotsCommands implements CommandExecutor {

	private DumbPlots plugin = DumbPlots.getInstance();
	private List<String> debugmode = new ArrayList<String>();

	public PlotsCommands() {
		debugmode.clear();
		try {
			BufferedReader in = new BufferedReader(new FileReader(new File(plugin.getDataFolder(), "debugmode.txt")));
			String line;
			while((line = in.readLine()) != null) {
				debugmode.add(line);
				Player player = plugin.getServer().getPlayer(line);
				if (player != null) {
					plugin.sendMessage(player, "You are currently " + ChatColor.GREEN + "in" + ChatColor.WHITE + " debug mode.");
				}
			}
			in.close();
		} catch(IOException e) {}// Consume
	}

	@SuppressWarnings ("deprecation")
	@Override
	public boolean onCommand(final CommandSender sender, Command command, String label, String[] args) {
		if (command.getName().equalsIgnoreCase("plot")) {
			Plot plot = null;
			if (sender instanceof Player) {
				plot = DumbUtil.getPlot(((Player) sender).getLocation());
			}
			if (args.length > 0) {
				if (args[0].equalsIgnoreCase("debug")) {
					if (sender.hasPermission(Permission.DEBUG)) {
						if (sender instanceof Player) {
							Player player = (Player) sender;
							if (debugmode.contains(player.getName())) {
								debugmode.remove(player.getName());
								if (!(args.length > 1 && args[1].equalsIgnoreCase("noclear"))) {
									player.getInventory().clear();
								}
							} else {
								// TODO: ItemMeta
								if (!(args.length > 1 && args[1].equalsIgnoreCase("noclear"))) {
									player.getInventory().clear();
									player.getInventory().setItem(0, new ItemStack(Material.BLAZE_ROD));
									player.getInventory().setItem(1, new ItemStack(Material.BLAZE_POWDER));
									player.getInventory().setItem(2, new ItemStack(Material.COAL));
									player.getInventory().setItem(3, new ItemStack(Material.ARROW));
									player.getInventory().setItem(4, new ItemStack(Material.GOLD_NUGGET));
									player.getInventory().setItem(5, new ItemStack(Material.GHAST_TEAR));
									player.getInventory().setItem(6, new ItemStack(Material.PAPER));
									player.getInventory().setItem(7, new ItemStack(Material.PORK));
								}
								debugmode.add(player.getName());
								plugin.sendMessage(sender, ChatColor.DARK_AQUA + "Blaze Rod" + ChatColor.RED + " - " + ChatColor.AQUA + "Is plot?");
								plugin.sendMessage(sender, ChatColor.DARK_AQUA + "Blaze Powder" + ChatColor.RED + " - " + ChatColor.AQUA + "Location in chunk");
								plugin.sendMessage(sender, ChatColor.DARK_AQUA + "Coal" + ChatColor.RED + " - " + ChatColor.AQUA + "Side of linear chunk. Diamond = right, Gold = left");
								plugin.sendMessage(sender, ChatColor.DARK_AQUA + "Arrow" + ChatColor.RED + " - " + ChatColor.AQUA + "Plot ID");
								plugin.sendMessage(sender, ChatColor.DARK_AQUA + "Gold Nugget" + ChatColor.RED + " - " + ChatColor.AQUA + "Plot finding");
								plugin.sendMessage(sender, ChatColor.DARK_AQUA + "Ghast Tear" + ChatColor.RED + " - " + ChatColor.AQUA + "Chunk type");
								plugin.sendMessage(sender, ChatColor.DARK_AQUA + "Paper" + ChatColor.RED + " - " + ChatColor.AQUA + "Corner information");
								plugin.sendMessage(sender, ChatColor.DARK_AQUA + "Pork" + ChatColor.RED + " - " + ChatColor.AQUA + "Reset/Clear plot");
							}
							player.updateInventory();
							plugin.sendMessage(sender, "You are now " + (debugmode.contains(player.getName()) ? ChatColor.GREEN + "in" : ChatColor.RED + "not in") + ChatColor.WHITE + " debug mode.");
						} else {
							mustBePlayer(sender);
						}
					} else {
						noPermission(sender);
					}
				} else if (args[0].equalsIgnoreCase("claim")) {
					if (sender.hasPermission(Permission.CLAIM)) {
						if (sender instanceof Player) {
							if (plot != null) {
								Player player = (Player) sender;
								if (plot.getPlotType() == PlotType.CLAIMED) {
									plugin.sendMessage(sender, ChatColor.RED + "That plot is already claimed by " + (plot.getOwner().equals(player.getName()) ? "you" : plot.getOwner()));
								} else {
									plot.setOwner(player.getName());
									plot.setName(plugin.getPlotManager().getPlotID(player));
									plot.setPlotType(PlotType.CLAIMED);
									plugin.sendMessage(sender, ChatColor.GREEN + "Welcome to your new plot! " + ChatColor.GRAY + "(Plot ID = " + plot.getName() + ")");
								}
							} else {
								plugin.sendMessage(sender, ChatColor.RED + "That's not a plot!");
							}
						} else {
							mustBePlayer(sender);
						}
					} else {
						noPermission(sender);
					}
				} else if (args[0].equalsIgnoreCase("unclaim")) {
					if (sender.hasPermission(Permission.UNCLAIM)) {
						boolean doCheck = true;
						if (args.length > 1) {
							// Has ID
							String id = args[1];
							plot = plugin.getPlotManager().getPlot(id);
						} else {
							// No ID
							if (!(sender instanceof Player)) {
								mustBePlayer(sender);
								doCheck = false;
							}
						}
						if (doCheck) {
							if (plot != null) {
								if (plot.getPlotType() == PlotType.CLAIMED) {
									if (plot.getOwner().equals(sender.getName()) || sender.hasPermission(Permission.BYPASS)) {
										plot.setOwner("CONSOLE");
										plot.setName(DumbUtil.generateUnclaimedID());
										plot.setPlotType(PlotType.UNCLAIMED);
										ResetPlot.reset(plot);
										plugin.sendMessage(sender, ChatColor.GREEN + "Your plot has been unclaimed.");
									} else {
										plugin.sendMessage(sender, ChatColor.RED + "You don't own this plot!");
									}
								} else {
									plugin.sendMessage(sender, ChatColor.RED + "You don't own this plot!");
								}
							} else {
								plugin.sendMessage(sender, ChatColor.RED + "Plot not found. Check the ID?");
							}
						}
					} else {
						noPermission(sender);
					}
				} else if (args[0].equalsIgnoreCase("free")) {
					if (sender.hasPermission(Permission.FREE)) {
						if (sender instanceof Player) {
							final Player player = (Player) sender;
							plugin.sendMessage(sender, ChatColor.GRAY + "Locating a plot for you...");
							plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, new Runnable() {
								@Override
								public void run() {
									Plot plot = null;
									boolean plotFound = false;
									int px = 0, pz = 0, ring = 0;
									while(!plotFound) {
										int tx = (ring * 2) + 1;
										// TODO wat
										for(int i = -tx; i <= tx; i++) {
											px = i;
											pz = ring;
											plot = DumbUtil.getPlot(px, pz, player.getWorld());
											if (plot != null && plot.getPlotType() == PlotType.UNCLAIMED) {
												plotFound = true;
												break;
											} else {
												pz = -ring;
												plot = DumbUtil.getPlot(px, pz, player.getWorld());
												if (plot != null && plot.getPlotType() == PlotType.UNCLAIMED) {
													plotFound = true;
													break;
												} else {
													px = -i;
													plot = DumbUtil.getPlot(px, pz, player.getWorld());
													if (plot != null && plot.getPlotType() == PlotType.UNCLAIMED) {
														plotFound = true;
														break;
													} else {
														pz = ring;
														plot = DumbUtil.getPlot(px, pz, player.getWorld());
														if (plot != null && plot.getPlotType() == PlotType.UNCLAIMED) {
															plotFound = true;
															break;
														}
													}
												}
											}
										}
										ring++;
									}
									plot.setName(plugin.getPlotManager().getPlotID(player));
									plot.setOwner(player.getName());
									plot.setPlotType(PlotType.CLAIMED);
									plot.assignCorners();
									ChunkLoc a = null;
									for(CornerPlotCorner corner : plot.getCorners()) {
										if (corner.getType() == CornerType.A) {
											a = corner.getLocation();
											break;
										}
									}
									if (a == null) {
										plugin.sendMessage(sender, ChatColor.RED + "Critical error: Corner A does not exist. Contact the developer.");
									} else {
										Chunk chunk = a.getChunk(plot.getWorld());
										int x = (chunk.getX() * 16);
										int z = (chunk.getZ() * 16);
										Location location = new Location(plot.getWorld(), x, chunk.getWorld().getHighestBlockYAt(x, z), z);
										location.setYaw(135f);
										player.teleport(location);
										plugin.sendMessage(sender, ChatColor.GREEN + "Welcome to your new plot! " + ChatColor.GRAY + "(Plot ID = " + plot.getName() + ")");
									}
								}
							});
						} else {
							mustBePlayer(sender);
						}
					} else {
						noPermission(sender);
					}
				} else if (args[0].equalsIgnoreCase("allow")) {
					if (sender.hasPermission(Permission.ALLOW)) {
						if (args.length > 1) {
							boolean doCheck = true;
							if (args.length > 2) {
								plot = plugin.getPlotManager().getPlot(args[2]);
							}
							if (plot == null) {
								plugin.sendMessage(sender, ChatColor.RED + "Plot not found. Check the ID?");
								doCheck = false;
							}
							if (doCheck && args.length < 3 && !(sender instanceof Player)) {
								doCheck = false;
								mustBePlayer(sender);
							}
							if (doCheck) {
								if (plot.getOwner().equals(sender.getName()) || sender.hasPermission(Permission.BYPASS)) {
									OfflinePlayer player = DumbUtil.getPlayer(args[1]);
									if (plot.isAllowed(player)) {
										plugin.sendMessage(sender, ChatColor.RED + player.getName() + " is already allowed to build in " + plot.getName());
									} else {
										plugin.sendMessage(sender, ChatColor.GREEN + player.getName() + " can now build in " + plot.getName());
										plot.addAllowedMember(player);
										if (player.isOnline()) {
											Player pl = player.getPlayer();
											plugin.sendMessage(pl, ChatColor.GREEN + "You can now build in " + plot.getName());
										}
									}
								} else {
									plugin.sendMessage(sender, ChatColor.RED + "You don't own this plot!");
								}
							}
						} else {
							plugin.sendMessage(sender, ChatColor.RED + "Incorrect syntax! Try " + ChatColor.YELLOW + "/plot allow <player> [plot id]");
						}
					} else {
						noPermission(sender);
					}
				} else if (args[0].equalsIgnoreCase("disallow") || args[0].equalsIgnoreCase("deny")) {
					if (sender.hasPermission(Permission.DISALLOW)) {
						if (args.length > 1) {
							boolean doCheck = true;
							if (args.length > 2) {
								plot = plugin.getPlotManager().getPlot(args[2]);
							}
							if (plot == null) {
								plugin.sendMessage(sender, ChatColor.RED + "Plot not found. Check the ID?");
								doCheck = false;
							}
							if (doCheck && args.length < 3 && !(sender instanceof Player)) {
								doCheck = false;
								mustBePlayer(sender);
							}
							if (doCheck) {
								if (plot.getOwner().equals(sender.getName()) || sender.hasPermission(Permission.BYPASS)) {
									OfflinePlayer player = DumbUtil.getPlayer(args[1]);
									if (!plot.isAllowed(player)) {
										plugin.sendMessage(sender, ChatColor.RED + player.getName() + " is already no allowed to build in " + plot.getName());
									} else {
										plugin.sendMessage(sender, ChatColor.GREEN + player.getName() + " can no longer build in " + plot.getName());
										plot.removeAllowedMember(player);
										if (player.isOnline()) {
											Player pl = player.getPlayer();
											plugin.sendMessage(pl, ChatColor.RED + "You can no longer build in " + plot.getName());
										}
									}
								} else {
									plugin.sendMessage(sender, ChatColor.RED + "You don't own this plot!");
								}
							}
						} else {
							plugin.sendMessage(sender, ChatColor.RED + "Incorrect syntax! Try " + ChatColor.YELLOW + "/plot disallow <player> [plot id]");
						}
					} else {
						noPermission(sender);
					}
				} else if (args[0].equalsIgnoreCase("tp") || args[0].equalsIgnoreCase("teleport")) {
					if (sender.hasPermission(Permission.TELEPORT)) {
						if (sender instanceof Player) {
							Player player = (Player) sender;
							if (args.length > 1) {
								plot = plugin.getPlotManager().getPlot(args[1]);
							} else {
								plot = plugin.getPlotManager().getDefaultPlot(player);
							}
							if (plot == null) {
								plugin.sendMessage(sender, ChatColor.RED + "Plot not found. Check the ID?");
							} else {
								ChunkLoc a = null;
								for(CornerPlotCorner corner : plot.getCorners()) {
									if (corner.getType() == CornerType.A) {
										a = corner.getLocation();
										break;
									}
								}
								if (a == null) {
									plugin.sendMessage(sender, ChatColor.RED + "Critical error: Corner A does not exist. Contact the developer.");
								} else {
									Chunk chunk = a.getChunk(plot.getWorld());
									int x = (chunk.getX() * 16) + 7;
									int z = (chunk.getZ() * 16) + 7;
									Location location = new Location(plot.getWorld(), x, chunk.getWorld().getHighestBlockYAt(x, z), z);
									location.setYaw(135f);
									player.teleport(location);
								}
							}
						} else {
							mustBePlayer(sender);
						}
					} else {
						noPermission(sender);
					}
				} else if (args[0].equalsIgnoreCase("reset") || args[0].equalsIgnoreCase("clear")) {
					if (sender.hasPermission(Permission.CLEAR)) {
						boolean doCheck = true;
						if (args.length > 1) {
							plot = plugin.getPlotManager().getPlot(args[1]);
						}
						if (plot == null) {
							plugin.sendMessage(sender, ChatColor.RED + "Plot not found. Check the ID?");
							doCheck = false;
						}
						if (doCheck && args.length < 2 && !(sender instanceof Player)) {
							mustBePlayer(sender);
							doCheck = false;
						}
						if (doCheck) {
							if (plot.getOwner().equals(sender.getName()) || sender.hasPermission(Permission.BYPASS)) {
								ResetPlot.reset(plot);
								plugin.sendMessage(sender, ChatColor.GREEN + "Your plot has been reset.");
							} else {
								plugin.sendMessage(sender, ChatColor.RED + "You don't own this plot!");
							}
						}
					} else {
						noPermission(sender);
					}
				} else if (args[0].equalsIgnoreCase("nukeall")) {
					if (sender.hasPermission(Permission.NUKE)) {
						if (args.length > 1) {
							int deleted = 0;
							for(Plot p : plugin.getPlotManager().getOwnedPlots(DumbUtil.getPlayer(args[1]))) {
								p.setName(DumbUtil.generateUnclaimedID());
								p.setOwner("CONSOLE");
								p.setPlotType(PlotType.UNCLAIMED);
								ResetPlot.reset(p);
								deleted++;
							}
							plugin.sendMessage(sender, ChatColor.GREEN + "Deleted " + deleted + " plot" + (deleted > 1 || deleted == 0 ? "s" : ""));
						} else {
							plugin.sendMessage(sender, ChatColor.RED + "Incorrect Syntax! Try " + ChatColor.YELLOW + "/plot nukeall <player>");
						}
					} else {
						noPermission(sender);
					}
				} else {
					showHelp(sender);
				}
			} else {
				showHelp(sender);
			}
			return true;
		}
		return false;
	}

	private void mustBePlayer(CommandSender sender) {
		plugin.sendMessage(sender, ChatColor.RED + "You kinda need to be a player... Sorry :/");
	}

	private void noPermission(CommandSender sender) {
		plugin.sendMessage(sender, ChatColor.RED + "No permission! Ask your server admin if you think this is a mistake.");
	}

	private void showHelp(CommandSender sender) {
		if (sender.hasPermission(Permission.NUKE)) {
			plugin.sendMessage(sender, ChatColor.DARK_RED + "[W] " + ChatColor.YELLOW + "/plot nukeall <player>" + ChatColor.RED + " - Reset player plots");
		}
		if (sender.hasPermission(Permission.ALLOW)) {
			plugin.sendMessage(sender, ChatColor.YELLOW + "/plot allow <player> [plot]" + ChatColor.RED + " - Allow <player> in your plot");
		}
		if (sender.hasPermission(Permission.DISALLOW)) {
			plugin.sendMessage(sender, ChatColor.YELLOW + "/plot disallow <player> [plot]" + ChatColor.RED + " - Disallow <player> in a plot");
		}
		if (sender.hasPermission(Permission.CLAIM)) {
			plugin.sendMessage(sender, ChatColor.YELLOW + "/plot claim" + ChatColor.RED + " - Claim the plot you are standing in");
		}
		if (sender.hasPermission(Permission.UNCLAIM)) {
			plugin.sendMessage(sender, ChatColor.DARK_RED + "[W] " + ChatColor.YELLOW + "/plot unclaim [plot]" + ChatColor.RED + " - Unclaim a plot.");
		}
		if (sender.hasPermission(Permission.CLEAR)) {
			plugin.sendMessage(sender, ChatColor.DARK_RED + "[W] " + ChatColor.YELLOW + "/plot clear [plot]" + ChatColor.RED + " - Clear a plot");
		}
		if (sender.hasPermission(Permission.FREE)) {
			plugin.sendMessage(sender, ChatColor.YELLOW + "/plot free" + ChatColor.RED + " - Find a random plot");
		}
		if (sender.hasPermission(Permission.TELEPORT)) {
			plugin.sendMessage(sender, ChatColor.YELLOW + "/plot tp <plot>" + ChatColor.RED + " - Teleport to a plot, if you can build in it");
		}
		if (sender.hasPermission(Permission.DEBUG)) {
			plugin.sendMessage(sender, ChatColor.YELLOW + "/plot debug" + ChatColor.RED + " - Toggle debug mode");
		}
		plugin.sendMessage(sender, "A " + ChatColor.DARK_RED + "[W]" + ChatColor.WHITE + " means that the command can be harmful if mistyped");
		plugin.sendMessage(sender, "Square brackets ( [ ] ) are optional. Angled ( < > ) are not.");
		plugin.sendMessage(sender, "'[plot]' will default to the plot you are standing in if not provided.");
	}

	public boolean inDebug(String name) {
		return debugmode.contains(name);
	}

	public void saveDebugModePlayers() {
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(new File(plugin.getDataFolder(), "debugmode.txt"), false));
			for(String name : debugmode) {
				out.write(name);
				out.newLine();
			}
			out.close();
		} catch(IOException e) {}// Consume
	}

}
