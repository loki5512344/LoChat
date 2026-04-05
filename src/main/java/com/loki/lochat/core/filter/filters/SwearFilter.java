package com.loki.lochat.core.filter.filters;

import com.loki.lochat.core.filter.FilterResult;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Pattern;

public class SwearFilter {
    private final boolean enabled;
    private final boolean replaceMode;
    private final String replacement;
    private final Set<Pattern> swearPatterns = new HashSet<>();
    
    // Карта замены латиницы на кириллицу для обхода фильтра
    private static final Map<Character, Character> LATIN_TO_CYRILLIC = Map.ofEntries(
        Map.entry('a', 'а'), Map.entry('A', 'А'),
        Map.entry('e', 'е'), Map.entry('E', 'Е'),
        Map.entry('o', 'о'), Map.entry('O', 'О'),
        Map.entry('p', 'р'), Map.entry('P', 'Р'),
        Map.entry('c', 'с'), Map.entry('C', 'С'),
        Map.entry('y', 'у'), Map.entry('Y', 'У'),
        Map.entry('x', 'х'), Map.entry('X', 'Х'),
        Map.entry('k', 'к'), Map.entry('K', 'К'),
        Map.entry('m', 'м'), Map.entry('M', 'М'),
        Map.entry('h', 'н'), Map.entry('H', 'Н'),
        Map.entry('t', 'т'), Map.entry('T', 'Т'),
        Map.entry('b', 'в'), Map.entry('B', 'В')
    );

    public SwearFilter(FileConfiguration config, JavaPlugin plugin) {
        this.enabled = config.getBoolean("filters.swear.enabled", true);
        this.replaceMode = config.getBoolean("filters.swear.replace", false);
        this.replacement = config.getString("filters.swear.replacement", "***");
        
        if (enabled) {
            loadWordList(plugin);
        }
    }

    private void loadWordList(JavaPlugin plugin) {
        try (InputStream is = plugin.getResource("badwords.txt");
             BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            
            reader.lines()
                .map(String::trim)
                .filter(line -> !line.isEmpty() && !line.startsWith("#"))
                .forEach(word -> {
                    // Создаем паттерн с учетом возможной замены букв
                    String regex = buildFlexiblePattern(word);
                    swearPatterns.add(Pattern.compile(regex, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE));
                });
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to load badwords.txt: " + e.getMessage());
        }
    }
    
    /**
     * Создает гибкий паттерн, который ловит замены букв (х->x, о->o и т.д.)
     */
    private String buildFlexiblePattern(String word) {
        StringBuilder pattern = new StringBuilder();
        
        for (char c : word.toLowerCase().toCharArray()) {
            // Для каждой буквы создаем группу с возможными заменами
            switch (c) {
                case 'а' -> pattern.append("[аaА@]");
                case 'е' -> pattern.append("[еeЕ3]");
                case 'о' -> pattern.append("[оoО0]");
                case 'р' -> pattern.append("[рpР]");
                case 'с' -> pattern.append("[сcС]");
                case 'у' -> pattern.append("[уyУ]");
                case 'х' -> pattern.append("[хxХ]");
                case 'к' -> pattern.append("[кkК]");
                case 'м' -> pattern.append("[мmМ]");
                case 'н' -> pattern.append("[нhНN]");
                case 'т' -> pattern.append("[тtТ]");
                case 'в' -> pattern.append("[вbВ]");
                case 'и' -> pattern.append("[иiИ1]");
                case 'з' -> pattern.append("[з3З]");
                case 'б' -> pattern.append("[б6Б]");
                default -> pattern.append(Pattern.quote(String.valueOf(c)));
            }
        }
        
        return pattern.toString();
    }

    public FilterResult filter(Player player, String message) {
        if (!enabled || player.hasPermission("lochat.bypass.swear")) {
            return FilterResult.ok(message);
        }

        for (Pattern pattern : swearPatterns) {
            if (pattern.matcher(message).find()) {
                if (replaceMode) {
                    return FilterResult.ok(replaceSwear(message));
                }
                return FilterResult.blocked("Сообщение содержит запрещенные слова");
            }
        }

        return FilterResult.ok(message);
    }

    private String replaceSwear(String message) {
        String result = message;
        for (Pattern pattern : swearPatterns) {
            result = pattern.matcher(result).replaceAll(replacement);
        }
        return result;
    }
}
