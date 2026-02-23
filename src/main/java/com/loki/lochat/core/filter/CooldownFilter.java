package com.loki.lochat.core.filter;

import com.loki.lochat.api.filter.MessageFilter;
import com.loki.lochat.api.service.CooldownService;
import com.loki.lochat.data.model.ChatMessage;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Фильтр кулдаунов
 */
public class CooldownFilter implements MessageFilter {
    private final CooldownService cooldownService;
    private final FileConfiguration config;
    
    public CooldownFilter(CooldownService cooldownService, JavaPlugin plugin) {
        this.cooldownService = cooldownService;
        this.config = plugin.getConfig();
    }
    
    @Override
    public boolean apply(Player player, ChatMessage message) {
        if (player.hasPermission("chat.bypass.cooldown")) {
            return true;
        }
        
        String chatType = message.getChatType();
        int cooldown = config.getInt("chat." + chatType + ".cooldown", 0);
        
        if (cooldown > 0) {
            if (cooldownService.isOnCooldown(player.getUniqueId(), chatType, cooldown)) {
                int remaining = cooldownService.getRemainingCooldown(
                    player.getUniqueId(), chatType, cooldown);
                player.sendMessage("§cПодождите " + remaining + " сек.");
                return false;
            }
        }
        
        cooldownService.setCooldown(player.getUniqueId(), chatType);
        return true;
    }
}
