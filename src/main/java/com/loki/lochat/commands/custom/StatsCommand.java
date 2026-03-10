package com.loki.lochat.commands.custom;

import com.loki.lochat.LoChat;
import com.loki.lochat.utils.ChatFormatter;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Команда статистики чата
 */
public class StatsCommand implements CommandExecutor {

    private final LoChat plugin;

    public StatsCommand(LoChat plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("lochat.stats")) {
            sender.sendMessage(ChatFormatter.parse("<red>У вас нет прав для просмотра статистики!</red>"));
            return true;
        }

        if (args.length > 0) {
            // Статистика конкретного игрока
            Player target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                sender.sendMessage(ChatFormatter.parse("<red>Игрок не найден!</red>"));
                return true;
            }

            showPlayerStats(sender, target);
        } else {
            // Общая статистика сервера
            showServerStats(sender);
        }

        return true;
    }

    private void showPlayerStats(CommandSender sender, Player target) {
        // Получаем статистику игрока из конфига или базы данных
        int messagesSent = plugin.getPlayerData().getInt("players." + target.getUniqueId() + ".messages-sent", 0);
        int commandsUsed = plugin.getPlayerData().getInt("players." + target.getUniqueId() + ".commands-used", 0);
        long playtime = plugin.getPlayerData().getLong("players." + target.getUniqueId() + ".total-playtime", 0);
        
        String playtimeFormatted = formatTime(playtime);

        String message = String.format("""
            <gradient:#FFD700:#FFA500>═══ Статистика игрока %s ═══</gradient>
            <gradient:#87CEEB:#4682B4>Сообщений отправлено:</gradient> <white>%d</white>
            <gradient:#87CEEB:#4682B4>Команд использовано:</gradient> <white>%d</white>
            <gradient:#87CEEB:#4682B4>Время в игре:</gradient> <white>%s</white>
            <gradient:#87CEEB:#4682B4>Статус:</gradient> <green>В сети</green>
            <gradient:#87CEEB:#4682B4>Пинг:</gradient> <yellow>%dмс</yellow>
            """, target.getName(), messagesSent, commandsUsed, playtimeFormatted, target.getPing());

        sender.sendMessage(ChatFormatter.parse(message));
    }

    private void showServerStats(CommandSender sender) {
        // Получаем общую статистику сервера
        int totalMessages = plugin.getStatisticsData().getInt("global.total-messages", 0);
        int messagesToday = plugin.getStatisticsData().getInt("global.messages-today", 0);
        int onlinePlayers = Bukkit.getOnlinePlayers().size();
        int maxPlayers = Bukkit.getMaxPlayers();

        String message = String.format("""
            <gradient:#FFD700:#FFA500>═══ Статистика сервера ═══</gradient>
            <gradient:#87CEEB:#4682B4>Всего сообщений:</gradient> <white>%d</white>
            <gradient:#87CEEB:#4682B4>Сообщений сегодня:</gradient> <white>%d</white>
            <gradient:#87CEEB:#4682B4>Игроков онлайн:</gradient> <white>%d/%d</white>
            <gradient:#87CEEB:#4682B4>Активных мутов:</gradient> <white>%d</white>
            <gradient:#87CEEB:#4682B4>TPS:</gradient> <green>%.1f</green>
            """, totalMessages, messagesToday, onlinePlayers, maxPlayers, 
            getActiveMutesCount(), getCurrentTPS());

        sender.sendMessage(ChatFormatter.parse(message));
    }

    private String formatTime(long milliseconds) {
        long seconds = milliseconds / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        if (days > 0) {
            return String.format("%dд %dч %dм", days, hours % 24, minutes % 60);
        } else if (hours > 0) {
            return String.format("%dч %dм", hours, minutes % 60);
        } else {
            return String.format("%dм", minutes);
        }
    }

    private int getActiveMutesCount() {
        return plugin.getMuteData().getConfigurationSection("active-mutes") != null ?
            plugin.getMuteData().getConfigurationSection("active-mutes").getKeys(false).size() : 0;
    }

    private double getCurrentTPS() {
        try {
            return Bukkit.getTPS()[0];
        } catch (Exception e) {
            return 20.0;
        }
    }
}