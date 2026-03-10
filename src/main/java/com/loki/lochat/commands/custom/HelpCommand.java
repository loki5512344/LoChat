package com.loki.lochat.commands.custom;

import com.loki.lochat.LoChat;
import com.loki.lochat.utils.ChatFormatter;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Команда помощи сервера
 */
public class HelpCommand implements CommandExecutor {

    private final LoChat plugin;

    public HelpCommand(LoChat plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        String message = """
            <gradient:#FFD700:#FFA500>═══════ Помощь по серверу ═══════</gradient>
            <gradient:#87CEEB:#4682B4>/rules</gradient> <gray>- правила сервера</gray>
            <gradient:#87CEEB:#4682B4>/discord</gradient> <gray>- наш Discord</gray>
            <gradient:#87CEEB:#4682B4>/website</gradient> <gray>- официальный сайт</gray>
            <gradient:#87CEEB:#4682B4>/coords</gradient> <gray>- ваши координаты</gray>
            <gradient:#87CEEB:#4682B4>/time</gradient> <gray>- время на сервере</gray>
            <gradient:#87CEEB:#4682B4>/ping</gradient> <gray>- ваш пинг</gray>
            <gradient:#87CEEB:#4682B4>/list</gradient> <gray>- список игроков</gray>
            <gradient:#87CEEB:#4682B4>/me <действие></gradient> <gray>- действие от третьего лица</gray>
            <gradient:#87CEEB:#4682B4>/msg <игрок> <сообщение></gradient> <gray>- личное сообщение</gray>
            <gradient:#87CEEB:#4682B4>/reply <сообщение></gradient> <gray>- ответить на ЛС</gray>
            <gradient:#87CEEB:#4682B4>/ignore <игрок></gradient> <gray>- игнорировать игрока</gray>
            <gradient:#87CEEB:#4682B4>/color <цвета></gradient> <gray>- градиентный ник</gray>
            <gradient:#87CEEB:#4682B4>/prefix <текст></gradient> <gray>- кастомный префикс</gray>
            """;

        sender.sendMessage(ChatFormatter.parse(message));
        return true;
    }
}