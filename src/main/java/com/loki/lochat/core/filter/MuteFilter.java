package com.loki.lochat.core.filter;

import com.loki.lochat.api.filter.MessageFilter;
import com.loki.lochat.api.service.MuteService;
import com.loki.lochat.data.model.ChatMessage;
import org.bukkit.entity.Player;

/**
 * Фильтр проверки мута
 */
public class MuteFilter implements MessageFilter {
    private final MuteService muteService;
    
    public MuteFilter(MuteService muteService) {
        this.muteService = muteService;
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
        String timeStr = muteService.formatTime(remaining);
        player.sendMessage("§cВы замучены! Осталось: " + timeStr);
    }
}
