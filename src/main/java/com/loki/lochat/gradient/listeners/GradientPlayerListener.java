package com.loki.lochat.gradient.listeners;

import com.loki.lochat.gradient.GradientModule;
import com.loki.lochat.utils.FoliaUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Слушатель событий игроков для градиентного модуля
 */
public class GradientPlayerListener implements Listener {

    private final GradientModule module;

    public GradientPlayerListener(GradientModule module) {
        this.module = module;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        FoliaUtil.runAsync(module.getPlugin(), () -> {
            var data = module.getDataManager().getPlayerData(player.getUniqueId());
            // Применяем градиент к displayName при входе
            FoliaUtil.runEntityTask(module.getPlugin(), player, () -> {
                com.loki.lochat.gradient.util.DisplayNameUtil.updateDisplayName(module, player, data);
            });
        });
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        FoliaUtil.runAsync(module.getPlugin(),
                () -> module.getDataManager().savePlayerData(player.getUniqueId()));
    }
}
