package com.turt2live.dumbplots.terrain;

import java.util.Random;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.generator.BlockPopulator;

public class TerrainPopulator extends BlockPopulator {

	private int scanH = 16;

	public TerrainPopulator(int scanH) {
		this.scanH = scanH;
	}

	@SuppressWarnings ("deprecation")
	@Override
	public void populate(World world, Random random, Chunk source) {
		for(int x = 0; x < 16; x++) {
			for(int z = 0; z < 16; z++) {
				Block block = source.getBlock(x, scanH + 1, z);
				if (block.getType() == Material.DIAMOND_BLOCK) {
					block.setType(Material.FENCE_GATE);
					block.setData((byte) 0x3);
				} else if (block.getType() == Material.GOLD_BLOCK) {
					block.setType(Material.FENCE_GATE);
				}
			}
		}
	}

}
