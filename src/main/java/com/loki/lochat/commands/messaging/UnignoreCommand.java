package com.loki.lochat.commands.messaging;

import com.loki.lochat.LoChat;
import com.loki.lochat.api.service.IgnoreService;
import com.loki.lochat.utils.ChatFormatter;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class UnignoreCommand implements CommandExecutor {

    private final LoChat plugin;
    private final IgnoreService ignoreService;

    public UnignoreCommand(LoChat plugin) {
        this.plugin = plugin;
        this.ignoreService = plugin.getServiceRegistry().get(IgnoreService.class);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) { sender.sendMessage(plugin.getConfigManager().getMessagesConfig().getPlayerOnly()); return true; }
        if (args.length == 0) { player.sendMessage(ChatFormatter.parse(plugin.getMessageConfig().getInvalidUsage("/unignore <ник>"))); return true; }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) { player.sendMessage(ChatFormatter.parse(plugin.getMessageConfig().getPlayerNotFound())); return true; }
        if (!ignoreService.isIgnoring(player.getUniqueId(), target.getUniqueId())) { player.sendMessage(ChatFormatter.parse(plugin.getMessageConfig().get("ignore.not-ignored"))); return true; }

        ignoreService.removeIgnore(player.getUniqueId(), target.getUniqueId());
        player.sendMessage(ChatFormatter.parse(plugin.getMessageConfig().get("ignore.removed", "{player}", target.getName())));
        return true;
    }
}
