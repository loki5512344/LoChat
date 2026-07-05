package com.loki.lochat.core.filter;

import com.loki.lochat.api.filter.MessageFilter;
import com.loki.lochat.api.service.PlayerService;
import com.loki.lochat.data.model.ChatMessage;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Фильтр кулдаунов
 */
public class CooldownFilter implements MessageFilter {
    private final PlayerService playerService;
    private final FileConfiguration config;
    private final JavaPlugin plugin;

    public CooldownFilter(PlayerService playerService, JavaPlugin plugin) {
        this.playerService = playerService;
        this.config = plugin.getConfig();
        this.plugin = plugin;
    }

    @Override
    public boolean apply(Player player, ChatMessage message) {
        if (player.hasPermission("chat.bypass.cooldown")) {
            return true;
        }

        String chatType = message.getChatType();
        int cooldown = config.getInt("chat." + chatType + ".cooldown", 0);

        if (cooldown > 0) {
            if (playerService.isOnCooldown(player.getUniqueId(), chatType, cooldown)) {
                int remaining = playerService.getRemainingCooldown(
                        player.getUniqueId(), chatType, cooldown);
                
                // Получаем сообщение из конфигурации
                com.loki.lochat.LoChat loChat = (com.loki.lochat.LoChat) plugin;
                String cooldownMessage = loChat.getConfigManager().getMessagesConfig().getCooldownMessage();
                cooldownMessage = cooldownMessage.replace("{remaining}", String.valueOf(remaining));
                
                player.sendMessage(cooldownMessage);
                return false;
            }
        }

        playerService.setCooldown(player.getUniqueId(), chatType);
        return true;
    }
}
