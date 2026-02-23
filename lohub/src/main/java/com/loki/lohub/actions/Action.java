package com.loki.lohub.actions;

import org.bukkit.entity.Player;

public interface Action {
    
    void execute(Player player);
    
    ActionType getType();
}
