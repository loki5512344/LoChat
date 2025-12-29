package com.loki.lochat.gradient.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * Сообщения градиентного модуля
 */
public class GradientMessages {

    private final JavaPlugin plugin;
    private FileConfiguration messages;
    private File messagesFile;

    public GradientMessages(JavaPlugin plugin) {
        this.plugin = plugin;
        loadMessages();
    }

    private void loadMessages() {
        messagesFile = new File(plugin.getDataFolder(), "gradient-messages.yml");
        if (!messagesFile.exists()) {
            plugin.saveResource("gradient-messages.yml", false);
        }
        messages = YamlConfiguration.loadConfiguration(messagesFile);
        
        InputStream defStream = plugin.getResource("gradient-messages.yml");
        if (defStream != null) {
            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(
                    new InputStreamReader(defStream, StandardCharsets.UTF_8));
            messages.setDefaults(defConfig);
        }
    }

    public void reload() {
        loadMessages();
    }

    public String get(String key) {
        return messages.getString(key, "§cСообщение не найдено: " + key);
    }

    public String get(String key, String... replacements) {
        String message = get(key);
        for (int i = 0; i < replacements.length; i += 2) {
            if (i + 1 < replacements.length) {
                message = message.replace("{" + replacements[i] + "}", replacements[i + 1]);
            }
        }
        return message;
    }
}
