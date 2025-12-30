package com.loki.lochat.listeners;

import com.loki.lochat.LoChat;
import com.loki.lochat.managers.ChatColorManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * Слушатель для обновления display name игроков с префиксом и цветом ника
 */
public class DisplayNameListener implements Listener {

    private final LoChat plugin;
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    public DisplayNameListener(LoChat plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        // Обновляем display name через RegionScheduler для Folia
        player.getScheduler().run(plugin, task -> {
            updatePlayerDisplayName(player);
        }, null);
    }

    /**
     * Обновляет display name игрока с префиксом и цветом ника
     */
    public void updatePlayerDisplayName(Player player) {
        // Если градиентный модуль включен и обновляет display name, не дублируем
        if (plugin.getGradientModule() != null && plugin.getGradientModule().isEnabled()) {
            if (plugin.getGradientModule().getConfig().isUpdateDisplayName()) {
                // Градиентный модуль сам обновляет display name
                return;
            }
        }

        String displayName = buildDisplayName(player);
        Component component = MINI_MESSAGE.deserialize(displayName);
        player.displayName(component);
    }

    /**
     * Строит display name с префиксом и цветом ника
     */
    private String buildDisplayName(Player player) {
        StringBuilder result = new StringBuilder();
        
        // Получаем префикс из градиентного модуля (если есть)
        String prefix = null;
        if (plugin.getGradientModule() != null && plugin.getGradientModule().isEnabled()) {
            var data = plugin.getGradientModule().getDataManager().getPlayerData(player.getUniqueId());
            if (data != null && data.isPrefixEnabled() && data.hasPrefix()) {
                prefix = plugin.getGradientModule().getConfig().getPrefixFormat()
                        .replace("{prefix}", data.getPrefix());
            }
        }
        
        // Добавляем префикс если есть
        if (prefix != null && !prefix.isEmpty()) {
            result.append(prefix);
        }
        
        // Получаем цвет чата
        ChatColorManager colorManager = plugin.getChatColorManager();
        String chatColor = colorManager.getChatColor(player.getUniqueId());
        
        // Получаем имя игрока
        String playerName = player.getName();
        
        // Применяем цвет к нику
        if (chatColor != null) {
            // Если цвет в формате &X, конвертируем его в MiniMessage
            if (chatColor.startsWith("&") && chatColor.length() == 2) {
                String miniMessageColor = convertLegacyColorToMiniMessage(chatColor);
                if (!miniMessageColor.isEmpty()) {
                    String tagName = miniMessageColor.substring(1, miniMessageColor.length() - 1);
                    result.append(miniMessageColor).append(playerName).append("</").append(tagName).append(">");
                } else {
                    result.append(playerName);
                }
            } else {
                // Если уже в формате MiniMessage
                result.append("<").append(chatColor).append(">").append(playerName).append("</").append(chatColor).append(">");
            }
        } else {
            // Нет цвета, просто имя
            result.append(playerName);
        }
        
        return result.toString();
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

