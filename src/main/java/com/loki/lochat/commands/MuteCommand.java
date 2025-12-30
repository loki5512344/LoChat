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
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class MuteCommand implements CommandExecutor, TabCompleter {

    private final LoChat plugin;

    public MuteCommand(LoChat plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("chat.mute")) {
            sender.sendMessage(ChatFormatter.parse(plugin.getMessageConfig().getNoPermission()));
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(ChatFormatter.parse(plugin.getMessageConfig().getInvalidUsage("/mute <игрок> <время> [причина]")));
            sender.sendMessage(ChatFormatter.parse("&#808080Время: 1m, 1h, 1d, permanent"));
            return true;
        }

        String playerName = args[0];
        String timeStr = args[1];
        String reason = args.length > 2 ? String.join(" ", Arrays.copyOfRange(args, 2, args.length)) : "Нарушение правил чата";

        Player target = Bukkit.getPlayer(playerName);
        if (target == null) {
            sender.sendMessage(ChatFormatter.parse(plugin.getMessageConfig().getPlayerNotFound()));
            return true;
        }

        // Парсим время
        long duration = parseTime(timeStr);
        if (duration == -1) {
            sender.sendMessage(ChatFormatter.parse("&#FF0000Неверный формат времени! Используйте: 1m, 1h, 1d, permanent"));
            return true;
        }

        // Мутим игрока
        plugin.getMuteManager().mutePlayer(target.getUniqueId(), duration, reason, sender.getName());

        // Уведомления
        String timeDisplay = duration == 0 ? "навсегда" : formatTime(duration);
        sender.sendMessage(ChatFormatter.parse(
            plugin.getMessageConfig().get("mute.muted", "{player}", target.getName(), "{time}", timeDisplay)
        ));

        if (target.isOnline()) {
            target.sendMessage(ChatFormatter.parse(
                plugin.getMessageConfig().get("mute.you-muted", "{time}", timeDisplay)
            ));
        }

        return true;
    }

    private long parseTime(String timeStr) {
        if (timeStr.equalsIgnoreCase("permanent") || timeStr.equalsIgnoreCase("perm")) {
            return 0; // 0 = permanent
        }

        try {
            char unit = timeStr.charAt(timeStr.length() - 1);
            int amount = Integer.parseInt(timeStr.substring(0, timeStr.length() - 1));

            return switch (unit) {
                case 'm' -> amount * 60L * 1000L; // минуты
                case 'h' -> amount * 60L * 60L * 1000L; // часы
                case 'd' -> amount * 24L * 60L * 60L * 1000L; // дни
                default -> -1;
            };
        } catch (NumberFormatException | StringIndexOutOfBoundsException e) {
            return -1;
        }
    }

    private String formatTime(long millis) {
        long seconds = millis / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        if (days > 0) return days + "д";
        if (hours > 0) return hours + "ч";
        if (minutes > 0) return minutes + "м";
        return seconds + "с";
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        } else if (args.length == 2) {
            List<String> times = Arrays.asList("1m", "5m", "10m", "30m", "1h", "2h", "6h", "12h", "1d", "3d", "7d", "permanent");
            return times.stream()
                    .filter(time -> time.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }
}