package com.loki.lochat.core.service;

import com.loki.lochat.api.service.CooldownService;
import com.loki.lochat.api.service.PlayerDataService;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

/**
 * Реализация сервиса данных игроков
 */
public class PlayerDataServiceImpl implements PlayerDataService {
    private final CooldownService cooldownService;
    
    public PlayerDataServiceImpl(JavaPlugin plugin) {
        this.cooldownService = new CooldownServiceImpl();
    }
    
    @Override
    public void clearPlayerData(UUID player) {
        cooldownService.removeCooldown(player);
    }
    
    @Override
    public void saveAll() {
        // Сохранение всех данных при выключении
    }
}
