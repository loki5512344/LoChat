package com.loki.lochat.commands;

import com.loki.lochat.LoChat;
import com.loki.lochat.utils.ChatFormatter;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class UnmuteCommand implements CommandExecutor {

    private final LoChat plugin;

    public UnmuteCommand(LoChat plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length < 1) {
            sender.sendMessage(ChatFormatter.parse(plugin.getMessageConfig().getInvalidUsage("/unmute <ник>")));
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(ChatFormatter.parse(plugin.getMessageConfig().getPlayerNotFound()));
            return true;
        }

        if (!plugin.getMuteManager().isMuted(target.getUniqueId())) {
            sender.sendMessage(ChatFormatter.parse(plugin.getMessageConfig().get("mute.not-muted")));
            return true;
        }

        plugin.getMuteManager().unmute(target.getUniqueId());
        sender.sendMessage(ChatFormatter.parse(
                plugin.getMessageConfig().get("mute.unmuted", "{player}", target.getName())
        ));

        return true;
    }
}
