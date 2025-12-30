package com.loki.lochat.managers;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.loki.lochat.LoChat;

import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class FilterManager {

    private final LoChat plugin;
    private List<String> badWords = new ArrayList<>();
    private List<String> fragments = new ArrayList<>();

    public FilterManager(LoChat plugin) {
        this.plugin = plugin;
        loadFilterWords();
    }

    /**
     * Загружает плохие слова из JSON файла
     */
    private void loadFilterWords() {
        badWords.clear();
        fragments.clear();
        
        try {
            // Сначала пытаемся загрузить из data folder (если есть кастомный)
            File customFile = new File(plugin.getDataFolder(), "filter-words.json");
            if (customFile.exists()) {
                try (FileReader reader = new FileReader(customFile, StandardCharsets.UTF_8)) {
                    JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
                    loadFromJsonObject(json);
                    plugin.getLogger().info("Загружены плохие слова из filter-words.json (data folder): " + badWords.size() + " слов, " + fragments.size() + " фрагментов");
                    return;
                }
            }
            
            // Если кастомного нет, копируем из ресурсов
            plugin.saveResource("filter-words.json", false);
            
            // Теперь загружаем из data folder
            if (customFile.exists()) {
                try (FileReader reader = new FileReader(customFile, StandardCharsets.UTF_8)) {
                    JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
                    loadFromJsonObject(json);
                    plugin.getLogger().info("Загружено " + badWords.size() + " плохих слов и " + fragments.size() + " фрагментов из filter-words.json");
                    return;
                }
            }
            
            // Fallback на старый способ из config.yml
            List<String> configWords = plugin.getConfigManager().getFilterWords();
            if (configWords != null && !configWords.isEmpty()) {
                badWords.addAll(configWords);
                plugin.getLogger().warning("Используются слова из config.yml (filter-words.json не найден)");
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Ошибка загрузки filter-words.json: " + e.getMessage());
            e.printStackTrace();
            // Fallback на старый способ
            List<String> configWords = plugin.getConfigManager().getFilterWords();
            if (configWords != null && !configWords.isEmpty()) {
                badWords.addAll(configWords);
                plugin.getLogger().info("Используются слова из config.yml (fallback)");
            }
        }
    }

    /**
     * Загружает данные из JSON объекта
     */
    private void loadFromJsonObject(JsonObject json) {
        if (json.has("words")) {
            JsonArray wordsArray = json.getAsJsonArray("words");
            for (JsonElement element : wordsArray) {
                String word = element.getAsString();
                if (word != null && !word.trim().isEmpty()) {
                    badWords.add(word.trim().toLowerCase());
                }
            }
        }
        
        if (json.has("fragments")) {
            JsonArray fragmentsArray = json.getAsJsonArray("fragments");
            for (JsonElement element : fragmentsArray) {
                String fragment = element.getAsString();
                if (fragment != null && !fragment.trim().isEmpty()) {
                    fragments.add(fragment.trim().toLowerCase());
                }
            }
        }
    }

    /**
     * Перезагружает список плохих слов
     */
    public void reload() {
        loadFilterWords();
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

        if (badWords.isEmpty() && fragments.isEmpty()) {
            return FilterResult.ALLOWED;
        }

        // Нормализуем сообщение (убираем обход фильтра)
        String normalizedMessage = normalizeMessage(message);
        String lowerMessage = normalizedMessage.toLowerCase();
        String originalLower = message.toLowerCase();

        // Проверяем полные слова
        for (String word : badWords) {
            if (word == null || word.trim().isEmpty()) {
                continue;
            }
            
            String lowerWord = word.toLowerCase().trim();
            
            // Проверяем точное совпадение слова (с границами слов)
            Pattern wordPattern = Pattern.compile("\\b" + Pattern.quote(lowerWord) + "\\b", Pattern.CASE_INSENSITIVE);
            if (wordPattern.matcher(normalizedMessage).find() || wordPattern.matcher(originalLower).find()) {
                return getFilterResult();
            }
            
            // Также проверяем как подстроку
            if (lowerMessage.contains(lowerWord) || originalLower.contains(lowerWord)) {
                return getFilterResult();
            }
        }

        // Проверяем фрагменты (поиск по фрагментам типа "пизд" найдет "пизда", "пиздец" и т.д.)
        for (String fragment : fragments) {
            if (fragment == null || fragment.trim().isEmpty()) {
                continue;
            }
            
            String lowerFragment = fragment.toLowerCase().trim();
            
            // Ищем фрагмент в нормализованном сообщении
            if (lowerMessage.contains(lowerFragment) || originalLower.contains(lowerFragment)) {
                return getFilterResult();
            }
        }

        return FilterResult.ALLOWED;
    }

    /**
     * Возвращает результат фильтрации на основе настроек
     */
    private FilterResult getFilterResult() {
        String action = plugin.getConfigManager().getFilterAction();
        return switch (action.toLowerCase()) {
            case "block" -> FilterResult.BLOCKED;
            case "warn" -> FilterResult.WARNED;
            default -> FilterResult.CENSORED;
        };
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

        if (badWords.isEmpty() && fragments.isEmpty()) {
            return message;
        }

        String replacement = plugin.getConfigManager().getFilterReplacement();
        if (replacement == null) {
            replacement = "***";
        }

        String result = message;
        String normalizedMessage = normalizeMessage(message);

        // Заменяем полные слова
        for (String word : badWords) {
            if (word == null || word.trim().isEmpty()) {
                continue;
            }
            
            // Заменяем точные совпадения слов
            Pattern wordPattern = Pattern.compile("\\b" + Pattern.quote(word) + "\\b", Pattern.CASE_INSENSITIVE);
            result = wordPattern.matcher(result).replaceAll(replacement);
            
            // Также заменяем как подстроку
            Pattern substringPattern = Pattern.compile("(?i)" + Pattern.quote(word));
            result = substringPattern.matcher(result).replaceAll(replacement);
        }

        // Заменяем фрагменты (находим слова содержащие фрагмент и заменяем их)
        for (String fragment : fragments) {
            if (fragment == null || fragment.trim().isEmpty()) {
                continue;
            }
            
            // Ищем слова содержащие фрагмент и заменяем весь фрагмент
            Pattern fragmentPattern = Pattern.compile("(?i)" + Pattern.quote(fragment));
            result = fragmentPattern.matcher(result).replaceAll(replacement);
            
            // Также заменяем слова начинающиеся с фрагмента
            Pattern wordStartPattern = Pattern.compile("\\b" + Pattern.quote(fragment) + "\\w*", Pattern.CASE_INSENSITIVE);
            result = wordStartPattern.matcher(result).replaceAll(replacement);
        }

        return result;
    }

    /**
     * Нормализует сообщение, убирая попытки обхода фильтра
     * Заменяет специальные символы на обычные буквы
     * Например: "пизд@" -> "пизда", "х*й" -> "хай" (но "хуй" найдет)
     */
    private String normalizeMessage(String message) {
        if (message == null) {
            return "";
        }
        
        // Заменяем символы обхода на буквы (для обнаружения х*й, х_уй, пизд@ и т.д.)
        String normalized = message;
        
        // Заменяем символы обхода на похожие буквы
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
        normalized = normalized.replace('@', 'а');  // пизд@ -> пизда
        normalized = normalized.replace('#', 'х');
        normalized = normalized.replace('$', 'с');
        normalized = normalized.replace('%', 'о');
        normalized = normalized.replace('^', 'х');
        normalized = normalized.replace('&', 'а');
        normalized = normalized.replace('~', 'а');
        normalized = normalized.replace('`', 'а');
        normalized = normalized.replace('=', 'а');
        normalized = normalized.replace('+', 'а');
        normalized = normalized.replace('[', 'а');
        normalized = normalized.replace(']', 'а');
        normalized = normalized.replace('{', 'а');
        normalized = normalized.replace('}', 'а');
        normalized = normalized.replace('(', 'а');
        normalized = normalized.replace(')', 'а');
        normalized = normalized.replace('/', 'а');
        normalized = normalized.replace('\\', 'а');
        normalized = normalized.replace('?', 'а');
        normalized = normalized.replace('<', 'а');
        normalized = normalized.replace('>', 'а');
        
        // Убираем множественные пробелы
        normalized = normalized.replaceAll("\\s+", " ");
        
        return normalized;
    }
}
