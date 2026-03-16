package com.loki.lochat.core.filter;

import com.loki.lochat.api.filter.MessageFilter;
import com.loki.lochat.api.service.MuteService;
import com.loki.lochat.config.HardcodedMessages;
import com.loki.lochat.data.model.ChatMessage;
import com.loki.lochat.utils.ChatFormatter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Фильтр проверки мута
 */
public class MuteFilter implements MessageFilter {
    private final MuteService muteService;
    private final HardcodedMessages hardcodedMessages;

    public MuteFilter(MuteService muteService, JavaPlugin plugin) {
        this.muteService = muteService;
        // Получаем HardcodedMessages через LoChat
        com.loki.lochat.LoChat loChat = (com.loki.lochat.LoChat) plugin;
        this.hardcodedMessages = loChat.getConfigManager().getHardcodedMessages();
    }

    @Override
    public boolean apply(Player player, ChatMessage message) {
        if (muteService.isMuted(player.getUniqueId())) {
            sendMuteNotification(player);
            return false;
        }
        return true;
    }

    private void sendMuteNotification(Player player) {
        long remaining = muteService.getRemainingTime(player.getUniqueId());
        com.loki.lochat.data.model.MuteData muteData = muteService.getMuteData(player.getUniqueId());
        String reason = muteData != null && muteData.getReason() != null ? muteData.getReason() : "";

        String msg;
        if (remaining < 0) {
            // Permanent mute
            msg = hardcodedMessages.getMutedPermanent()
                    .replace("{reason}", reason);
        } else {
            String timeStr = muteService.formatTime(remaining);
            msg = hardcodedMessages.getMutedMessage()
                    .replace("{reason}", reason)
                    .replace("{time}", timeStr);
        }

        player.sendMessage(ChatFormatter.parse(msg));
    }
}
