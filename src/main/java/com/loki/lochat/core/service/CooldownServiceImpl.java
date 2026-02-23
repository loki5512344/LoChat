package com.loki.lochat.core.service;

import com.loki.lochat.api.service.CooldownService;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Реализация сервиса кулдаунов
 */
public class CooldownServiceImpl implements CooldownService {
    private final Map<UUID, Long> globalCooldowns = new ConcurrentHashMap<>();
    private final Map<UUID, Long> localCooldowns = new ConcurrentHashMap<>();
    
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
}
