package com.loki.lochat.gradient.util;

import java.awt.Color;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Утилита для работы с градиентами
 */
public final class GradientUtil {

    private static final Pattern HEX_PATTERN = Pattern.compile("^#[0-9A-Fa-f]{6}$");

    private GradientUtil() {}

    public static boolean isValidHex(String color) {
        return HEX_PATTERN.matcher(color).matches();
    }

    public static String applyGradient(String text, List<String> hexColors, boolean useLegacyFormat) {
        if (text == null || text.isEmpty() || hexColors == null || hexColors.isEmpty()) {
            return text;
        }

        char[] chars = text.toCharArray();
        int length = chars.length;
        if (length == 0) return text;

        StringBuilder result = new StringBuilder();

        if (hexColors.size() == 1) {
            // Для одного цвета применяем цвет один раз в начале
            String colorCode = formatHex(hexColors.get(0), useLegacyFormat);
            if (useLegacyFormat) {
                // В legacy формате цвет применяется к каждому символу, но мы применяем один цвет
                // Поэтому применяем цвет один раз в начале и добавляем весь текст
                result.append(colorCode).append(text);
            } else {
                // В MiniMessage формате применяем цвет один раз
                result.append(colorCode).append(text).append("</color>");
            }
        } else {
            Color[] colors = hexColors.stream()
                    .map(GradientUtil::parseHex)
                    .toArray(Color[]::new);

            for (int i = 0; i < length; i++) {
                Color interpolated = interpolateMultiColor(colors, (double) i / Math.max(1, length - 1));
                String hex = String.format("#%02x%02x%02x", 
                        interpolated.getRed(), interpolated.getGreen(), interpolated.getBlue());
                result.append(formatHex(hex, useLegacyFormat)).append(chars[i]);
            }
        }

        return result.toString();
    }

    private static Color interpolateMultiColor(Color[] colors, double ratio) {
        if (colors.length == 1) return colors[0];
        if (ratio <= 0) return colors[0];
        if (ratio >= 1) return colors[colors.length - 1];

        double scaledRatio = ratio * (colors.length - 1);
        int index = (int) scaledRatio;
        double localRatio = scaledRatio - index;

        if (index >= colors.length - 1) {
            return colors[colors.length - 1];
        }

        return interpolate(colors[index], colors[index + 1], localRatio);
    }

    private static Color interpolate(Color c1, Color c2, double ratio) {
        int r = (int) (c1.getRed() + (c2.getRed() - c1.getRed()) * ratio);
        int g = (int) (c1.getGreen() + (c2.getGreen() - c1.getGreen()) * ratio);
        int b = (int) (c1.getBlue() + (c2.getBlue() - c1.getBlue()) * ratio);
        return new Color(
                Math.max(0, Math.min(255, r)),
                Math.max(0, Math.min(255, g)),
                Math.max(0, Math.min(255, b))
        );
    }

    private static Color parseHex(String hex) {
        hex = hex.replace("#", "");
        return new Color(
                Integer.parseInt(hex.substring(0, 2), 16),
                Integer.parseInt(hex.substring(2, 4), 16),
                Integer.parseInt(hex.substring(4, 6), 16)
        );
    }

    private static String formatHex(String hex, boolean useLegacyFormat) {
        hex = hex.replace("#", "").toLowerCase();
        if (useLegacyFormat) {
            // Формат §x§R§R§G§G§B§B для TAB и других плагинов
            StringBuilder sb = new StringBuilder("§x");
            for (char c : hex.toCharArray()) {
                sb.append("§").append(c);
            }
            return sb.toString();
        } else {
            return "<#" + hex + ">";
        }
    }

    public static String buildDisplayName(String prefix, String nick, List<String> colors, 
                                          boolean gradientOnPrefix, boolean continuousGradient, 
                                          String prefixFormat, boolean useLegacyFormat) {
        if (colors == null || colors.isEmpty()) {
            if (prefix != null && !prefix.isEmpty()) {
                return prefixFormat.replace("{prefix}", prefix) + nick;
            }
            return nick;
        }

        if (prefix == null || prefix.isEmpty()) {
            return applyGradient(nick, colors, useLegacyFormat);
        }

        if (continuousGradient && gradientOnPrefix) {
            String fullText = prefixFormat.replace("{prefix}", prefix) + nick;
            return applyGradient(fullText, colors, useLegacyFormat);
        } else if (gradientOnPrefix) {
            String coloredPrefix = applyGradient(prefixFormat.replace("{prefix}", prefix), colors, useLegacyFormat);
            String coloredNick = applyGradient(nick, colors, useLegacyFormat);
            return coloredPrefix + coloredNick;
        } else {
            String formattedPrefix = prefixFormat.replace("{prefix}", prefix);
            return formattedPrefix + applyGradient(nick, colors, useLegacyFormat);
        }
    }
}
