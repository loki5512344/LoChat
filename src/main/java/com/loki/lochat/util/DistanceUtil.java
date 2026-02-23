package com.loki.lochat.util;

import org.bukkit.entity.Player;

/**
 * Утилита для проверки расстояния между игроками
 */
public class DistanceUtil {
    
    public static boolean isInRange(Player player1, Player player2, int radius) {
        if (!player1.getWorld().equals(player2.getWorld())) {
            return false;
        }
        
        return player1.getLocation().distance(player2.getLocation()) <= radius;
    }
}
