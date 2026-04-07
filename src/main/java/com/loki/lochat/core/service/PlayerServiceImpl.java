package com.loki.lochat.core.service;

import com.loki.lochat.api.service.PlayerService;
import com.loki.lochat.utils.platform.FoliaUtil;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Объединённая реализация сервиса данных игроков
 * Включает: Cooldown, PlayerData (статистика)
 */
public class PlayerServiceImpl implements PlayerService {
    
    private final JavaPlugin plugin;
    
    // Cooldown state
    private final Map<UUID, Long> globalCooldowns = new ConcurrentHashMap<>();
    private final Map<UUID, Long> localCooldowns = new ConcurrentHashMap<>();
    
    // Statistics state
    private final Map<UUID, Long> playerMessages = new ConcurrentHashMap<>();
    private final AtomicLong globalChatCount = new AtomicLong(0);
    private final AtomicLong localChatCount = new AtomicLong(0);
    private final AtomicLong pmCount = new AtomicLong(0);
    private final AtomicLong totalMessages = new AtomicLong(0);
    
    private final File statsFile;
    private YamlConfiguration stats;
    
    public PlayerServiceImpl(JavaPlugin plugin) {
        this.plugin = plugin;
        this.statsFile = new File(plugin.getDataFolder(), "data/statistics.yml");
        load();
    }
    
    // ========== Cooldown Implementation ==========
    
    @Override
    public boolean isOnCooldown(UUID player, String type, int cooldownSeconds) {
        Map<UUID, Long> cooldowns = getCooldownMap(type);
        Long lastTime = cooldowns.get(player);
        
        if (lastTime == null) return false;
        
        long elapsed = (System.currentTimeMillis() - lastTime) / 1000;
        return elapsed < cooldownSeconds;
    }
    
    @Override
    public int getRemainingCooldown(UUID player, String type, int cooldownSeconds) {
        Map<UUID, Long> cooldowns = getCooldownMap(type);
        Long lastTime = cooldowns.get(player);
        
        if (lastTime == null) return 0;
        
        long elapsed = (System.currentTimeMillis() - lastTime) / 1000;
        return Math.max(0, cooldownSeconds - (int) elapsed);
    }
    
    @Override
    public void setCooldown(UUID player, String type) {
        getCooldownMap(type).put(player, System.currentTimeMillis());
    }
    
    @Override
    public void removeCooldown(UUID player) {
        globalCooldowns.remove(player);
        localCooldowns.remove(player);
    }
    
    private Map<UUID, Long> getCooldownMap(String type) {
        return type.equals("global") ? globalCooldowns : localCooldowns;
    }
    
    // ========== Statistics Implementation ==========
    
    @Override
    public void recordMessage(UUID player, String chatType) {
        playerMessages.compute(player, (k, v) -> (v == null ? 0L : v) + 1L);
        totalMessages.incrementAndGet();
        switch (chatType) {
            case "global" -> globalChatCount.incrementAndGet();
            case "local" -> localChatCount.incrementAndGet();
            case "pm" -> pmCount.incrementAndGet();
        }
    }
    
    @Override
    public long getPlayerMessages(UUID player) {
        return playerMessages.getOrDefault(player, 0L);
    }
    
    @Override
    public void clearPlayerData(UUID player) {
        removeCooldown(player);
        // Сохраняем статистику игрока в файл при выходе
        long msgs = playerMessages.getOrDefault(player, 0L);
        if (msgs > 0) {
            FoliaUtil.runAsync(plugin, () -> persistPlayerMessages(player, msgs));
        }
    }
    
    @Override
    public void saveAll() {
        try {
            ensureDir();
            // Обновляем глобальные счётчики
            stats.set("global.total-messages",
                    stats.getLong("global.total-messages", 0) + totalMessages.getAndSet(0));
            stats.set("by-type.global-chat",
                    stats.getLong("by-type.global-chat", 0) + globalChatCount.getAndSet(0));
            stats.set("by-type.local-chat",
                    stats.getLong("by-type.local-chat", 0) + localChatCount.getAndSet(0));
            stats.set("by-type.private-messages",
                    stats.getLong("by-type.private-messages", 0) + pmCount.getAndSet(0));
            stats.set("last-updated", System.currentTimeMillis());
            stats.save(statsFile);
        } catch (IOException e) {
            plugin.getLogger().warning("[LoChat] Failed to save statistics: " + e.getMessage());
        }
    }
    
    // ========== Private Methods ==========
    
    private void persistPlayerMessages(UUID uuid, long count) {
        try {
            String path = "top-players.by-messages." + uuid;
            long prev = stats.getLong(path + ".count", 0);
            stats.set(path + ".count", prev + count);
            stats.set(path + ".last-updated", System.currentTimeMillis());
            stats.save(statsFile);
            playerMessages.remove(uuid);
        } catch (IOException e) {
            plugin.getLogger().warning("[LoChat] Failed to persist player stats: " + e.getMessage());
        }
    }
    
    private void load() {
        ensureDir();
        if (!statsFile.exists()) {
            plugin.saveResource("data/statistics.yml", false);
        }
        stats = YamlConfiguration.loadConfiguration(statsFile);
    }
    
    private void ensureDir() {
        File dir = statsFile.getParentFile();
        if (!dir.exists()) dir.mkdirs();
    }
}
