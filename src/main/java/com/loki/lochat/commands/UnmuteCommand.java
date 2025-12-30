package com.loki.lochat.commands;

import com.loki.lochat.LoChat;
import com.loki.lochat.utils.ChatFormatter;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Collectors;

public class UnmuteCommand implements CommandExecutor, TabCompleter {

    private final LoChat plugin;

    public UnmuteCommand(LoChat plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("chat.mute")) {
            sender.sendMessage(ChatFormatter.parse(plugin.getMessageConfig().getNoPermission()));
            return true;
        }

        if (args.length != 1) {
            sender.sendMessage(ChatFormatter.parse(plugin.getMessageConfig().getInvalidUsage("/unmute <игрок>")));
            return true;
        }

        String playerName = args[0];
        Player target = Bukkit.getPlayer(playerName);
        if (target == null) {
            sender.sendMessage(ChatFormatter.parse(plugin.getMessageConfig().getPlayerNotFound()));
            return true;
        }

        if (!plugin.getMuteManager().isMuted(target.getUniqueId())) {
            sender.sendMessage(ChatFormatter.parse(
                plugin.getMessageConfig().get("mute.not-muted", "{player}", target.getName())
            ));
            return true;
        }

        // Размучиваем
        plugin.getMuteManager().unmutePlayer(target.getUniqueId());

        // Уведомления
        sender.sendMessage(ChatFormatter.parse(
            plugin.getMessageConfig().get("mute.unmuted", "{player}", target.getName())
        ));

        if (target.isOnline()) {
            target.sendMessage(ChatFormatter.parse("&#00FF00Вы были размучены!"));
        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return null;
    }
}