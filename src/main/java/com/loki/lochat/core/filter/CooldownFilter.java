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
    private final JavaPlugin plugin;

    public CooldownFilter(CooldownService cooldownService, JavaPlugin plugin) {
        this.cooldownService = cooldownService;
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
            if (cooldownService.isOnCooldown(player.getUniqueId(), chatType, cooldown)) {
                int remaining = cooldownService.getRemainingCooldown(
                        player.getUniqueId(), chatType, cooldown);
                
                // Получаем сообщение из конфигурации
                com.loki.lochat.LoChat loChat = (com.loki.lochat.LoChat) plugin;
                String cooldownMessage = loChat.getConfigManager().getHardcodedMessages().getCooldownMessage();
                cooldownMessage = cooldownMessage.replace("{remaining}", String.valueOf(remaining));
                
                player.sendMessage(cooldownMessage);
                return false;
            }
        }

        cooldownService.setCooldown(player.getUniqueId(), chatType);
        return true;
    }
}
