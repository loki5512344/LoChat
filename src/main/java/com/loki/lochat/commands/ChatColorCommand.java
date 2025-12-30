package com.loki.lochat.commands;

import com.loki.lochat.LoChat;
import com.loki.lochat.utils.ChatFormatter;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ChatColorCommand implements CommandExecutor, TabCompleter {

    private final LoChat plugin;

    public ChatColorCommand(LoChat plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Только для игроков!");
            return true;
        }

        if (!player.hasPermission("lochat.chatcolor")) {
            player.sendMessage(ChatFormatter.parse(plugin.getMessageConfig().getNoPermission()));
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(ChatFormatter.parse(plugin.getMessageConfig().getInvalidUsage("/chatcolor <цвет>")));
            player.sendMessage(ChatFormatter.parse("<gray>Доступные цвета: <red>red</red>, <blue>blue</blue>, <green>green</green>, <yellow>yellow</yellow>, <gold>gold</gold>, <aqua>aqua</aqua>, <light_purple>light_purple</light_purple>, <white>white</white>, <gray>gray</gray>, <dark_gray>dark_gray</dark_gray>"));
            return true;
        }

        String color = args[0].toLowerCase();
        
        // Проверяем валидность цвета
        if (!isValidColor(color)) {
            player.sendMessage(ChatFormatter.parse("<red>Неверный цвет! Используйте один из доступных цветов.</red>"));
            return true;
        }

        // Сохраняем цвет чата игрока
        plugin.getChatColorManager().setChatColor(player.getUniqueId(), color);
        player.sendMessage(ChatFormatter.parse("<green>Цвет чата установлен на <" + color + ">" + color + "</color>!"));

        return true;
    }

    private boolean isValidColor(String color) {
        List<String> validColors = Arrays.asList(
            "red", "blue", "green", "yellow", "gold", "aqua", 
            "light_purple", "white", "gray", "dark_gray", "black",
            "dark_red", "dark_green", "dark_blue", "dark_aqua", "dark_purple"
        );
        return validColors.contains(color);
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            List<String> colors = Arrays.asList(
                "red", "blue", "green", "yellow", "gold", "aqua", 
                "light_purple", "white", "gray", "dark_gray"
            );
            List<String> completions = new ArrayList<>();
            String input = args[0].toLowerCase();
            
            for (String color : colors) {
                if (color.startsWith(input)) {
                    completions.add(color);
                }
            }
            return completions;
        }
        return new ArrayList<>();
    }
}