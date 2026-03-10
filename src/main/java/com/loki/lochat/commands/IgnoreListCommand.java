package com.loki.lochat.commands;

import com.loki.lochat.LoChat;
import com.loki.lochat.api.service.IgnoreService;
import com.loki.lochat.utils.ChatFormatter;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.UUID;

/**
 * Команда /ignorelist - показывает список игнорируемых игроков
 */
public class IgnoreListCommand implements CommandExecutor {

    private final LoChat plugin;
    private final IgnoreService ignoreService;

    public IgnoreListCommand(LoChat plugin) {
        this.plugin = plugin;
        this.ignoreService = plugin.getServiceRegistry().get(IgnoreService.class);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Только для игроков!");
            return true;
        }

        Set<UUID> ignoredPlayers = ignoreService.getIgnoredPlayers(player.getUniqueId());

        if (ignoredPlayers.isEmpty()) {
            player.sendMessage(ChatFormatter.parse(plugin.getMessageConfig().get("ignore.list-empty")));
            return true;
        }

        player.sendMessage(ChatFormatter.parse(plugin.getMessageConfig().get("ignore.list-header")));

        for (UUID ignoredUuid : ignoredPlayers) {
            String playerName = com.loki.lochat.utils.PlayerUtil.getPlayerName(ignoredUuid);
            if (playerName != null) {
                player.sendMessage(ChatFormatter.parse("&#87CEEB• &#FFFFFF" + playerName));
            }
        }

        player.sendMessage(ChatFormatter.parse(
                plugin.getMessageConfig().get("ignore.list-footer")
                        .replace("{count}", String.valueOf(ignoredPlayers.size()))
        ));

        return true;
    }
}
