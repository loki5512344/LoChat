package com.loki.lochat.commands;

import com.loki.lochat.LoChat;
import com.loki.lochat.utils.ChatFormatter;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class GlobalChatCommand implements CommandExecutor {

    private final LoChat plugin;

    public GlobalChatCommand(LoChat plugin) {
        this.plugin = plugin;
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
                boolean enabled = plugin.getChatManager().toggleGlobalChat(player.getUniqueId());
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
        plugin.getChatManager().sendGlobalMessage(player, message);
        return true;
    }
}
