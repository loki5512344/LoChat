package com.loki.lochat.core.service;

import com.loki.lochat.api.service.CooldownService;
import com.loki.lochat.api.service.PlayerDataService;
import com.loki.lochat.util.FoliaUtil;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Реализация сервиса данных игроков.
 * Хранит и сохраняет статистику сообщений в statistics.yml.
 */
public class PlayerDataServiceImpl implements PlayerDataService {

    private final JavaPlugin plugin;
    private final CooldownService cooldownService;

    // Счётчики сообщений per-player (сбрасываются при выходе, сохраняются в файл)
    private final Map<UUID, Long> playerMessages = new ConcurrentHashMap<>();
    // Глобальные счётчики по типу чата
    private final AtomicLong globalChatCount  = new AtomicLong(0);
    private final AtomicLong localChatCount   = new AtomicLong(0);
    private final AtomicLong pmCount          = new AtomicLong(0);
    private final AtomicLong totalMessages    = new AtomicLong(0);

    private final File statsFile;
    private YamlConfiguration stats;

    public PlayerDataServiceImpl(JavaPlugin plugin, CooldownService cooldownService) {
        this.plugin = plugin;
        this.cooldownService = cooldownService;
        this.statsFile = new File(plugin.getDataFolder(), "data/statistics.yml");
        load();
    }

    // ── Публичные методы статистики ────────────────────────────────────────────

    /** Вызывать при каждом сообщении в чате */
    public void recordMessage(UUID player, String chatType) {
        playerMessages.compute(player, (k, v) -> (v == null ? 0L : v) + 1L);
        totalMessages.incrementAndGet();
        switch (chatType) {
            case "global" -> globalChatCount.incrementAndGet();
            case "local"  -> localChatCount.incrementAndGet();
            case "pm"     -> pmCount.incrementAndGet();
        }
    }

    public long getPlayerMessages(UUID player) {
        return playerMessages.getOrDefault(player, 0L);
    }

    // ── PlayerDataService ──────────────────────────────────────────────────────

    @Override
    public void clearPlayerData(UUID player) {
        cooldownService.removeCooldown(player);
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

    // ── Внутренние методы ──────────────────────────────────────────────────────

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
