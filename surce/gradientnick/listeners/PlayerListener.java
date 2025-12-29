package ru.lovar.gradientnick.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import ru.lovar.gradientnick.GradientNick;
import ru.lovar.gradientnick.data.PlayerData;
import ru.lovar.gradientnick.util.DisplayNameUtil;
import ru.lovar.gradientnick.util.FoliaUtil;

public class PlayerListener implements Listener {

    private final GradientNick plugin;

    public PlayerListener(GradientNick plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        // Загружаем данные асинхронно, применяем синхронно
        FoliaUtil.runAsync(plugin, () -> {
            PlayerData data = plugin.getDataManager().getPlayerData(player.getUniqueId());
            
            FoliaUtil.runEntityTask(plugin, player, () -> {
                if (!player.isOnline()) return;
                
                // Всегда обновляем displayName при заходе
                DisplayNameUtil.updateDisplayName(plugin, player, data);
                
                // Устанавливаем кастомный префикс в LuckPerms если включён
                if (data.hasPrefix() && data.isPrefixEnabled()) {
                    plugin.getLuckPermsHook().setPrefix(player, DisplayNameUtil.buildColoredPrefix(plugin, data));
                }
            });
        });
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        
        // Сохраняем данные при выходе
        FoliaUtil.runAsync(plugin, () -> plugin.getDataManager().savePlayerData(player.getUniqueId()));
    }
}
