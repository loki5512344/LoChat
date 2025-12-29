package com.loki.lochat.managers;

import com.loki.lochat.LoChat;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class AntiSpamManager {

    private final LoChat plugin;
    private final Map<UUID, String> lastMessages = new ConcurrentHashMap<>();
    private final Map<UUID, Long> lastMessageTime = new ConcurrentHashMap<>();

    public AntiSpamManager(LoChat plugin) {
        this.plugin = plugin;
    }

    public enum SpamResult {
        ALLOWED,
        TOO_MANY_CAPS,
        REPEAT_CHARS,
        SIMILAR_MESSAGE
    }

    public SpamResult checkMessage(UUID player, String message) {
        if (!plugin.getConfigManager().isAntiSpamEnabled()) {
            return SpamResult.ALLOWED;
        }

        // Проверка CAPS
        if (hasTooManyCaps(message)) {
            return SpamResult.TOO_MANY_CAPS;
        }

        // Проверка повторяющихся символов
        if (hasRepeatChars(message)) {
            return SpamResult.REPEAT_CHARS;
        }

        // Проверка похожих сообщений
        if (isSimilarMessage(player, message)) {
            return SpamResult.SIMILAR_MESSAGE;
        }

        // Сохраняем сообщение
        lastMessages.put(player, message.toLowerCase());
        lastMessageTime.put(player, System.currentTimeMillis());

        return SpamResult.ALLOWED;
    }

    private boolean hasTooManyCaps(String message) {
        if (message.length() < 5) return false;

        int maxPercent = plugin.getConfigManager().getMaxCapsPercent();
        long upperCount = message.chars().filter(Character::isUpperCase).count();
        long letterCount = message.chars().filter(Character::isLetter).count();

        if (letterCount == 0) return false;

        int percent = (int) ((upperCount * 100) / letterCount);
        return percent > maxPercent;
    }

    private boolean hasRepeatChars(String message) {
        int maxRepeat = plugin.getConfigManager().getMaxRepeatChars();
        int count = 1;
        char lastChar = 0;

        for (char c : message.toCharArray()) {
            if (c == lastChar) {
                count++;
                if (count > maxRepeat) return true;
            } else {
                count = 1;
                lastChar = c;
            }
        }

        return false;
    }

    private boolean isSimilarMessage(UUID player, String message) {
        String lastMsg = lastMessages.get(player);
        Long lastTime = lastMessageTime.get(player);

        if (lastMsg == null || lastTime == null) return false;

        int delay = plugin.getConfigManager().getSimilarMessageDelay();
        long elapsed = (System.currentTimeMillis() - lastTime) / 1000;

        if (elapsed > delay) return false;

        // Проверка схожести (простая)
        return message.toLowerCase().equals(lastMsg) || 
               similarity(message.toLowerCase(), lastMsg) > 0.8;
    }

    private double similarity(String s1, String s2) {
        int maxLen = Math.max(s1.length(), s2.length());
        if (maxLen == 0) return 1.0;

        int distance = levenshteinDistance(s1, s2);
        return 1.0 - ((double) distance / maxLen);
    }

    private int levenshteinDistance(String s1, String s2) {
        int[][] dp = new int[s1.length() + 1][s2.length() + 1];

        for (int i = 0; i <= s1.length(); i++) dp[i][0] = i;
        for (int j = 0; j <= s2.length(); j++) dp[0][j] = j;

        for (int i = 1; i <= s1.length(); i++) {
            for (int j = 1; j <= s2.length(); j++) {
                int cost = s1.charAt(i - 1) == s2.charAt(j - 1) ? 0 : 1;
                dp[i][j] = Math.min(Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1), dp[i - 1][j - 1] + cost);
            }
        }

        return dp[s1.length()][s2.length()];
    }

    public void clearPlayer(UUID player) {
        lastMessages.remove(player);
        lastMessageTime.remove(player);
    }
}
