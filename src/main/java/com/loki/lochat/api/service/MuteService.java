package com.loki.lochat.api.service;

import com.loki.lochat.data.model.MuteData;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Сервис для управления мутами
 */
public interface MuteService {
    // Основные операции
    void mute(UUID uuid, String playerName, long duration, String reason, String mutedBy);

    boolean unmute(UUID uuid, String unmutedBy);

    boolean isMuted(UUID uuid);

    MuteData getMuteData(UUID uuid);

    long getRemainingTime(UUID uuid);

    // Утилиты времени
    String formatTime(long millis);

    long parseTime(String timeStr);

    // Права доступа
    long getMaxDuration(Player player);

    boolean canMuteForDuration(Player player, long duration);

    // История
    List<MuteData.MuteHistoryEntry> getPlayerHistory(UUID uuid);

    List<MuteData.MuteHistoryEntry> getMutesByOperator(String operatorName);

    UUID getUUIDByName(String name);

    // Список активных мутов
    Map<UUID, MuteData> getActiveMutes();

    // Форматирование сообщений
    String formatMessage(String message, String player, String operator, String duration, String reason);

    // Сохранение
    void save();
}
