package com.loki.lochat.commands;

import com.loki.lochat.LoChat;
import com.loki.lochat.api.service.MuteService;
import com.loki.lochat.data.model.MuteData;
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

/**
 * Команда /lmuteblame <модератор> - история мутов выданных модератором
 */
public class MuteBlameCommand implements CommandExecutor, TabCompleter {

    private final LoChat plugin;
    private final MuteService muteService;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm");

    public MuteBlameCommand(LoChat plugin) {
        this.plugin = plugin;
        this.muteService = plugin.getServiceRegistry().get(MuteService.class);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("lochat.muteblame")) {
            sender.sendMessage(plugin.getMessageConfig().getComponent("errors.no-permission"));
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage("§cИспользование: /lmuteblame <модератор>");
            return true;
        }

        String operatorName = args[0];

        List<MuteData.MuteHistoryEntry> mutes = muteService.getMutesByOperator(operatorName);

        if (mutes.isEmpty()) {
            sender.sendMessage("§aМодератор §e" + operatorName + " §aне выдавал мутов");
            return true;
        }

        sender.sendMessage("§6§l=== Муты выданные " + operatorName + " (" + mutes.size() + ") ===");

        int index = 1;
        for (MuteData.MuteHistoryEntry entry : mutes) {
            // Статус
            String status;
            if (entry.unmuted) {
                status = "§aРазмучен";
            } else {
                if (entry.duration == 0) {
                    status = "§cАктивен (навсегда)";
                } else {
                    long endTime = entry.mutedAt + entry.duration;
                    if (System.currentTimeMillis() > endTime) {
                        status = "§7Истёк";
                    } else {
                        long remaining = endTime - System.currentTimeMillis();
                        status = "§cАктивен (" + muteService.formatTime(remaining) + ")";
                    }
                }
            }

            String durationStr = entry.duration == 0 ? "Навсегда" : muteService.formatTime(entry.duration);

            String playerName = entry.playerName != null ? entry.playerName : "???";

            sender.sendMessage("§7" + index + ". §f" + playerName + " §7- " + status);
            sender.sendMessage("   §7Дата: §f" + dateFormat.format(new Date(entry.mutedAt)));
            sender.sendMessage("   §7Длительность: §f" + durationStr);
            sender.sendMessage("   §7Причина: §f" + (entry.reason != null ? entry.reason : "Не указана"));

            if (entry.unmuted && entry.unmutedBy != null) {
                sender.sendMessage("   §7Размутил: §f" + entry.unmutedBy);
            }

            index++;

            if (index > 15) {
                sender.sendMessage("§7... и ещё " + (mutes.size() - 15) + " записей");
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
