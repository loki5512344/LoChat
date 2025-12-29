package com.loki.lochat.managers;

import com.loki.lochat.LoChat;
import com.loki.lochat.utils.ChatFormatter;
import com.loki.lochat.utils.DistanceUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ChatManager {

    private final LoChat plugin;
    private final Set<UUID> globalChatDisabled = ConcurrentHashMap.newKeySet();

    public ChatManager(LoChat plugin) {
        this.plugin = plugin;
    }

    public void sendGlobalMessage(Player sender, String message) {
        if (!plugin.getConfigManager().isGlobalEnabled()) {
            sender.sendMessage(ChatFormatter.parse(plugin.getMessageConfig().get("global.disabled")));
            return;
        }

        // Обработка упоминаний @ник
        Set<Player> mentionedPlayers = new HashSet<>();
        String processedMessage = plugin.getMentionManager().processMentions(message, mentionedPlayers);

        String format = plugin.getConfigManager().getGlobalFormat();
        String prefix = plugin.getConfigManager().getGlobalPrefix();
        boolean hasColor = sender.hasPermission("chat.global.color");

        // Отправка каждому игроку персонализированное сообщение
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (isGlobalChatDisabled(player.getUniqueId())) continue;
            
            // Персонализируем сообщение для этого игрока (выделяем его ник)
            String personalizedMsg = plugin.getMentionManager().getPersonalizedMessage(processedMessage, player);
            
            Component formatted = ChatFormatter.formatGlobalMessage(format, prefix, sender, personalizedMsg, hasColor);
            player.sendMessage(formatted);
            
            // Проверяем упоминание ника без @ и уведомляем
            plugin.getMentionManager().notifyIfMentioned(message, player, sender);
        }

        // Уведомление упомянутых через @
        plugin.getMentionManager().notifyMentioned(mentionedPlayers);

        plugin.getLogger().info("[G] " + sender.getName() + ": " + ChatFormatter.stripTags(message));
    }

    public void sendLocalMessage(Player sender, String message) {
        if (!plugin.getConfigManager().isLocalEnabled()) {
            return;
        }

        // Обработка упоминаний @ник
        Set<Player> mentionedPlayers = new HashSet<>();
        String processedMessage = plugin.getMentionManager().processMentions(message, mentionedPlayers);

        int radius = plugin.getConfigManager().getLocalRadius();
        String format = plugin.getConfigManager().getLocalFormat();
        String prefix = plugin.getConfigManager().getLocalPrefix();
        boolean hasColor = sender.hasPermission("chat.local.color");

        boolean foundRecipient = false;
        Set<Player> recipients = new HashSet<>();

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (DistanceUtil.isInRange(sender, player, radius)) {
                // Персонализируем сообщение для этого игрока
                String personalizedMsg = plugin.getMentionManager().getPersonalizedMessage(processedMessage, player);
                
                Component formatted = ChatFormatter.formatLocalMessage(format, prefix, sender, personalizedMsg, hasColor);
                player.sendMessage(formatted);
                recipients.add(player);
                
                // Проверяем упоминание ника без @
                plugin.getMentionManager().notifyIfMentioned(message, player, sender);
                
                if (!player.equals(sender)) {
                    foundRecipient = true;
                }
            }
        }

        // Уведомление упомянутых через @ (только тех кто в радиусе)
        mentionedPlayers.retainAll(recipients);
        plugin.getMentionManager().notifyMentioned(mentionedPlayers);

        if (!foundRecipient) {
            sender.sendMessage(ChatFormatter.parse(plugin.getMessageConfig().get("local.no-players")));
        }
    }

    public boolean toggleGlobalChat(UUID player) {
        if (globalChatDisabled.contains(player)) {
            globalChatDisabled.remove(player);
            return true;
        } else {
            globalChatDisabled.add(player);
            return false;
        }
    }

    public boolean isGlobalChatDisabled(UUID player) {
        return globalChatDisabled.contains(player);
    }
}
