package com.loki.lochat.commands;

import com.loki.lochat.LoChat;
import com.loki.lochat.utils.ChatFormatter;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class LoChatCommand implements CommandExecutor, TabCompleter {

    private final LoChat plugin;

    public LoChatCommand(LoChat plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("lochat.admin")) {
            sender.sendMessage(ChatFormatter.parse(plugin.getMessageConfig().getNoPermission()));
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(ChatFormatter.parse("<gold>LoChat v" + plugin.getDescription().getVersion() + "</gold>"));
            sender.sendMessage(ChatFormatter.parse("&#808080Команды:"));
            sender.sendMessage(ChatFormatter.parse("<yellow>/lochat reload</yellow> - Перезагрузить конфиги"));
            sender.sendMessage(ChatFormatter.parse("<yellow>/lochat commands</yellow> - Список кастомных команд"));
            sender.sendMessage(ChatFormatter.parse("<yellow>/lochat commands reload</yellow> - Перезагрузить кастомные команды"));
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload" -> {
                plugin.reload();
                sender.sendMessage(ChatFormatter.parse("<green>Конфигурация перезагружена!</green>"));
            }
            case "commands" -> {
                if (args.length > 1 && args[1].equalsIgnoreCase("reload")) {
                    plugin.getCustomCommandManager().reload();
                    sender.sendMessage(ChatFormatter.parse("<green>Кастомные команды перезагружены!</green>"));
                } else {
                    var commands = plugin.getCustomCommandManager().getCommands();
                    sender.sendMessage(ChatFormatter.parse("<gold>Кастомные команды (" + commands.size() + "):</gold>"));
                    for (var cmd : commands.values()) {
                        sender.sendMessage(ChatFormatter.parse("&e/" + cmd.name + " &#808080(" + cmd.type + ")"));
                        if (!cmd.aliases.isEmpty()) {
                            sender.sendMessage(ChatFormatter.parse("&#808080  Алиасы: " + String.join(", ", cmd.aliases)));
                        }
                    }
                }
            }
            default -> sender.sendMessage(ChatFormatter.parse("<red>Неизвестная команда. Используйте /lochat для помощи.</red>"));
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
