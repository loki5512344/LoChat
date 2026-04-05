package com.loki.lochat.managers.commands;

import com.loki.lochat.utils.ChatFormatter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.time.Duration;

public class MessageSender {
    
    public static void sendChat(String target, Player sender, String message) {
        Component formatted = ChatFormatter.parse(message);
        
        switch (target.toLowerCase()) {
            case "player" -> sender.sendMessage(formatted);
            case "broadcast" -> Bukkit.broadcast(formatted);
            case "server" -> Bukkit.getOnlinePlayers().forEach(p -> p.sendMessage(formatted));
            default -> {
                Player targetPlayer = Bukkit.getPlayer(target);
                if (targetPlayer != null && targetPlayer.isOnline()) {
                    targetPlayer.sendMessage(formatted);
                }
            }
        }
    }
    
    public static void sendTitle(String target, Player sender, String message) {
        String[] parts = message.split("\\|", 2);
        String titleText = parts.length > 0 ? parts[0] : "";
        String subtitleText = parts.length > 1 ? parts[1] : "";
        
        Component title = ChatFormatter.parse(titleText);
        Component subtitle = ChatFormatter.parse(subtitleText);
        
        Title titleObj = Title.title(
            title,
            subtitle,
            Title.Times.times(
                Duration.ofMillis(500),
                Duration.ofMillis(3000),
                Duration.ofMillis(500)
            )
        );
        
        switch (target.toLowerCase()) {
            case "player" -> sender.showTitle(titleObj);
            case "broadcast", "server" -> Bukkit.getOnlinePlayers().forEach(p -> p.showTitle(titleObj));
            default -> {
                Player targetPlayer = Bukkit.getPlayer(target);
                if (targetPlayer != null && targetPlayer.isOnline()) {
                    targetPlayer.showTitle(titleObj);
                }
            }
        }
    }
    
    public static void sendActionBar(String target, Player sender, String message) {
        Component formatted = ChatFormatter.parse(message);
        
        switch (target.toLowerCase()) {
            case "player" -> sender.sendActionBar(formatted);
            case "broadcast", "server" -> Bukkit.getOnlinePlayers().forEach(p -> p.sendActionBar(formatted));
            default -> {
                Player targetPlayer = Bukkit.getPlayer(target);
                if (targetPlayer != null && targetPlayer.isOnline()) {
                    targetPlayer.sendActionBar(formatted);
                }
            }
        }
    }
}
