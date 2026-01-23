package com.loki.lochat.commands;

import com.loki.lochat.LoChat;
import com.loki.lochat.utils.ChatFormatter;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ClearChatCommand implements CommandExecutor {

    private final LoChat plugin;

    public ClearChatCommand(LoChat plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        // Отправляем пустые строки всем игрокам для очистки чата
        for (Player player : Bukkit.getOnlinePlayers()) {
            for (int i = 0; i < com.loki.lochat.utils.Constants.CLEAR_CHAT_LINES; i++) {
                player.sendMessage("");
            }
            
            // Уведомление (если включено)
            if (plugin.getConfigManager().isClearChatMessageEnabled()) {
                String message = plugin.getConfigManager().getClearChatMessage();
                player.sendMessage(ChatFormatter.parse(message.replace("{player}", sender.getName())));
            }
        }

        return true;
    }
}
