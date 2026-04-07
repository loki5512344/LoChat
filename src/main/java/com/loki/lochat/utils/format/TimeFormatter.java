package com.loki.lochat.utils.format;

public final class TimeFormatter {

    private TimeFormatter() {
    }

    public static String format(long millis) {
        if (millis < 0) {
            return "0с";
        }
        
        if (millis == 0) {
            return "навсегда";
        }

        long seconds = millis / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        if (days > 0) {
            long remainingHours = hours % 24;
            return remainingHours > 0 ? days + "д " + remainingHours + "ч" : days + "д";
        } else if (hours > 0) {
            long remainingMinutes = minutes % 60;
            return remainingMinutes > 0 ? hours + "ч " + remainingMinutes + "м" : hours + "ч";
        } else if (minutes > 0) {
            long remainingSeconds = seconds % 60;
            return remainingSeconds > 0 ? minutes + "м " + remainingSeconds + "с" : minutes + "м";
        } else {
            return seconds + "с";
        }
    }

    public static long parse(String timeStr) {
        if (timeStr == null || timeStr.trim().isEmpty()) {
            return 0;
        }

        long totalMillis = 0;
        StringBuilder number = new StringBuilder();

        for (char c : timeStr.toCharArray()) {
            if (Character.isDigit(c)) {
                number.append(c);
            } else if (number.length() > 0) {
                try {
                    long value = Long.parseLong(number.toString());
                    totalMillis += getMillisForUnit(c, value);
                } catch (NumberFormatException e) {
                    // Skip invalid numbers
                }
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
