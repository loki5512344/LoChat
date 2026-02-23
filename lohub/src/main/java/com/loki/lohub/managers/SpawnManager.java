package com.loki.lohub.managers;

import com.loki.lohub.LoHub;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;

public class SpawnManager {

    private final LoHub plugin;
    private final File spawnFile;
    private FileConfiguration spawnConfig;
    private Location spawnLocation;

    public SpawnManager(LoHub plugin) {
        this.plugin = plugin;
        this.spawnFile = new File(plugin.getDataFolder(), "spawn.yml");
        loadSpawn();
    }

    private void loadSpawn() {
        if (!spawnFile.exists()) {
            try {
                spawnFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Failed to create spawn.yml: " + e.getMessage());
            }
        }

        spawnConfig = YamlConfiguration.loadConfiguration(spawnFile);

        if (spawnConfig.contains("spawn")) {
            String worldName = spawnConfig.getString("spawn.world");
            double x = spawnConfig.getDouble("spawn.x");
            double y = spawnConfig.getDouble("spawn.y");
            double z = spawnConfig.getDouble("spawn.z");
            float yaw = (float) spawnConfig.getDouble("spawn.yaw");
            float pitch = (float) spawnConfig.getDouble("spawn.pitch");

            World world = Bukkit.getWorld(worldName);
            if (world != null) {
                spawnLocation = new Location(world, x, y, z, yaw, pitch);
                plugin.getLogger().info("Spawn loaded: " + worldName + " (" + x + ", " + y + ", " + z + ")");
            } else {
                plugin.getLogger().warning("World '" + worldName + "' not found!");
            }
        }
    }

    public Location getSpawn() {
        return spawnLocation;
    }

    public void setSpawn(Location location) {
        this.spawnLocation = location;

        spawnConfig.set("spawn.world", location.getWorld().getName());
        spawnConfig.set("spawn.x", location.getX());
        spawnConfig.set("spawn.y", location.getY());
        spawnConfig.set("spawn.z", location.getZ());
        spawnConfig.set("spawn.yaw", location.getYaw());
        spawnConfig.set("spawn.pitch", location.getPitch());

        try {
            spawnConfig.save(spawnFile);
            plugin.getLogger().info("Spawn set: " + location.getWorld().getName() +
                    " (" + location.getBlockX() + ", " + location.getBlockY() + ", " + location.getBlockZ() + ")");
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save spawn.yml: " + e.getMessage());
        }
    }

    public void teleportToSpawn(Player player) {
        if (spawnLocation == null) {
            return;
        }

        player.teleport(spawnLocation);
    }
}
