package com.loki.lochat.utils;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * Утилиты для работы с игроками
 */
public final class PlayerUtil {

    private PlayerUtil() {}

    /**
     * Находит игрока по имени (онлайн или оффлайн)
     * @param name имя игрока
     * @return UUID игрока или null если не найден
     */
    public static UUID findPlayerUUID(String name) {
        // Сначала ищем онлайн игрока
        Player onlinePlayer = Bukkit.getPlayer(name);
        if (onlinePlayer != null) {
            return onlinePlayer.getUniqueId();
        }

        // Затем ищем оффлайн игрока
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(name);
        if (offlinePlayer.hasPlayedBefore() || offlinePlayer.isOnline()) {
            return offlinePlayer.getUniqueId();
        }

        return null;
    }

    /**
     * Проверяет, существует ли игрок (играл на сервере)
     * @param name имя игрока
     * @return true если игрок существует
     */
    public static boolean playerExists(String name) {
        return findPlayerUUID(name) != null;
    }

    /**
     * Получает имя игрока по UUID
     * @param uuid UUID игрока
     * @return имя игрока или null
     */
    public static String getPlayerName(UUID uuid) {
        Player onlinePlayer = Bukkit.getPlayer(uuid);
        if (onlinePlayer != null) {
            return onlinePlayer.getName();
        }

        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
        return offlinePlayer.getName();
    }

    /**
     * Проверяет, онлайн ли игрок
     * @param uuid UUID игрока
     * @return true если игрок онлайн
     */
    public static boolean isPlayerOnline(UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        return player != null && player.isOnline();
    }
}