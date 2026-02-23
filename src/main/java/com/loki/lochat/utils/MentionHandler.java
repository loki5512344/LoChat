package com.loki.lochat.utils;

import com.loki.lochat.util.FoliaUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MentionHandler {
    
    private static final Pattern EVERYONE_PATTERN = Pattern.compile("@everyone\\b");
    private static final Pattern HERE_PATTERN = Pattern.compile("@here\\b");
    private static final Pattern ROLE_PATTERN = Pattern.compile("@role:([\\w.]+)");
    private static final Pattern PLAYER_PATTERN = Pattern.compile("@(\\w+)");
    
    private final JavaPlugin plugin;
    
    public MentionHandler(JavaPlugin plugin) {
        this.plugin = plugin;
    }
    
    public Component processMentions(Component message, Player sender, Player viewer) {
        String plainText = PlainTextComponentSerializer.plainText().serialize(message);
        List<Player> mentionedPlayers = new ArrayList<>();
        
        if (sender.hasPermission("lochat.mention.everyone")) {
            Matcher everyoneMatcher = EVERYONE_PATTERN.matcher(plainText);
            if (everyoneMatcher.find()) {
                mentionedPlayers.addAll(Bukkit.getOnlinePlayers());
                message = highlightMention(message, "@everyone");
            }
        }
        
        if (sender.hasPermission("lochat.mention.here")) {
            Matcher hereMatcher = HERE_PATTERN.matcher(plainText);
            if (hereMatcher.find()) {
                mentionedPlayers.addAll(Bukkit.getOnlinePlayers());
                message = highlightMention(message, "@here");
            }
        }
        
        if (sender.hasPermission("lochat.mention.role")) {
            Matcher roleMatcher = ROLE_PATTERN.matcher(plainText);
            while (roleMatcher.find()) {
                String permission = roleMatcher.group(1);
                String fullMention = "@role:" + permission;
                
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (player.hasPermission(permission)) {
                        mentionedPlayers.add(player);
                    }
                }
                
                message = highlightMention(message, fullMention);
            }
        }
        
        Matcher playerMatcher = PLAYER_PATTERN.matcher(plainText);
        while (playerMatcher.find()) {
            String playerName = playerMatcher.group(1);
            Player mentioned = Bukkit.getPlayerExact(playerName);
            
            if (mentioned != null && mentioned.isOnline()) {
                mentionedPlayers.add(mentioned);
                message = highlightMention(message, "@" + playerName);
            }
        }
        
        if (mentionedPlayers.contains(viewer)) {
            playMentionSound(viewer);
            message = highlightForViewer(message, viewer);
        }
        
        return message;
    }
    
    private Component highlightMention(Component message, String mention) {
        return message.replaceText(builder -> 
            builder.matchLiteral(mention)
                   .replacement(Component.text(mention, NamedTextColor.YELLOW, TextDecoration.BOLD))
        );
    }
    
    private Component highlightForViewer(Component message, Player viewer) {
        String viewerMention = "@" + viewer.getName();
        return message.replaceText(builder -> 
            builder.matchLiteral(viewerMention)
                   .replacement(Component.text(viewerMention, NamedTextColor.GOLD, TextDecoration.BOLD))
        );
    }
    
    private void playMentionSound(Player player) {
        FoliaUtil.runEntityTask(plugin, player, () -> {
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.5f);
        });
    }
}
