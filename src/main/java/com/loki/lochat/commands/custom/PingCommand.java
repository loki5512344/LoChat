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
 * Команда пинга игрока
 */
public class PingCommand implements CommandExecutor {

    private final LoChat plugin;

    public PingCommand(LoChat plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatFormatter.parse("<red>Эта команда только для игроков!</red>"));
            return true;
        }

        // Если указан аргумент - показать пинг другого игрока
        if (args.length > 0) {
            if (!player.hasPermission("lochat.ping.others")) {
                player.sendMessage(ChatFormatter.parse("<red>У вас нет прав для просмотра пинга других игроков!</red>"));
                return true;
            }

            Player target = Bukkit.getPlayer(args[0]);
            if (target == null || !target.isOnline()) {
                player.sendMessage(ChatFormatter.parse("<red>Игрок не найден или не в сети!</red>"));
                return true;
            }

            int ping = target.getPing();
            String color = getPingColor(ping);
            
            String message = String.format(
                "<gradient:#87CEEB:#4682B4>Пинг игрока %s:</gradient> <%s>%dмс</%s>",
                target.getName(), color, ping, color
            );
            
            player.sendMessage(ChatFormatter.parse(message));
        } else {
            // Показать свой пинг
            int ping = player.getPing();
            String color = getPingColor(ping);
            
            String message = String.format(
                "<gradient:#87CEEB:#4682B4>Ваш пинг:</gradient> <%s>%dмс</%s>",
                color, ping, color
            );
            
            player.sendMessage(ChatFormatter.parse(message));
        }

        return true;
    }

    private String getPingColor(int ping) {
        if (ping < 50) return "green";
        if (ping < 100) return "yellow";
        if (ping < 200) return "gold";
        return "red";
    }
}