package com.loki.lochat.integrations.discord;

import com.loki.lochat.integrations.DiscordWebhook;
import com.loki.lochat.utils.platform.FoliaUtil;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.regex.Pattern;

/**
 * Сервис для отправки сообщений в Discord
 */
public class DiscordMessageService {
    private static final Pattern MENTION_PATTERN = Pattern.compile("@(everyone|here)", Pattern.CASE_INSENSITIVE);
    private static final Pattern COLOR_CODE_PATTERN = Pattern.compile("§[0-9a-fk-or]", Pattern.CASE_INSENSITIVE);
    
    private final JavaPlugin plugin;
    private final DiscordConfig config;
    private final DiscordWebhook webhook;
    
    public DiscordMessageService(JavaPlugin plugin, DiscordConfig config) {
        this.plugin = plugin;
        this.config = config;
        this.webhook = new DiscordWebhook(
            plugin,
            config.getWebhookUrl(),
            config.getUsername(),
            getAvatarUrl(),
            config.getTimeout(),
            config.getRetryAttempts(),
            config.getRetryDelay()
        );
    }
    
    private String getAvatarUrl() {
        String url = config.getAvatarUrl();
        if (url.isEmpty()) {
            com.loki.lochat.LoChat loChat = (com.loki.lochat.LoChat) plugin;
            return loChat.getConfigManager().getAppearanceConfig().getDefaultAvatarUrl();
        }
        return url;
    }
    
    public void sendChatMessage(Player player, String message, boolean isGlobal) {
        String cleanMessage = sanitizeMessage(message);
        
        if (cleanMessage.length() < config.getMinMessageLength()) {
            return;
        }
        
        if (cleanMessage.length() > config.getMaxMessageLength()) {
            cleanMessage = cleanMessage.substring(0, config.getMaxMessageLength() - 3) + "...";
        }
        
        String formatted = config.getChatFormat()
            .replace("{player}", player.getName())
            .replace("{message}", cleanMessage);
        
        if (config.isAsync()) {
            FoliaUtil.runAsync(plugin, () -> sendFormatted(formatted, player, isGlobal));
        } else {
            sendFormatted(formatted, player, isGlobal);
        }
    }
    
    private void sendFormatted(String message, Player player, boolean isGlobal) {
        if (config.useEmbed()) {
            com.loki.lochat.LoChat loChat = (com.loki.lochat.LoChat) plugin;
            com.loki.lochat.config.AppearanceConfig appearanceConfig = loChat.getConfigManager().getAppearanceConfig();
            
            String title = isGlobal ? 
                appearanceConfig.getDiscordEventTitle("global_chat") : 
                appearanceConfig.getDiscordEventTitle("local_chat");
            
            if (title.isEmpty()) {
                title = isGlobal ? "🌍 Глобальный чат" : "📍 Локальный чат";
            }
            
            String color = config.getEmbedColor();
            String thumbnail = appearanceConfig.getPlayerAvatarUrl().replace("{player}", player.getName());
            
            webhook.sendEmbed(title, message, color, thumbnail);
        } else {
            webhook.sendMessage(message);
        }
    }
    
    public void sendPlayerJoin(Player player) {
        sendEvent(player, "join", null);
    }
    
    public void sendPlayerQuit(Player player) {
        sendEvent(player, "quit", null);
    }
    
    public void sendPlayerDeath(Player player, String deathMessage) {
        sendEvent(player, "death", deathMessage);
    }
    
    private void sendEvent(Player player, String event, String extraMessage) {
        com.loki.lochat.LoChat loChat = (com.loki.lochat.LoChat) plugin;
        com.loki.lochat.config.AppearanceConfig appearanceConfig = loChat.getConfigManager().getAppearanceConfig();
        
        String title = config.getEventTitle(event);
        if (title.isEmpty()) {
            title = appearanceConfig.getDiscordEventTitle(event);
        }
        
        String description = config.getEventDescription(event)
            .replace("{player}", player.getName());
        
        if (extraMessage != null) {
            description = description.replace("{death_message}", sanitizeMessage(extraMessage));
        }
        
        String color = config.getEventColor(event);
        String thumbnail = config.getEventThumbnail(event);
        if (thumbnail.isEmpty()) {
            thumbnail = appearanceConfig.getPlayerAvatarUrl();
        }
        final String finalThumbnail = thumbnail.replace("{player}", player.getName());
        final String finalTitle = title;
        final String finalDescription = description;
        final String finalColor = color;
        
        if (config.isAsync()) {
            FoliaUtil.runAsync(plugin, () -> webhook.sendEmbed(finalTitle, finalDescription, finalColor, finalThumbnail));
        } else {
            webhook.sendEmbed(title, description, color, finalThumbnail);
        }
    }
    
    public void sendTestMessage(String message, String senderName) {
        String title = "🧪 Тестовое сообщение";
        String description = "**Отправитель:** " + senderName + "\n**Сообщение:** " + message;
        String color = "FFD700";
        String thumbnail = "https://mc-heads.net/avatar/" + senderName + "/64";
        
        webhook.sendEmbed(title, description, color, thumbnail);
    }
    
    private String sanitizeMessage(String message) {
        if (message == null) return "";
        
        message = COLOR_CODE_PATTERN.matcher(message).replaceAll("");
        
        if (config.sanitizeMentions()) {
            message = MENTION_PATTERN.matcher(message).replaceAll("@\u200B$1");
        }
        
        message = message.replace("*", "\\*")
                        .replace("_", "\\_")
                        .replace("`", "\\`")
                        .replace("~", "\\~");
        
        return message.trim();
    }
    
    public boolean isValid() {
        return webhook.isValid();
    }
    
    public void shutdown() {
        webhook.shutdown();
    }
}
