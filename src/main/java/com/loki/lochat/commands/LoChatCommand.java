package com.loki.lochat.commands;

import com.loki.lochat.LoChat;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class LoChatCommand implements CommandExecutor, TabCompleter {

    private final LoChat plugin;

    public LoChatCommand(LoChat plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            sender.sendMessage("§e=== LoChat v" + plugin.getDescription().getVersion() + " ===");
            sender.sendMessage("§e/lochat reload §7— перезагрузить конфиги");
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("lochat.admin")) {
                sender.sendMessage(plugin.getMessageConfig().get("errors.no-permission"));
                return true;
            }
            
            plugin.reload();
            sender.sendMessage("§aLoChat перезагружен!");
            return true;
        }

        sender.sendMessage("§cНеизвестная команда. Используй /lochat reload");
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                                 @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1 && sender.hasPermission("lochat.admin")) {
            return List.of("reload").stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .toList();
        }
        return List.of();
    }
}
