package com.loki.lohub.common;

import org.bukkit.configuration.file.FileConfiguration;

public final class ConfigHelper {

    private ConfigHelper() {
    }

    public static boolean isEnabled(FileConfiguration config, String path) {
        return config.getBoolean(path + ".enabled", true);
    }

    public static double getPower(FileConfiguration config, String path, String key, double defaultValue) {
        return config.getDouble(path + "." + key, defaultValue);
    }

    public static int getCooldown(FileConfiguration config, String path, int defaultValue) {
        return config.getInt(path + ".cooldown", defaultValue);
    }
}
