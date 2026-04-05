package com.loki.lochat.gradient.service;

import com.loki.lochat.gradient.config.GradientConfig;
import com.loki.lochat.gradient.data.GradientPlayerData;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.cacheddata.CachedMetaData;
import net.luckperms.api.model.user.User;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * Сервис для работы с префиксами - упрощенная интеграция с LuckPerms
 */
public class PrefixService {
    
    private final GradientConfig config;
    private final boolean luckPermsAvailable;
    
    public PrefixService(GradientConfig config) {
        this.config = config;
        this.luckPermsAvailable = Bukkit.getPluginManager().isPluginEnabled("LuckPerms");
    }
    
    /**
     * Получает префикс игрока (кастомный или из LuckPerms)
     */
    public String getPrefix(Player player, GradientPlayerData data) {
        // Приоритет: кастомный префикс > LuckPerms префикс
        if (data.hasPrefix() && data.isPrefixEnabled()) {
            String prefix = config.getPrefixFormat().replace("{prefix}", data.getPrefix());
            
            // Применяем градиент если нужно
            if (data.hasColors() && data.isColorEnabled() && config.isGradientOnPrefix()) {
                return GradientService.applyGradient(prefix, data.getColors());
            }
            
            return prefix;
        }
        
        // Пробуем получить из LuckPerms
        return getLuckPermsPrefix(player, data);
    }
    
    /**
     * Получает префикс из LuckPerms
     */
    public String getLuckPermsPrefix(Player player, GradientPlayerData data) {
        if (!luckPermsAvailable) {
            return "";
        }
        
        try {
            LuckPerms luckPerms = LuckPermsProvider.get();
            User user = luckPerms.getUserManager().getUser(player.getUniqueId());
            
            if (user == null) {
                return "";
            }
            
            CachedMetaData metaData = user.getCachedData().getMetaData();
            String prefix = metaData.getPrefix();
            
            if (prefix == null || prefix.isEmpty()) {
                return "";
            }
            
            // Применяем градиент если нужно
            if (data != null && data.hasColors() && data.isColorEnabled() && config.isGradientOnLuckPermsPrefix()) {
                String cleanPrefix = GradientService.stripColors(prefix);
                return GradientService.applyGradient(cleanPrefix, data.getColors());
            }
            
            return prefix;
            
        } catch (Exception e) {
            return "";
        }
    }
    
    /**
     * Получает полное имя игрока: префикс + ник с градиентом
     */
    public String getFullName(Player player, GradientPlayerData data) {
        String prefix = getPrefix(player, data);
        String nick = player.getName();
        
        // Применяем градиент к нику если есть цвета
        if (data.hasColors() && data.isColorEnabled()) {
            nick = GradientService.applyGradient(nick, data.getColors());
        }
        
        return prefix.isEmpty() ? nick : prefix + " " + nick;
    }
    
    /**
     * Получает полное имя для TAB плагина
     */
    public String getFullNameForTab(Player player, GradientPlayerData data) {
        String prefix = getPrefix(player, data);
        String nick = player.getName();
        
        // Применяем градиент в формате TAB
        if (data.hasColors() && data.isColorEnabled()) {
            nick = GradientService.applyGradientForTab(nick, data.getColors());
        }
        
        return prefix.isEmpty() ? nick : prefix + " " + nick;
    }
    
    public boolean isLuckPermsAvailable() {
        return luckPermsAvailable;
    }
}
