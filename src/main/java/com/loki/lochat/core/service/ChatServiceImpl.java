package com.loki.lochat.core.service;

import com.loki.lochat.api.service.ChatService;
import com.loki.lochat.core.registry.ServiceRegistry;
import com.loki.lochat.util.DistanceUtil;
import com.loki.lochat.utils.ChatFormatter;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Реализация сервиса чата
 */
public class ChatServiceImpl implements ChatService {
    private final JavaPlugin plugin;
    private final Set<UUID> globalChatDisabled = ConcurrentHashMap.newKeySet();
    
    public ChatServiceImpl(JavaPlugin plugin, ServiceRegistry registry) {
        this.plugin = plugin;
    }
    
    @Override
    public void sendGlobalMessage(Player sender, Object message) {
        String format = plugin.getConfig().getString("chat.global.format", 
            "{prefix} {player}: {message}");
        String prefix = plugin.getConfig().getString("chat.global.prefix", "[G]");
        
        Component messageComponent = (message instanceof Component) 
            ? (Component) message 
            : ChatFormatter.parse(message.toString());
        
        String formatted = format
            .replace("{prefix}", prefix)
            .replace("{player}", sender.getName());
        
        Component finalComponent = ChatFormatter.parse(formatted)
            .replaceText(builder -> builder.matchLiteral("{message}").replacement(messageComponent));
        
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!isGlobalChatDisabled(player.getUniqueId())) {
                player.sendMessage(finalComponent);
            }
        }
    }
    
    @Override
    public void sendLocalMessage(Player sender, Object message) {
        int radius = plugin.getConfig().getInt("chat.local.radius", 100);
        String format = plugin.getConfig().getString("chat.local.format", 
            "{player}: {message}");
        
        Component messageComponent = (message instanceof Component) 
            ? (Component) message 
            : ChatFormatter.parse(message.toString());
        
        String formatted = format.replace("{player}", sender.getName());
        
        Component finalComponent = ChatFormatter.parse(formatted)
            .replaceText(builder -> builder.matchLiteral("{message}").replacement(messageComponent));
        
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (DistanceUtil.isInRange(sender, player, radius)) {
                player.sendMessage(finalComponent);
            }
        }
    }
    
    @Override
    public boolean toggleGlobalChat(UUID player) {
        if (globalChatDisabled.contains(player)) {
            globalChatDisabled.remove(player);
            return true;
        } else {
            globalChatDisabled.add(player);
            return false;
        }
    }
    
    @Override
    public boolean isGlobalChatDisabled(UUID player) {
        return globalChatDisabled.contains(player);
    }
}
