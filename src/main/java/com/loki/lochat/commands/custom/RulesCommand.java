package com.loki.lochat.commands.custom;

import com.loki.lochat.LoChat;
import com.loki.lochat.utils.ChatFormatter;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

/**
 * Команда правил сервера
 */
public class RulesCommand implements CommandExecutor {

    private final LoChat plugin;

    public RulesCommand(LoChat plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        String message = """
            <gradient:#FF6B6B:#FF4757>═══════ Правила сервера ═══════</gradient>
            <white>1.</white> <gray>Не используйте читы и моды</gray>
            <white>2.</white> <gray>Уважайте других игроков</gray>
            <white>3.</white> <gray>Не спамьте в чате</gray>
            <white>4.</white> <gray>Не стройте рядом с чужими постройками</gray>
            <white>5.</white> <gray>Не используйте мат и оскорбления</gray>
            <white>6.</white> <gray>Не рекламируйте другие серверы</gray>
            <white>7.</white> <gray>Слушайтесь администрацию</gray>
            <white>8.</white> <gray>Не злоупотребляйте багами</gray>
            
            <gradient:#FFA726:#FF9800>Нарушение правил карается мутом или баном!</gradient>
            <gradient:#87CEEB:#4682B4>За помощью обращайтесь к администрации</gradient>
            """;

        sender.sendMessage(ChatFormatter.parse(message));
        return true;
    }
}