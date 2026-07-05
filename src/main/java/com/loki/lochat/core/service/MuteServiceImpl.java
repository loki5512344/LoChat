package com.loki.lochat.core.service;

import com.loki.lochat.api.service.MuteService;
import com.loki.lochat.core.service.mute.MuteDataStorage;
import com.loki.lochat.core.service.mute.MuteHistoryManager;
import com.loki.lochat.core.service.mute.MutePermissionChecker;
import com.loki.lochat.core.service.mute.strategies.MuteStrategy;
import com.loki.lochat.data.model.MuteData;
import com.loki.lochat.utils.format.TimeFormatter;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

/**
 * Сервис мутов с использованием Strategy pattern
 * Разделен на: стратегии мута, хранилище данных, история, проверка прав
 */
public class MuteServiceImpl implements MuteService {

    private final JavaPlugin plugin;
    private final List<MuteStrategy> strategies;
    private final MuteDataStorage storage;
    private final MuteHistoryManager historyManager;
    private final MutePermissionChecker permissionChecker;

    public MuteServiceImpl(JavaPlugin plugin, List<MuteStrategy> strategies,
                           MuteDataStorage storage, MuteHistoryManager historyManager) {
        this.plugin = plugin;
        this.strategies = strategies;
        this.storage = storage;
        this.historyManager = historyManager;
        this.permissionChecker = new MutePermissionChecker();
    }

    @Override
    public void mute(UUID uuid, String playerName, long duration, String reason, String mutedBy) {
        if (uuid == null || playerName == null || playerName.isEmpty()) {
            plugin.getLogger().warning("Attempted to mute player with invalid data");
            return;
        }

        long endTime = duration > 0 ? System.currentTimeMillis() + duration : 0;
        MuteData data = new MuteData(uuid, playerName, endTime, reason, mutedBy);

        storage.save(data);
        historyManager.addEntry(uuid, playerName, duration, reason, mutedBy);

        // Применяем все стратегии мута
        strategies.forEach(strategy -> {
            strategy.apply(uuid);
            plugin.getLogger().fine(strategy.getName() + " mute applied to " + playerName);
        });

        plugin.getLogger().info("Player " + playerName + " muted by " + mutedBy +
                " for " + (duration > 0 ? formatTime(duration) : "permanent") +
                (reason != null ? " (reason: " + reason + ")" : ""));
    }

    @Override
    public boolean unmute(UUID uuid, String unmutedBy) {
        if (uuid == null) {
            plugin.getLogger().warning("Attempted to unmute player with null UUID");
            return false;
        }

        MuteData removed = storage.remove(uuid);
        if (removed != null) {
            historyManager.updateUnmute(uuid, unmutedBy);

            // Снимаем все стратегии мута
            strategies.forEach(strategy -> {
                strategy.remove(uuid);
                plugin.getLogger().fine(strategy.getName() + " mute removed from " + removed.getPlayerName());
            });

            plugin.getLogger().info("Player " + removed.getPlayerName() + " unmuted by " + unmutedBy);
            return true;
        }
        return false;
    }

    @Override
    public boolean isMuted(UUID uuid) {
        if (uuid == null) {
            return false;
        }

        MuteData data = storage.get(uuid);
        if (data == null) {
            return false;
        }

        if (!isExpired(data)) {
            return true;
        }

        // Мут истек - удаляем
        storage.remove(uuid);
        strategies.forEach(strategy -> strategy.remove(uuid));
        plugin.getLogger().info("Mute expired for player " + data.getPlayerName());
        return false;
    }

    @Override
    public MuteData getMuteData(UUID uuid) {
        return isMuted(uuid) ? storage.get(uuid) : null;
    }

    @Override
    public long getRemainingTime(UUID uuid) {
        MuteData data = storage.get(uuid);
        if (data == null) {
            return 0;
        }
        if (data.getEndTime() == 0) {
            return -1;
        }
        return Math.max(0, data.getEndTime() - System.currentTimeMillis());
    }

    @Override
    public String formatTime(long millis) {
        return TimeFormatter.format(millis);
    }

    @Override
    public long parseTime(String timeStr) {
        return TimeFormatter.parse(timeStr);
    }

    @Override
    public long getMaxDuration(Player player) {
        return permissionChecker.getMaxDuration(player);
    }

    @Override
    public boolean canMuteForDuration(Player player, long duration) {
        return permissionChecker.canMuteForDuration(player, duration);
    }

    @Override
    public List<MuteData.MuteHistoryEntry> getPlayerHistory(UUID uuid) {
        return historyManager.getHistory(uuid);
    }

    @Override
    public List<MuteData.MuteHistoryEntry> getMutesByOperator(String operatorName) {
        return historyManager.getMutesByOperator(operatorName);
    }

    @Override
    public UUID getUUIDByName(String name) {
        return historyManager.getUUIDByName(name);
    }

    @Override
    public Map<UUID, MuteData> getActiveMutes() {
        return storage.getActiveMutes();
    }

    @Override
    public String formatMessage(String message, String player, String operator, String duration, String reason) {
        return message
                .replace("%player%", player != null ? player : "")
                .replace("%operator%", operator != null ? operator : "")
                .replace("%duration%", duration != null ? duration : "")
                .replace("%reason%", reason != null ? reason : "");
    }

    @Override
    public void save() {
        storage.saveToFile();
        historyManager.save();
    }

    private boolean isExpired(MuteData data) {
        return data.getEndTime() > 0 && System.currentTimeMillis() > data.getEndTime();
    }
}
