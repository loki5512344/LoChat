package com.loki.lohub.managers;

import com.loki.lohub.LoHub;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class SpawnManager {

    private final LoHub plugin;

    public SpawnManager(LoHub plugin) {
        this.plugin = plugin;
    }

    public Location getSpawn() {
        String worldName = plugin.getConfigManager().getData().getString("spawn.world");
        if (worldName == null || worldName.equals("null")) {
            return null;
        }

        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            plugin.getLogger().warning("World '" + worldName + "' not found!");
            return null;
        }

        double x = plugin.getConfigManager().getData().getDouble("spawn.x");
        double y = plugin.getConfigManager().getData().getDouble("spawn.y");
        double z = plugin.getConfigManager().getData().getDouble("spawn.z");
        float yaw = (float) plugin.getConfigManager().getData().getDouble("spawn.yaw");
        float pitch = (float) plugin.getConfigManager().getData().getDouble("spawn.pitch");

        return new Location(world, x, y, z, yaw, pitch);
    }

    public void setSpawn(Location location) {
        plugin.getConfigManager().getData().set("spawn.world", location.getWorld().getName());
        plugin.getConfigManager().getData().set("spawn.x", location.getX());
        plugin.getConfigManager().getData().set("spawn.y", location.getY());
        plugin.getConfigManager().getData().set("spawn.z", location.getZ());
        plugin.getConfigManager().getData().set("spawn.yaw", location.getYaw());
        plugin.getConfigManager().getData().set("spawn.pitch", location.getPitch());
        plugin.getConfigManager().saveData();

        plugin.getLogger().info("Spawn set: " + location.getWorld().getName() +
                " (" + location.getBlockX() + ", " + location.getBlockY() + ", " + location.getBlockZ() + ")");
    }

    public void teleportToSpawn(Player player) {
        Location spawn = getSpawn();
        if (spawn == null) {
            return;
        }

        player.teleport(spawn);
    }
}
