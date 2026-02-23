package com.loki.lochat.commands;

import com.loki.lochat.LoChat;
import com.loki.lochat.api.service.NickService;
import com.loki.lochat.utils.ChatFormatter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class NickCommand implements CommandExecutor, TabCompleter {
    private final LoChat plugin;
    private final NickService nickService;
    
    public NickCommand(LoChat plugin) {
        this.plugin = plugin;
        this.nickService = plugin.getServiceRegistry().get(NickService.class);
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Эта команда только для игроков!", NamedTextColor.RED));
            return true;
        }
        
        if (!player.hasPermission("lochat.nick")) {
            player.sendMessage(Component.text("У вас нет прав на использование этой команды!", NamedTextColor.RED));
            return true;
        }
        
        if (args.length == 0) {
            return showCurrentNick(player);
        }
        
        if (args[0].equalsIgnoreCase("reset")) {
            return resetNick(player);
        }
        
        String nickname = String.join(" ", args);
        return setNick(player, nickname);
    }
    
    private boolean showCurrentNick(Player player) {
        var currentNick = nickService.getNickname(player.getUniqueId());
        
        if (currentNick.isPresent()) {
            Component nickComponent = ChatFormatter.parse(currentNick.get());
            player.sendMessage(Component.text("Ваш текущий ник: ", NamedTextColor.GRAY)
                    .append(nickComponent));
            player.sendMessage(Component.text("Используйте ", NamedTextColor.GRAY)
                    .append(Component.text("/nick reset", NamedTextColor.YELLOW))
                    .append(Component.text(" для сброса", NamedTextColor.GRAY)));
        } else {
            player.sendMessage(Component.text("У вас нет кастомного ника", NamedTextColor.GRAY));
            player.sendMessage(Component.text("Используйте ", NamedTextColor.GRAY)
                    .append(Component.text("/nick <ник>", NamedTextColor.YELLOW))
                    .append(Component.text(" для установки", NamedTextColor.GRAY)));
        }
        
        return true;
    }
    
    private boolean resetNick(Player player) {
        var currentNick = nickService.getNickname(player.getUniqueId());
        
        if (currentNick.isEmpty()) {
            player.sendMessage(Component.text("У вас нет кастомного ника!", NamedTextColor.RED));
            return true;
        }
        
        nickService.resetNickname(player.getUniqueId());
        player.sendMessage(Component.text("Ваш ник сброшен!", NamedTextColor.GREEN));
        return true;
    }
    
    private boolean setNick(Player player, String nickname) {
        if (!nickService.isValidNickname(nickname)) {
            player.sendMessage(Component.text("Невалидный ник!", NamedTextColor.RED));
            player.sendMessage(Component.text("Требования:", NamedTextColor.GRAY));
            player.sendMessage(Component.text("• Длина: 3-16 символов", NamedTextColor.GRAY));
            player.sendMessage(Component.text("• Разрешены: буквы, цифры, _, русские буквы", NamedTextColor.GRAY));
            player.sendMessage(Component.text("• Поддержка цветов: &#FF0000текст, <gradient:&#FF0000:&#00FF00>текст</gradient>", NamedTextColor.GRAY));
            return true;
        }
        
        if (nickService.isNicknameTaken(nickname)) {
            var currentNick = nickService.getNickname(player.getUniqueId());
            if (currentNick.isEmpty() || !currentNick.get().equals(nickname)) {
                player.sendMessage(Component.text("Этот ник уже занят!", NamedTextColor.RED));
                return true;
            }
        }
        
        boolean success = nickService.setNickname(player.getUniqueId(), nickname);
        
        if (success) {
            Component nickComponent = ChatFormatter.parse(nickname);
            player.sendMessage(Component.text("Ваш ник изменён на: ", NamedTextColor.GREEN)
                    .append(nickComponent));
        } else {
            player.sendMessage(Component.text("Не удалось установить ник!", NamedTextColor.RED));
        }
        
        return true;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            String input = args[0].toLowerCase();
            
            if ("reset".startsWith(input)) {
                completions.add("reset");
            }
            
            if (sender instanceof Player player) {
                var currentNick = nickService.getNickname(player.getUniqueId());
                if (currentNick.isPresent()) {
                    completions.add(currentNick.get());
                }
            }
            
            completions.add("&#FF0000Красный");
            completions.add("&#00FF00Зелёный");
            completions.add("Русский_Ник");
        }
        
        return completions;
    }
}
