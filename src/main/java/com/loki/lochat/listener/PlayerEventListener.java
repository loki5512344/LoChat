package com.loki.lochat.listener;

import com.loki.lochat.api.service.MessagingService;
import com.loki.lochat.api.service.NickService;
import com.loki.lochat.api.service.PlayerService;
import com.loki.lochat.core.filter.AdvancedMessageFilter;
import com.loki.lochat.core.registry.ServiceRegistry;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Слушатель событий игрока
 */
public class PlayerEventListener implements Listener {
    private final PlayerService playerService;
    private final MessagingService messagingService;
    private final NickService nickService;
    private final AdvancedMessageFilter advancedFilter;

    public PlayerEventListener(ServiceRegistry registry, AdvancedMessageFilter advancedFilter) {
        this.playerService = registry.get(PlayerService.class);
        this.messagingService = registry.get(MessagingService.class);
        this.nickService = registry.get(NickService.class);
        this.advancedFilter     = advancedFilter;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (nickService != null) {
            nickService.updatePlayerDisplay(player);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        java.util.UUID uuid = player.getUniqueId();

        playerService.clearPlayerData(uuid);
        messagingService.removeConversation(uuid);
        messagingService.removeSpy(uuid);

        // Очищаем flood / spam трекеры — иначе данные висят в памяти вечно
        advancedFilter.clearPlayer(uuid);
    }
}
