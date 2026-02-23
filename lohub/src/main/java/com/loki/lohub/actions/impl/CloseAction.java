package com.loki.lohub.actions.impl;

import com.loki.lohub.actions.Action;
import com.loki.lohub.actions.ActionType;
import org.bukkit.entity.Player;

public class CloseAction implements Action {
    
    @Override
    public void execute(Player player) {
        player.closeInventory();
    }
    
    @Override
    public ActionType getType() {
        return ActionType.CLOSE;
    }
}
