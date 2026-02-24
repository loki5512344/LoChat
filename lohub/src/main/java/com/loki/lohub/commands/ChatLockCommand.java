package com.loki.lohub.commands;

import com.loki.lohub.LoHub;
import com.loki.lohub.utils.TextUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class ChatLockCommand implements CommandExecutor {

    private final LoHub plugin;

    public ChatLockCommand(LoHub plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("lohub.chatlock")) {
            sender.sendMessage(TextUtil.colorize(plugin.getConfigManager().getMessage("general.no-permission")));
            return true;
        }

        plugin.getChatLockManager().toggle();
        boolean locked = plugin.getChatLockManager().isChatLocked();

        String message = locked
                ? plugin.getConfigManager().getMessage("chat.lock-enabled")
                : plugin.getConfigManager().getMessage("chat.lock-disabled");

        sender.sendMessage(TextUtil.colorize(message));
        return true;
    }
}
