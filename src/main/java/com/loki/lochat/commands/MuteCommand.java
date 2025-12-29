package com.loki.lochat.commands;

import com.loki.lochat.LoChat;
import com.loki.lochat.managers.MuteManager;
import com.loki.lochat.utils.ChatFormatter;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class MuteCommand implements CommandExecutor {

    private final LoChat plugin;

    public MuteCommand(LoChat plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ChatFormatter.parse(plugin.getMessageConfig().getInvalidUsage("/mute <ник> <время> [причина]")));
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(ChatFormatter.parse(plugin.getMessageConfig().getPlayerNotFound()));
            return true;
        }

        if (plugin.getMuteManager().isAlreadyMuted(target.getUniqueId())) {
            sender.sendMessage(ChatFormatter.parse(plugin.getMessageConfig().get("mute.already-muted")));
            return true;
        }

        long duration = MuteManager.parseTime(args[1]);
        String reason = args.length > 2 ? String.join(" ", java.util.Arrays.copyOfRange(args, 2, args.length)) : null;

        plugin.getMuteManager().mute(target.getUniqueId(), duration, reason);

        String timeStr = duration <= 0 ? "навсегда" : args[1];
        sender.sendMessage(ChatFormatter.parse(
                plugin.getMessageConfig().get("mute.muted", "{player}", target.getName(), "{time}", timeStr)
        ));

        // Уведомляем игрока
        target.sendMessage(ChatFormatter.parse(
                plugin.getMessageConfig().get("mute.you-muted", "{time}", timeStr)
        ));

        return true;
    }
}
