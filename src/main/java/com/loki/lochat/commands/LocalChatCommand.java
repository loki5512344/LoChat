package com.loki.lochat.commands;

import com.loki.lochat.LoChat;
import com.loki.lochat.api.service.ChatService;
import com.loki.lochat.utils.ChatFormatter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Команда для локального чата
 * /l <сообщение> - отправить сообщение в локальный чат
 */
public class LocalChatCommand implements CommandExecutor {

    private final LoChat plugin;
    private final ChatService chatService;
    private static final MiniMessage MM = MiniMessage.miniMessage();

    public LocalChatCommand(LoChat plugin) {
        this.plugin = plugin;
        this.chatService = plugin.getServiceRegistry().get(ChatService.class);
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
        
        // Конвертируем String в Component для новой сигнатуры ChatService
        Component messageComponent = MM.deserialize(message);
        
        chatService.sendLocalMessage(player, messageComponent);
        return true;
    }
}
