package com.loki.lochat.managers;

import com.loki.lochat.LoChat;
import com.loki.lochat.utils.ChatFormatter;
import com.loki.lochat.utils.DistanceUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ChatManager {

    private final LoChat plugin;
    private final Set<UUID> globalChatDisabled = ConcurrentHashMap.newKeySet();
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private static final PlainTextComponentSerializer PLAIN = PlainTextComponentSerializer.plainText();

    public ChatManager(LoChat plugin) {
        this.plugin = plugin;
    }

    public void sendGlobalMessage(Player sender, Component message) {
        if (!plugin.getConfigManager().isGlobalEnabled()) {
            sender.sendMessage(plugin.getMessageConfig().getComponent("global.disabled"));
            return;
        }

        // Конвертируем Component в String для обработки упоминаний
        String messageText = PLAIN.serialize(message);

        // Обработка упоминаний @ник
        Set<Player> mentionedPlayers = new HashSet<>();
        String processedMessage = plugin.getMentionManager().processMentions(messageText, mentionedPlayers);

        String format = plugin.getConfigManager().getGlobalFormat();
        String prefix = plugin.getConfigManager().getGlobalPrefix();

        // Получаем отображаемое имя игрока (с градиентом если есть)
        String playerDisplay = getPlayerDisplay(sender);

        // Отправка каждому игроку персонализированное сообщение
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (isGlobalChatDisabled(player.getUniqueId())) continue;
            
            // Персонализируем сообщение для этого игрока (выделяем его ник)
            String personalizedMsg = plugin.getMentionManager().getPersonalizedMessage(processedMessage, player);
            
            // Формируем итоговую строку
            String formatted = format
                    .replace("<prefix>", prefix)
                    .replace("<player>", playerDisplay)
                    .replace("<message>", personalizedMsg);
            
            // Парсим в Component и отправляем
            Component component = MINI_MESSAGE.deserialize(formatted);
            player.sendMessage(component);
            
            // Проверяем упоминание ника без @ и уведомляем
            plugin.getMentionManager().notifyIfMentioned(messageText, player, sender);
        }

        // Уведомление упомянутых через @
        plugin.getMentionManager().notifyMentioned(mentionedPlayers);

        plugin.getLogger().info("[G] " + sender.getName() + ": " + ChatFormatter.stripTags(messageText));
    }

    public void sendLocalMessage(Player sender, Component message) {
        if (!plugin.getConfigManager().isLocalEnabled()) {
            return;
        }

        // Конвертируем Component в String для обработки упоминаний
        String messageText = PLAIN.serialize(message);

        // Обработка упоминаний @ник
        Set<Player> mentionedPlayers = new HashSet<>();
        String processedMessage = plugin.getMentionManager().processMentions(messageText, mentionedPlayers);

        int radius = plugin.getConfigManager().getLocalRadius();
        String format = plugin.getConfigManager().getLocalFormat();
        String prefix = plugin.getConfigManager().getLocalPrefix();

        // Получаем отображаемое имя игрока (с градиентом если есть)
        String playerDisplay = getPlayerDisplay(sender);

        boolean foundRecipient = false;
        Set<Player> recipients = new HashSet<>();

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (DistanceUtil.isInRange(sender, player, radius)) {
                // Персонализируем сообщение для этого игрока
                String personalizedMsg = plugin.getMentionManager().getPersonalizedMessage(processedMessage, player);
                
                // Формируем итоговую строку
                String formatted = prefix + " " + format
                        .replace("<player>", playerDisplay)
                        .replace("<message>", personalizedMsg);
                
                // Парсим в Component и отправляем
                Component component = MINI_MESSAGE.deserialize(formatted);
                player.sendMessage(component);
                
                recipients.add(player);
                
                // Проверяем упоминание ника без @
                plugin.getMentionManager().notifyIfMentioned(messageText, player, sender);
                
                if (!player.equals(sender)) {
                    foundRecipient = true;
                }
            }
        }

        // Уведомление упомянутых через @ (только тех кто в радиусе)
        mentionedPlayers.retainAll(recipients);
        plugin.getMentionManager().notifyMentioned(mentionedPlayers);

        if (!foundRecipient) {
            sender.sendMessage(plugin.getMessageConfig().getComponent("local.no-players"));
        }
    }

    /**
     * Получает отображаемое имя игрока с градиентом (если включено)
     * Возвращает строку в MiniMessage формате
     */
    private String getPlayerDisplay(Player player) {
        if (plugin.getGradientModule() != null && plugin.getGradientModule().isEnabled()) {
            String name = plugin.getGradientModule().getFormattedName(player);
            // Конвертируем &#RRGGBB формат в MiniMessage
            return ChatFormatter.convertAllColors(name);
        }
        return player.getName();
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