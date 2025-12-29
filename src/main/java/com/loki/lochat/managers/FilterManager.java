package com.loki.lochat.managers;

import com.loki.lochat.LoChat;

import java.util.List;
import java.util.regex.Pattern;

public class FilterManager {

    private final LoChat plugin;

    public FilterManager(LoChat plugin) {
        this.plugin = plugin;
    }

    public enum FilterResult {
        ALLOWED,
        CENSORED,
        BLOCKED,
        WARNED
    }

    public FilterResult checkMessage(String message) {
        if (!plugin.getConfigManager().isFilterEnabled()) {
            return FilterResult.ALLOWED;
        }

        List<String> badWords = plugin.getConfigManager().getFilterWords();
        String lowerMessage = message.toLowerCase();

        for (String word : badWords) {
            if (lowerMessage.contains(word.toLowerCase())) {
                String action = plugin.getConfigManager().getFilterAction();
                return switch (action) {
                    case "block" -> FilterResult.BLOCKED;
                    case "warn" -> FilterResult.WARNED;
                    default -> FilterResult.CENSORED;
                };
            }
        }

        return FilterResult.ALLOWED;
    }

    public String censorMessage(String message) {
        if (!plugin.getConfigManager().isFilterEnabled()) {
            return message;
        }

        List<String> badWords = plugin.getConfigManager().getFilterWords();
        String replacement = plugin.getConfigManager().getFilterReplacement();
        String result = message;

        for (String word : badWords) {
            Pattern pattern = Pattern.compile("(?i)" + Pattern.quote(word));
            result = pattern.matcher(result).replaceAll(replacement);
        }

        return result;
    }
}
