package com.loki.lochat.gradient.listeners;

import com.loki.lochat.gradient.GradientModule;
import com.loki.lochat.gradient.data.GradientPlayerData;
import com.loki.lochat.gradient.util.DisplayNameUtil;
import com.loki.lochat.gradient.util.FoliaUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

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
            GradientPlayerData data = module.getDataManager().getPlayerData(player.getUniqueId());
            
            FoliaUtil.runEntityTask(module.getPlugin(), player, () -> {
                if (!player.isOnline()) return;
                
                // Обновляем display name если включено
                if (module.getConfig().isUpdateDisplayName()) {
                    DisplayNameUtil.updateDisplayName(module, player, data);
                }
                
                if (data.hasPrefix() && data.isPrefixEnabled()) {
                    module.getLuckPermsHook().setPrefix(player, 
                            DisplayNameUtil.buildColoredPrefix(module, data));
                }
            });
        });
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        
        // Удаляем TextDisplay если есть
        if (module.getTextDisplayManager() != null) {
            module.getTextDisplayManager().removePlayerDisplay(player.getUniqueId());
        }
        
        FoliaUtil.runAsync(module.getPlugin(), 
                () -> module.getDataManager().savePlayerData(player.getUniqueId()));
    }

    @EventHandler
    public void onPlayerToggleSneak(org.bukkit.event.player.PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();
        
        // Скрываем/показываем TextDisplay при приседании
        if (module.getTextDisplayManager() != null && module.getTextDisplayManager().hasDisplay(player.getUniqueId())) {
            FoliaUtil.runEntityTask(module.getPlugin(), player, () -> {
                if (event.isSneaking()) {
                    // Игрок начал приседать - скрываем TextDisplay
                    module.getTextDisplayManager().hidePlayerDisplay(player.getUniqueId());
                } else {
                    // Игрок перестал приседать - показываем TextDisplay
                    module.getTextDisplayManager().showPlayerDisplay(player.getUniqueId());
                }
            });
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        if (event.isCancelled()) return;
        
        Player player = event.getPlayer();
        
        // Обновляем TextDisplay после телепортации
        if (module.getTextDisplayManager() != null && module.getTextDisplayManager().hasDisplay(player.getUniqueId())) {
            FoliaUtil.runEntityTask(module.getPlugin(), player, () -> {
                if (!player.isOnline()) return;
                module.getTextDisplayManager().updateDisplayPosition(player);
            });
        }
    }
}
