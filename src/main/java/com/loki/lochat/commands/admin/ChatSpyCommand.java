package com.loki.lochat.commands.admin;

import com.loki.lochat.LoChat;
import com.loki.lochat.api.service.SpyService;
import com.loki.lochat.utils.ChatFormatter;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ChatSpyCommand implements CommandExecutor {

    private final LoChat plugin;
    private final SpyService spyService;

    public ChatSpyCommand(LoChat plugin) {
        this.plugin = plugin;
        this.spyService = plugin.getServiceRegistry().get(SpyService.class);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getConfigManager().getHardcodedMessages().getPlayerOnly());
            return true;
        }
        boolean enabled = spyService.toggleSpy(player.getUniqueId());
        player.sendMessage(ChatFormatter.parse(plugin.getMessageConfig().get(enabled ? "spy.enabled" : "spy.disabled")));
        return true;
    }
}
