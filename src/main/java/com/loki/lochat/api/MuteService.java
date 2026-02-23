package com.loki.lochat.api;

import java.util.UUID;

/**
 * Сервис для управления мутами
 */
public interface MuteService {
    void mute(UUID uuid, String playerName, long duration, String reason, String mutedBy);

    boolean unmute(UUID uuid, String unmutedBy);

    boolean isMuted(UUID uuid);

    long getRemainingTime(UUID uuid);

    String formatTime(long millis);

    long parseTime(String timeStr);
}
