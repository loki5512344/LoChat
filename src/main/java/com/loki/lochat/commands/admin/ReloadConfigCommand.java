package com.loki.lochat.commands.admin;

import com.loki.lochat.LoChat;
import com.loki.lochat.utils.ChatFormatter;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class ReloadConfigCommand implements CommandExecutor {

    private final LoChat plugin;

    public ReloadConfigCommand(LoChat plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("lochat.admin.reload")) {
            sender.sendMessage(ChatFormatter.parse(plugin.getConfigManager().getMessagesConfig().getNoPermission()));
            return true;
        }
        try {
            plugin.reload();
            sender.sendMessage(ChatFormatter.parse("&#9878C9Все конфигурации &#7858E9успешно перезагружены!"));
        } catch (Exception e) {
            sender.sendMessage(ChatFormatter.parse("&#CF6679Ошибка при перезагрузке: " + e.getMessage()));
            plugin.getLogger().severe("Ошибка при перезагрузке: " + e.getMessage());
        }
        return true;
    }
}
