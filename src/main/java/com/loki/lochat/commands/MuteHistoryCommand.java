package com.loki.lochat.commands;

import com.loki.lochat.LoChat;
import com.loki.lochat.managers.MuteManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Команда /lmutehistory [ник] - история мутов игрока
 */
public class MuteHistoryCommand implements CommandExecutor, TabCompleter {

    private final LoChat plugin;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm");

    public MuteHistoryCommand(LoChat plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("lochat.mutehistory")) {
            sender.sendMessage(plugin.getMessageConfig().getComponent("errors.no-permission"));
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage("§cИспользование: /lmutehistory <ник>");
            return true;
        }

        String targetName = args[0];
        UUID targetUUID = null;

        // Ищем игрока
        Player target = Bukkit.getPlayer(targetName);
        if (target != null) {
            targetUUID = target.getUniqueId();
        } else {
            // Пробуем найти оффлайн игрока
            @SuppressWarnings("deprecation")
            var offlinePlayer = Bukkit.getOfflinePlayer(targetName);
            if (offlinePlayer.hasPlayedBefore() || offlinePlayer.isOnline()) {
                targetUUID = offlinePlayer.getUniqueId();
            } else {
                // Пробуем найти по имени в истории
                targetUUID = plugin.getMuteManager().getUUIDByName(targetName);
            }
        }

        if (targetUUID == null) {
            sender.sendMessage(plugin.getMessageConfig().getComponent("errors.player-not-found"));
            return true;
        }

        List<MuteManager.MuteHistoryEntry> history = plugin.getMuteManager().getPlayerHistory(targetUUID);

        if (history.isEmpty()) {
            sender.sendMessage("§aУ игрока §e" + targetName + " §aнет истории мутов");
            return true;
        }

        sender.sendMessage("§6§l=== История мутов " + targetName + " (" + history.size() + ") ===");

        int index = 1;
        // Выводим от новых к старым
        for (int i = history.size() - 1; i >= 0; i--) {
            MuteManager.MuteHistoryEntry entry = history.get(i);
            
            // Статус
            String status;
            if (entry.unmuted) {
                status = "§aРазмучен";
            } else {
                // Проверяем, активен ли ещё мут
                if (entry.isPermanent()) {
                    status = "§cАктивен (навсегда)";
                } else {
                    long endTime = entry.mutedAt + entry.duration;
                    if (System.currentTimeMillis() > endTime) {
                        status = "§7Истёк";
                    } else {
                        long remaining = endTime - System.currentTimeMillis();
                        status = "§cАктивен (" + plugin.getMuteManager().formatTime(remaining) + ")";
                    }
                }
            }

            // Длительность
            String durationStr = entry.isPermanent() ? "Навсегда" : plugin.getMuteManager().formatTime(entry.duration);

            sender.sendMessage("§7" + index + ". §f" + dateFormat.format(new Date(entry.mutedAt)) + " §7- " + status);
            sender.sendMessage("   §7Длительность: §f" + durationStr);
            sender.sendMessage("   §7Причина: §f" + (entry.reason != null ? entry.reason : "Не указана"));
            sender.sendMessage("   §7Выдал: §f" + entry.mutedBy);
            
            if (entry.unmuted && entry.unmutedBy != null) {
                sender.sendMessage("   §7Размутил: §f" + entry.unmutedBy + " §7(" + dateFormat.format(new Date(entry.unmutedAt)) + ")");
            }

            index++;
            
            // Ограничиваем вывод
            if (index > 10) {
                sender.sendMessage("§7... и ещё " + (history.size() - 10) + " записей");
                break;
            }
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
        }

        return completions;
    }
}
