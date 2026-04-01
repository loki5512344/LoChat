package com.loki.lochat.commands;

import com.loki.lochat.managers.CustomCommandManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public final class CustomCommand extends Command {

    private final CustomCommandManager manager;

    public CustomCommand(String name, CustomCommandManager manager) {
        super(name);
        this.manager = manager;
        this.setDescription("Кастомная команда LoChat");
        this.setUsage("/" + name);
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Только для игроков!");
            return true;
        }

        return manager.executeCustomCommand(commandLabel, player, args);
    }

    @Override
    public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException {
        // Базовое автодополнение - можно расширить при необходимости
        return new ArrayList<>();
    }
}
