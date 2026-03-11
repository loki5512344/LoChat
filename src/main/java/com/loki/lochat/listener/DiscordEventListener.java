package com.loki.lochat.listener;

import com.loki.lochat.integrations.DiscordIntegration;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Слушатель событий для Discord интеграции
 */
public class DiscordEventListener implements Listener {
    private final DiscordIntegration discord;

    public DiscordEventListener(DiscordIntegration discord) {
        this.discord = discord;
    }

    /**
     * Обработка сообщений чата для Discord
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onChat(AsyncChatEvent event) {
        if (!discord.isEnabled()) return;
        
        Player player = event.getPlayer();
        String message = PlainTextComponentSerializer.plainText().serialize(event.message());
        
        // Определяем тип чата (глобальный если начинается с !)
        boolean isGlobal = message.startsWith("!");
        
        // Очищаем сообщение от ! если это глобальный чат
        if (isGlobal) {
            message = message.substring(1).stripLeading();
        }
        
        discord.sendChatMessage(player, message, isGlobal);
    }

    /**
     * Обработка входа игрока
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!discord.isEnabled()) return;
        
        Player player = event.getPlayer();
        discord.sendPlayerJoin(player);
    }

    /**
     * Обработка выхода игрока
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (!discord.isEnabled()) return;
        
        Player player = event.getPlayer();
        discord.sendPlayerQuit(player);
    }

    /**
     * Обработка смерти игрока
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (!discord.isEnabled()) return;
        
        Player player = event.getEntity();
        String deathMessage = event.getDeathMessage() != null ? 
            PlainTextComponentSerializer.plainText().serialize(event.getDeathMessage()) : 
            "умер";
        
        discord.sendPlayerDeath(player, deathMessage);
    }
}