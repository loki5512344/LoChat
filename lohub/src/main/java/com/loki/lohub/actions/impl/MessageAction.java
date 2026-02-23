package com.loki.lohub.actions.impl;

import com.loki.lohub.actions.Action;
import com.loki.lohub.actions.ActionType;
import com.loki.lohub.utils.PlaceholderUtil;
import com.loki.lohub.utils.TextUtil;
import org.bukkit.entity.Player;

public class MessageAction implements Action {

    private final String message;

    public MessageAction(String message) {
        this.message = message;
    }

    @Override
    public void execute(Player player) {
        String parsed = PlaceholderUtil.parse(message, player);
        player.sendMessage(TextUtil.colorize(parsed));
    }

    @Override
    public ActionType getType() {
        return ActionType.MESSAGE;
    }
}
