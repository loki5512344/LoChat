package com.loki.lochat.commands;

import com.loki.lochat.LoChat;
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
 * Команда /lunmute nick [-s]
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
            sender.sendMessage("§cИспользование: /lunmute <ник> [-s]");
            return true;
        }

        String targetName = args[0];
        boolean silent = args.length > 1 && args[1].equalsIgnoreCase("-s");

        // Ищем игрока
        Player target = Bukkit.getPlayer(targetName);
        if (target == null) {
            // Пробуем найти оффлайн игрока
            @SuppressWarnings("deprecation")
            var offlinePlayer = Bukkit.getOfflinePlayer(targetName);
            if (!offlinePlayer.hasPlayedBefore() && !offlinePlayer.isOnline()) {
                sender.sendMessage(plugin.getMessageConfig().getComponent("errors.player-not-found"));
                return true;
            }
            
            // Размучиваем оффлайн игрока
            if (plugin.getMuteManager().unmute(offlinePlayer.getUniqueId())) {
                sender.sendMessage("§aИгрок §e" + targetName + " §aразмучен");
                
                if (!silent) {
                    Bukkit.broadcast(plugin.getMessageConfig().getComponent("mute.unmuted", 
                            "{player}", targetName));
                }
            } else {
                sender.sendMessage("§cИгрок не замучен!");
            }
            return true;
        }

        // Размучиваем онлайн игрока
        if (plugin.getMuteManager().unmute(target.getUniqueId())) {
            sender.sendMessage("§aИгрок §e" + target.getName() + " §aразмучен");
            target.sendMessage("§aВы были размучены!");
            
            if (!silent) {
                Bukkit.broadcast(plugin.getMessageConfig().getComponent("mute.unmuted", 
                        "{player}", target.getName()));
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
