package com.loki.lochat.utils;

import com.loki.lochat.LoChat;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatFormatter {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    
    // Паттерны для цветов
    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");
    private static final Pattern LEGACY_COLOR_PATTERN = Pattern.compile("&([0-9a-fk-orA-FK-OR])");

    private ChatFormatter() {}

    /**
     * Конвертирует все форматы цветов в MiniMessage
     * Поддерживает: &коды, &#HEX, MiniMessage теги
     */
    public static String convertAllColors(String message) {
        String result = message;
        
        // 1. Конвертируем &#RRGGBB -> <#RRGGBB>
        result = convertHexColors(result);
        
        // 2. Конвертируем &коды -> MiniMessage теги
        result = convertLegacyColors(result);
        
        return result;
    }

    /**
     * Конвертирует &#RRGGBB в MiniMessage формат <#RRGGBB>
     */
    public static String convertHexColors(String message) {
        Matcher matcher = HEX_PATTERN.matcher(message);
        StringBuilder result = new StringBuilder();
        while (matcher.find()) {
            matcher.appendReplacement(result, "<#" + matcher.group(1) + ">");
        }
        matcher.appendTail(result);
        return result.toString();
    }

    /**
     * Конвертирует &коды в MiniMessage теги
     * &a -> <green>, &b -> <aqua>, &l -> <bold>, etc.
     */
    public static String convertLegacyColors(String message) {
        StringBuilder result = new StringBuilder();
        Matcher matcher = LEGACY_COLOR_PATTERN.matcher(message);
        
        while (matcher.find()) {
            String code = matcher.group(1).toLowerCase();
            String replacement = switch (code) {
                case "0" -> "<black>";
                case "1" -> "<dark_blue>";
                case "2" -> "<dark_green>";
                case "3" -> "<dark_aqua>";
                case "4" -> "<dark_red>";
                case "5" -> "<dark_purple>";
                case "6" -> "<gold>";
                case "7" -> "<gray>";
                case "8" -> "<dark_gray>";
                case "9" -> "<blue>";
                case "a" -> "<green>";
                case "b" -> "<aqua>";
                case "c" -> "<red>";
                case "d" -> "<light_purple>";
                case "e" -> "<yellow>";
                case "f" -> "<white>";
                case "k" -> "<obfuscated>";
                case "l" -> "<bold>";
                case "m" -> "<strikethrough>";
                case "n" -> "<underlined>";
                case "o" -> "<italic>";
                case "r" -> "<reset>";
                default -> "&" + code;
            };
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(result);
        
        return result.toString();
    }

    /**
     * Заменяет смайлики в сообщении
     */
    public static String replaceEmojis(String message, Player player) {
        LoChat plugin = LoChat.getInstance();
        if (plugin != null && plugin.getEmojiManager() != null) {
            return plugin.getEmojiManager().process(message, player);
        }
        return message;
    }

    /**
     * Заменяет смайлики без проверки прав
     */
    public static String replaceEmojis(String message) {
        LoChat plugin = LoChat.getInstance();
        if (plugin != null && plugin.getEmojiManager() != null) {
            return plugin.getEmojiManager().process(message);
        }
        return message;
    }

    /**
     * @deprecated Use replaceEmojis(String) instead
     */
    @Deprecated
    public static String replaceEmojis(String message, Map<String, String> emojis) {
        return replaceEmojis(message);
    }

    /**
     * Полная обработка сообщения игрока
     * Цвета + смайлики + цвет чата
     */
    public static String processPlayerMessage(String message, Player player, boolean hasColorPermission) {
        String result = message;
        
        // Заменяем смайлики
        result = replaceEmojis(result, player);
        
        // Если есть право на цвета - конвертируем
        if (hasColorPermission) {
            result = convertAllColors(result);
        } else {
            // Убираем цветовые коды
            result = stripLegacyColors(result);
            
            // Применяем цвет чата если установлен
            LoChat plugin = LoChat.getInstance();
            if (plugin != null && plugin.getChatColorManager() != null) {
                result = plugin.getChatColorManager().applyChatColor(player.getUniqueId(), result);
            }
        }
        
        return result;
    }

    /**
     * Убирает &коды из сообщения
     */
    public static String stripLegacyColors(String message) {
        return LEGACY_COLOR_PATTERN.matcher(message).replaceAll("");
    }

    public static Component parse(String message) {
        return MINI_MESSAGE.deserialize(convertAllColors(message));
    }

    public static Component parse(String message, String... replacements) {
        String result = message;
        for (int i = 0; i < replacements.length - 1; i += 2) {
            result = result.replace(replacements[i], replacements[i + 1]);
        }
        return MINI_MESSAGE.deserialize(convertAllColors(result));
    }

    public static String toPlain(Component component) {
        return PlainTextComponentSerializer.plainText().serialize(component);
    }

    public static String stripTags(String message) {
        // Убираем MiniMessage теги и legacy коды
        String result = MINI_MESSAGE.stripTags(message);
        result = stripLegacyColors(result);
        result = HEX_PATTERN.matcher(result).replaceAll("");
        return result;
    }

    /**
     * Форматирует глобальное сообщение с поддержкой LoPreff
     */
    public static Component formatGlobalMessage(String format, String prefix, Player player, String message, boolean hasColorPermission) {
        String playerDisplay = getPlayerDisplay(player);
        String finalMessage = processPlayerMessage(message, player, hasColorPermission);
        
        String formatted = format
                .replace("<prefix>", prefix)
                .replace("<player>", playerDisplay)
                .replace("<message>", finalMessage);
        
        return parse(formatted);
    }

    /**
     * Форматирует локальное сообщение с поддержкой LoPreff
     */
    public static Component formatLocalMessage(String format, String prefix, Player player, String message, boolean hasColorPermission) {
        String playerDisplay = getPlayerDisplay(player);
        String finalMessage = processPlayerMessage(message, player, hasColorPermission);
        
        String formatted = prefix + " " + format
                .replace("<player>", playerDisplay)
                .replace("<message>", finalMessage);
        
        return parse(formatted);
    }

    private static String getPlayerDisplay(Player player) {
        LoChat plugin = LoChat.getInstance();
        if (plugin != null && plugin.getGradientModule() != null && plugin.getGradientModule().isEnabled()) {
            return plugin.getGradientModule().getFormattedName(player);
        }
        return player.getName();
    }

    public static Component formatPmSent(String format, String targetName, String message) {
        return parse(format
                .replace("{player}", targetName)
                .replace("{message}", message));
    }

    public static Component formatPmReceived(String format, String senderName, String message) {
        return parse(format
                .replace("{player}", senderName)
                .replace("{message}", message));
    }

    /**
     * Форматирует исходящее PM с поддержкой градиентных имён
     */
    public static Component formatPmSentNew(String format, Player sender, Player receiver, String message) {
        LoChat plugin = LoChat.getInstance();
        String senderDisplay = sender.getName();
        String receiverDisplay = receiver.getName();
        
        // Используем градиентные имена если включено
        if (plugin != null && plugin.getMessageConfig().isPmUseGradientNames() 
                && plugin.getGradientModule() != null && plugin.getGradientModule().isEnabled()) {
            senderDisplay = plugin.getGradientModule().getFormattedName(sender);
            receiverDisplay = plugin.getGradientModule().getFormattedName(receiver);
            
            // Конвертируем legacy коды в MiniMessage формат
            senderDisplay = convertAllColors(senderDisplay);
            receiverDisplay = convertAllColors(receiverDisplay);
        }
        
        return parse(format
                .replace("{sender}", sender.getName())
                .replace("{receiver}", receiver.getName())
                .replace("{sender_display}", senderDisplay)
                .replace("{receiver_display}", receiverDisplay)
                .replace("{message}", message));
    }

    /**
     * Форматирует входящее PM с поддержкой градиентных имён
     */
    public static Component formatPmReceivedNew(String format, Player sender, Player receiver, String message) {
        LoChat plugin = LoChat.getInstance();
        String senderDisplay = sender.getName();
        String receiverDisplay = receiver.getName();
        
        // Используем градиентные имена если включено
        if (plugin != null && plugin.getMessageConfig().isPmUseGradientNames() 
                && plugin.getGradientModule() != null && plugin.getGradientModule().isEnabled()) {
            senderDisplay = plugin.getGradientModule().getFormattedName(sender);
            receiverDisplay = plugin.getGradientModule().getFormattedName(receiver);
            
            // Конвертируем legacy коды в MiniMessage формат
            senderDisplay = convertAllColors(senderDisplay);
            receiverDisplay = convertAllColors(receiverDisplay);
        }
        
        return parse(format
                .replace("{sender}", sender.getName())
                .replace("{receiver}", receiver.getName())
                .replace("{sender_display}", senderDisplay)
                .replace("{receiver_display}", receiverDisplay)
                .replace("{message}", message));
    }

    public static Component formatAnnouncement(String format, String message) {
        return parse(format.replace("<message>", message));
    }
}
