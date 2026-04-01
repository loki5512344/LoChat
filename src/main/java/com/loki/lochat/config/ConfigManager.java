package com.loki.lochat.config;

import com.loki.lochat.LoChat;
import com.loki.lochat.utils.ChatFormatter;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;

public class ConfigManager {

    private final LoChat plugin;
    
    // ✅ THREAD-SAFETY: volatile для безопасного reload из разных потоков
    // volatile гарантирует что все потоки видят актуальную версию конфига после reload
    private volatile FileConfiguration config;
    private volatile AppearanceConfig appearanceConfig;
    private volatile MessagesConfig messagesConfig;
    private volatile MuteConfig muteConfig;
    private volatile FiltersConfig filtersConfig;
    private volatile SoundsConfig soundsConfig;

    public ConfigManager(LoChat plugin) {
        this.plugin = plugin;
        plugin.saveDefaultConfig();
        this.config = plugin.getConfig();

        // Инициализируем новые конфигурации
        this.appearanceConfig = new AppearanceConfig(plugin);
        this.messagesConfig = new MessagesConfig(plugin);
        this.muteConfig = new MuteConfig(plugin);
        this.filtersConfig = new FiltersConfig(plugin);
        this.soundsConfig = new SoundsConfig(plugin);
        
        // Инициализируем их после создания
        this.appearanceConfig.init();

        // Проверяем версию конфига и обновляем при необходимости
        updateConfig();
    }

    /**
     * Получить конфигурацию внешнего вида
     */
    public AppearanceConfig getAppearanceConfig() {
        return appearanceConfig;
    }

    /**
     * Получить конфигурацию системных сообщений
     */
    public MessagesConfig getMessagesConfig() {
        return messagesConfig;
    }

    /**
     * Получить конфигурацию мутов
     */
    public MuteConfig getMuteConfig() {
        return muteConfig;
    }

    /**
     * Получить конфигурацию фильтров
     */
    public FiltersConfig getFiltersConfig() {
        return filtersConfig;
    }

    /**
     * Получить конфигурацию звуков
     */
    public SoundsConfig getSoundsConfig() {
        return soundsConfig;
    }

    private void updateConfig() {
        int currentVersion = config.getInt("config-version", 1);
        int requiredVersion = 5;

        if (currentVersion < requiredVersion) {
            plugin.getLogger().info("Обновление конфига с версии " + currentVersion + " до " + requiredVersion);
            addMissingConfigOptions();
            config.set("config-version", requiredVersion);
            plugin.saveConfig();
            plugin.getLogger().info("Конфиг успешно обновлен!");
        }
    }

    private void addMissingConfigOptions() {
        if (!config.contains("announcements")) {
            config.set("announcements.show-title", true);
            config.set("announcements.title-header", "&#FFD700ОБЪЯВЛЕНИЕ");
            config.set("announcements.show-actionbar", true);
            config.set("announcements.title-duration", 3);
        }

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

        if (!config.contains("automessages.enabled")) {
            config.set("automessages.enabled", true);
            config.set("automessages.interval", 300);
            config.set("automessages.random", false);
        }

        if (!config.contains("clearchat.message.enabled")) {
            config.set("clearchat.message.enabled", true);
            config.set("clearchat.message.text", "&#04CADFЧат очищен администратором {player}");
        }

        if (!config.contains("lopreff.enabled")) {
            config.set("lopreff.enabled", true);
            config.set("lopreff.use-gradient-name", true);
            config.set("lopreff.use-custom-prefix", true);
        }

        if (!config.contains("moderation.voice-mute-console-command")) {
            config.set("moderation.voice-mute-console-command", "");
        }
    }

    /**
     * Перезагрузить все конфигурации
     * ✅ THREAD-SAFE: использует volatile для безопасного обновления
     */
    public synchronized void reload() {
        plugin.reloadConfig();
        this.config = plugin.getConfig();
        
        // Перезагружаем новые конфигурации
        // volatile гарантирует что изменения видны всем потокам
        this.appearanceConfig.reload();
        this.messagesConfig.reload();
        this.muteConfig.reload();
        this.filtersConfig.reload();
        this.soundsConfig.reload();
        
        plugin.getLogger().info("Все конфигурации успешно перезагружены");
    }

    public String getString(String path, String defaultValue) {
        return config.getString(path, defaultValue);
    }

    public String getString(String path) {
        return config.getString(path);
    }

    public int getInt(String path, int defaultValue) {
        return config.getInt(path, defaultValue);
    }

    public boolean getBoolean(String path, boolean defaultValue) {
        return config.getBoolean(path, defaultValue);
    }

    public List<String> getStringList(String path) {
        return config.getStringList(path);
    }

    // Global chat
    public boolean isGlobalEnabled() {
        return config.getBoolean("chat.global.enabled", true);
    }

    public int getGlobalCooldown() {
        return config.getInt("chat.global.cooldown", 3);
    }

    // Local chat
    public boolean isLocalEnabled() {
        return config.getBoolean("chat.local.enabled", true);
    }

    public int getLocalRadius() {
        return appearanceConfig.getLocalRadius();
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
        // ✅ Используем SoundsConfig если доступен
        return soundsConfig != null ? soundsConfig.isPmSoundEnabled() : config.getBoolean("chat.pm.sound", true);
    }

    public String getPmSoundType() {
        // ✅ Используем SoundsConfig если доступен
        return soundsConfig != null ? soundsConfig.getPmSound() : config.getString("chat.pm.sound-type", "ENTITY_EXPERIENCE_ORB_PICKUP");
    }

    // Mentions
    public boolean isMentionsEnabled() {
        return config.getBoolean("mentions.enabled", true);
    }

    public boolean isMentionSoundEnabled() {
        // ✅ Используем SoundsConfig если доступен
        return soundsConfig != null ? soundsConfig.isMentionSoundEnabled() : config.getBoolean("mentions.sound", true);
    }

    public String getMentionSoundType() {
        // ✅ Используем SoundsConfig если доступен
        return soundsConfig != null ? soundsConfig.getMentionSound() : config.getString("mentions.sound-type", "BLOCK_NOTE_BLOCK_PLING");
    }

    public String getMentionHighlight() {
        return ChatFormatter.convertAllColors(config.getString("mentions.highlight", "&#FFFF00@{player}"));
    }

    public String getSelfMentionHighlight() {
        return ChatFormatter.convertAllColors(config.getString("mentions.self-highlight", "&#FFD700{player}"));
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
