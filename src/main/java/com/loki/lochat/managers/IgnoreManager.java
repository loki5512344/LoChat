package com.loki.lochat.managers;

import com.loki.lochat.LoChat;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class IgnoreManager {

    private final LoChat plugin;
    private final File ignoreFile;
    private final Map<UUID, Set<UUID>> ignoreMap = new ConcurrentHashMap<>();

    public IgnoreManager(LoChat plugin) {
        this.plugin = plugin;
        this.ignoreFile = new File(plugin.getDataFolder(), "ignores.yml");
        load();
    }

    public void load() {
        if (!ignoreFile.exists()) return;

        YamlConfiguration config = YamlConfiguration.loadConfiguration(ignoreFile);
        for (String key : config.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(key);
                List<String> ignoredList = config.getStringList(key);
                Set<UUID> ignored = ConcurrentHashMap.newKeySet();

                for (String ignoredStr : ignoredList) {
                    try {
                        ignored.add(UUID.fromString(ignoredStr));
                    } catch (Exception ignored2) {}
                }

                if (!ignored.isEmpty()) {
                    ignoreMap.put(uuid, ignored);
                }
            } catch (Exception ignored) {}
        }
    }

    public void save() {
        YamlConfiguration config = new YamlConfiguration();

        for (Map.Entry<UUID, Set<UUID>> entry : ignoreMap.entrySet()) {
            List<String> ignoredList = new ArrayList<>();
            for (UUID ignored : entry.getValue()) {
                ignoredList.add(ignored.toString());
            }
            config.set(entry.getKey().toString(), ignoredList);
        }

        try {
            config.save(ignoreFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Не удалось сохранить игноры: " + e.getMessage());
        }
    }

    public boolean isIgnoring(UUID player, UUID target) {
        Set<UUID> ignored = ignoreMap.get(player);
        return ignored != null && ignored.contains(target);
    }

    public boolean addIgnore(UUID player, UUID target) {
        Set<UUID> ignored = ignoreMap.computeIfAbsent(player, k -> ConcurrentHashMap.newKeySet());
        boolean added = ignored.add(target);
        if (added) save();
        return added;
    }

    public boolean removeIgnore(UUID player, UUID target) {
        Set<UUID> ignored = ignoreMap.get(player);
        if (ignored == null) return false;
        boolean removed = ignored.remove(target);
        if (removed) save();
        return removed;
    }

    public Set<UUID> getIgnoredPlayers(UUID player) {
        return ignoreMap.getOrDefault(player, Collections.emptySet());
    }

    public int getIgnoredCount(UUID player) {
        Set<UUID> ignored = ignoreMap.get(player);
        return ignored == null ? 0 : ignored.size();
    }

    public void clearIgnores(UUID player) {
        ignoreMap.remove(player);
        save();
    }
}
