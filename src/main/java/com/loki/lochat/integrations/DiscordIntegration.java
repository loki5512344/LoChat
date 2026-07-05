package com.loki.lochat.integrations;

import com.loki.lochat.integrations.discord.DiscordConfig;
import com.loki.lochat.integrations.discord.DiscordMessageService;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Интеграция с Discord - упрощенная версия
 */
public class DiscordIntegration {
    private final JavaPlugin plugin;
    private DiscordConfig config;
    private DiscordMessageService messageService;
    private boolean enabled = false;

    public DiscordIntegration(JavaPlugin plugin) {
        this.plugin = plugin;
        init();
    }

    private void init() {
        config = new DiscordConfig(plugin);
        enabled = config.isEnabled();
        
        if (!enabled) {
            plugin.getLogger().info("Discord integration disabled in config");
            return;
        }
        
        messageService = new DiscordMessageService(plugin, config);
        
        if (!messageService.isValid()) {
            plugin.getLogger().severe("Invalid Discord webhook URL! Please check config/discord.yml");
            enabled = false;
            return;
        }
        
        plugin.getLogger().info("Discord integration initialized successfully");
    }

    public void sendChatMessage(Player player, String message, boolean isGlobal) {
        if (!enabled || !config.isChatEnabled()) {
            return;
        }
        
        if (config.isGlobalOnly() && !isGlobal) {
            return;
        }
        
        messageService.sendChatMessage(player, message, isGlobal);
    }

    public void sendPlayerJoin(Player player) {
        if (!enabled || !config.isEventEnabled("join")) {
            return;
        }
        messageService.sendPlayerJoin(player);
    }

    public void sendPlayerQuit(Player player) {
        if (!enabled || !config.isEventEnabled("quit")) {
            return;
        }
        messageService.sendPlayerQuit(player);
    }

    public void sendPlayerDeath(Player player, String deathMessage) {
        if (!enabled || !config.isEventEnabled("death")) {
            return;
        }
        messageService.sendPlayerDeath(player, deathMessage);
    }

    public void sendTestMessage(String message, String senderName) {
        if (!enabled) {
            return;
        }
        messageService.sendTestMessage(message, senderName);
    }

    public void reload() {
        init();
        plugin.getLogger().info("Discord integration reloaded");
    }

    public void shutdown() {
        if (messageService != null) {
            messageService.shutdown();
        }
    }

    public boolean isEnabled() {
        return enabled;
    }
}
