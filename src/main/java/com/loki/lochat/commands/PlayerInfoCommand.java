package com.loki.lochat.commands;

import com.loki.lochat.LoChat;
import com.loki.lochat.utils.ChatFormatter;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Команда для показа информации об игроке (используется при клике на голову)
 */
public class PlayerInfoCommand implements CommandExecutor {

    private final LoChat plugin;

    public PlayerInfoCommand(LoChat plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length < 2 || !args[0].equals("playerinfo")) {
            return false;
        }

        String targetName = args[1];
        Player target = Bukkit.getPlayer(targetName);

        if (target == null || !target.isOnline()) {
            sender.sendMessage(ChatFormatter.parse("§cИгрок не найден или не в сети"));
            return true;
        }

        // Показываем информацию об игроке
        sender.sendMessage(ChatFormatter.parse("§6=== Информация об игроке ==="));
        sender.sendMessage(ChatFormatter.parse("§eИмя: §f" + target.getName()));
        sender.sendMessage(ChatFormatter.parse("§eОтображаемое имя: §f" + target.getDisplayName()));
        sender.sendMessage(ChatFormatter.parse("§eУровень: §f" + target.getLevel()));
        sender.sendMessage(ChatFormatter.parse("§eЗдоровье: §f" + Math.round(target.getHealth()) + "/20"));
        sender.sendMessage(ChatFormatter.parse("§eМир: §f" + target.getWorld().getName()));
        
        // Если есть градиентный модуль, показываем градиентное имя
        if (plugin.getGradientModule() != null) {
            String gradientName = plugin.getGradientModule().getFormattedName(target);
            sender.sendMessage(ChatFormatter.parse("§eГрадиентное имя: " + gradientName));
        }

        return true;
    }
}