package com.loki.lochat.core.service.messaging;

import com.loki.lochat.LoChat;
import com.loki.lochat.api.service.MessagingService;
import com.loki.lochat.api.service.pm.PrivateMessageService;
import com.loki.lochat.utils.format.ChatFormatter;
import com.loki.lochat.utils.player.PlayerUtil;

import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Сервис личных сообщений (PM)
 * Отслеживает последние диалоги между игроками
 */
public class PrivateMessageServiceImpl implements PrivateMessageService {

    private final Map<UUID, UUID> lastConversation = new ConcurrentHashMap<>();
    private final LoChat plugin;
    private MessagingService messagingService;

    public PrivateMessageServiceImpl(LoChat plugin) {
        this.plugin = plugin;
    }

    public void init(MessagingService messagingService) {
        this.messagingService = messagingService;
    }

    @Override
    public void setLastConversation(UUID player, UUID target) {
        lastConversation.put(player, target);
    }

    @Override
    public Optional<UUID> getLastConversation(UUID player) {
        return Optional.ofNullable(lastConversation.get(player));
    }

    @Override
    public void removeConversation(UUID player) {
        lastConversation.remove(player);
    }

    @Override
    public boolean hasConversation(UUID player) {
        return lastConversation.containsKey(player);
    }

    @Override
    public void sendPrivateMessage(CommandSender sender, Player target, String message) {
        Player playerSender = (Player) sender;
        sender.sendMessage(ChatFormatter.formatPmSentNew(
                plugin.getMessageConfig().getPmFormatSent(), playerSender, target, message));
        target.sendMessage(ChatFormatter.formatPmReceivedNew(
                plugin.getMessageConfig().getPmFormatReceived(), playerSender, target, message));
        if (plugin.getConfigManager().isPmSoundEnabled()) {
            Sound sound = PlayerUtil.parseSound(plugin.getConfigManager().getPmSoundType(), null);
            if (sound != null) {
                target.playSound(target.getLocation(), sound, 1.0f, 1.0f);
            }
        }
        messagingService.broadcastPM(playerSender, target, message);
        messagingService.setLastConversation(playerSender.getUniqueId(), target.getUniqueId());
        messagingService.setLastConversation(target.getUniqueId(), playerSender.getUniqueId());
        if (plugin.getConfigManager().isPmLogEnabled()) {
            plugin.getLogger().info("[PM] " + playerSender.getName() + " -> " + target.getName() + ": " + message);
        }
    }
}
