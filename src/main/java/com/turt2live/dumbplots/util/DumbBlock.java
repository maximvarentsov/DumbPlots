package com.turt2live.dumbplots.util;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;

public class DumbBlock {

    private double x, y, z;
    private World world;
    private Material material;
    private byte data;
    private Biome biome;

    @SuppressWarnings("deprecation")
    public DumbBlock(Block block) {
        this.x = block.getX();
        this.y = block.getY();
        this.z = block.getZ();
        this.world = block.getWorld();
        this.material = block.getType();
        this.data = block.getData();
        this.biome = block.getBiome();
    }

    @SuppressWarnings("deprecation")
    public void resetBlock() {
        Location location = new Location(world, x, y, z);
        Block at = location.getBlock();
        at.setType(material);
        at.setBiome(biome);
        at.setData(data);
    }

}
