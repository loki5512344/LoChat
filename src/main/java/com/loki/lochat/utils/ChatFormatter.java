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
    private static final Pattern HEX_PATTERN_NO_AMPERSAND = Pattern.compile("(?<!&)#([A-Fa-f0-9]{6})");
    private static final Pattern LEGACY_COLOR_PATTERN = Pattern.compile("&([0-9a-fk-orA-FK-OR])");
    private static final Pattern SECTION_COLOR_PATTERN = Pattern.compile("§([0-9a-fk-orA-FK-OR])");
    private static final Pattern SECTION_HEX_PATTERN = Pattern.compile("§x(§[0-9A-Fa-f]){6}");
    private static final Pattern MINIMESSAGE_HEX_PATTERN = Pattern.compile("<#([A-Fa-f0-9]{6})>");

    private ChatFormatter() {}

    /**
     * Конвертирует все форматы цветов в MiniMessage
     * Поддерживает: &коды, §коды, &#HEX, §x§...HEX
     * Идемпотентный - не конвертирует уже конвертированные теги
     */
    public static String convertAllColors(String message) {
        if (message == null) return "";
        
        // Если уже содержит MiniMessage теги <color:...> - не конвертируем повторно
        if (message.contains("<color:#")) {
            return message;
        }
        
        String result = message;
        
        // 1. Конвертируем §x§R§R§G§G§B§B -> <color:#rrggbb>
        result = convertSectionHexColors(result);
        
        // 2. Конвертируем §коды -> &коды (для унификации)
        result = convertSectionToAmpersand(result);
        
        // 3. Нормализуем существующие MiniMessage HEX теги <#RRGGBB> -> <color:#rrggbb>
        result = normalizeMiniMessageHex(result);
        
        // 4. Конвертируем &#RRGGBB -> <color:#rrggbb>
        result = convertHexColors(result);
        
        // 5. Конвертируем &коды -> MiniMessage теги
        result = convertLegacyColors(result);
        
        return result;
    }
    
    /**
     * Нормализует существующие MiniMessage HEX теги в формат <color:#rrggbb>
     */
    public static String normalizeMiniMessageHex(String message) {
        if (message == null) return "";
        Matcher matcher = MINIMESSAGE_HEX_PATTERN.matcher(message);
        StringBuilder result = new StringBuilder();
        while (matcher.find()) {
            matcher.appendReplacement(result, "<color:#" + matcher.group(1).toLowerCase() + ">");
        }
        matcher.appendTail(result);
        return result.toString();
    }
    
    /**
     * Конвертирует §x§R§R§G§G§B§B в <color:#RRGGBB>
     */
    public static String convertSectionHexColors(String message) {
        if (message == null) return "";
        Matcher matcher = SECTION_HEX_PATTERN.matcher(message);
        StringBuilder result = new StringBuilder();
        while (matcher.find()) {
            String match = matcher.group();
            // §x§R§R§G§G§B§B -> RRGGBB
            String hex = match.replace("§x", "").replace("§", "").toLowerCase();
            matcher.appendReplacement(result, "<color:#" + hex + ">");
        }
        matcher.appendTail(result);
        return result.toString();
    }
    
    /**
     * Конвертирует §коды в &коды
     */
    public static String convertSectionToAmpersand(String message) {
        if (message == null) return "";
        return SECTION_COLOR_PATTERN.matcher(message).replaceAll("&$1");
    }

    /**
     * Конвертирует &#RRGGBB и #RRGGBB в MiniMessage формат <color:#RRGGBB>
     */
    public static String convertHexColors(String message) {
        String result = message;
        
        // Конвертируем &#RRGGBB -> <color:#rrggbb>
        Matcher matcher = HEX_PATTERN.matcher(result);
        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            matcher.appendReplacement(sb, "<color:#" + matcher.group(1).toLowerCase() + ">");
        }
        matcher.appendTail(sb);
        result = sb.toString();
        
        // Конвертируем #RRGGBB (без &, но не если это часть &#RRGGBB или <color:#)
        matcher = HEX_PATTERN_NO_AMPERSAND.matcher(result);
        sb = new StringBuilder();
        while (matcher.find()) {
            matcher.appendReplacement(sb, "<color:#" + matcher.group(1).toLowerCase() + ">");
        }
        matcher.appendTail(sb);
        return sb.toString();
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
        
        // Проверяем есть ли цветовые коды в сообщении
        boolean hasColorCodes = LEGACY_COLOR_PATTERN.matcher(result).find() 
                || SECTION_COLOR_PATTERN.matcher(result).find()
                || HEX_PATTERN.matcher(result).find()
                || HEX_PATTERN_NO_AMPERSAND.matcher(result).find()
                || MINIMESSAGE_HEX_PATTERN.matcher(result).find();
        
        // Если есть право на цвета И есть цветовые коды - конвертируем
        if (hasColorPermission && hasColorCodes) {
            result = convertAllColors(result);
        } else {
            // Убираем цветовые коды если нет прав
            if (!hasColorPermission) {
                result = stripLegacyColors(result);
            }
            
            // Применяем цвет чата если установлен (когда нет своих цветов)
            LoChat plugin = LoChat.getInstance();
            if (plugin != null && plugin.getChatColorManager() != null) {
                result = plugin.getChatColorManager().applyChatColor(player.getUniqueId(), result);
            }
        }
        
        return result;
    }

    /**
     * Убирает &коды и §коды из сообщения
     */
    public static String stripLegacyColors(String message) {
        String result = LEGACY_COLOR_PATTERN.matcher(message).replaceAll("");
        return SECTION_COLOR_PATTERN.matcher(result).replaceAll("");
    }

    public static Component parse(String message) {
        return MINI_MESSAGE.deserialize(message);
    }

    public static Component parseWithColors(String message) {
        return MINI_MESSAGE.deserialize(convertAllColors(message));
    }

    public static Component parse(String message, String... replacements) {
        String result = message;
        for (int i = 0; i < replacements.length - 1; i += 2) {
            result = result.replace(replacements[i], replacements[i + 1]);
        }
        return MINI_MESSAGE.deserialize(result);
    }

    public static Component parseWithColors(String message, String... replacements) {
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
        if (message == null) return "";
        // Убираем MiniMessage теги и legacy коды
        String result = MINI_MESSAGE.stripTags(message);
        result = stripLegacyColors(result);
        result = HEX_PATTERN.matcher(result).replaceAll("");
        result = MINIMESSAGE_HEX_PATTERN.matcher(result).replaceAll("");
        return result;
    }

    /**
     * Полностью очищает все цветовые коды из строки
     */
    public static String stripAllColors(String message) {
        if (message == null) return "";
        String result = message;
        
        // Убираем MiniMessage HEX теги <#RRGGBB>
        result = MINIMESSAGE_HEX_PATTERN.matcher(result).replaceAll("");
        
        // Убираем &#RRGGBB
        result = HEX_PATTERN.matcher(result).replaceAll("");
        
        // Убираем #RRGGBB (без &)
        result = HEX_PATTERN_NO_AMPERSAND.matcher(result).replaceAll("");
        
        // Убираем &коды
        result = stripLegacyColors(result);
        
        // Убираем MiniMessage теги
        result = MINI_MESSAGE.stripTags(result);
        
        return result;
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
