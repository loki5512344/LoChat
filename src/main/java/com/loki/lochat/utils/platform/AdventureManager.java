package com.loki.lochat.utils.platform;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 * Менеджер для работы с Adventure API на Arclight/Forge серверах
 */
public class AdventureManager {

    private AdventureManager() {
    }

    private static boolean adventureAvailable = false;
    private static Object audiences;

    public static void init(Plugin plugin) {
        try {
            // Проверяем наличие Adventure API
            Class.forName("net.kyori.adventure.text.Component");
            Class<?> audiencesClass = Class.forName("net.kyori.adventure.platform.bukkit.BukkitAudiences");

            // Создаем BukkitAudiences через рефлексию
            Object audiencesInstance = audiencesClass.getMethod("create", Plugin.class).invoke(null, plugin);
            audiences = audiencesInstance;
            adventureAvailable = true;

            plugin.getLogger().info("Adventure API обнаружен - используем нативную поддержку");
        } catch (Exception e) {
            adventureAvailable = false;
            plugin.getLogger().info("Adventure API не найден - используем legacy режим");
        }
    }

    public static void shutdown() {
        if (audiences != null && adventureAvailable) {
            try {
                audiences.getClass().getMethod("close").invoke(audiences);
            } catch (Exception e) {
                // Игнорируем ошибки при закрытии
            }
            audiences = null;
        }
        adventureAvailable = false;
    }

    public static void sendMessage(CommandSender sender, Object message) {
        if (adventureAvailable && audiences != null) {
            try {
                // Используем Adventure API
                Object senderAudience = audiences.getClass().getMethod("sender", CommandSender.class).invoke(audiences, sender);
                senderAudience.getClass()
                        .getMethod("sendMessage", Class.forName("net.kyori.adventure.text.Component"))
                        .invoke(senderAudience, message);
                return;
            } catch (Exception e) {
                // Fallback на legacy
            }
        }

        // Legacy fallback
        if (message instanceof String) {
            sender.sendMessage((String) message);
        } else {
            sender.sendMessage(message.toString());
        }
    }

    public static void sendMessage(Player player, Object message) {
        if (adventureAvailable && audiences != null) {
            try {
                // Используем Adventure API
                Object playerAudience = audiences.getClass().getMethod("player", Player.class).invoke(audiences, player);
                playerAudience.getClass()
                    .getMethod("sendMessage", Class.forName("net.kyori.adventure.text.Component"))
                    .invoke(playerAudience, message);
                return;
            } catch (Exception e) {
                // Fallback на legacy
            }
        }

        // Legacy fallback
        if (message instanceof String) {
            player.sendMessage((String) message);
        } else {
            player.sendMessage(message.toString());
        }
    }

    public static boolean isAdventureAvailable() {
        return adventureAvailable;
    }
}
