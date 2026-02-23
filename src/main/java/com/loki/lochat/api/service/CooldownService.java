package com.loki.lochat.api.service;

import java.util.UUID;

/**
 * Сервис для управления кулдаунами
 */
public interface CooldownService {
    boolean isOnCooldown(UUID player, String type, int cooldownSeconds);
    int getRemainingCooldown(UUID player, String type, int cooldownSeconds);
    void setCooldown(UUID player, String type);
    void removeCooldown(UUID player);
}
