package com.loki.lochat.commands.messaging;

import com.loki.lochat.LoChat;
import com.loki.lochat.api.service.MessagingService;
import com.loki.lochat.utils.ChatFormatter;
import com.loki.lochat.utils.PlayerUtil;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;

public class ReplyCommand implements CommandExecutor {

    private final LoChat plugin;
    private final MessagingService messagingService;

    public ReplyCommand(LoChat plugin) {
        this.plugin = plugin;
        this.messagingService = plugin.getServiceRegistry().get(MessagingService.class);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getConfigManager().getMessagesConfig().getPlayerOnly());
            return true;
        }
        if (!plugin.getConfigManager().isPmEnabled()) return true;
        if (args.length == 0) {
            player.sendMessage(ChatFormatter.parse(plugin.getMessageConfig().getInvalidUsage("/reply <сообщение>")));
            return true;
        }

        Optional<UUID> lastUuid = messagingService.getLastConversation(player.getUniqueId());
        if (lastUuid.isEmpty()) { player.sendMessage(ChatFormatter.parse(plugin.getMessageConfig().get("pm.no-reply"))); return true; }

        Player target = Bukkit.getPlayer(lastUuid.get());
        if (target == null) { player.sendMessage(ChatFormatter.parse(plugin.getMessageConfig().get("pm.player-offline"))); return true; }
        if (messagingService.isIgnoring(target.getUniqueId(), player.getUniqueId())) { player.sendMessage(ChatFormatter.parse(plugin.getMessageConfig().get("pm.ignored"))); return true; }

        String message = String.join(" ", args);
        player.sendMessage(ChatFormatter.formatPmSentNew(plugin.getMessageConfig().getPmFormatSent(), player, target, message));
        target.sendMessage(ChatFormatter.formatPmReceivedNew(plugin.getMessageConfig().getPmFormatReceived(), player, target, message));
        if (plugin.getConfigManager().isPmSoundEnabled()) {
            Sound sound = PlayerUtil.parseSound(plugin.getConfigManager().getPmSoundType(), null);
            if (sound != null) target.playSound(target.getLocation(), sound, 1.0f, 1.0f);
        }
        messagingService.broadcastPM(player, target, message);
        messagingService.setLastConversation(target.getUniqueId(), player.getUniqueId());
        if (plugin.getConfigManager().isPmLogEnabled())
            plugin.getLogger().info("[PM] " + player.getName() + " -> " + target.getName() + ": " + message);
        return true;
    }
}
