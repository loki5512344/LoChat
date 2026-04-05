package com.loki.lochat.core.filter.filters;

import com.loki.lochat.core.filter.FilterResult;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public class SwearFilter {
    private final boolean enabled;
    private final boolean replaceMode;
    private final String replacement;
    private final Set<Pattern> swearPatterns = new HashSet<>();

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
                .forEach(word -> swearPatterns.add(
                    Pattern.compile("\\b" + Pattern.quote(word) + "\\b", Pattern.CASE_INSENSITIVE)
                ));
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to load badwords.txt: " + e.getMessage());
        }
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
