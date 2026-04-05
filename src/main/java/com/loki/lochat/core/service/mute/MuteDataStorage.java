package com.loki.lochat.core.service.mute;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.loki.lochat.data.model.MuteData;
import com.loki.lochat.utils.FoliaUtil;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Хранилище данных о мутах
 */
public class MuteDataStorage {
    
    private final JavaPlugin plugin;
    private final Map<UUID, MuteData> mutes = new ConcurrentHashMap<>();
    private final File dataFile;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    
    public MuteDataStorage(JavaPlugin plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "mutes.json");
    }
    
    public void addMute(UUID uuid, MuteData data) {
        mutes.put(uuid, data);
        saveAsync();
    }
    
    public MuteData removeMute(UUID uuid) {
        MuteData removed = mutes.remove(uuid);
        if (removed != null) {
            saveAsync();
        }
        return removed;
    }
    
    public boolean isMuted(UUID uuid) {
        if (uuid == null) return false;

        MuteData data = mutes.get(uuid);
        if (data == null) return false;

        if (!isExpired(data)) return true;

        boolean removed = mutes.remove(uuid, data);
        if (removed) {
            saveAsync();
            plugin.getLogger().info("Mute expired for player " + data.getPlayerName());
        }
        return false;
    }
    
    public MuteData getMute(UUID uuid) {
        return mutes.get(uuid);
    }
    
    public Map<UUID, MuteData> getAllMutes() {
        mutes.entrySet().removeIf(entry -> isExpired(entry.getValue()));
        return new HashMap<>(mutes);
    }
    
    private boolean isExpired(MuteData data) {
        return data.getEndTime() > 0 && System.currentTimeMillis() > data.getEndTime();
    }
    
    public void load() {
        if (!dataFile.exists()) {
            return;
        }

        try (Reader reader = new FileReader(dataFile)) {
            Type type = new TypeToken<Map<String, MuteData>>() {}.getType();
            Map<String, MuteData> loaded = gson.fromJson(reader, type);
            if (loaded != null) {
                loaded.forEach((key, value) -> {
                    try {
                        mutes.put(UUID.fromString(key), value);
                    } catch (IllegalArgumentException ignored) {
                    }
                });
            }
        } catch (IOException e) {
            plugin.getLogger().warning("Error loading mutes: " + e.getMessage());
        }
    }
    
    public void save() {
        try {
            ensureDataFolderExists();
            
            Map<String, MuteData> toSave = new ConcurrentHashMap<>();
            mutes.forEach((uuid, data) -> toSave.put(uuid.toString(), data));

            try (Writer writer = new FileWriter(dataFile)) {
                gson.toJson(toSave, writer);
            }
        } catch (IOException e) {
            plugin.getLogger().warning("Error saving mutes: " + e.getMessage());
        }
    }
    
    private void saveAsync() {
        FoliaUtil.runAsync(plugin, this::save);
    }
    
    private void ensureDataFolderExists() {
        if (!dataFile.getParentFile().exists()) {
            dataFile.getParentFile().mkdirs();
        }
    }
}
