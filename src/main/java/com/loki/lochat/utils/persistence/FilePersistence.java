package com.loki.lochat.utils.persistence;

import com.google.gson.Gson;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

public final class FilePersistence {

    private FilePersistence() {}

    public static File getFile(JavaPlugin plugin, String name) {
        return new File(plugin.getDataFolder(), name);
    }

    public static void ensureDataFolder(JavaPlugin plugin) {
        plugin.getDataFolder().mkdirs();
    }

    public static FileConfiguration loadYaml(JavaPlugin plugin, String name) {
        File file = getFile(plugin, name);
        return file.exists() ? YamlConfiguration.loadConfiguration(file) : new YamlConfiguration();
    }

    public static void saveYaml(JavaPlugin plugin, String name, FileConfiguration config) {
        try {
            File file = getFile(plugin, name);
            file.getParentFile().mkdirs();
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save " + name + ": " + e.getMessage());
        }
    }

    public static <T> T loadJson(JavaPlugin plugin, String name, Class<T> clazz, Gson gson) {
        File file = getFile(plugin, name);
        if (!file.exists()) {
            return null;
        }
        try (Reader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
            return gson.fromJson(reader, clazz);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to load " + name + ": " + e.getMessage());
            return null;
        }
    }

    public static void saveJson(JavaPlugin plugin, String name, Object data, Gson gson) {
        try {
            File file = getFile(plugin, name);
            file.getParentFile().mkdirs();
            try (Writer writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
                gson.toJson(data, writer);
            }
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save " + name + ": " + e.getMessage());
        }
    }
}
