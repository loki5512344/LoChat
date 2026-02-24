package com.loki.lohub.common;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;

public final class GameModeHelper {

    private GameModeHelper() {
    }

    public static boolean isCreativeOrSpectator(Player player) {
        GameMode mode = player.getGameMode();
        return mode == GameMode.CREATIVE || mode == GameMode.SPECTATOR;
    }
}
