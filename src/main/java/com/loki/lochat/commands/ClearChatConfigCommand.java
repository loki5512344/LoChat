package com.loki.lochat.commands;

import com.loki.lochat.LoChat;
import com.loki.lochat.utils.ChatFormatter;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

/**
 * Команда для настройки сообщения очистки чата
 * /clearchatconfig <enable|disable|message> [текст]
 */
public class ClearChatConfigCommand implements CommandExecutor, TabCompleter {

    private final LoChat plugin;

    public ClearChatConfigCommand(LoChat plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("lochat.clearchat.config")) {
            sender.sendMessage(plugin.getMessageConfig().getComponent("errors.no-permission"));
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(ChatFormatter.parse("&6Использование:"));
            sender.sendMessage(ChatFormatter.parse("&e/clearchatconfig enable &7- включить сообщение"));
            sender.sendMessage(ChatFormatter.parse("&e/clearchatconfig disable &7- отключить сообщение"));
            sender.sendMessage(ChatFormatter.parse("&e/clearchatconfig message <текст> &7- установить сообщение"));
            sender.sendMessage(ChatFormatter.parse("&e/clearchatconfig status &7- показать статус"));
            return true;
        }

        String action = args[0].toLowerCase();

        switch (action) {
            case "enable" -> {
                plugin.getConfigManager().setClearChatMessageEnabled(true);
                sender.sendMessage(ChatFormatter.parse("&aСообщение очистки чата включено"));
            }
            case "disable" -> {
                plugin.getConfigManager().setClearChatMessageEnabled(false);
                sender.sendMessage(ChatFormatter.parse("&cСообщение очистки чата отключено"));
            }
            case "message" -> {
                if (args.length < 2) {
                    sender.sendMessage(ChatFormatter.parse("&cУкажите текст сообщения!"));
                    return true;
                }

                String message = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
                plugin.getConfigManager().setClearChatMessage(message);
                sender.sendMessage(ChatFormatter.parse("&aСообщение установлено: &f" + message));
            }
            case "status" -> {
                boolean enabled = plugin.getConfigManager().isClearChatMessageEnabled();
                String message = plugin.getConfigManager().getClearChatMessage();

                sender.sendMessage(ChatFormatter.parse("&6Статус сообщения очистки чата:"));
                sender.sendMessage(ChatFormatter.parse("&eВключено: " + (enabled ? "&aДа" : "&cНет")));
                sender.sendMessage(ChatFormatter.parse("&eСообщение: &f" + message));
            }
            default -> {
                sender.sendMessage(ChatFormatter.parse("&cНеизвестная команда! Используйте: enable, disable, message, status"));
            }
        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            return Arrays.asList("enable", "disable", "message", "status").stream()
                    .filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase()))
                    .toList();
        }
        return null;
    }
}