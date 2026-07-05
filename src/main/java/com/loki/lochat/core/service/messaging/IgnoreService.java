package com.loki.lochat.core.service.messaging;

import com.loki.lochat.utils.persistence.FilePersistence;
import com.loki.lochat.utils.platform.FoliaUtil;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Сервис игнорирования игроков
 * Управляет списками игнорируемых игроков
 */
public class IgnoreService {

    private final JavaPlugin plugin;
    private final Map<UUID, Set<UUID>> ignoreMap = new ConcurrentHashMap<>();

    public IgnoreService(JavaPlugin plugin) {
        this.plugin = plugin;
    }
    
    public void init() {
        load();
    }

    public boolean isIgnoring(UUID player, UUID target) {
        Set<UUID> ignored = ignoreMap.get(player);
        return ignored != null && ignored.contains(target);
    }

    public boolean addIgnore(UUID player, UUID target) {
        Set<UUID> ignored = ignoreMap.computeIfAbsent(player, k -> ConcurrentHashMap.newKeySet());
        boolean added = ignored.add(target);
        if (added) {
            saveAsync();
        }
        return added;
    }

    public boolean removeIgnore(UUID player, UUID target) {
        Set<UUID> ignored = ignoreMap.get(player);
        if (ignored == null) {
            return false;
        }
        boolean removed = ignored.remove(target);
        if (removed) {
            saveAsync();
        }
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
        saveAsync();
    }

    public void load() {
        FileConfiguration config = FilePersistence.loadYaml(plugin, "ignores.yml");
        for (String key : config.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(key);
                List<String> ignoredList = config.getStringList(key);
                Set<UUID> ignored = ConcurrentHashMap.newKeySet();

                for (String ignoredStr : ignoredList) {
                    try {
                        ignored.add(UUID.fromString(ignoredStr));
                    } catch (Exception e) {
                        // Ignore invalid UUID
                    }
                }

                if (!ignored.isEmpty()) {
                    ignoreMap.put(uuid, ignored);
                }
            } catch (Exception e) {
                // Ignore invalid entry
            }
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

        FilePersistence.saveYaml(plugin, "ignores.yml", config);
    }

    private void saveAsync() {
        FoliaUtil.runAsync(plugin, this::save);
    }
}
