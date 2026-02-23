package com.loki.lohub.actions.impl;

import com.loki.lohub.actions.Action;
import com.loki.lohub.actions.ActionType;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

public class GamemodeAction implements Action {
    
    private final GameMode gameMode;
    
    public GamemodeAction(String mode) {
        GameMode parsed;
        try {
            parsed = GameMode.valueOf(mode.toUpperCase());
        } catch (IllegalArgumentException e) {
            parsed = switch (mode.toLowerCase()) {
                case "0", "s" -> GameMode.SURVIVAL;
                case "1", "c" -> GameMode.CREATIVE;
                case "2", "a" -> GameMode.ADVENTURE;
                case "3", "sp" -> GameMode.SPECTATOR;
                default -> GameMode.SURVIVAL;
            };
        }
        this.gameMode = parsed;
    }
    
    @Override
    public void execute(Player player) {
        player.setGameMode(gameMode);
    }
    
    @Override
    public ActionType getType() {
        return ActionType.GAMEMODE;
    }
}
