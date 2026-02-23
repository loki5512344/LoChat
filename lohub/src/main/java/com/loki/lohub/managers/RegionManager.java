package com.loki.lohub.managers;

import com.loki.lohub.LoHub;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import org.bukkit.Location;
import org.bukkit.World;

public class RegionManager {

    private static final String HUB_REGION_NAME = "lohub_spawn";
    private final LoHub plugin;

    public RegionManager(LoHub plugin) {
        this.plugin = plugin;
    }

    public void createHubRegion() {
        if (!plugin.getConfig().getBoolean("worldguard.auto-create-region", true)) {
            plugin.getLogger().info("WorldGuard region auto-creation is disabled");
            return;
        }

        if (!isWorldGuardAvailable()) {
            plugin.getLogger().warning("WorldGuard not found! Region will not be created.");
            return;
        }

        Location spawn = plugin.getSpawnManager().getSpawn();
        if (spawn == null || spawn.getWorld() == null) {
            plugin.getLogger().warning("Spawn not set! Use /sethub");
            return;
        }

        World world = spawn.getWorld();
        int radius = plugin.getConfig().getInt("worldguard.region-radius", 100);

        try {
            RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
            com.sk89q.worldguard.protection.managers.RegionManager wgRegionManager = container.get(BukkitAdapter.adapt(world));

            if (wgRegionManager == null) {
                plugin.getLogger().warning("Failed to get RegionManager for world " + world.getName());
                return;
            }

            ProtectedRegion existingRegion = wgRegionManager.getRegion(HUB_REGION_NAME);
            if (existingRegion != null) {
                plugin.getLogger().info("Region '" + HUB_REGION_NAME + "' already exists");
                return;
            }

            BlockVector3 min = BlockVector3.at(
                    spawn.getBlockX() - radius,
                    world.getMinHeight(),
                    spawn.getBlockZ() - radius
            );

            BlockVector3 max = BlockVector3.at(
                    spawn.getBlockX() + radius,
                    world.getMaxHeight(),
                    spawn.getBlockZ() + radius
            );

            ProtectedCuboidRegion region = new ProtectedCuboidRegion(HUB_REGION_NAME, min, max);
            setRegionFlags(region);

            wgRegionManager.addRegion(region);

            plugin.getLogger().info("Region '" + HUB_REGION_NAME + "' created around spawn (radius: " + radius + " blocks)");
            plugin.getLogger().info("Coordinates: " + min + " -> " + max);

        } catch (Exception e) {
            plugin.getLogger().severe("Error creating WorldGuard region: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setRegionFlags(ProtectedCuboidRegion region) {
        region.setFlag(Flags.PVP, StateFlag.State.DENY);
        region.setFlag(Flags.BLOCK_BREAK, StateFlag.State.DENY);
        region.setFlag(Flags.BLOCK_PLACE, StateFlag.State.DENY);
        region.setFlag(Flags.DAMAGE_ANIMALS, StateFlag.State.DENY);
        region.setFlag(Flags.MOB_SPAWNING, StateFlag.State.DENY);
        region.setFlag(Flags.LEAF_DECAY, StateFlag.State.DENY);
        region.setFlag(Flags.ITEM_DROP, StateFlag.State.DENY);
        region.setFlag(Flags.ITEM_PICKUP, StateFlag.State.DENY);
    }

    public void deleteHubRegion() {
        if (!isWorldGuardAvailable()) {
            return;
        }

        Location spawn = plugin.getSpawnManager().getSpawn();
        if (spawn == null || spawn.getWorld() == null) {
            return;
        }

        try {
            RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
            com.sk89q.worldguard.protection.managers.RegionManager wgRegionManager = container.get(BukkitAdapter.adapt(spawn.getWorld()));

            if (wgRegionManager != null) {
                wgRegionManager.removeRegion(HUB_REGION_NAME);
                plugin.getLogger().info("Region '" + HUB_REGION_NAME + "' deleted");
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Error deleting region: " + e.getMessage());
        }
    }

    private boolean isWorldGuardAvailable() {
        try {
            Class.forName("com.sk89q.worldguard.WorldGuard");
            return plugin.getServer().getPluginManager().isPluginEnabled("WorldGuard");
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
