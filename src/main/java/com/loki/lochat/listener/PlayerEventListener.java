package com.loki.lochat.listener;

import com.loki.lochat.api.service.NickService;
import com.loki.lochat.api.service.PlayerDataService;
import com.loki.lochat.api.service.PMService;
import com.loki.lochat.api.service.SpyService;
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
    private final PlayerDataService playerDataService;
    private final NickService nickService;
    private final PMService pmService;
    private final SpyService spyService;
    private final AdvancedMessageFilter advancedFilter;

    public PlayerEventListener(ServiceRegistry registry, AdvancedMessageFilter advancedFilter) {
        this.playerDataService  = registry.get(PlayerDataService.class);
        this.nickService        = registry.get(NickService.class);
        this.pmService          = registry.get(PMService.class);
        this.spyService         = registry.get(SpyService.class);
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

        playerDataService.clearPlayerData(uuid);
        pmService.removeConversation(uuid);
        spyService.removeSpy(uuid);

        // Очищаем flood / spam трекеры — иначе данные висят в памяти вечно
        advancedFilter.clearPlayer(uuid);
    }
}
