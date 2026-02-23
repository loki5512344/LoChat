package com.loki.lohub.utils;

import net.md_5.bungee.api.ChatColor;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class TextUtil {

    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");

    private TextUtil() {
        // Utility class - prevent instantiation
    }

    public static String colorize(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        text = translateHexCodes(text);
        text = ChatColor.translateAlternateColorCodes('&', text);

        return text;
    }

    public static List<String> colorize(List<String> lines) {
        return lines.stream()
                .map(TextUtil::colorize)
                .collect(Collectors.toList());
    }

    private static String translateHexCodes(String text) {
        Matcher matcher = HEX_PATTERN.matcher(text);
        StringBuffer buffer = new StringBuffer();

        while (matcher.find()) {
            String hexCode = matcher.group(1);
            String replacement = ChatColor.of("#" + hexCode).toString();
            matcher.appendReplacement(buffer, replacement);
        }

        matcher.appendTail(buffer);
        return buffer.toString();
    }

    public static String strip(String text) {
        return ChatColor.stripColor(text);
    }

    public static String joinFrom(int startIndex, String[] args) {
        if (args == null || startIndex >= args.length) {
            return "";
        }

        StringBuilder builder = new StringBuilder();
        for (int i = startIndex; i < args.length; i++) {
            builder.append(args[i]);
            if (i < args.length - 1) {
                builder.append(" ");
            }
        }

        return builder.toString();
    }

    public static String listToString(List<String> list) {
        if (list == null || list.isEmpty()) {
            return "";
        }

        return String.join("\n", list);
    }
}
