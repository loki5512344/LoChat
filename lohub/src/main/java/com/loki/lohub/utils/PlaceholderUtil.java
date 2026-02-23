package com.loki.lohub.utils;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class PlaceholderUtil {

    private static boolean placeholderAPIEnabled = false;

    private PlaceholderUtil() {
        // Utility class - prevent instantiation
    }

    public static void init() {
        placeholderAPIEnabled = Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI");
    }

    public static String parse(String text, Player player) {
        if (text == null) {
            return "";
        }

        text = parseInternalPlaceholders(text, player);

        if (placeholderAPIEnabled && player != null) {
            text = PlaceholderAPI.setPlaceholders(player, text);
        }

        return text;
    }

    private static String parseInternalPlaceholders(String text, Player player) {
        if (player != null) {
            text = text.replace("%player%", player.getName());
            text = text.replace("%player_displayname%", player.getDisplayName());

            if (text.contains("%location%")) {
                Location loc = player.getLocation();
                text = text.replace("%location%",
                        loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ());
            }

            if (text.contains("%world%")) {
                text = text.replace("%world%", player.getWorld().getName());
            }
        }

        text = text.replace("%online%", String.valueOf(Bukkit.getOnlinePlayers().size()));
        text = text.replace("%online_max%", String.valueOf(Bukkit.getMaxPlayers()));

        return text;
    }

    public static boolean isPlaceholderAPIEnabled() {
        return placeholderAPIEnabled;
    }
}
