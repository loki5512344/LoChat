package com.loki.lochat.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.CommandSender;

/**
 * Утилита для форматирования сообщений
 */
public class ChatFormatter {
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacyAmpersand();

    public static Object parse(String message) {
        try {
            return MINI_MESSAGE.deserialize(message);
        } catch (Exception e) {
            return LEGACY.deserialize(message);
        }
    }

    public static String toPlain(Object message) {
        if (message instanceof Component component) {
            return LegacyComponentSerializer.legacySection().serialize(component);
        }
        return message.toString();
    }

    public static void sendMessage(CommandSender sender, Object message) {
        if (message instanceof Component component) {
            sender.sendMessage(component);
        } else {
            sender.sendMessage(message.toString());
        }
    }

    public static void broadcast(Object message) {
        if (message instanceof Component component) {
            org.bukkit.Bukkit.broadcast(component);
        }
    }

    public static String stripTags(String message) {
        return message.replaceAll("<[^>]+>", "").replaceAll("§[0-9a-fk-or]", "");
    }

    public static String convertAllColors(String message) {
        return message.replace("&", "§");
    }
}
