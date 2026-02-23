package com.loki.lohub.actions.impl;

import com.loki.lohub.actions.Action;
import com.loki.lohub.actions.ActionType;
import com.loki.lohub.utils.PlaceholderUtil;
import com.loki.lohub.utils.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class BroadcastAction implements Action {
    
    private final String message;
    
    public BroadcastAction(String message) {
        this.message = message;
    }
    
    @Override
    public void execute(Player player) {
        String parsed = PlaceholderUtil.parse(message, player);
        Bukkit.broadcastMessage(TextUtil.colorize(parsed));
    }
    
    @Override
    public ActionType getType() {
        return ActionType.BROADCAST;
    }
}
