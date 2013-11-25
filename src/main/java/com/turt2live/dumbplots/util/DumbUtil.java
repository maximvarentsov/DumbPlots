package com.turt2live.dumbplots.util;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.EntityType;

import com.turt2live.dumbplots.DumbPlots;
import com.turt2live.dumbplots.plot.ChunkLoc;
import com.turt2live.dumbplots.plot.ChunkType;
import com.turt2live.dumbplots.plot.CornerType;
import com.turt2live.dumbplots.plot.LinearSide;
import com.turt2live.dumbplots.plot.Plot;
import com.turt2live.dumbplots.plot.Plot.PlotType;
import com.turt2live.dumbplots.plot.PlotCorner;

public class DumbUtil {

	public static String moneyFormat(double amount) {
		NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US);
		return currencyFormatter.format(amount);
	}

	@SuppressWarnings ("deprecation")
	public static Material getMaterial(String raw) {
		if (Integer.valueOf(raw) != null) {
			int i = Integer.valueOf(raw);
			return Material.getMaterial(i);
		}
		return Material.matchMaterial(raw);
	}

	public static String listToString(List<String> list) {
		StringBuilder builder = new StringBuilder();
		if (list.size() <= 0) {
			return "no one";
		}
		for(String s : list) {
			builder.append(s).append(", ");
		}
		String result = builder.toString();
		return result.trim().substring(0, result.length() - 2);
	}

	public static boolean isPlotPath(Location location) {
		ChunkType chunk = getChunkType(location.getChunk().getX(), location.getChunk().getZ());
		int x = getXInChunk(location);
		int z = getZInChunk(location);
		switch (chunk) {
		case CORNER:
			if ((x > 4 && x < 11 && z > 4 && z < 11)
					|| (x < 5 && z < 11 && z > 4)
					|| (x > 8 && z < 11 && z > 4)
					|| (z < 5 && x < 11 && x > 4)
					|| (z > 8 && x < 11 && x > 4)
					|| (x == 4 && x < 5)
					|| (x == 11 && x > 12)
					|| (x == 11 && x < 12)
					|| (z == 4 && z < 5)
					|| (z == 11 && z < 12)) {
				return true;
			}
			return false;
		case LINEAR_X:
			return !(x > 11 || x < 4);
		case LINEAR_Z:
			return !(z > 11 || z < 4);
		default:
			return false;
		}
	}

	public static int getZInChunk(Location location) {
		return (location.getBlockZ() % 16 + 16) % 16;
	}

	public static int getXInChunk(Location location) {
		return (location.getBlockX() % 16 + 16) % 16;
	}

	public static ChunkType getChunkType(int chunkX, int chunkZ) {
		if (chunkX % 4 == 0 && chunkZ % 4 == 0) {
			return ChunkType.CORNER;
		} else if (chunkX % 4 == 0 && chunkZ % 4 != 0) {
			return ChunkType.LINEAR_X;
		} else if (chunkX % 4 != 0 && chunkZ % 4 != 0) {
			return ChunkType.FLAT;
		} else if (chunkX % 4 != 0 && chunkZ % 4 == 0) {
			return ChunkType.LINEAR_Z;
		}
		return ChunkType.UNKNOWN;
	}

	public static ChunkType getChunkType(ChunkLoc chunk) {
		return getChunkType(chunk.getX(), chunk.getZ());
	}

	public static PlotType getPlotType(Location location) {
		if (isPlotPath(location)) {
			return PlotType.INVALID;
		} else {
			Plot plot = getPlot(location);
			return plot == null ? PlotType.INVALID : plot.getPlotType();
		}
	}

	public static Plot getPlot(Location location) {
		if (isPlotPath(location) || !DumbPlots.getInstance().getPlotManager().isWorldManaged(location.getWorld())) {
			return null;
		} else {
			// Find a corner (corners hold owner information)
			int chunkX = location.getChunk().getX();
			int chunkZ = location.getChunk().getZ();
			Location newLocation = location.clone();
			Location startLocation = location.clone();
			ChunkType type = getChunkType(chunkX, chunkZ);
			int c = 0;
			switch (type) {
			case CORNER:
				break;
			case LINEAR_Z:
				// Find a corner by looping down X
				boolean cornerFound = false;
				int cx = chunkX;
				c = 0;
				while(!cornerFound) {
					cx--;
					newLocation.setX(newLocation.getX() - 16);
					ChunkType ctype = getChunkType(cx, chunkZ);
					if (ctype == ChunkType.CORNER) {
						cornerFound = true;
					}
					c++;
					if (c > 10) {
						break;
					}
				}
				chunkX = cx;
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
				// Find a corner by looping down Z
				cornerFound = false;
				int cz = chunkZ;
				c = 0;
				while(!cornerFound) {
					cz--;
					newLocation.setZ(newLocation.getZ() - 16);
					ChunkType ctype = getChunkType(chunkX, cz);
					if (ctype == ChunkType.CORNER) {
						cornerFound = true;
					}
					c++;
					if (c > 10) {
						break;
					}
				}
				chunkZ = cz;
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
				// We need to find the nearest edge (LINEAR_X/Z)
				boolean edgeFound = false;
				ChunkType edge = ChunkType.UNKNOWN;
				cx = chunkX;
				c = 0;
				while(!edgeFound) {
					cx--;
					newLocation.setX(newLocation.getX() - 16);
					ChunkType ctype = getChunkType(cx, chunkZ);
					if (ctype == ChunkType.LINEAR_X || ctype == ChunkType.LINEAR_Z) {
						edgeFound = true;
						edge = ctype;
					}
					c++;
					if (c > 10) {
						break;
					}
				}
				chunkX = cx;
				// Correct location (move to relative 15,15 in chunk)
				newLocation.setX((chunkX * 16) + 15);
				newLocation.setZ((chunkZ * 16) + 15);
				switch (edge) {
				case LINEAR_Z:
					// Find a corner by looping down X
					cornerFound = false;
					cx = chunkX;
					c = 0;
					while(!cornerFound) {
						cx--;
						newLocation.setX(newLocation.getX() - 16);
						ChunkType ctype = getChunkType(cx, chunkZ);
						if (ctype == ChunkType.CORNER) {
							cornerFound = true;
						}
						c++;
						if (c > 10) {
							break;
						}
					}
					chunkX = cx;
				case LINEAR_X:
					// Find a corner by looping down Z
					cornerFound = false;
					cz = chunkZ;
					c = 0;
					while(!cornerFound) {
						cz--;
						newLocation.setZ(newLocation.getZ() - 16);
						ChunkType ctype = getChunkType(chunkX, cz);
						if (ctype == ChunkType.CORNER) {
							cornerFound = true;
						}
						c++;
						if (c > 10) {
							break;
						}
					}
					chunkZ = cz;
				default:
					break;
				}
				break;
			default:
				break;
			}
			// Try to identify the plot by the corner
			CornerType corner = CornerType.getCornerType(newLocation);
			PlotCorner pcorner = new PlotCorner(chunkX, chunkZ, location.getWorld().getName());
			String id = pcorner.getID(corner);
			if (id != null) {
				return DumbPlots.getInstance().getPlotManager().getPlot(id);
			} else {
				Plot plot = Plot.generateUnclaimedPlot(location);
				DumbPlots.getInstance().getPlotManager().addPlot(plot);
				return plot;
			}
		}
	}

	@Deprecated
	// Unsafe
	public static Location asCorner(Location location) {
		return new Location(location.getWorld(), DumbUtil.getXInChunk(location), location.getY(), DumbUtil.getZInChunk(location));
	}

	public static ChunkLoc oppositeCorner(ChunkLoc chunk, CornerType thisCorner) {
		return getCorner(chunk, thisCorner, thisCorner.opposite());
	}

	public static ChunkLoc getCorner(ChunkLoc currentCorner, CornerType thisCorner, CornerType desiredCorner) {
		int xm = 0;
		int zm = 0;
		if (thisCorner == desiredCorner) {
			return currentCorner;
		} else {
			switch (thisCorner) {
			case A:
				switch (desiredCorner) {
				case B:
					xm = 4;
					break;
				case D:
					zm = 4;
					break;
				case C:
					xm = 4;
					zm = 4;
					break;
				default:
					break;
				}
				break;
			case B:
				switch (desiredCorner) {
				case A:
					xm = -4;
					break;
				case C:
					zm = 4;
					break;
				case D:
					xm = -4;
					zm = 4;
					break;
				default:
					break;
				}
				break;
			case C:
				switch (desiredCorner) {
				case B:
					zm = -4;
					break;
				case D:
					xm = -4;
					break;
				case A:
					xm = -4;
					zm = -4;
					break;
				default:
					break;
				}
				break;
			case D:
				switch (desiredCorner) {
				case A:
					zm = -4;
					break;
				case C:
					xm = 4;
					break;
				case B:
					xm = 4;
					zm = -4;
					break;
				default:
					break;
				}
				break;
			default:
				break;
			}
		}
		ChunkLoc nloc = new ChunkLoc(currentCorner.getX() - xm, currentCorner.getZ() - zm);
		return nloc;
	}

	public static ChunkLoc[] findCorners(Location location, int chunkX, int chunkZ) {
		ChunkLoc a = new ChunkLoc(0, 0), b = new ChunkLoc(0, 0), c = new ChunkLoc(0, 0), d = new ChunkLoc(0, 0);
		switch (CornerType.getCornerType(location)) {
		case A:
			a = new ChunkLoc(chunkX, chunkZ);
			c = oppositeCorner(a, CornerType.A);
			b = getCorner(a, CornerType.A, CornerType.B);
			d = oppositeCorner(b, CornerType.B);
			break;
		case B:
			b = new ChunkLoc(chunkX, chunkZ);
			d = oppositeCorner(b, CornerType.B);
			a = getCorner(b, CornerType.B, CornerType.A);
			c = oppositeCorner(a, CornerType.A);
			break;
		case C:
			c = new ChunkLoc(chunkX, chunkZ);
			a = oppositeCorner(c, CornerType.C);
			d = getCorner(c, CornerType.C, CornerType.D);
			b = oppositeCorner(d, CornerType.D);
			break;
		case D:
			d = new ChunkLoc(chunkX, chunkZ);
			b = oppositeCorner(d, CornerType.D);
			c = getCorner(d, CornerType.D, CornerType.C);
			a = oppositeCorner(c, CornerType.C);
			break;
		default:
			break;
		}
		ChunkLoc[] ret = {a, b, c, d};
		return ret;
	}

	public static boolean isPeacefulMob(EntityType type) {
		switch (type) {
		case WOLF:
		case VILLAGER:
		case SQUID:
		case SNOWMAN:
		case SHEEP:
		case PIG:
		case OCELOT:
		case MUSHROOM_COW:
		case IRON_GOLEM:
		case COW:
		case CHICKEN:
			return true;
		default:
			return false;
		}
	}

	public static boolean doControl(EntityType type) {
		switch (type) {
		case ARROW:
		case BOAT:
		case COMPLEX_PART:
		case DROPPED_ITEM:
		case ENDER_CRYSTAL:
		case ENDER_PEARL:
		case ENDER_SIGNAL:
		case EXPERIENCE_ORB:
		case FALLING_BLOCK:
		case FIREBALL:
		case FISHING_HOOK:
		case LIGHTNING:
		case MINECART:
		case PRIMED_TNT:
		case SMALL_FIREBALL:
		case SNOWBALL:
		case SPLASH_POTION:
		case THROWN_EXP_BOTTLE:
		case UNKNOWN:
		case WEATHER:
			return false;
		default:
			return true;
		}
	}

	public static String generateUnclaimedID() {
		return "Unclaimed-" + String.valueOf(System.currentTimeMillis());
	}

	public static OfflinePlayer getPlayer(String name) {
		OfflinePlayer player = DumbPlots.getInstance().getServer().getPlayer(name);
		if (player == null) {
			for(OfflinePlayer potential : DumbPlots.getInstance().getServer().getOfflinePlayers()) {
				if (potential.getName().toLowerCase().startsWith(name.toLowerCase())) {
					player = potential;
					break;
				}
			}
			if (player == null) {
				player = DumbPlots.getInstance().getServer().getOfflinePlayer(name);
			}
		}
		return player;
	}

	public static Plot getPlot(int x, int z, World world) {
		Location location = new Location(world, (x * 64) + ((x * 64) < 0 ? -23 : 23), 16, (z * 64) + ((z * 64) < 0 ? -23 : 23));
		return getPlot(location);
	}

	public static long nextId() {
		// TODO Auto-generated method stub
		return 0;
	}

}
