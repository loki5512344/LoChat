package com.loki.lochat.managers;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class CooldownManager {

    private final Map<UUID, Long> globalCooldowns = new ConcurrentHashMap<>();
    private final Map<UUID, Long> localCooldowns = new ConcurrentHashMap<>();

    public boolean isOnCooldown(UUID player, String chatType, int cooldownSeconds) {
        Map<UUID, Long> cooldowns = chatType.equals("global") ? globalCooldowns : localCooldowns;
        Long lastTime = cooldowns.get(player);
        
        if (lastTime == null) {
            return false;
        }
        
        long elapsed = (System.currentTimeMillis() - lastTime) / 1000;
        return elapsed < cooldownSeconds;
    }

    public int getRemainingCooldown(UUID player, String chatType, int cooldownSeconds) {
        Map<UUID, Long> cooldowns = chatType.equals("global") ? globalCooldowns : localCooldowns;
        Long lastTime = cooldowns.get(player);
        
        if (lastTime == null) {
            return 0;
        }
        
        long elapsed = (System.currentTimeMillis() - lastTime) / 1000;
        return Math.max(0, cooldownSeconds - (int) elapsed);
    }

    public void setCooldown(UUID player, String chatType) {
        Map<UUID, Long> cooldowns = chatType.equals("global") ? globalCooldowns : localCooldowns;
        cooldowns.put(player, System.currentTimeMillis());
    }

    public void removeCooldown(UUID player) {
        globalCooldowns.remove(player);
        localCooldowns.remove(player);
    }
}
