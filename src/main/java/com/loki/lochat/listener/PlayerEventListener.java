package com.loki.lochat.listener;

import com.loki.lochat.api.service.NickService;
import com.loki.lochat.api.service.PlayerDataService;
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

    public PlayerEventListener(ServiceRegistry registry) {
        this.playerDataService = registry.get(PlayerDataService.class);
        this.nickService = registry.get(NickService.class);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        // Обновляем displayName с кастомным ником если есть
        if (nickService != null) {
            nickService.updatePlayerDisplay(player);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        playerDataService.clearPlayerData(player.getUniqueId());
    }
}
