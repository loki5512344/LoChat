package com.loki.lochat.listener;

import com.loki.lochat.api.service.PlayerDataService;
import com.loki.lochat.core.registry.ServiceRegistry;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Слушатель событий игрока
 */
public class PlayerEventListener implements Listener {
    private final PlayerDataService playerDataService;
    
    public PlayerEventListener(ServiceRegistry registry) {
        this.playerDataService = registry.get(PlayerDataService.class);
    }
    
    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        playerDataService.clearPlayerData(player.getUniqueId());
    }
}
