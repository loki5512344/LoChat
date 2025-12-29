package ru.lovar.gradientnick.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import ru.lovar.gradientnick.GradientNick;

import java.io.File;

public class MessagesManager {

    private final GradientNick plugin;
    private FileConfiguration messages;
    private File messagesFile;

    public MessagesManager(GradientNick plugin) {
        this.plugin = plugin;
        loadMessages();
    }

    private void loadMessages() {
        messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        if (!messagesFile.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        messages = YamlConfiguration.loadConfiguration(messagesFile);
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
