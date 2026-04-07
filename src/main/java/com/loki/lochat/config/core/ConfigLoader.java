package com.loki.lochat.config.core;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Загрузчик и обновлятель конфигураций
 * Отвечает за миграцию версий конфига
 */
public class ConfigLoader {

    private final JavaPlugin plugin;
    private FileConfiguration config;

    public ConfigLoader(JavaPlugin plugin) {
        this.plugin = plugin;
        plugin.saveDefaultConfig();
        this.config = plugin.getConfig();
    }

    public void updateConfig() {
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

    public void reload() {
        plugin.reloadConfig();
        this.config = plugin.getConfig();
    }

    public FileConfiguration getConfig() {
        return config;
    }
}
