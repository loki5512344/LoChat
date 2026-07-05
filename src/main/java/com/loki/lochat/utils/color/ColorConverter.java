package com.loki.lochat.utils.color;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Конвертер различных форматов цветов в MiniMessage
 */
public class ColorConverter {
    
    private ColorConverter() {
    }

    private static final Pattern HEX_PATTERN_1 = Pattern.compile("&#([0-9a-fA-F]{6})");
    private static final Pattern HEX_PATTERN_2 = Pattern.compile("(?<!<)#([0-9a-fA-F]{6})(?![^<]*>)");
    
    // Мапа для быстрой конвертации цветов (вместо 32 вызовов replaceAll)
    private static final Map<String, String> COLOR_MAP = Map.ofEntries(
        // § коды
        Map.entry("§0", "<black>"),
        Map.entry("§1", "<dark_blue>"),
        Map.entry("§2", "<dark_green>"),
        Map.entry("§3", "<dark_aqua>"),
        Map.entry("§4", "<dark_red>"),
        Map.entry("§5", "<dark_purple>"),
        Map.entry("§6", "<gold>"),
        Map.entry("§7", "<gray>"),
        Map.entry("§8", "<dark_gray>"),
        Map.entry("§9", "<blue>"),
        Map.entry("§a", "<green>"),
        Map.entry("§b", "<aqua>"),
        Map.entry("§c", "<red>"),
        Map.entry("§d", "<light_purple>"),
        Map.entry("§e", "<yellow>"),
        Map.entry("§f", "<white>"),
        Map.entry("§k", "<obfuscated>"),
        Map.entry("§l", "<bold>"),
        Map.entry("§m", "<strikethrough>"),
        Map.entry("§n", "<underlined>"),
        Map.entry("§o", "<italic>"),
        Map.entry("§r", "<reset>"),
        // & коды
        Map.entry("&0", "<black>"),
        Map.entry("&1", "<dark_blue>"),
        Map.entry("&2", "<dark_green>"),
        Map.entry("&3", "<dark_aqua>"),
        Map.entry("&4", "<dark_red>"),
        Map.entry("&5", "<dark_purple>"),
        Map.entry("&6", "<gold>"),
        Map.entry("&7", "<gray>"),
        Map.entry("&8", "<dark_gray>"),
        Map.entry("&9", "<blue>"),
        Map.entry("&a", "<green>"),
        Map.entry("&b", "<aqua>"),
        Map.entry("&c", "<red>"),
        Map.entry("&d", "<light_purple>"),
        Map.entry("&e", "<yellow>"),
        Map.entry("&f", "<white>"),
        Map.entry("&k", "<obfuscated>"),
        Map.entry("&l", "<bold>"),
        Map.entry("&m", "<strikethrough>"),
        Map.entry("&n", "<underlined>"),
        Map.entry("&o", "<italic>"),
        Map.entry("&r", "<reset>")
    );
    
    /**
     * Конвертирует legacy форматы цветов в MiniMessage формат
     */
    public static String convertLegacyFormats(String message) {
        if (message == null) {
            return "";
        }

        // Конвертируем &#RRGGBB в <color:#RRGGBB>
        message = convertHexFormat1(message);
        
        // Конвертируем #RRGGBB в <color:#RRGGBB>
        message = convertHexFormat2(message);
        
        // Конвертируем § и & коды через Map (быстрее чем 32 replaceAll)
        message = convertLegacyCodes(message);

        return message;
    }
    
    private static String convertHexFormat1(String message) {
        Matcher matcher = HEX_PATTERN_1.matcher(message);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String hex = matcher.group(1).toLowerCase();
            matcher.appendReplacement(sb, "<color:#" + hex + ">");
        }
        matcher.appendTail(sb);
        return sb.toString();
    }
    
    private static String convertHexFormat2(String message) {
        Matcher matcher = HEX_PATTERN_2.matcher(message);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String hex = matcher.group(1).toLowerCase();
            matcher.appendReplacement(sb, "<color:#" + hex + ">");
        }
        matcher.appendTail(sb);
        return sb.toString();
    }
    
    /**
     * Конвертирует legacy коды (§ и &) через Map - быстрее чем 32 replaceAll
     */
    private static String convertLegacyCodes(String message) {
        StringBuilder result = new StringBuilder(message.length());
        int i = 0;
        
        while (i < message.length()) {
            if (i + 1 < message.length()) {
                char current = message.charAt(i);
                char next = message.charAt(i + 1);
                
                // Проверяем § или &
                if (current == '§' || current == '&') {
                    String code = String.valueOf(current) + next;
                    String replacement = COLOR_MAP.get(code);
                    
                    if (replacement != null) {
                        result.append(replacement);
                        i += 2;
                        continue;
                    }
                }
            }
            
            result.append(message.charAt(i));
            i++;
        }
        
        return result.toString();
    }
    
    /**
     * Экранирует текст для безопасного использования внутри MiniMessage тегов
     */
    public static String escapeForMiniMessage(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("\\", "\\\\").replace("<", "\\<");
    }
}
