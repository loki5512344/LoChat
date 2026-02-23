package com.loki.lohub.actions.impl;

import com.loki.lohub.actions.Action;
import com.loki.lohub.actions.ActionType;
import com.loki.lohub.utils.PlaceholderUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class ConsoleAction implements Action {

    private final String command;

    public ConsoleAction(String command) {
        this.command = command;
    }

    @Override
    public void execute(Player player) {
        String parsed = PlaceholderUtil.parse(command, player);
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), parsed);
    }

    @Override
    public ActionType getType() {
        return ActionType.CONSOLE;
    }
}
