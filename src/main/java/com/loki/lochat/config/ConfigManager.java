package com.loki.lochat.config;

import com.loki.lochat.LoChat;
import com.loki.lochat.utils.ChatFormatter;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;

public class ConfigManager {

    private final LoChat plugin;
    private FileConfiguration config;

    public ConfigManager(LoChat plugin) {
        this.plugin = plugin;
        plugin.saveDefaultConfig();
        this.config = plugin.getConfig();
        
        // Проверяем версию конфига и обновляем при необходимости
        updateConfig();
    }

    private void updateConfig() {
        int currentVersion = config.getInt("config-version", 1);
        int requiredVersion = 2; // Текущая версия конфига
        
        if (currentVersion < requiredVersion) {
            plugin.getLogger().info("Обновление конфига с версии " + currentVersion + " до " + requiredVersion);
            
            // Добавляем новые настройки если их нет
            addMissingConfigOptions();
            
            // Обновляем версию
            config.set("config-version", requiredVersion);
            plugin.saveConfig();
            
            plugin.getLogger().info("Конфиг успешно обновлен!");
        }
    }

    private void addMissingConfigOptions() {
        // Добавляем настройки объявлений если их нет
        if (!config.contains("announcements")) {
            config.set("announcements.show-title", true);
            config.set("announcements.title-header", "&#FFD700ОБЪЯВЛЕНИЕ");
            config.set("announcements.show-actionbar", true);
            config.set("announcements.title-duration", 3);
        }
        
        // Можно добавить другие недостающие настройки
        if (!config.contains("chat.pm.enabled")) {
            config.set("chat.pm.enabled", true);
        }
        
        if (!config.contains("mentions.enabled")) {
            config.set("mentions.enabled", true);
            config.set("mentions.sound", true);
            config.set("mentions.sound-type", "BLOCK_NOTE_BLOCK_PLING");
            config.set("mentions.highlight", "&#FFFF00@{player}");
            config.set("mentions.self-highlight", "&#FFD700{player}");
        }
        
        if (!config.contains("filter.enabled")) {
            config.set("filter.enabled", true);
            config.set("filter.words", java.util.Arrays.asList("хуй", "пизд", "блять", "ебать", "сука"));
            config.set("filter.action", "censor");
            config.set("filter.replacement", "***");
        }
        
        if (!config.contains("antispam.enabled")) {
            config.set("antispam.enabled", true);
            config.set("antispam.max-caps-percent", 70);
            config.set("antispam.max-repeat-chars", 5);
            config.set("antispam.similar-message-delay", 30);
        }
        
        if (!config.contains("automessages.enabled")) {
            config.set("automessages.enabled", true);
            config.set("automessages.interval", 300);
            config.set("automessages.random", false);
        }
        
        if (!config.contains("clearchat.message.enabled")) {
            config.set("clearchat.message.enabled", true);
            config.set("clearchat.message.text", "&#04CADFЧат очищен администратором {player}");
        }
        
        if (!config.contains("head-emoji.enabled")) {
            config.set("head-emoji.enabled", true);
            config.set("head-emoji.allow-player-heads", true);
            config.set("head-emoji.max-heads-per-message", 3);
        }
        
        if (!config.contains("lopreff.enabled")) {
            config.set("lopreff.enabled", true);
            config.set("lopreff.use-gradient-name", true);
            config.set("lopreff.use-custom-prefix", true);
        }
    }

    public void reload() {
        plugin.reloadConfig();
        this.config = plugin.getConfig();
    }

    /**
     * Получить строку из конфига с дефолтным значением
     */
    public String getString(String path, String defaultValue) {
        return config.getString(path, defaultValue);
    }

    /**
     * Получить строку из конфига
     */
    public String getString(String path) {
        return config.getString(path);
    }

    /**
     * Получить int из конфига
     */
    public int getInt(String path, int defaultValue) {
        return config.getInt(path, defaultValue);
    }

    /**
     * Получить boolean из конфига
     */
    public boolean getBoolean(String path, boolean defaultValue) {
        return config.getBoolean(path, defaultValue);
    }

    /**
     * Получить список строк из конфига
     */
    public List<String> getStringList(String path) {
        return config.getStringList(path);
    }

    // Global chat
    public boolean isGlobalEnabled() {
        return config.getBoolean("chat.global.enabled", true);
    }

    public String getGlobalPrefix() {
        return ChatFormatter.convertAllColors(config.getString("chat.global.prefix", "[G]"));
    }

    public String getGlobalFormat() {
        return ChatFormatter.convertAllColors(config.getString("chat.global.format", "<prefix><player>: <message>"));
    }

    public String getGlobalSymbol() {
        return config.getString("chat.global.symbol", "!");
    }

    public int getGlobalCooldown() {
        return config.getInt("chat.global.cooldown", 3);
    }

    // Local chat
    public boolean isLocalEnabled() {
        return config.getBoolean("chat.local.enabled", true);
    }

    public int getLocalRadius() {
        return config.getInt("chat.local.radius", com.loki.lochat.utils.Constants.DEFAULT_LOCAL_RADIUS);
    }

    public String getLocalPrefix() {
        return ChatFormatter.convertAllColors(config.getString("chat.local.prefix", "[L]"));
    }

    public String getLocalFormat() {
        return ChatFormatter.convertAllColors(config.getString("chat.local.format", "<player>: <message>"));
    }

    public int getLocalCooldown() {
        return config.getInt("chat.local.cooldown", 1);
    }

    // PM
    public boolean isPmEnabled() {
        return config.getBoolean("chat.pm.enabled", true);
    }

    public boolean isPmLogEnabled() {
        return config.getBoolean("chat.pm.log", false);
    }

    public boolean isPmSoundEnabled() {
        return config.getBoolean("chat.pm.sound", true);
    }

    public String getPmSoundType() {
        return config.getString("chat.pm.sound-type", "ENTITY_EXPERIENCE_ORB_PICKUP");
    }

    // Mentions
    public boolean isMentionsEnabled() {
        return config.getBoolean("mentions.enabled", true);
    }

    public boolean isMentionSoundEnabled() {
        return config.getBoolean("mentions.sound", true);
    }

    public String getMentionSoundType() {
        return config.getString("mentions.sound-type", "BLOCK_NOTE_BLOCK_PLING");
    }

    public String getMentionHighlight() {
        return ChatFormatter.convertAllColors(config.getString("mentions.highlight", "&#FFFF00@{player}"));
    }

    public String getSelfMentionHighlight() {
        return ChatFormatter.convertAllColors(config.getString("mentions.self-highlight", "&#FFD700{player}"));
    }

    // Filter
    public boolean isFilterEnabled() {
        return config.getBoolean("filter.enabled", true);
    }

    public List<String> getFilterWords() {
        return config.getStringList("filter.words");
    }

    public String getFilterAction() {
        return config.getString("filter.action", "censor");
    }

    public String getFilterReplacement() {
        return config.getString("filter.replacement", "***");
    }
    
    // Clear chat
    public boolean isClearChatMessageEnabled() {
        return config.getBoolean("clearchat.message.enabled", true);
    }
    
    public void setClearChatMessageEnabled(boolean enabled) {
        config.set("clearchat.message.enabled", enabled);
        plugin.saveConfig();
    }
    
    public String getClearChatMessage() {
        return config.getString("clearchat.message.text", "&#04CADFЧат очищен администратором {player}");
    }
    
    public void setClearChatMessage(String message) {
        config.set("clearchat.message.text", message);
        plugin.saveConfig();
    }

    // Anti-spam
    public boolean isAntiSpamEnabled() {
        return config.getBoolean("antispam.enabled", true);
    }

    public int getMaxCapsPercent() {
        return config.getInt("antispam.max-caps-percent", 70);
    }

    public int getMaxRepeatChars() {
        return config.getInt("antispam.max-repeat-chars", 5);
    }

    public int getSimilarMessageDelay() {
        return config.getInt("antispam.similar-message-delay", 30);
    }

    // Auto messages
    public boolean isAutoMessagesEnabled() {
        return config.getBoolean("automessages.enabled", true);
    }

    public int getAutoMessagesInterval() {
        return config.getInt("automessages.interval", 300);
    }

    public boolean isAutoMessagesRandom() {
        return config.getBoolean("automessages.random", false);
    }

    // Formats
    public String getAnnouncementFormat() {
        return ChatFormatter.convertAllColors(config.getString("formats.announcement", "&#FFD700[ОБЪЯВЛЕНИЕ] <message>"));
    }

    // Announcements
    public boolean isAnnouncementTitleEnabled() {
        return config.getBoolean("announcements.show-title", true);
    }

    public String getAnnouncementTitleHeader() {
        return ChatFormatter.convertAllColors(config.getString("announcements.title-header", "&#FFD700ОБЪЯВЛЕНИЕ"));
    }

    public boolean isAnnouncementActionBarEnabled() {
        return config.getBoolean("announcements.show-actionbar", true);
    }

    public int getAnnouncementTitleDuration() {
        return config.getInt("announcements.title-duration", 3);
    }

    // LoPreff integration
    public boolean isLopreffEnabled() {
        return config.getBoolean("lopreff.enabled", true);
    }

    public boolean useGradientName() {
        return config.getBoolean("lopreff.use-gradient-name", true);
    }

    public boolean useCustomPrefix() {
        return config.getBoolean("lopreff.use-custom-prefix", true);
    }
}
