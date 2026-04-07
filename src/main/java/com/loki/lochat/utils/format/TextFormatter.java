package com.loki.lochat.utils.format;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

import java.util.regex.Pattern;

public class TextFormatter {

    private static final Pattern BOLD_PATTERN = Pattern.compile("\\*\\*(.+?)\\*\\*");
    private static final Pattern ITALIC_PATTERN = Pattern.compile("\\*(.+?)\\*");
    private static final Pattern UNDERLINE_PATTERN = Pattern.compile("__(.+?)__");
    private static final Pattern STRIKETHROUGH_PATTERN = Pattern.compile("~~(.+?)~~");
    private static final Pattern CODE_PATTERN = Pattern.compile("`(.+?)`");

    // Паттерн для быстрой проверки — есть ли в тексте markdown-символы
    private static final Pattern HAS_MARKDOWN = Pattern.compile("[*_~`]");

    /**
     * Применяет markdown-форматирование к компоненту.
     *
     * Важно: PlainTextComponentSerializer стриппает все Adventure-цвета.
     * Поэтому мы обрабатываем только replaceText на оригинальном компоненте,
     * не конвертируя его в plain string и обратно.
     * Это сохраняет градиенты и цвета, добавленные Adventure.
     */
    public static Component formatMarkdown(Component message) {
        String plain = PlainTextComponentSerializer.plainText().serialize(message);

        // Быстрая проверка — нет markdown-символов, возвращаем как есть (без потери цветов)
        if (!HAS_MARKDOWN.matcher(plain).find()) {
            return message;
        }

        // Применяем замены через replaceText прямо на компоненте — цвета сохраняются
        message = applyBold(message);
        message = applyItalic(message);
        message = applyUnderline(message);
        message = applyStrikethrough(message);
        message = applyCode(message);

        return message;
    }

    private static Component applyBold(Component message) {
        return message.replaceText(config -> config
                .match(BOLD_PATTERN)
                .replacement((matchResult, builder) -> {
                    String content = matchResult.group(1);
                    return ChatFormatter.parse("<bold>" + content + "</bold>");
                })
        );
    }

    private static Component applyItalic(Component message) {
        return message.replaceText(config -> config
                .match(ITALIC_PATTERN)
                .replacement((matchResult, builder) -> {
                    String content = matchResult.group(1);
                    // Пропускаем если это часть ** (bold)
                    if (content.startsWith("*") || content.endsWith("*")) {
                        return builder.content(matchResult.group());
                    }
                    return ChatFormatter.parse("<italic>" + content + "</italic>");
                })
        );
    }

    private static Component applyUnderline(Component message) {
        return message.replaceText(config -> config
                .match(UNDERLINE_PATTERN)
                .replacement((matchResult, builder) -> {
                    String content = matchResult.group(1);
                    return ChatFormatter.parse("<underlined>" + content + "</underlined>");
                })
        );
    }

    private static Component applyStrikethrough(Component message) {
        return message.replaceText(config -> config
                .match(STRIKETHROUGH_PATTERN)
                .replacement((matchResult, builder) -> {
                    String content = matchResult.group(1);
                    return ChatFormatter.parse("<strikethrough>" + content + "</strikethrough>");
                })
        );
    }

    private static Component applyCode(Component message) {
        return message.replaceText(config -> config
                .match(CODE_PATTERN)
                .replacement((matchResult, builder) -> {
                    String content = matchResult.group(1);
                    return ChatFormatter.parse("&#888888`" + content + "`");
                })
        );
    }
}
