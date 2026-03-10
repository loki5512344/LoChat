package com.loki.lochat.commands.custom;

import com.loki.lochat.LoChat;
import com.loki.lochat.utils.ChatFormatter;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Команда управления звуками
 */
public class SoundsCommand implements CommandExecutor {

    private final LoChat plugin;

    public SoundsCommand(LoChat plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatFormatter.parse("<red>Эта команда только для игроков!</red>"));
            return true;
        }

        String playerUUID = player.getUniqueId().toString();
        boolean currentState = plugin.getPlayerData().getBoolean("players." + playerUUID + ".settings.sounds-enabled", true);

        if (args.length == 0) {
            // Показать текущее состояние
            String status = currentState ? "<green>включены</green>" : "<red>отключены</red>";
            String message = String.format(
                "<gradient:#87CEEB:#4682B4>Звуки сейчас %s</gradient>\n" +
                "<gray>Используйте /sounds on или /sounds off для изменения</gray>",
                status
            );
            player.sendMessage(ChatFormatter.parse(message));
            return true;
        }

        String action = args[0].toLowerCase();
        boolean newState;

        switch (action) {
            case "on", "включить", "вкл" -> {
                newState = true;
                player.sendMessage(ChatFormatter.parse("<green>Звуки включены!</green>"));
            }
            case "off", "отключить", "выкл" -> {
                newState = false;
                player.sendMessage(ChatFormatter.parse("<red>Звуки отключены!</red>"));
            }
            case "toggle", "переключить" -> {
                newState = !currentState;
                String status = newState ? "<green>включены</green>" : "<red>отключены</red>";
                player.sendMessage(ChatFormatter.parse(String.format("Звуки %s!", status)));
            }
            default -> {
                player.sendMessage(ChatFormatter.parse("""
                    <red>Неверное использование!</red>
                    <gray>Доступные команды:</gray>
                    <white>/sounds on</white> <gray>- включить звуки</gray>
                    <white>/sounds off</white> <gray>- отключить звуки</gray>
                    <white>/sounds toggle</white> <gray>- переключить</gray>
                    """));
                return true;
            }
        }

        // Сохраняем настройку
        plugin.getPlayerData().set("players." + playerUUID + ".settings.sounds-enabled", newState);
        plugin.savePlayerData();

        return true;
    }
}