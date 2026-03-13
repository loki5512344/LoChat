package com.loki.lochat.integrations;

import com.loki.lochat.util.FoliaUtil;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.regex.Pattern;

/**
 * Интеграция с Discord через вебхуки
 */
public class DiscordIntegration {
    private final JavaPlugin plugin;
    private YamlConfiguration config;
    private DiscordWebhook webhook;
    private boolean enabled = false;
    
    // Паттерны для очистки сообщений
    private static final Pattern MENTION_PATTERN = Pattern.compile("@(everyone|here)", Pattern.CASE_INSENSITIVE);
    private static final Pattern COLOR_CODE_PATTERN = Pattern.compile("§[0-9a-fk-or]", Pattern.CASE_INSENSITIVE);

    public DiscordIntegration(JavaPlugin plugin) {
        this.plugin = plugin;
        loadConfig();
        initWebhook();
    }

    /**
     * Загрузить конфигурацию
     */
    private void loadConfig() {
        File configFile = new File(plugin.getDataFolder(), "config/discord.yml");
        if (!configFile.exists()) {
            plugin.saveResource("config/discord.yml", false);
        }
        
        config = YamlConfiguration.loadConfiguration(configFile);
        enabled = config.getBoolean("enabled", false);
        
        if (!enabled) {
            plugin.getLogger().info("Discord integration disabled in config");
            return;
        }
    }

    /**
     * Инициализировать вебхук
     */
    private void initWebhook() {
        if (!enabled) return;
        
        String webhookUrl = config.getString("webhook.url", "");
        String username = config.getString("webhook.username", "Minecraft Server");
        String avatarUrl = config.getString("webhook.avatar_url", "https://mc-heads.net/avatar/minecraft/64");
        int timeout = config.getInt("performance.timeout", 10);
        int retryAttempts = config.getInt("performance.retry_attempts", 3);
        long retryDelay = config.getLong("performance.retry_delay", 1000);
        
        // Логируем информацию о URL для отладки
        plugin.getLogger().info("Discord webhook URL length: " + webhookUrl.length());
        if (webhookUrl.length() > 0) {
            plugin.getLogger().info("Discord webhook URL starts with: " + webhookUrl.substring(0, Math.min(50, webhookUrl.length())));
        }
        
        webhook = new DiscordWebhook(plugin, webhookUrl, username, avatarUrl, timeout, retryAttempts, retryDelay);
        
        if (!webhook.isValid()) {
            plugin.getLogger().severe("Invalid Discord webhook URL! Please check config/discord.yml");
            enabled = false;
            return;
        }
        
        plugin.getLogger().info("Discord integration initialized successfully");
    }

    /**
     * Отправить сообщение чата в Discord
     */
    public void sendChatMessage(Player player, String message, boolean isGlobal) {
        if (!enabled || !config.getBoolean("chat.enabled", true)) return;
        
        // Проверяем фильтр глобального чата
        if (config.getBoolean("chat.global_only", true) && !isGlobal) {
            return;
        }
        
        // Фильтрация сообщения
        String cleanMessage = sanitizeMessage(message);
        if (cleanMessage.length() < config.getInt("filter.min_message_length", 1)) {
            return;
        }
        
        // Обрезаем если слишком длинное
        int maxLength = config.getInt("filter.max_message_length", 2000);
        if (cleanMessage.length() > maxLength) {
            cleanMessage = cleanMessage.substring(0, maxLength - 3) + "...";
        }
        
        String playerName = player.getName();
        String format = config.getString("chat.format", "**{player}**: {message}");
        String formattedMessage = format
                .replace("{player}", playerName)
                .replace("{message}", cleanMessage);
        
        // Отправляем асинхронно
        if (config.getBoolean("performance.async", true)) {
            FoliaUtil.runAsync(plugin, () -> sendChatMessageInternal(formattedMessage, playerName, isGlobal));
        } else {
            sendChatMessageInternal(formattedMessage, playerName, isGlobal);
        }
    }

    /**
     * Внутренний метод отправки сообщения чата
     */
    private void sendChatMessageInternal(String message, String playerName, boolean isGlobal) {
        if (config.getBoolean("chat.use_embed", true)) {
            String title = isGlobal ? "🌍 Глобальный чат" : "📍 Локальный чат";
            String color = config.getString("chat.embed_color", "5865F2");
            String thumbnail = "https://mc-heads.net/avatar/" + playerName + "/64";
            
            webhook.sendEmbed(title, message, color, thumbnail);
        } else {
            webhook.sendMessage(message);
        }
    }

    /**
     * Отправить событие входа игрока
     */
    public void sendPlayerJoin(Player player) {
        if (!enabled || !config.getBoolean("events.enabled", true) || 
            !config.getBoolean("events.join.enabled", true)) return;
        
        String playerName = player.getName();
        String title = config.getString("events.join.title", "🟢 Игрок зашел на сервер");
        String description = config.getString("events.join.description", "**{player}** присоединился к серверу")
                .replace("{player}", playerName);
        String color = config.getString("events.join.color", "00FF00");
        String thumbnail = config.getString("events.join.thumbnail", "https://mc-heads.net/avatar/{player}/64")
                .replace("{player}", playerName);
        
        if (config.getBoolean("performance.async", true)) {
            FoliaUtil.runAsync(plugin, () -> webhook.sendEmbed(title, description, color, thumbnail));
        } else {
            webhook.sendEmbed(title, description, color, thumbnail);
        }
    }

    /**
     * Отправить событие выхода игрока
     */
    public void sendPlayerQuit(Player player) {
        if (!enabled || !config.getBoolean("events.enabled", true) || 
            !config.getBoolean("events.quit.enabled", true)) return;
        
        String playerName = player.getName();
        String title = config.getString("events.quit.title", "🔴 Игрок покинул сервер");
        String description = config.getString("events.quit.description", "**{player}** покинул сервер")
                .replace("{player}", playerName);
        String color = config.getString("events.quit.color", "FF0000");
        String thumbnail = config.getString("events.quit.thumbnail", "https://mc-heads.net/avatar/{player}/64")
                .replace("{player}", playerName);
        
        if (config.getBoolean("performance.async", true)) {
            FoliaUtil.runAsync(plugin, () -> webhook.sendEmbed(title, description, color, thumbnail));
        } else {
            webhook.sendEmbed(title, description, color, thumbnail);
        }
    }

    /**
     * Отправить событие смерти игрока
     */
    public void sendPlayerDeath(Player player, String deathMessage) {
        if (!enabled || !config.getBoolean("events.enabled", true) || 
            !config.getBoolean("events.death.enabled", true)) return;
        
        String playerName = player.getName();
        String title = config.getString("events.death.title", "💀 Смерть игрока");
        String description = config.getString("events.death.description", "**{player}** {death_message}")
                .replace("{player}", playerName)
                .replace("{death_message}", sanitizeMessage(deathMessage));
        String color = config.getString("events.death.color", "800080");
        String thumbnail = config.getString("events.death.thumbnail", "https://mc-heads.net/avatar/{player}/64")
                .replace("{player}", playerName);
        
        if (config.getBoolean("performance.async", true)) {
            FoliaUtil.runAsync(plugin, () -> webhook.sendEmbed(title, description, color, thumbnail));
        } else {
            webhook.sendEmbed(title, description, color, thumbnail);
        }
    }

    /**
     * Очистить сообщение от нежелательных элементов
     */
    private String sanitizeMessage(String message) {
        if (message == null) return "";
        
        // Удаляем цветовые коды Minecraft
        message = COLOR_CODE_PATTERN.matcher(message).replaceAll("");
        
        // Заменяем опасные упоминания
        if (config.getBoolean("filter.sanitize_mentions", true)) {
            message = MENTION_PATTERN.matcher(message).replaceAll("@\u200B$1"); // Добавляем невидимый символ
        }
        
        // Экранируем Discord markdown
        message = message.replace("*", "\\*")
                        .replace("_", "\\_")
                        .replace("`", "\\`")
                        .replace("~", "\\~");
        
        return message.trim();
    }

    /**
     * Перезагрузить конфигурацию
     */
    public void reload() {
        loadConfig();
        initWebhook();
        plugin.getLogger().info("Discord integration reloaded");
    }

    /**
     * Отправить тестовое сообщение
     */
    public void sendTestMessage(String message, String senderName) {
        if (!enabled) return;
        
        String title = "🧪 Тестовое сообщение";
        String description = "**Отправитель:** " + senderName + "\n**Сообщение:** " + message;
        String color = "FFD700"; // золотой цвет
        String thumbnail = "https://mc-heads.net/avatar/" + senderName + "/64";
        
        webhook.sendEmbed(title, description, color, thumbnail);
    }

    /**
     * Проверить включена ли интеграция
     */
    public boolean isEnabled() {
        return enabled;
    }
}