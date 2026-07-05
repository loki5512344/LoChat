package com.loki.lochat.renderer.components;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.TextColor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UrlProcessor {
    private UrlProcessor() {
    }

    private static final Pattern URL_PATTERN = Pattern.compile(
        "https?://[\\w\\-._~:/?#\\[\\]@!$&'()*+,;=%]+"
    );

    public static Component process(Component message) {
        if (!(message instanceof TextComponent textComp)) {
            return message;
        }

        String content = textComp.content();
        Matcher matcher = URL_PATTERN.matcher(content);

        if (!matcher.find()) {
            return message;
        }

        TextComponent.Builder builder = Component.text();
        int lastEnd = 0;

        matcher.reset();
        while (matcher.find()) {
            if (matcher.start() > lastEnd) {
                builder.append(Component.text(content.substring(lastEnd, matcher.start())));
            }

            String url = matcher.group();
            builder.append(Component.text(url)
                .color(TextColor.fromHexString("#5DADE2"))
                .clickEvent(ClickEvent.openUrl(url))
                .hoverEvent(HoverEvent.showText(Component.text("Открыть: " + url))));

            lastEnd = matcher.end();
        }

        if (lastEnd < content.length()) {
            builder.append(Component.text(content.substring(lastEnd)));
        }

        return builder.build();
    }
}
