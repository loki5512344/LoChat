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

import java.util.ArrayList;
import java.util.List;

/**
 * Команда /unmute nick [-s]
 */
public class UnmuteCommand implements CommandExecutor, TabCompleter {

    private final LoChat plugin;

    public UnmuteCommand(LoChat plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("lochat.unmute")) {
            sender.sendMessage(plugin.getMessageConfig().getComponent("errors.no-permission"));
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage("§cИспользование: /unmute <ник> [-s]");
            return true;
        }

        // Парсим аргументы
        String targetName = null;
        boolean silent = false;

        for (String arg : args) {
            if (arg.equalsIgnoreCase("-s")) {
                silent = true;
            } else if (targetName == null) {
                targetName = arg;
            }
        }

        // Проверка права на тихий размут
        if (silent && !sender.hasPermission("lochat.mute.silent")) {
            sender.sendMessage("§cУ вас нет права на тихий размут!");
            return true;
        }

        // Ищем игрока
        Player target = Bukkit.getPlayer(targetName);
        String finalTargetName = targetName;
        java.util.UUID targetUUID;

        if (target == null) {
            @SuppressWarnings("deprecation")
            var offlinePlayer = Bukkit.getOfflinePlayer(targetName);
            if (!offlinePlayer.hasPlayedBefore() && !offlinePlayer.isOnline()) {
                sender.sendMessage(plugin.getMessageConfig().getComponent("errors.player-not-found"));
                return true;
            }
            targetUUID = offlinePlayer.getUniqueId();
            finalTargetName = offlinePlayer.getName() != null ? offlinePlayer.getName() : targetName;
        } else {
            targetUUID = target.getUniqueId();
            finalTargetName = target.getName();
        }

        String operatorName = sender.getName();

        // Размучиваем
        if (plugin.getMuteManager().unmute(targetUUID, operatorName)) {
            sender.sendMessage("§aИгрок §e" + finalTargetName + " §aразмучен");

            // Уведомляем игрока (если онлайн)
            if (target != null) {
                target.sendMessage("§aВы были размучены!");
            }

            // Broadcast
            if (silent) {
                String silentMsg = plugin.getConfigManager().getString("mute.messages.silent-unmuted",
                        "§8[Тихо] §a%player% §7размучен (%operator%)");
                silentMsg = plugin.getMuteManager().formatMessage(silentMsg, finalTargetName, operatorName, null, null);

                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (p.hasPermission("lochat.mute.see-silent")) {
                        p.sendMessage(ChatFormatter.parse(silentMsg));
                    }
                }
            } else {
                String broadcastMsg = plugin.getConfigManager().getString("mute.messages.unmuted",
                        "§a%player% §7был размучен модератором §a%operator%");
                broadcastMsg = plugin.getMuteManager().formatMessage(broadcastMsg, finalTargetName, operatorName, null, null);
                Bukkit.broadcast(ChatFormatter.parse(broadcastMsg));
            }
        } else {
            sender.sendMessage("§cИгрок не замучен!");
        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                                 @NotNull String alias, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            String prefix = args[0].toLowerCase();
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.getName().toLowerCase().startsWith(prefix)) {
                    completions.add(player.getName());
                }
            }
        } else if (args.length == 2) {
            completions.add("-s");
        }

        return completions;
    }
}
