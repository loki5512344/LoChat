package com.loki.lochat.config;

import com.loki.lochat.LoChat;
import com.loki.lochat.utils.ChatFormatter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class MessageConfig {

    private final LoChat plugin;
    private FileConfiguration messages;
    private File messagesFile;

    public MessageConfig(LoChat plugin) {
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

    public String get(String path) {
        return messages.getString(path, "<red>Message not found: " + path + "</red>");
    }

    public String get(String path, String... replacements) {
        String message = get(path);
        for (int i = 0; i < replacements.length - 1; i += 2) {
            message = message.replace(replacements[i], replacements[i + 1]);
        }
        // Убираем legacy коды если они есть
        message = ChatFormatter.convertAllColors(message);
        return message;
    }

    // Shortcuts
    public String getPrefix() {
        return get("prefix");
    }

    public String getNoPermission() {
        return get("errors.no-permission");
    }

    public String getPlayerNotFound() {
        return get("errors.player-not-found");
    }

    public String getInvalidUsage(String usage) {
        return get("errors.invalid-usage", "{usage}", usage);
    }

    // PM форматы
    public String getPmFormatSent() {
        return get("pm.format-sent");
    }

    public String getPmFormatReceived() {
        return get("pm.format-received");
    }

    public boolean isPmUseGradientNames() {
        return messages.getBoolean("pm.use-gradient-names", true);
    }
}
