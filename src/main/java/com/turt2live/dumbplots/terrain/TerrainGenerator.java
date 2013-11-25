package com.turt2live.dumbplots.terrain;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator;

import com.turt2live.dumbplots.plot.ChunkType;
import com.turt2live.dumbplots.util.DumbUtil;

public class TerrainGenerator extends ChunkGenerator {

	private int height;
	private Logger logger;

	public TerrainGenerator(String id, Logger log) {
		logger = log;

		//Parse id
		if (id == null) {
			height = 16;
		} else {
			try {
				height = Integer.parseInt(id);
			} catch(NumberFormatException e) {
				logger.info("Invalid Height! Set to 16.");
				height = 16;
			}
		}
	}

	private void setBlock(byte[][] result, int x, int y, int z, byte blkid) {
		if (result[y >> 4] == null) {
			result[y >> 4] = new byte[4096];
		}
		result[y >> 4][((y & 0xF) << 8) | (z << 4) | x] = blkid;
	}

	@SuppressWarnings ("deprecation")
	@Override
	public byte[][] generateBlockSections(World world, Random random, int chunkX, int chunkZ, BiomeGrid biomeGrid) {
		byte[][] result = new byte[16][];
		int x, y, z;

		//Sets the layers
		for(x = 0; x < 16; x++) {
			for(z = 0; z < 16; z++) {
				// Set biome
				biomeGrid.setBiome(x, z, Biome.FOREST);

				// Set bedrock
				setBlock(result, x, 0, z, (byte) Material.BEDROCK.getId());

				// Set dirt
				for(y = 1; y < height - 1; y++) {
					setBlock(result, x, y, z, (byte) Material.DIRT.getId());
				}

				// Set surface
				ChunkType ctype = DumbUtil.getChunkType(chunkX, chunkZ);
				switch (ctype) {
				case CORNER:
					if ((x > 4 && x < 11 && z > 4 && z < 11) // Fills center
							|| (x < 5 && z < 11 && z > 4) // Fill X-Direction
							|| (x > 8 && z < 11 && z > 4)
							|| (z < 5 && x < 11 && x > 4) // Fill Z-Direction
							|| (z > 8 && x < 11 && x > 4)) {
						setBlock(result, x, height - 1, z, (byte) Material.SMOOTH_BRICK.getId());
					} else if ((x == 4 && x < 5) // Weird math to determine where the L's of fences should be
							|| (x == 11 && x > 12)
							|| (x == 11 && x < 12)
							|| (z == 4 && z < 5)
							|| (z == 11 && z < 12)) {
						setBlock(result, x, height - 1, z, (byte) Material.GLOWSTONE.getId());
						setBlock(result, x, height, z, (byte) Material.FENCE.getId());
					} else {
						setBlock(result, x, height - 1, z, (byte) Material.GRASS.getId());
					}
					break;
				case LINEAR_X:
				case LINEAR_Z:
					int v = ctype == ChunkType.LINEAR_X ? x : z;
					if (v > 11 || v < 4) {
						setBlock(result, x, height - 1, z, (byte) Material.GRASS.getId());
					} else if (v == 4 || v == 11) {
						setBlock(result, x, height - 1, z, (byte) Material.GLOWSTONE.getId());
						setBlock(result, x, height, z, (byte) Material.FENCE.getId());
					} else {
						setBlock(result, x, height - 1, z, (byte) Material.SMOOTH_BRICK.getId());
					}
					break;
				case FLAT:
					setBlock(result, x, height - 1, z, (byte) Material.GRASS.getId());
					break;
				default: // OH NOES case
					setBlock(result, x, height - 1, z, (byte) Material.LEAVES.getId());
				}
			}
		}
		return result;
	}

	@Override
	public List<BlockPopulator> getDefaultPopulators(World world) {
		List<BlockPopulator> pops = new ArrayList<BlockPopulator>();
		//pops.add(new TerrainPopulator(height - 1));
		return pops;
	}

	@Override
	public Location getFixedSpawnLocation(World world, Random random) {
		return new Location(world, 0, height + 2, 0);
	}

}