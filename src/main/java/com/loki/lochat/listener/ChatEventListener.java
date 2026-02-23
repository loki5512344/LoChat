package com.loki.lochat.listener;

import com.loki.lochat.api.service.MessageService;
import com.loki.lochat.core.registry.ServiceRegistry;
import com.loki.lochat.util.FoliaUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Слушатель событий чата (минимальная ответственность)
 */
public class ChatEventListener implements Listener {
    private final JavaPlugin plugin;
    private final MessageService messageService;
    
    public ChatEventListener(JavaPlugin plugin, ServiceRegistry registry) {
        this.plugin = plugin;
        this.messageService = registry.get(MessageService.class);
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChat(AsyncPlayerChatEvent event) {
        event.setCancelled(true);
        
        Player player = event.getPlayer();
        String message = event.getMessage();
        
        // Folia-safe обработка
        FoliaUtil.runEntityTask(plugin, player, () -> {
            messageService.processMessage(player, message);
        });
    }
}
