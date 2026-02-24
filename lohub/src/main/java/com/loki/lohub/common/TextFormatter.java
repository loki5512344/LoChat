package com.loki.lohub.common;

import com.loki.lohub.utils.PlaceholderUtil;
import com.loki.lohub.utils.TextUtil;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Collectors;

public final class TextFormatter {

    private TextFormatter() {
    }

    public static String format(String text, Player player) {
        return TextUtil.colorize(PlaceholderUtil.parse(text, player));
    }

    public static String joinLines(List<String> lines, Player player, String separator) {
        return lines.stream()
                .map(line -> format(line, player))
                .collect(Collectors.joining(separator));
    }
}
