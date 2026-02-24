package com.loki.lohub.common;

import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public final class LaunchHelper {

    private LaunchHelper() {
    }

    public static void launch(Player player, double power, double powerY) {
        Vector velocity = player.getLocation().getDirection().multiply(power);
        velocity.setY(powerY);
        player.setVelocity(velocity);
    }
}
