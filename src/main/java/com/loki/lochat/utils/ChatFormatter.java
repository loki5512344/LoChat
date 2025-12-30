package com.loki.lochat.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

public final class ChatFormatter {

    private static final MiniMessage MM = MiniMessage.miniMessage();

    private ChatFormatter() {}

    public static Component format(
            String format,
            Component name,
            Component prefix,
            Component message
    ) {
        return MM.deserialize(format)
                .replaceText(b -> b.matchLiteral("{name}").replacement(name))
                .replaceText(b -> b.matchLiteral("{prefix}").replacement(prefix))
                .replaceText(b -> b.matchLiteral("{message}").replacement(message));
    }
}
