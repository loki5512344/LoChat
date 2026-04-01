package com.loki.lochat.core.filter;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileReader;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Главный фильтр сообщений.
 * Порядок: mute (в MessageService) → caps → swear → advertising → ip → repeat → flood → spam
 */
public class AdvancedMessageFilter {

    // ── URL / IP / повторы ─────────────────────────────────────────────────────
    private static final Pattern URL_PATTERN = Pattern.compile(
            "(https?://)?([\\w-]+\\.)+[\\w-]+(/[\\w-./?%&=]*)?", Pattern.CASE_INSENSITIVE);
    private static final Pattern IP_PATTERN = Pattern.compile(
            "\\b(?:\\d{1,3}\\.){3}\\d{1,3}\\b");
    private static final Pattern REPEAT_PATTERN = Pattern.compile("(.)\\1{3,}");
    private static final Pattern HIDDEN_URL_PATTERN = Pattern.compile(
            "(?i)(h\\s*t\\s*t\\s*p|w\\s*w\\s*w|d\\s*o\\s*t\\s*c\\s*o\\s*m)");
    private static final Pattern DOMAIN_PATTERN = Pattern.compile(
            "\\b([a-zA-Z0-9-]+\\.(com|net|org|ru|gg|me|io|xyz|tk|ml|ga|cf|gq))\\b",
            Pattern.CASE_INSENSITIVE);

    private final FileConfiguration config;
    private final List<String> whitelistedDomains;
    private final List<String> blacklistedDomains;
    private final CapsFilter capsFilter;

    // ── Word filter ────────────────────────────────────────────────────────────
    private final Set<String> badWords = new HashSet<>();
    private final Set<String> badFragments = new HashSet<>();
    private final char replacementChar;
    private final boolean checkFragments;
    private final boolean ignoreCase;

    // ── Anti-flood: сколько сообщений за период ────────────────────────────────
    // UUID → список timestamp'ов
    private final Map<UUID, Deque<Long>> floodTracker = new ConcurrentHashMap<>();

    // ── Anti-spam: последние N сообщений игрока ────────────────────────────────
    private final Map<UUID, Deque<String>> spamTracker = new ConcurrentHashMap<>();

    public AdvancedMessageFilter(FileConfiguration config) {
        this(config, null);
    }

    public AdvancedMessageFilter(FileConfiguration config, JavaPlugin plugin) {
        this.config = config;
        
        // ✅ Используем FiltersConfig если доступен
        com.loki.lochat.config.FiltersConfig filtersConfig = null;
        if (plugin instanceof com.loki.lochat.LoChat loChat) {
            filtersConfig = loChat.getConfigManager().getFiltersConfig();
        }
        
        // Читаем настройки из FiltersConfig или fallback на config.yml
        if (filtersConfig != null) {
            this.whitelistedDomains = filtersConfig.getWhitelistedDomains();
            this.blacklistedDomains = filtersConfig.getBlacklistedDomains();
            
            int maxCapsPercent = filtersConfig.getCapsMaxPercent();
            int minLength = filtersConfig.getCapsMinLength();
            boolean autoLower = filtersConfig.isCapsAutoLowercase();
            boolean blockCaps = filtersConfig.isCapsBlock();
            this.capsFilter = new CapsFilter(maxCapsPercent, minLength, autoLower, blockCaps);
            
            this.replacementChar = filtersConfig.getSwearReplacementChar().charAt(0);
            this.checkFragments = filtersConfig.isSwearCheckFragments();
            this.ignoreCase = filtersConfig.isSwearIgnoreCase();
        } else {
            // Fallback на config.yml
            this.whitelistedDomains = config.getStringList("filters.advertising.whitelist");
            this.blacklistedDomains = config.getStringList("filters.advertising.blacklist");

            int maxCapsPercent = config.getInt("filters.caps.max-percent", 70);
            int minLength = config.getInt("filters.caps.min-length", 5);
            boolean autoLower = config.getBoolean("filters.caps.auto-lowercase", true);
            boolean blockCaps = config.getBoolean("filters.caps.block", false);
            this.capsFilter = new CapsFilter(maxCapsPercent, minLength, autoLower, blockCaps);

            this.replacementChar = config.getString("filters.swear.replacement-char", "*").charAt(0);
            this.checkFragments = config.getBoolean("filters.swear.check-fragments", true);
            this.ignoreCase = config.getBoolean("filters.swear.ignore-case", true);
        }

        loadWordFilter(plugin);
    }

    // ──────────────────────────────────────────────────────────────────────────
    //  Загрузка словаря
    // ──────────────────────────────────────────────────────────────────────────

    private void loadWordFilter(JavaPlugin plugin) {
        if (!config.getBoolean("filters.swear.enabled", true)) return;

        // Встроенные слова из config
        List<String> builtIn = config.getStringList("filters.swear.words");
        for (String w : builtIn) {
            if (!w.isBlank() && !w.startsWith("плохое")) {
                badWords.add(ignoreCase ? w.toLowerCase() : w);
            }
        }

        // Внешний файл filter-words.json
        if (!config.getBoolean("filters.swear.use-external-file", true)) return;
        if (plugin == null) return;

        File jsonFile = new File(plugin.getDataFolder(), "filter-words.json");
        if (!jsonFile.exists()) {
            // Копируем из ресурсов
            plugin.saveResource("data/filter-words.json", false);
            jsonFile = new File(plugin.getDataFolder(), "data/filter-words.json");
        }
        if (!jsonFile.exists()) return;

        try (FileReader reader = new FileReader(jsonFile)) {
            JsonObject root = new Gson().fromJson(reader, JsonObject.class);

            JsonArray words = root.getAsJsonArray("words");
            if (words != null) {
                for (var el : words) {
                    String w = el.getAsString();
                    badWords.add(ignoreCase ? w.toLowerCase() : w);
                }
            }

            JsonArray fragments = root.getAsJsonArray("fragments");
            if (fragments != null && checkFragments) {
                for (var el : fragments) {
                    String f = el.getAsString();
                    badFragments.add(ignoreCase ? f.toLowerCase() : f);
                }
            }

            plugin.getLogger().info("[LoChat] Word filter loaded: "
                    + badWords.size() + " words, " + badFragments.size() + " fragments");
        } catch (Exception e) {
            if (plugin != null)
                plugin.getLogger().warning("[LoChat] Failed to load filter-words.json: " + e.getMessage());
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    //  Главный метод
    // ──────────────────────────────────────────────────────────────────────────

    public FilterResult filterMessage(Player player, String message) {

        // 1. Капс
        if (config.getBoolean("filters.caps.enabled", true)) {
            String filtered = capsFilter.filter(player, message);
            if (filtered == null) {
                return blocked("&#CF6679Не кричите в чате");
            }
            message = filtered;
        }

        // 2. Мат
        if (config.getBoolean("filters.swear.enabled", true)
                && !player.hasPermission("lochat.bypass.swear")
                && !player.hasPermission("lochat.bypass.filter")) {
            FilterResult swear = filterSwear(player, message);
            if (!swear.allowed()) return swear;
            message = swear.filteredMessage();
        }

        // 3. Реклама / URL
        if (config.getBoolean("filters.advertising.enabled", true)) {
            FilterResult r = filterUrls(player, message);
            if (!r.allowed()) return r;
            message = r.filteredMessage();

            r = filterHiddenUrls(player, message);
            if (!r.allowed()) return r;

            r = filterDomains(player, message);
            if (!r.allowed()) return r;
        }

        // 4. IP
        if (config.getBoolean("filters.ip.enabled", true)) {
            FilterResult r = filterIPs(player, message);
            if (!r.allowed()) return r;
            message = r.filteredMessage();
        }

        // 5. Повторяющиеся символы
        if (config.getBoolean("filters.repeat.enabled", true)) {
            message = filterRepeatingChars(message);
        }

        // 6. Anti-flood (количество сообщений в период)
        if (config.getBoolean("filters.flood.enabled", true)
                && !player.hasPermission("lochat.bypass.flood")
                && !player.hasPermission("lochat.bypass.filter")) {
            FilterResult r = filterFlood(player);
            if (!r.allowed()) return r;
        }

        // 7. Anti-spam (похожие сообщения)
        if (config.getBoolean("filters.spam.enabled", true)
                && !player.hasPermission("lochat.bypass.spam")
                && !player.hasPermission("lochat.bypass.filter")) {
            FilterResult r = filterSpam(player, message);
            if (!r.allowed()) return r;
        }

        return new FilterResult(true, message, null);
    }

    // ──────────────────────────────────────────────────────────────────────────
    //  Фильтр мата
    // ──────────────────────────────────────────────────────────────────────────

    private FilterResult filterSwear(Player player, String message) {
        String action = config.getString("filters.swear.action", "replace");
        String check  = ignoreCase ? message.toLowerCase() : message;

        // Проверка полных слов
        String[] tokens = check.split("\\s+");
        boolean hasBad = false;
        for (String token : tokens) {
            // убираем пунктуацию по краям
            String clean = token.replaceAll("^[^а-яёa-z]+|[^а-яёa-z]+$", "");
            if (badWords.contains(clean)) {
                hasBad = true;
                break;
            }
        }

        // Проверка фрагментов (подстрок)
        if (!hasBad && checkFragments) {
            for (String frag : badFragments) {
                if (check.contains(frag)) {
                    hasBad = true;
                    break;
                }
            }
        }

        if (!hasBad) return new FilterResult(true, message, null);

        return switch (action) {
            case "block" -> blocked(config.getString("filters.swear.block-message",
                    "&#CF6679Мат запрещён"));
            case "replace" -> {
                String replaced = replaceSwear(message);
                yield new FilterResult(true, replaced, null);
            }
            default -> new FilterResult(true, message, null);
        };
    }

    /**
     * Заменяет матные слова звёздочками, сохраняя длину слова.
     */
    private String replaceSwear(String message) {
        String[] words = message.split("(?<=\\s)|(?=\\s)"); // разбиваем с сохранением пробелов
        StringBuilder result = new StringBuilder();
        for (String word : words) {
            String check = ignoreCase ? word.toLowerCase() : word;
            String cleanCheck = check.replaceAll("^[^а-яёa-z]+|[^а-яёa-z]+$", "");

            boolean isBad = badWords.contains(cleanCheck);
            if (!isBad && checkFragments) {
                for (String frag : badFragments) {
                    if (check.contains(frag)) { isBad = true; break; }
                }
            }

            if (isBad) {
                result.append(String.valueOf(replacementChar).repeat(word.length()));
            } else {
                result.append(word);
            }
        }
        return result.toString();
    }

    // ──────────────────────────────────────────────────────────────────────────
    //  Anti-flood
    // ──────────────────────────────────────────────────────────────────────────

    private FilterResult filterFlood(Player player) {
        int maxMessages = config.getInt("filters.flood.max-messages", 5);
        int timePeriod  = config.getInt("filters.flood.time-period", 10); // секунды
        long now        = System.currentTimeMillis();
        long windowMs   = timePeriod * 1000L;

        Deque<Long> timestamps = floodTracker.computeIfAbsent(
                player.getUniqueId(), k -> new ArrayDeque<>());

        // Удаляем старые timestamp'ы за пределами окна
        while (!timestamps.isEmpty() && now - timestamps.peekFirst() > windowMs) {
            timestamps.pollFirst();
        }

        if (timestamps.size() >= maxMessages) {
            return blocked(config.getString("filters.flood.block-message",
                    "&#CF6679Не флудите!"));
        }

        timestamps.addLast(now);
        return new FilterResult(true, null, null);
    }

    // ──────────────────────────────────────────────────────────────────────────
    //  Anti-spam (похожие сообщения)
    // ──────────────────────────────────────────────────────────────────────────

    private FilterResult filterSpam(Player player, String message) {
        int maxSimilar = config.getInt("filters.spam.max-similar-messages", 3);
        int threshold  = config.getInt("filters.spam.similarity-threshold", 80);

        Deque<String> history = spamTracker.computeIfAbsent(
                player.getUniqueId(), k -> new ArrayDeque<>());

        // Считаем сколько прошлых сообщений похожи
        long similarCount = history.stream()
                .filter(prev -> similarity(prev, message) >= threshold)
                .count();

        if (similarCount >= maxSimilar) {
            return blocked(config.getString("filters.spam.block-message",
                    "&#CF6679Не отправляйте одинаковые сообщения"));
        }

        // Добавляем в историю (храним последние 10)
        history.addLast(message);
        if (history.size() > 10) history.pollFirst();

        return new FilterResult(true, null, null);
    }

    /**
     * Простая метрика схожести: процент общих символов (0-100).
     */
    private int similarity(String a, String b) {
        if (a.equals(b)) return 100;
        int maxLen = Math.max(a.length(), b.length());
        if (maxLen == 0) return 100;
        int dist = levenshtein(
                a.toLowerCase().substring(0, Math.min(a.length(), 50)),
                b.toLowerCase().substring(0, Math.min(b.length(), 50)));
        return 100 - (dist * 100 / maxLen);
    }

    private int levenshtein(String a, String b) {
        int[] dp = new int[b.length() + 1];
        for (int j = 0; j <= b.length(); j++) dp[j] = j;
        for (int i = 1; i <= a.length(); i++) {
            int prev = dp[0];
            dp[0] = i;
            for (int j = 1; j <= b.length(); j++) {
                int temp = dp[j];
                dp[j] = a.charAt(i - 1) == b.charAt(j - 1)
                        ? prev
                        : 1 + Math.min(prev, Math.min(dp[j], dp[j - 1]));
                prev = temp;
            }
        }
        return dp[b.length()];
    }

    // ──────────────────────────────────────────────────────────────────────────
    //  URL / IP / повторы — прежняя логика
    // ──────────────────────────────────────────────────────────────────────────

    private FilterResult filterUrls(Player player, String message) {
        if (player.hasPermission("lochat.bypass.urlfilter")) return ok(message);
        Matcher matcher = URL_PATTERN.matcher(message);
        while (matcher.find()) {
            String domain = extractDomain(matcher.group());
            if (!whitelistedDomains.isEmpty()) {
                boolean ok = whitelistedDomains.stream()
                        .anyMatch(d -> domain.toLowerCase().contains(d.toLowerCase()));
                if (!ok) return blocked(config.getString("filters.url.blocked-message",
                        "&#CF6679Ссылки запрещены!"));
            }
            if (!blacklistedDomains.isEmpty()) {
                boolean bad = blacklistedDomains.stream()
                        .anyMatch(d -> domain.toLowerCase().contains(d.toLowerCase()));
                if (bad) return blocked(config.getString("filters.url.blocked-message",
                        "&#CF6679Эта ссылка запрещена!"));
            }
        }
        return ok(message);
    }

    private FilterResult filterIPs(Player player, String message) {
        if (player.hasPermission("lochat.bypass.ipfilter")) return ok(message);
        Matcher matcher = IP_PATTERN.matcher(message);
        if (matcher.find()) {
            if (config.getBoolean("filters.ip.block", false))
                return blocked(config.getString("filters.ip.blocked-message",
                        "&#CF6679IP адреса запрещены!"));
            return ok(matcher.replaceAll(config.getString("filters.ip.replacement", "[IP скрыт]")));
        }
        return ok(message);
    }

    private FilterResult filterHiddenUrls(Player player, String message) {
        if (!config.getBoolean("filters.advertising.block-hidden-urls", true)) return ok(message);
        if (player.hasPermission("lochat.bypass.hiddenurls")) return ok(message);
        if (HIDDEN_URL_PATTERN.matcher(message).find())
            return blocked(config.getString("filters.advertising.blocked-message",
                    "&#CF6679Реклама запрещена!"));
        return ok(message);
    }

    private FilterResult filterDomains(Player player, String message) {
        if (!config.getBoolean("filters.advertising.block-domains", true)) return ok(message);
        if (player.hasPermission("lochat.bypass.domains")) return ok(message);
        Matcher matcher = DOMAIN_PATTERN.matcher(message);
        while (matcher.find()) {
            String domain = matcher.group();
            if (!whitelistedDomains.isEmpty()) {
                boolean ok = whitelistedDomains.stream()
                        .anyMatch(d -> domain.toLowerCase().contains(d.toLowerCase()));
                if (!ok) return blocked(config.getString("filters.advertising.blocked-message",
                        "&#CF6679Реклама запрещена!"));
            }
            if (!blacklistedDomains.isEmpty()) {
                boolean bad = blacklistedDomains.stream()
                        .anyMatch(d -> domain.toLowerCase().contains(d.toLowerCase()));
                if (bad) return blocked(config.getString("filters.advertising.blocked-message",
                        "&#CF6679Реклама запрещена!"));
            }
        }
        return ok(message);
    }

    private String filterRepeatingChars(String message) {
        int max = config.getInt("filters.repeat.max-repeats", 3);
        Matcher m = REPEAT_PATTERN.matcher(message);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            m.appendReplacement(sb, Matcher.quoteReplacement(
                    m.group(1).repeat(Math.min(max, m.group().length()))));
        }
        m.appendTail(sb);
        return sb.toString();
    }

    private String extractDomain(String url) {
        url = url.replaceFirst("^https?://", "");
        int slash = url.indexOf('/');
        return slash != -1 ? url.substring(0, slash) : url;
    }

    // ── Утилиты ────────────────────────────────────────────────────────────────

    private static FilterResult ok(String msg)      { return new FilterResult(true,  msg, null); }
    private static FilterResult blocked(String reason) { return new FilterResult(false, null, reason); }

    /**
     * Очищает трекеры для игрока при выходе (вызывать из PlayerEventListener).
     */
    public void clearPlayer(java.util.UUID uuid) {
        floodTracker.remove(uuid);
        spamTracker.remove(uuid);
    }

    public record FilterResult(boolean allowed, String filteredMessage, String blockReason) {}
}
