package com.loki.lochat.config;

import com.loki.lochat.LoChat;
import com.loki.lochat.utils.format.ChatFormatter;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;

/**
 * Менеджер конфигураций - упрощенная версия
 */
public class ConfigManager {

    private final LoChat plugin;
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

        // Инициализируем конфигурации
        this.appearanceConfig = new AppearanceConfig(plugin);
        this.messagesConfig = new MessagesConfig(plugin);
        this.muteConfig = new MuteConfig(plugin);
        this.filtersConfig = new FiltersConfig(plugin);
        this.soundsConfig = new SoundsConfig(plugin);
        
        this.appearanceConfig.init();

        updateConfig();
    }

    // ========== Геттеры конфигураций ==========

    public AppearanceConfig getAppearanceConfig() {
        return appearanceConfig;
    }

    public MessagesConfig getMessagesConfig() {
        return messagesConfig;
    }

    public MuteConfig getMuteConfig() {
        return muteConfig;
    }

    public FiltersConfig getFiltersConfig() {
        return filtersConfig;
    }

    public SoundsConfig getSoundsConfig() {
        return soundsConfig;
    }

    // ========== Обновление и перезагрузка ==========

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
        setIfMissing("announcements.show-title", true);
        setIfMissing("announcements.title-header", "&#FFD700ОБЪЯВЛЕНИЕ");
        setIfMissing("announcements.show-actionbar", true);
        setIfMissing("announcements.title-duration", 3);
        setIfMissing("chat.pm.enabled", true);
        setIfMissing("mentions.enabled", true);
        setIfMissing("mentions.sound", true);
        setIfMissing("mentions.sound-type", "BLOCK_NOTE_BLOCK_PLING");
        setIfMissing("mentions.highlight", "&#FFFF00@{player}");
        setIfMissing("mentions.self-highlight", "&#FFD700{player}");
        setIfMissing("automessages.enabled", true);
        setIfMissing("automessages.interval", 300);
        setIfMissing("automessages.random", false);
        setIfMissing("clearchat.message.enabled", true);
        setIfMissing("clearchat.message.text", "&#04CADFЧат очищен администратором {player}");
        setIfMissing("lopreff.enabled", true);
        setIfMissing("lopreff.use-gradient-name", true);
        setIfMissing("lopreff.use-custom-prefix", true);
        setIfMissing("moderation.voice-mute-console-command", "");
    }

    private void setIfMissing(String path, Object value) {
        if (!config.contains(path)) {
            config.set(path, value);
        }
    }

    public synchronized void reload() {
        plugin.reloadConfig();
        this.config = plugin.getConfig();
        
        this.appearanceConfig.reload();
        this.messagesConfig.reload();
        this.muteConfig.reload();
        this.filtersConfig.reload();
        this.soundsConfig.reload();
        
        plugin.getLogger().info("Все конфигурации успешно перезагружены");
    }

    // ========== Базовые методы доступа ==========

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

    // ========== Настройки чата ==========

    public boolean isGlobalEnabled() {
        return config.getBoolean("chat.global.enabled", true);
    }

    public int getGlobalCooldown() {
        return config.getInt("chat.global.cooldown", 3);
    }

    public boolean isLocalEnabled() {
        return config.getBoolean("chat.local.enabled", true);
    }

    public int getLocalRadius() {
        return appearanceConfig.getLocalRadius();
    }

    public int getLocalCooldown() {
        return config.getInt("chat.local.cooldown", 1);
    }

    // ========== PM ==========

    public boolean isPmEnabled() {
        return config.getBoolean("chat.pm.enabled", true);
    }

    public boolean isPmLogEnabled() {
        return config.getBoolean("chat.pm.log", false);
    }

    public boolean isPmSoundEnabled() {
        return soundsConfig != null ? soundsConfig.isPmSoundEnabled() : 
               config.getBoolean("chat.pm.sound", true);
    }

    public String getPmSoundType() {
        return soundsConfig != null ? soundsConfig.getPmSound() : 
               config.getString("chat.pm.sound-type", "ENTITY_EXPERIENCE_ORB_PICKUP");
    }

    // ========== Mentions ==========

    public boolean isMentionsEnabled() {
        return config.getBoolean("mentions.enabled", true);
    }

    public boolean isMentionSoundEnabled() {
        return soundsConfig != null ? soundsConfig.isMentionSoundEnabled() : 
               config.getBoolean("mentions.sound", true);
    }

    public String getMentionSoundType() {
        return soundsConfig != null ? soundsConfig.getMentionSound() : 
               config.getString("mentions.sound-type", "BLOCK_NOTE_BLOCK_PLING");
    }

    public String getMentionHighlight() {
        return ChatFormatter.convertAllColors(config.getString("mentions.highlight", "&#FFFF00@{player}"));
    }

    public String getSelfMentionHighlight() {
        return ChatFormatter.convertAllColors(config.getString("mentions.self-highlight", "&#FFD700{player}"));
    }

    // ========== Clear chat ==========

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

    // ========== Auto messages ==========

    public boolean isAutoMessagesEnabled() {
        return config.getBoolean("automessages.enabled", true);
    }

    public int getAutoMessagesInterval() {
        return config.getInt("automessages.interval", 300);
    }

    public boolean isAutoMessagesRandom() {
        return config.getBoolean("automessages.random", false);
    }

    // ========== Formats ==========

    public String getAnnouncementFormat() {
        return ChatFormatter.convertAllColors(config.getString("formats.announcement", 
            "&#FFD700[ОБЪЯВЛЕНИЕ] <message>"));
    }

    // ========== Announcements ==========

    public boolean isAnnouncementTitleEnabled() {
        return config.getBoolean("announcements.show-title", true);
    }

    public String getAnnouncementTitleHeader() {
        return ChatFormatter.convertAllColors(config.getString("announcements.title-header", 
            "&#FFD700ОБЪЯВЛЕНИЕ"));
    }

    public boolean isAnnouncementActionBarEnabled() {
        return config.getBoolean("announcements.show-actionbar", true);
    }

    public int getAnnouncementTitleDuration() {
        return config.getInt("announcements.title-duration", 3);
    }

    // ========== LoPreff integration ==========

    public boolean isLopreffEnabled() {
        return config.getBoolean("lopreff.enabled", true);
    }

    public boolean useGradientName() {
        return config.getBoolean("lopreff.use-gradient-name", true);
    }

    public boolean useCustomPrefix() {
        return config.getBoolean("lopreff.use-custom-prefix", true);
    }

    // ========== Кастомные сообщения ==========

    public boolean isJoinMessageEnabled() {
        return config.getBoolean("messages.join.enabled", true);
    }

    public String getJoinMessageFormat() {
        return config.getString("messages.join.format", "&#9878C9✦ &#B798A8{player} &#7858E9зашел на сервер");
    }

    public boolean isQuitMessageEnabled() {
        return config.getBoolean("messages.quit.enabled", true);
    }

    public String getQuitMessageFormat() {
        return config.getString("messages.quit.format", "&#9878C9✦ &#B798A8{player} &#7858E9вышел с сервера");
    }

    public boolean isDeathMessageEnabled() {
        return config.getBoolean("messages.death.enabled", true);
    }

    public String getDeathPlayerKillFormat() {
        return config.getString("messages.death.player_kill", "&#CF6679☠ &#B798A8{player} &#7858E9был убит игроком &#B798A8{killer} &#9878C9({weapon})");
    }

    public String getDeathMobKillFormat() {
        return config.getString("messages.death.mob_kill", "&#CF6679☠ &#B798A8{player} &#7858E9был убит мобом &#B798A8{killer}");
    }

    public String getDeathDefaultFormat() {
        return config.getString("messages.death.default", "&#CF6679☠ &#B798A8{player} &#7858E9{death_message}");
    }
}
