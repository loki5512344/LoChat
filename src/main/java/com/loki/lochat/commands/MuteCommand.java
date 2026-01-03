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
 * Команда /lmute nick time [-s] причина
 */
public class MuteCommand implements CommandExecutor, TabCompleter {

    private final LoChat plugin;

    public MuteCommand(LoChat plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("lochat.mute")) {
            sender.sendMessage(plugin.getMessageConfig().getComponent("errors.no-permission"));
            return true;
        }

        // /lmute nick time [-s] причина
        if (args.length < 2) {
            sender.sendMessage("§cИспользование: /lmute <ник> <время> [-s] [причина]");
            sender.sendMessage("§7Время: 1d, 2h, 30m, 60s или 0 для перманентного");
            return true;
        }

        String targetName = args[0];
        String timeStr = args[1];
        
        // Парсим время
        long duration = plugin.getMuteManager().parseTime(timeStr);
        if (duration < 0) {
            sender.sendMessage("§cНеверный формат времени! Используйте: 1d, 2h, 30m, 60s");
            return true;
        }

        // Проверяем флаг -s (silent) и собираем причину
        boolean silent = false;
        StringBuilder reasonBuilder = new StringBuilder();
        
        for (int i = 2; i < args.length; i++) {
            if (args[i].equalsIgnoreCase("-s")) {
                silent = true;
            } else {
                if (reasonBuilder.length() > 0) reasonBuilder.append(" ");
                reasonBuilder.append(args[i]);
            }
        }
        
        String reason = reasonBuilder.length() > 0 ? reasonBuilder.toString() : "Не указана";

        // Ищем игрока (онлайн или оффлайн)
        Player target = Bukkit.getPlayer(targetName);
        if (target == null) {
            // Пробуем найти оффлайн игрока
            @SuppressWarnings("deprecation")
            var offlinePlayer = Bukkit.getOfflinePlayer(targetName);
            if (!offlinePlayer.hasPlayedBefore() && !offlinePlayer.isOnline()) {
                sender.sendMessage(plugin.getMessageConfig().getComponent("errors.player-not-found"));
                return true;
            }
            
            // Мутим оффлайн игрока
            String offlineName = offlinePlayer.getName() != null ? offlinePlayer.getName() : targetName;
            plugin.getMuteManager().mute(offlinePlayer.getUniqueId(), offlineName, duration, reason, sender.getName());
            
            String timeDisplay = duration == 0 ? "навсегда" : plugin.getMuteManager().formatTime(duration);
            sender.sendMessage("§aИгрок §e" + targetName + " §aзамучен на §e" + timeDisplay);
            sender.sendMessage("§7Причина: §f" + reason);
            
            if (!silent) {
                Bukkit.broadcast(plugin.getMessageConfig().getComponent("mute.muted", 
                        "{player}", targetName, "{time}", timeDisplay));
            }
            return true;
        }

        // Проверяем, не пытается ли замутить себя
        if (sender instanceof Player && ((Player) sender).getUniqueId().equals(target.getUniqueId())) {
            sender.sendMessage("§cВы не можете замутить себя!");
            return true;
        }

        // Проверяем, не замучен ли уже
        if (plugin.getMuteManager().isMuted(target.getUniqueId())) {
            sender.sendMessage("§cИгрок уже замучен!");
            return true;
        }

        // Мутим игрока
        plugin.getMuteManager().mute(target.getUniqueId(), target.getName(), duration, reason, sender.getName());

        String timeDisplay = duration == 0 ? "навсегда" : plugin.getMuteManager().formatTime(duration);
        
        // Уведомляем отправителя
        sender.sendMessage("§aИгрок §e" + target.getName() + " §aзамучен на §e" + timeDisplay);
        sender.sendMessage("§7Причина: §f" + reason);

        // Уведомляем замученного
        if (duration == 0) {
            target.sendMessage(plugin.getMessageConfig().getComponent("mute.permanent"));
        } else {
            target.sendMessage(plugin.getMessageConfig().getComponent("mute.you-muted", 
                    "{time}", timeDisplay));
        }

        // Broadcast если не silent
        if (!silent) {
            Bukkit.broadcast(plugin.getMessageConfig().getComponent("mute.muted", 
                    "{player}", target.getName(), "{time}", timeDisplay));
        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                                 @NotNull String alias, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            // Ники игроков
            String prefix = args[0].toLowerCase();
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.getName().toLowerCase().startsWith(prefix)) {
                    completions.add(player.getName());
                }
            }
        } else if (args.length == 2) {
            // Время
            completions.add("10m");
            completions.add("30m");
            completions.add("1h");
            completions.add("6h");
            completions.add("12h");
            completions.add("1d");
            completions.add("7d");
            completions.add("30d");
            completions.add("0");
        } else if (args.length == 3) {
            completions.add("-s");
        }
        
        return completions;
    }
}
