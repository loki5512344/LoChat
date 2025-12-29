package com.loki.lochat.commands;

import com.loki.lochat.LoChat;
import com.loki.lochat.utils.ChatFormatter;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ChatSpyCommand implements CommandExecutor {

    private final LoChat plugin;

    public ChatSpyCommand(LoChat plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Только для игроков!");
            return true;
        }

        boolean enabled = plugin.getSpyManager().toggleSpy(player.getUniqueId());
        String msgKey = enabled ? "spy.enabled" : "spy.disabled";
        player.sendMessage(ChatFormatter.parse(plugin.getMessageConfig().get(msgKey)));

        return true;
    }
}
