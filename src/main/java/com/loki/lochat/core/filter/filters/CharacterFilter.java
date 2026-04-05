package com.loki.lochat.core.filter.filters;

import org.bukkit.configuration.file.FileConfiguration;

import java.util.regex.Pattern;

public class CharacterFilter {
    private final boolean enabled;
    private final int maxRepeating;

    public CharacterFilter(FileConfiguration config) {
        this.enabled = config.getBoolean("filters.repeating-chars.enabled", true);
        this.maxRepeating = config.getInt("filters.repeating-chars.max", 3);
    }

    public String filter(String message) {
        if (!enabled || maxRepeating <= 0) {
            return message;
        }

        Pattern pattern = Pattern.compile("(.)\\1{" + maxRepeating + ",}");
        return pattern.matcher(message).replaceAll(m -> {
            String repeated = m.group(1);
            return repeated.repeat(maxRepeating);
        });
    }
}
