package com.loki.lohub.managers;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CooldownManager {

    private final Map<String, Map<UUID, Long>> cooldowns = new HashMap<>();

    public void setCooldown(String key, UUID playerId, int seconds) {
        cooldowns.computeIfAbsent(key, k -> new HashMap<>())
                .put(playerId, System.currentTimeMillis() + (seconds * 1000L));
    }

    public boolean hasCooldown(String key, UUID playerId) {
        Map<UUID, Long> keyCooldowns = cooldowns.get(key);
        if (keyCooldowns == null) {
            return false;
        }

        Long expireTime = keyCooldowns.get(playerId);
        if (expireTime == null) {
            return false;
        }

        if (System.currentTimeMillis() >= expireTime) {
            keyCooldowns.remove(playerId);
            return false;
        }

        return true;
    }

    public int getRemainingSeconds(String key, UUID playerId) {
        Map<UUID, Long> keyCooldowns = cooldowns.get(key);
        if (keyCooldowns == null) {
            return 0;
        }

        Long expireTime = keyCooldowns.get(playerId);
        if (expireTime == null) {
            return 0;
        }

        long remaining = expireTime - System.currentTimeMillis();
        return remaining > 0 ? (int) (remaining / 1000) : 0;
    }

    public void removeCooldown(String key, UUID playerId) {
        Map<UUID, Long> keyCooldowns = cooldowns.get(key);
        if (keyCooldowns != null) {
            keyCooldowns.remove(playerId);
        }
    }

    public void clearAll() {
        cooldowns.clear();
    }
}
