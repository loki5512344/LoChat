package com.loki.lochat.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;

public final class ChatFormatter {

    private static final MiniMessage MM = MiniMessage.miniMessage();
    private static final PlainTextComponentSerializer PLAIN =
            PlainTextComponentSerializer.plainText();

    private ChatFormatter() {
    }

    /* ===================== BASIC ===================== */

    public static Component parse(String message) {
        if (message == null) return Component.empty();
        // Конвертируем legacy форматы в MiniMessage перед парсингом
        String converted = convertLegacyFormats(message);
        return MM.deserialize(converted);
    }

    public static Component parse(String message, String... replacements) {
        if (message == null) return Component.empty();
        String result = message;
        for (int i = 0; i + 1 < replacements.length; i += 2) {
            result = result.replace(replacements[i], replacements[i + 1]);
        }
        // Конвертируем legacy форматы в MiniMessage перед парсингом
        String converted = convertLegacyFormats(result);
        return MM.deserialize(converted);
    }

    /**
     * Конвертирует legacy форматы цветов в MiniMessage формат
     */
    private static String convertLegacyFormats(String message) {
        if (message == null) return "";

        // Конвертируем &#RRGGBB в <color:#RRGGBB>
        java.util.regex.Pattern pattern1 = java.util.regex.Pattern.compile("&#([0-9a-fA-F]{6})");
        java.util.regex.Matcher matcher1 = pattern1.matcher(message);
        StringBuffer sb1 = new StringBuffer();
        while (matcher1.find()) {
            String hex = matcher1.group(1).toLowerCase();
            matcher1.appendReplacement(sb1, "<color:#" + hex + ">");
        }
        matcher1.appendTail(sb1);
        message = sb1.toString();

        // Конвертируем #RRGGBB в <color:#RRGGBB> (только если не внутри тегов)
        java.util.regex.Pattern pattern2 = java.util.regex.Pattern.compile("(?<!<)#([0-9a-fA-F]{6})(?![^<]*>)");
        java.util.regex.Matcher matcher2 = pattern2.matcher(message);
        StringBuffer sb2 = new StringBuffer();
        while (matcher2.find()) {
            String hex = matcher2.group(1).toLowerCase();
            matcher2.appendReplacement(sb2, "<color:#" + hex + ">");
        }
        matcher2.appendTail(sb2);
        message = sb2.toString();

        // Конвертируем §[0-9a-fk-or] в соответствующие MiniMessage теги
        message = message.replaceAll("§0", "<black>");
        message = message.replaceAll("§1", "<dark_blue>");
        message = message.replaceAll("§2", "<dark_green>");
        message = message.replaceAll("§3", "<dark_aqua>");
        message = message.replaceAll("§4", "<dark_red>");
        message = message.replaceAll("§5", "<dark_purple>");
        message = message.replaceAll("§6", "<gold>");
        message = message.replaceAll("§7", "<gray>");
        message = message.replaceAll("§8", "<dark_gray>");
        message = message.replaceAll("§9", "<blue>");
        message = message.replaceAll("§a", "<green>");
        message = message.replaceAll("§b", "<aqua>");
        message = message.replaceAll("§c", "<red>");
        message = message.replaceAll("§d", "<light_purple>");
        message = message.replaceAll("§e", "<yellow>");
        message = message.replaceAll("§f", "<white>");
        message = message.replaceAll("§k", "<obfuscated>");
        message = message.replaceAll("§l", "<bold>");
        message = message.replaceAll("§m", "<strikethrough>");
        message = message.replaceAll("§n", "<underlined>");
        message = message.replaceAll("§o", "<italic>");
        message = message.replaceAll("§r", "<reset>");

        // Конвертируем &[0-9a-fk-or] в соответствующие MiniMessage теги
        message = message.replaceAll("&0", "<black>");
        message = message.replaceAll("&1", "<dark_blue>");
        message = message.replaceAll("&2", "<dark_green>");
        message = message.replaceAll("&3", "<dark_aqua>");
        message = message.replaceAll("&4", "<dark_red>");
        message = message.replaceAll("&5", "<dark_purple>");
        message = message.replaceAll("&6", "<gold>");
        message = message.replaceAll("&7", "<gray>");
        message = message.replaceAll("&8", "<dark_gray>");
        message = message.replaceAll("&9", "<blue>");
        message = message.replaceAll("&a", "<green>");
        message = message.replaceAll("&b", "<aqua>");
        message = message.replaceAll("&c", "<red>");
        message = message.replaceAll("&d", "<light_purple>");
        message = message.replaceAll("&e", "<yellow>");
        message = message.replaceAll("&f", "<white>");

        // Конвертируем форматирование
        message = message.replaceAll("&k", "<obfuscated>");
        message = message.replaceAll("&l", "<bold>");
        message = message.replaceAll("&m", "<strikethrough>");
        message = message.replaceAll("&n", "<underlined>");
        message = message.replaceAll("&o", "<italic>");
        message = message.replaceAll("&r", "<reset>");

        return message;
    }

    public static String stripTags(String message) {
        if (message == null) return "";
        return PLAIN.serialize(MM.deserialize(message));
    }

    public static String toPlain(Component component) {
        return component == null ? "" : PLAIN.serialize(component);
    }

    public static String toPlain(Object obj) {
        if (obj == null) return "";
        if (obj instanceof Component component) {
            return PLAIN.serialize(component);
        }
        return obj.toString();
    }

    /**
     * Отправляет сообщение игроку
     */
    public static void sendMessage(Player player, Component component) {
        if (player != null && component != null) {
            player.sendMessage(component);
        }
    }

    /* ===================== COLORS ===================== */

    /**
     * Конвертирует различные форматы цветов в MiniMessage формат
     */
    public static String convertAllColors(String message) {
        if (message == null) return "";
        return convertLegacyFormats(message);
    }

    /* ===================== EMOJIS (REMOVED) ===================== */

    public static String replaceEmojis(String message) {
        return message; // Эмодзи удалены
    }

    public static String replaceEmojis(String message, Player player) {
        return message; // Эмодзи удалены
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
