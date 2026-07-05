package com.loki.lochat.managers;

import com.loki.lochat.LoChat;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class EmojiManager {

    private final LoChat plugin;
    private final Map<String, String> emojis = new HashMap<>();
    private boolean enabled;
    private boolean requirePermission;

    private EmojiManager(LoChat plugin) {
        this.plugin = plugin;
    }

    public static EmojiManager create(LoChat plugin) {
        EmojiManager manager = new EmojiManager(plugin);
        manager.load();
        return manager;
    }

    public void load() {
        File file = new File(plugin.getDataFolder(), "emojis.yml");
        if (!file.exists()) {
            plugin.saveResource("emojis.yml", false);
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        emojis.clear();

        // Настройки
        enabled = config.getBoolean("settings.enabled", true);
        requirePermission = config.getBoolean("settings.require-permission", false);

        // Загружаем все категории
        for (String category : config.getKeys(false)) {
            if (category.equals("settings")) {
                continue;
            }

            ConfigurationSection section = config.getConfigurationSection(category);
            if (section != null) {
                for (String key : section.getKeys(false)) {
                    String value = section.getString(key);
                    if (value != null) {
                        emojis.put(key, value);
                    }
                }
            }
        }

        plugin.getLogger().info("Загружено " + emojis.size() + " смайликов");
    }

    public void reload() {
        load();
    }

    /**
     * Заменяет смайлики в сообщении
     */
    public String process(String message, Player player) {
        if (!enabled) {
            return message;
        }

        // Проверка права
        if (requirePermission && player != null && !player.hasPermission("chat.emoji.use")) {
            return message;
        }

        String result = message;
        for (Map.Entry<String, String> entry : emojis.entrySet()) {
            result = result.replace(entry.getKey(), entry.getValue());
        }

        return result;
    }

    public String process(String message) {
        if (!enabled) {
            return message;
        }

        String result = message;
        for (Map.Entry<String, String> entry : emojis.entrySet()) {
            result = result.replace(entry.getKey(), entry.getValue());
        }

        return result;
    }

    public Map<String, String> getEmojis() {
        return emojis;
    }

    public boolean isEnabled() {
        return enabled;
    }
}
