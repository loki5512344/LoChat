package com.loki.lochat.utils;

public final class TimeFormatter {

    private TimeFormatter() {
    }

    public static String format(long millis) {
        if (millis <= 0) {
            return "0с";
        }

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

    public static long parse(String timeStr) {
        if (timeStr == null || timeStr.isEmpty()) {
            return 0;
        }

        long totalMillis = 0;
        StringBuilder number = new StringBuilder();

        for (char c : timeStr.toCharArray()) {
            if (Character.isDigit(c)) {
                number.append(c);
            } else if (number.length() > 0) {
                long value = Long.parseLong(number.toString());
                totalMillis += getMillisForUnit(c, value);
                number.setLength(0);
            }
        }

        return totalMillis;
    }

    private static long getMillisForUnit(char unit, long value) {
        return switch (Character.toLowerCase(unit)) {
            case 's', 'с' -> value * 1000;
            case 'm', 'м' -> value * 60 * 1000;
            case 'h', 'ч' -> value * 60 * 60 * 1000;
            case 'd', 'д' -> value * 24 * 60 * 60 * 1000;
            default -> 0;
        };
    }
}
