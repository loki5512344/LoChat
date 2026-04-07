package com.loki.lochat.commands.messaging;

import com.loki.lochat.LoChat;
import com.loki.lochat.api.service.MessagingService;
import com.loki.lochat.utils.format.ChatFormatter;
import com.loki.lochat.utils.player.PlayerUtil;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class MsgCommand implements CommandExecutor {

    private final LoChat plugin;
    private final MessagingService messagingService;

    public MsgCommand(LoChat plugin) {
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
        if (args.length < 2) {
            player.sendMessage(ChatFormatter.parse(plugin.getMessageConfig().getInvalidUsage("/msg <ник> <сообщение>")));
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) { player.sendMessage(ChatFormatter.parse(plugin.getMessageConfig().get("pm.player-offline"))); return true; }
        if (target.equals(player)) { player.sendMessage(ChatFormatter.parse(plugin.getMessageConfig().get("pm.self"))); return true; }
        if (messagingService.isIgnoring(target.getUniqueId(), player.getUniqueId())) { player.sendMessage(ChatFormatter.parse(plugin.getMessageConfig().get("pm.ignored"))); return true; }
        if (messagingService.isIgnoring(player.getUniqueId(), target.getUniqueId())) { player.sendMessage(ChatFormatter.parse(plugin.getMessageConfig().get("pm.ignored"))); return true; }

        String message = String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length));
        sendPm(player, target, message);
        return true;
    }

    private void sendPm(Player sender, Player target, String message) {
        sender.sendMessage(ChatFormatter.formatPmSentNew(plugin.getMessageConfig().getPmFormatSent(), sender, target, message));
        target.sendMessage(ChatFormatter.formatPmReceivedNew(plugin.getMessageConfig().getPmFormatReceived(), sender, target, message));
        if (plugin.getConfigManager().isPmSoundEnabled()) {
            Sound sound = PlayerUtil.parseSound(plugin.getConfigManager().getPmSoundType(), null);
            if (sound != null) target.playSound(target.getLocation(), sound, 1.0f, 1.0f);
        }
        messagingService.broadcastPM(sender, target, message);
        messagingService.setLastConversation(sender.getUniqueId(), target.getUniqueId());
        messagingService.setLastConversation(target.getUniqueId(), sender.getUniqueId());
        if (plugin.getConfigManager().isPmLogEnabled())
            plugin.getLogger().info("[PM] " + sender.getName() + " -> " + target.getName() + ": " + message);
    }
}
