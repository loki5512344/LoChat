package com.loki.lochat.commands;

import com.loki.lochat.LoChat;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class LoChatCommand implements CommandExecutor, TabCompleter {

    private static final MiniMessage MM = MiniMessage.miniMessage();
    private final LoChat plugin;

    public LoChatCommand(LoChat plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("lochat.admin")) {
            sender.sendMessage(MM.deserialize(plugin.getMessageConfig().getNoPermission()));
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(MM.deserialize("<#FFD700>LoChat v" + plugin.getPluginMeta().getVersion()));
            sender.sendMessage(MM.deserialize("<#808080>Команды:"));
            sender.sendMessage(MM.deserialize("<#FFFF00>/lochat reload</color> <#808080>- Перезагрузить конфиги"));
            sender.sendMessage(MM.deserialize("<#FFFF00>/lochat commands</color> <#808080>- Список кастомных команд"));
            sender.sendMessage(MM.deserialize("<#FFFF00>/lochat commands reload</color> <#808080>- Перезагрузить кастомные команды"));
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload" -> {
                plugin.reload();
                sender.sendMessage(MM.deserialize("<#00FF00>Конфигурация перезагружена!"));
            }
            case "commands" -> {
                if (args.length > 1 && args[1].equalsIgnoreCase("reload")) {
                    plugin.getCustomCommandManager().reload();
                    sender.sendMessage(MM.deserialize("<#00FF00>Кастомные команды перезагружены!"));
                } else {
                    var commands = plugin.getCustomCommandManager().getCommands();
                    sender.sendMessage(MM.deserialize("<#FFD700>Кастомные команды (" + commands.size() + "):"));
                    for (var cmd : commands.values()) {
                        sender.sendMessage(MM.deserialize("<#FFEB3B>/" + cmd.name() + "</color> <#808080>(" + cmd.type() + ")"));
                        if (!cmd.aliases().isEmpty()) {
                            sender.sendMessage(MM.deserialize("<#808080>  Алиасы: " + String.join(", ", cmd.aliases())));
                        }
                    }
                }
            }
            default ->
                    sender.sendMessage(MM.deserialize("<#FF0000>Неизвестная команда. Используйте /lochat для помощи."));
        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                                @NotNull String alias, @NotNull String[] args) {
        if (!sender.hasPermission("lochat.admin")) {
            return new ArrayList<>();
        }

        if (args.length == 1) {
            List<String> completions = new ArrayList<>();
            String input = args[0].toLowerCase();

            for (String cmd : List.of("reload", "commands")) {
                if (cmd.startsWith(input)) {
                    completions.add(cmd);
                }
            }
            return completions;
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("commands")) {
            List<String> completions = new ArrayList<>();
            String input = args[1].toLowerCase();

            if ("reload".startsWith(input)) {
                completions.add("reload");
            }
            return completions;
        }

        return new ArrayList<>();
    }
}