package com.loki.lochat.commands;

import com.loki.lochat.LoChat;
import com.loki.lochat.utils.ChatFormatter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class LocalChatCommand implements CommandExecutor {

    private final LoChat plugin;
    private static final MiniMessage MM = MiniMessage.miniMessage();

    public LocalChatCommand(LoChat plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Только для игроков!");
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(ChatFormatter.parse(plugin.getMessageConfig().getInvalidUsage("/l <сообщение>")));
            return true;
        }

        String message = String.join(" ", args);
        
        // Конвертируем String в Component для новой сигнатуры ChatManager
        Component messageComponent = MM.deserialize(message);
        
        plugin.getChatManager().sendLocalMessage(player, messageComponent);
        return true;
    }
}