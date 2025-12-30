package com.loki.lochat.managers;

import com.loki.lochat.LoChat;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ChatColorManager {

    private final LoChat plugin;
    private final Map<UUID, String> chatColors;
    private File dataFile;
    private FileConfiguration dataConfig;

    public ChatColorManager(LoChat plugin) {
        this.plugin = plugin;
        this.chatColors = new HashMap<>();
        loadData();
    }

    private void loadData() {
        dataFile = new File(plugin.getDataFolder(), "chatcolors.yml");
        if (!dataFile.exists()) {
            try {
                dataFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Не удалось создать файл chatcolors.yml: " + e.getMessage());
            }
        }
        
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
        
        // Загружаем цвета из файла
        for (String key : dataConfig.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(key);
                String color = dataConfig.getString(key);
                if (color != null) {
                    chatColors.put(uuid, color);
                }
            } catch (IllegalArgumentException ignored) {
                // Неверный UUID, пропускаем
            }
        }
    }

    public void saveData() {
        for (Map.Entry<UUID, String> entry : chatColors.entrySet()) {
            dataConfig.set(entry.getKey().toString(), entry.getValue());
        }
        
        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Не удалось сохранить chatcolors.yml: " + e.getMessage());
        }
    }

    public void setChatColor(UUID playerId, String color) {
        chatColors.put(playerId, color);
        saveData();
    }

    public String getChatColor(UUID playerId) {
        return chatColors.get(playerId);
    }

    public boolean hasChatColor(UUID playerId) {
        return chatColors.containsKey(playerId);
    }

    public void removeChatColor(UUID playerId) {
        chatColors.remove(playerId);
        dataConfig.set(playerId.toString(), null);
        saveData();
    }

    /**
     * Применяет цвет чата к сообщению
     * Поддерживает формат &X (Minecraft color codes)
     */
    public String applyChatColor(UUID playerId, String message) {
        String color = getChatColor(playerId);
        if (color != null) {
            // Если цвет в формате &X, конвертируем его в MiniMessage
            if (color.startsWith("&") && color.length() == 2) {
                String miniMessageColor = convertLegacyColorToMiniMessage(color);
                if (!miniMessageColor.isEmpty()) {
                    // Извлекаем имя тега из <tag> для закрывающего тега
                    String tagName = miniMessageColor.substring(1, miniMessageColor.length() - 1);
                    return miniMessageColor + message + "</" + tagName + ">";
                }
            }
            // Если уже в формате MiniMessage, используем как есть
            return "<" + color + ">" + message + "</" + color + ">";
        }
        return message;
    }
    
    /**
     * Конвертирует &X код в MiniMessage формат
     */
    private String convertLegacyColorToMiniMessage(String legacyColor) {
        if (legacyColor == null || legacyColor.length() != 2 || !legacyColor.startsWith("&")) {
            return "";
        }
        
        char code = legacyColor.charAt(1);
        return switch (Character.toLowerCase(code)) {
            case '0' -> "<black>";
            case '1' -> "<dark_blue>";
            case '2' -> "<dark_green>";
            case '3' -> "<dark_aqua>";
            case '4' -> "<dark_red>";
            case '5' -> "<dark_purple>";
            case '6' -> "<gold>";
            case '7' -> "<gray>";
            case '8' -> "<dark_gray>";
            case '9' -> "<blue>";
            case 'a' -> "<green>";
            case 'b' -> "<aqua>";
            case 'c' -> "<red>";
            case 'd' -> "<light_purple>";
            case 'e' -> "<yellow>";
            case 'f' -> "<white>";
            case 'k' -> "<obfuscated>";
            case 'l' -> "<bold>";
            case 'm' -> "<strikethrough>";
            case 'n' -> "<underlined>";
            case 'o' -> "<italic>";
            case 'r' -> "<reset>";
            default -> "";
        };
    }
}