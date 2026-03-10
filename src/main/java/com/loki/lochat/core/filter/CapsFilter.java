package com.loki.lochat.core.filter;

import org.bukkit.entity.Player;

/**
 * Фильтр капса (заглавных букв)
 */
public class CapsFilter {
    private final int maxCapsPercent;
    private final int minLength;
    private final boolean autoLowercase;
    private final boolean blockMessage;

    public CapsFilter(int maxCapsPercent, int minLength, boolean autoLowercase, boolean blockMessage) {
        this.maxCapsPercent = maxCapsPercent;
        this.minLength = minLength;
        this.autoLowercase = autoLowercase;
        this.blockMessage = blockMessage;
    }

    /**
     * Проверяет и обрабатывает сообщение с капсом
     *
     * @return обработанное сообщение или null если заблокировано
     */
    public String filter(Player player, String message) {
        // Bypass для админов
        if (player.hasPermission("lochat.bypass.caps")) {
            return message;
        }

        // Игнорируем короткие сообщения
        if (message.length() < minLength) {
            return message;
        }

        // Подсчитываем процент заглавных букв
        int totalLetters = 0;
        int capsLetters = 0;

        for (char c : message.toCharArray()) {
            if (Character.isLetter(c)) {
                totalLetters++;
                if (Character.isUpperCase(c)) {
                    capsLetters++;
                }
            }
        }

        // Если букв мало, пропускаем
        if (totalLetters < 3) {
            return message;
        }

        int capsPercent = (capsLetters * 100) / totalLetters;

        // Если капса больше лимита
        if (capsPercent > maxCapsPercent) {
            if (blockMessage) {
                return null; // Блокируем сообщение
            } else if (autoLowercase) {
                // Понижаем регистр (первая буква заглавная)
                return capitalizeFirst(message.toLowerCase());
            }
        }

        return message;
    }

    /**
     * Делает первую букву заглавной
     */
    private String capitalizeFirst(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        return Character.toUpperCase(text.charAt(0)) + text.substring(1);
    }

    public int getMaxCapsPercent() {
        return maxCapsPercent;
    }
}
