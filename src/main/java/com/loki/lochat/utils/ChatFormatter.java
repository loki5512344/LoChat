package com.loki.lochat.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;

public final class ChatFormatter {

    private static final MiniMessage MM = MiniMessage.miniMessage();
    private static final PlainTextComponentSerializer PLAIN =
            PlainTextComponentSerializer.plainText();

    private ChatFormatter() {}

    /* ===================== BASIC ===================== */

    public static Component parse(String message) {
        if (message == null) return Component.empty();
        return MM.deserialize(message);
    }

    public static Component parse(String message, String... replacements) {
        if (message == null) return Component.empty();
        String result = message;
        for (int i = 0; i + 1 < replacements.length; i += 2) {
            result = result.replace(replacements[i], replacements[i + 1]);
        }
        return MM.deserialize(result);
    }

    public static String stripTags(String message) {
        if (message == null) return "";
        return PLAIN.serialize(MM.deserialize(message));
    }

    public static String toPlain(Component component) {
        return component == null ? "" : PLAIN.serialize(component);
    }

    /* ===================== COLORS ===================== */

    /**
     * Возвращает строку как есть, так как теперь градиенты генерируются в MiniMessage формате
     */
    public static String convertAllColors(String message) {
        if (message == null) return "";
        return message;
    }

    /* ===================== EMOJIS ===================== */

    public static String replaceEmojis(String message) {
        return message;
    }

    public static String replaceEmojis(String message, Player player) {
        return message;
    }

    /* ===================== CHAT ===================== */

    /**
     * Legacy API для ChatManager
     */
    public static String processPlayerMessage(
            String message,
            Player player,
            boolean hasColorPermission
    ) {
        return message;
    }

    /* ===================== ANNOUNCE ===================== */

    public static Component formatAnnouncement(String format, String message) {
        return parse(format.replace("<message>", message));
    }

    /* ===================== PM ===================== */

    public static Component formatPmSentNew(
            String format,
            Player sender,
            Player receiver,
            String message
    ) {
        // Получаем отображаемые имена игроков
        String senderDisplay = getPlayerDisplayName(sender);
        String receiverDisplay = getPlayerDisplayName(receiver);
        
        return parse(format
                .replace("{sender}", sender.getName())
                .replace("{receiver}", receiver.getName())
                .replace("{sender_display}", senderDisplay)
                .replace("{receiver_display}", receiverDisplay)
                .replace("{message}", message));
    }

    public static Component formatPmReceivedNew(
            String format,
            Player sender,
            Player receiver,
            String message
    ) {
        // Получаем отображаемые имена игроков
        String senderDisplay = getPlayerDisplayName(sender);
        String receiverDisplay = getPlayerDisplayName(receiver);
        
        return parse(format
                .replace("{sender}", sender.getName())
                .replace("{receiver}", receiver.getName())
                .replace("{sender_display}", senderDisplay)
                .replace("{receiver_display}", receiverDisplay)
                .replace("{message}", message));
    }

    /**
     * Получает отображаемое имя игрока с градиентом
     */
    private static String getPlayerDisplayName(Player player) {
        // Получаем экземпляр плагина
        com.loki.lochat.LoChat plugin = com.loki.lochat.LoChat.getInstance();
        
        // Если градиентный модуль включен, используем его
        if (plugin.getGradientModule() != null && plugin.getGradientModule().isEnabled()) {
            return plugin.getGradientModule().getFormattedName(player);
        }
        
        // Иначе просто имя игрока
        return player.getName();
    }

    /* ===================== FORMAT (Component-based) ===================== */

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
