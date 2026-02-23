package com.loki.lochat.util;

/**
 * Утилита для парсинга времени (DRY - вынесена общая логика)
 */
public class TimeParser {

    /**
     * Парсит строку времени в миллисекунды
     *
     * @param timeStr строка вида "1d", "2h", "30m", "60s"
     * @return миллисекунды или -1 при ошибке
     */
    public static long parse(String timeStr) {
        if (timeStr == null || timeStr.isEmpty()) return 0;

        timeStr = timeStr.toLowerCase();

        try {
            if (timeStr.endsWith("d")) {
                return Long.parseLong(timeStr.replace("d", "")) * 24 * 60 * 60 * 1000L;
            } else if (timeStr.endsWith("h")) {
                return Long.parseLong(timeStr.replace("h", "")) * 60 * 60 * 1000L;
            } else if (timeStr.endsWith("m")) {
                return Long.parseLong(timeStr.replace("m", "")) * 60 * 1000L;
            } else if (timeStr.endsWith("s")) {
                return Long.parseLong(timeStr.replace("s", "")) * 1000L;
            } else {
                return Long.parseLong(timeStr) * 60 * 1000L; // По умолчанию минуты
            }
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    /**
     * Форматирует миллисекунды в читаемую строку
     */
    public static String format(long millis) {
        if (millis <= 0) return "0с";

        long seconds = millis / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        if (days > 0) {
            return days + "д " + (hours % 24) + "ч";
        } else if (hours > 0) {
            return hours + "ч " + (minutes % 60) + "м";
        } else if (minutes > 0) {
            return minutes + "м " + (seconds % 60) + "с";
        } else {
            return seconds + "с";
        }
    }
}
