package com.loki.lochat.utils;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public class DistanceUtil {

    private DistanceUtil() {
    }

    public static boolean isInRange(Player player1, Player player2, int radius) {
        // Проверка на один мир
        if (!player1.getWorld().equals(player2.getWorld())) {
            return false;
        }

        Location loc1 = player1.getLocation();
        Location loc2 = player2.getLocation();

        // Быстрая проверка по квадрату (без sqrt)
        double dx = loc1.getX() - loc2.getX();
        double dz = loc1.getZ() - loc2.getZ();
        double radiusSquared = (double) radius * radius;

        return (dx * dx + dz * dz) <= radiusSquared;
    }

    public static double getDistance(Player player1, Player player2) {
        if (!player1.getWorld().equals(player2.getWorld())) {
            return Double.MAX_VALUE;
        }
        return player1.getLocation().distance(player2.getLocation());
    }
}
