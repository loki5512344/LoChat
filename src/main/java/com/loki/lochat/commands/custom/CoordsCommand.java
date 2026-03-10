package com.loki.lochat.commands.custom;

import com.loki.lochat.LoChat;
import com.loki.lochat.utils.ChatFormatter;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Команда координат игрока
 */
public class CoordsCommand implements CommandExecutor {

    private final LoChat plugin;

    public CoordsCommand(LoChat plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatFormatter.parse("<red>Эта команда только для игроков!</red>"));
            return true;
        }

        int x = (int) player.getLocation().getX();
        int y = (int) player.getLocation().getY();
        int z = (int) player.getLocation().getZ();
        String world = player.getWorld().getName();

        String message = String.format("""
            <gradient:#FFA726:#FF9800>Ваши координаты:</gradient>
            <gradient:#87CEEB:#4682B4>X:</gradient> <white>%d</white>
            <gradient:#87CEEB:#4682B4>Y:</gradient> <white>%d</white>
            <gradient:#87CEEB:#4682B4>Z:</gradient> <white>%d</white>
            <gradient:#87CEEB:#4682B4>Мир:</gradient> <white>%s</white>
            """, x, y, z, world);

        player.sendMessage(ChatFormatter.parse(message));
        return true;
    }
}