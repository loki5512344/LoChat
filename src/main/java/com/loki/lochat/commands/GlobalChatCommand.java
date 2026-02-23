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
 * Команда для глобального чата
 * /g <сообщение> - отправить сообщение в глобальный чат
 * /globalchat toggle - переключить режим глобального чата
 */
public class GlobalChatCommand implements CommandExecutor {

    private static final MiniMessage MM = MiniMessage.miniMessage();
    private final LoChat plugin;
    private final ChatService chatService;

    public GlobalChatCommand(LoChat plugin) {
        this.plugin = plugin;
        this.chatService = plugin.getServiceRegistry().get(ChatService.class);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Только для игроков!");
            return true;
        }

        // /globalchat toggle
        if (command.getName().equalsIgnoreCase("globalchat")) {
            if (args.length > 0 && args[0].equalsIgnoreCase("toggle")) {
                boolean enabled = chatService.toggleGlobalChat(player.getUniqueId());
                String msgKey = enabled ? "global.toggled-on" : "global.toggled-off";
                player.sendMessage(ChatFormatter.parse(plugin.getMessageConfig().get(msgKey)));
            } else {
                player.sendMessage(ChatFormatter.parse(plugin.getMessageConfig().getInvalidUsage("/globalchat toggle")));
            }
            return true;
        }

        // /g <message>
        if (args.length == 0) {
            player.sendMessage(ChatFormatter.parse(plugin.getMessageConfig().getInvalidUsage("/g <сообщение>")));
            return true;
        }

        String message = String.join(" ", args);

        // Конвертируем String в Component для новой сигнатуры ChatService
        Component messageComponent = MM.deserialize(message);

        chatService.sendGlobalMessage(player, messageComponent);
        return true;
    }
}
