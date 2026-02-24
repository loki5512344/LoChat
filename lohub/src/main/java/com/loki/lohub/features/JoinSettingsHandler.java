package com.loki.lohub.features;

import com.loki.lohub.LoHub;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public final class JoinSettingsHandler {

    private static final String CONFIG_PATH = "join_settings";

    private JoinSettingsHandler() {
    }

    public static void apply(Player player, LoHub plugin) {
        FileConfiguration config = plugin.getConfig();

        teleportToSpawn(player, plugin, config);
        healPlayer(player, config);
        extinguishPlayer(player, config);
        clearInventory(player, config);
        giveItems(player, plugin);
    }

    private static void teleportToSpawn(Player player, LoHub plugin, FileConfiguration config) {
        if (!config.getBoolean(CONFIG_PATH + ".spawn_join", true)) {
            return;
        }

        Location spawn = plugin.getSpawnManager().getSpawn();
        if (spawn != null) {
            player.teleport(spawn);
        }
    }

    private static void healPlayer(Player player, FileConfiguration config) {
        if (!config.getBoolean(CONFIG_PATH + ".heal", true)) {
            return;
        }

        player.setHealth(player.getMaxHealth());
        player.setFoodLevel(20);
        player.setSaturation(20);
    }

    private static void extinguishPlayer(Player player, FileConfiguration config) {
        if (config.getBoolean(CONFIG_PATH + ".extinguish", true)) {
            player.setFireTicks(0);
        }
    }

    private static void clearInventory(Player player, FileConfiguration config) {
        if (config.getBoolean(CONFIG_PATH + ".clear_inventory", false)) {
            player.getInventory().clear();
        }
    }

    private static void giveItems(Player player, LoHub plugin) {
        plugin.getHotbarManager().giveItems(player);
        plugin.getPlayerHiderManager().giveItem(player);
    }
}
