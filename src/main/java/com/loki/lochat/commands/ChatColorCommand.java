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
import java.util.List;
import java.util.regex.Pattern;

public class ChatColorCommand implements CommandExecutor, TabCompleter {

    private final LoChat plugin;
    private static final Pattern LEGACY_COLOR_PATTERN = Pattern.compile("^&([0-9a-fk-orA-FK-OR])$");

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
            player.sendMessage(ChatFormatter.parse("<gray>Используйте Minecraft color codes: &a, &b, &c, &d, &e, &f, &0-&9, &k, &l, &m, &n, &o, &r</gray>"));
            player.sendMessage(ChatFormatter.parse("<gray>Пример: /chatcolor &b</gray>"));
            return true;
        }

        String color = args[0];
        
        // Проверяем валидность цвета (формат &X где X - это цветовой код)
        if (!isValidColor(color)) {
            player.sendMessage(ChatFormatter.parse("<red>Неверный формат цвета! Используйте формат &X, например: &b, &a, &c</red>"));
            return true;
        }

        // Сохраняем цвет чата игрока
        plugin.getChatColorManager().setChatColor(player.getUniqueId(), color);
        
        // Обновляем display name игрока
        player.getScheduler().run(plugin, task -> {
            if (plugin.getDisplayNameListener() != null) {
                plugin.getDisplayNameListener().updatePlayerDisplayName(player);
            }
        }, null);
        
        // Показываем пример с примененным цветом
        String preview = ChatFormatter.convertAllColors(color + "Пример текста");
        player.sendMessage(ChatFormatter.parse("<green>Цвет чата установлен! Пример: " + preview));

        return true;
    }

    private boolean isValidColor(String color) {
        if (color == null || color.length() != 2) {
            return false;
        }
        if (!color.startsWith("&")) {
            return false;
        }
        char code = color.charAt(1);
        return (code >= '0' && code <= '9') || 
               (code >= 'a' && code <= 'f') || 
               (code >= 'A' && code <= 'F') ||
               code == 'k' || code == 'K' ||
               code == 'l' || code == 'L' ||
               code == 'm' || code == 'M' ||
               code == 'n' || code == 'N' ||
               code == 'o' || code == 'O' ||
               code == 'r' || code == 'R';
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        // Убираем автодополнение, так как теперь пользователь сам вводит цвет
        return new ArrayList<>();
    }
}