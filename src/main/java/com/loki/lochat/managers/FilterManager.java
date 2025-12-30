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

    /**
     * Проверяет сообщение на наличие запрещенных слов
     */
    public FilterResult checkMessage(String message) {
        if (!plugin.getConfigManager().isFilterEnabled()) {
            return FilterResult.ALLOWED;
        }

        if (message == null || message.trim().isEmpty()) {
            return FilterResult.ALLOWED;
        }

        List<String> badWords = plugin.getConfigManager().getFilterWords();
        if (badWords == null || badWords.isEmpty()) {
            return FilterResult.ALLOWED;
        }

        // Нормализуем сообщение (убираем обход фильтра)
        String normalizedMessage = normalizeMessage(message);
        String lowerMessage = normalizedMessage.toLowerCase();

        for (String word : badWords) {
            if (word == null || word.trim().isEmpty()) {
                continue;
            }
            
            String lowerWord = word.toLowerCase().trim();
            
            // Проверяем точное совпадение слова (с границами слов)
            Pattern wordPattern = Pattern.compile("\\b" + Pattern.quote(lowerWord) + "\\b", Pattern.CASE_INSENSITIVE);
            if (wordPattern.matcher(normalizedMessage).find()) {
                String action = plugin.getConfigManager().getFilterAction();
                return switch (action.toLowerCase()) {
                    case "block" -> FilterResult.BLOCKED;
                    case "warn" -> FilterResult.WARNED;
                    default -> FilterResult.CENSORED;
                };
            }
            
            // Также проверяем как подстроку (для частичных совпадений типа "пизд")
            if (lowerMessage.contains(lowerWord)) {
                String action = plugin.getConfigManager().getFilterAction();
                return switch (action.toLowerCase()) {
                    case "block" -> FilterResult.BLOCKED;
                    case "warn" -> FilterResult.WARNED;
                    default -> FilterResult.CENSORED;
                };
            }
        }

        return FilterResult.ALLOWED;
    }

    /**
     * Цензурирует сообщение, заменяя запрещенные слова
     */
    public String censorMessage(String message) {
        if (!plugin.getConfigManager().isFilterEnabled()) {
            return message;
        }

        if (message == null || message.trim().isEmpty()) {
            return message;
        }

        List<String> badWords = plugin.getConfigManager().getFilterWords();
        if (badWords == null || badWords.isEmpty()) {
            return message;
        }

        String replacement = plugin.getConfigManager().getFilterReplacement();
        if (replacement == null) {
            replacement = "***";
        }

        String result = message;
        String normalizedMessage = normalizeMessage(message);

        for (String word : badWords) {
            if (word == null || word.trim().isEmpty()) {
                continue;
            }
            
            String lowerWord = word.toLowerCase().trim();
            
            // Заменяем точные совпадения слов
            Pattern wordPattern = Pattern.compile("\\b" + Pattern.quote(word) + "\\b", Pattern.CASE_INSENSITIVE);
            result = wordPattern.matcher(result).replaceAll(replacement);
            
            // Также заменяем как подстроку (для частичных совпадений)
            Pattern substringPattern = Pattern.compile("(?i)" + Pattern.quote(word));
            result = substringPattern.matcher(result).replaceAll(replacement);
        }

        return result;
    }

    /**
     * Нормализует сообщение, убирая попытки обхода фильтра
     * Заменяет специальные символы на обычные буквы
     */
    private String normalizeMessage(String message) {
        if (message == null) {
            return "";
        }
        
        // Заменяем символы обхода на буквы (для обнаружения х*й, х_уй и т.д.)
        String normalized = message;
        
        // Заменяем символы обхода на буквы
        normalized = normalized.replace('*', 'а');
        normalized = normalized.replace('_', 'а');
        normalized = normalized.replace('-', 'а');
        normalized = normalized.replace('.', 'а');
        normalized = normalized.replace(',', 'а');
        normalized = normalized.replace('|', 'и');
        normalized = normalized.replace('1', 'и');
        normalized = normalized.replace('!', 'и');
        normalized = normalized.replace('0', 'о');
        normalized = normalized.replace('3', 'з');
        normalized = normalized.replace('4', 'ч');
        normalized = normalized.replace('5', 'с');
        normalized = normalized.replace('6', 'б');
        normalized = normalized.replace('7', 'т');
        normalized = normalized.replace('8', 'в');
        normalized = normalized.replace('9', 'д');
        normalized = normalized.replace('@', 'а');
        normalized = normalized.replace('#', 'х');
        normalized = normalized.replace('$', 'с');
        normalized = normalized.replace('%', 'о');
        normalized = normalized.replace('^', 'х');
        normalized = normalized.replace('&', 'а');
        
        // Убираем множественные пробелы
        normalized = normalized.replaceAll("\\s+", " ");
        
        return normalized;
    }
}
