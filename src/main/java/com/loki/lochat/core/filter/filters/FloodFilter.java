package com.loki.lochat.core.filter.filters;

import com.loki.lochat.core.filter.FilterResult;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class FloodFilter {
    private final FileConfiguration config;
    private final Map<UUID, Deque<Long>> floodTracker = new ConcurrentHashMap<>();

    public FloodFilter(FileConfiguration config) {
        this.config = config;
    }

    public FilterResult filter(Player player) {
        if (player.hasPermission("lochat.bypass.flood")) {
            return FilterResult.ok(null);
        }

        int maxMessages = config.getInt("filters.flood.max-messages", 5);
        int timePeriod = config.getInt("filters.flood.time-period", 10);
        long now = System.currentTimeMillis();
        long windowMs = timePeriod * 1000L;

        Deque<Long> timestamps = floodTracker.computeIfAbsent(
            player.getUniqueId(), k -> new ArrayDeque<>());

        // Удаляем старые timestamp'ы
        while (!timestamps.isEmpty() && now - timestamps.peekFirst() > windowMs) {
            timestamps.pollFirst();
        }

        if (timestamps.size() >= maxMessages) {
            return FilterResult.blocked(config.getString("filters.flood.block-message",
                "&#CF6679Не флудите!"));
        }

        timestamps.addLast(now);
        return FilterResult.ok(null);
    }

    public void clearPlayer(UUID uuid) {
        floodTracker.remove(uuid);
    }
}
