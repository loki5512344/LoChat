package com.loki.lochat.listeners;

import com.loki.lochat.LoChat;
import com.loki.lochat.gradient.util.FoliaUtil;
import com.loki.lochat.managers.AntiSpamManager;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class ChatListener implements Listener {

    private final LoChat plugin;
    private static final PlainTextComponentSerializer PLAIN =
            PlainTextComponentSerializer.plainText();

    public ChatListener(LoChat plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChat(AsyncChatEvent event) {
        event.setCancelled(true);

        Player player = event.getPlayer();
        Component originalMessage = event.message();
        String plainMessage = PLAIN.serialize(originalMessage);

        // Paper/Folia-safe
        FoliaUtil.runEntityTask(plugin, player, () -> {
            processChat(player, originalMessage, plainMessage);
        });
    }

    private void processChat(Player player, Component message, String plainMessage) {
        // ===== MUTE CHECK =====
        if (plugin.getMuteManager().isMuted(player.getUniqueId())) {
            var muteData = plugin.getMuteManager().getMuteData(player.getUniqueId());
            if (muteData != null) {
                String msg;
                if (muteData.isPermanent()) {
                    msg = plugin.getConfigManager().getString("mute.messages.cannot-chat-permanent", 
                            "§cВы замучены навсегда!");
                } else {
                    long remaining = plugin.getMuteManager().getRemainingTime(player.getUniqueId());
                    String timeStr = plugin.getMuteManager().formatTime(remaining);
                    msg = plugin.getConfigManager().getString("mute.messages.cannot-chat", 
                            "§cВы замучены! Осталось: %duration%");
                    msg = msg.replace("%duration%", timeStr);
                }
                player.sendMessage(com.loki.lochat.utils.ChatFormatter.parse(msg));
            }
            return;
        }
        
        // Продолжаем обработку чата
        processChatInternal(player, message, plainMessage);
    }

    private void processChatInternal(Player player, Component message, String plainMessage) {
        // ===== GLOBAL / LOCAL =====
        String globalSymbol = plugin.getConfigManager().getGlobalSymbol();
        boolean isGlobal = plainMessage.startsWith(globalSymbol)
                && player.hasPermission("chat.global.use");

        String chatType = isGlobal ? "global" : "local";

        Component processedMessage = isGlobal
                ? message.replaceText(TextReplacementConfig.builder()
                    .matchLiteral(globalSymbol)
                    .once()
                    .replacement(Component.empty())
                    .build())
                : message;

        String processedPlain = PLAIN.serialize(processedMessage).trim();
        if (processedPlain.isEmpty()) return;

        // ===== FILTER =====
        processedMessage = applyMessageFilter(player, processedMessage, processedPlain);
        if (processedMessage == null) return; // Сообщение заблокировано

        processedPlain = PLAIN.serialize(processedMessage);

        // ===== COOLDOWN =====
        if (!checkCooldown(player, chatType)) return;

        // ===== ANTI SPAM =====
        if (!checkAntiSpam(player, processedPlain)) return;

        plugin.getCooldownManager().setCooldown(player.getUniqueId(), chatType);

        // ===== SPY =====
        plugin.getSpyManager().sendToSpies(player, processedMessage, isGlobal);

        // ===== HEAD EMOJI =====
        // Обрабатываем головы игроков, если функция включена
        if (plugin.getHeadEmojiManager().isHeadEmojiEnabled() && 
            plugin.getHeadEmojiManager().canUseHeadEmoji(player)) {
            processedMessage = plugin.getHeadEmojiManager().processHeads(processedMessage, player);
        }

        // ===== SEND =====
        if (isGlobal) {
            plugin.getChatManager().sendGlobalMessage(player, processedMessage);
        } else {
            plugin.getChatManager().sendLocalMessage(player, processedMessage);
        }
    }

    private Component applyMessageFilter(Player player, Component message, String plainMessage) {
        if (!plugin.getConfigManager().isFilterEnabled() || player.hasPermission("chat.bypass.filter")) {
            return message;
        }

        String filteredMessage = applyFilter(plainMessage);
        if (!filteredMessage.equals(plainMessage)) {
            String action = plugin.getConfigManager().getFilterAction();
            switch (action.toLowerCase()) {
                case "block" -> {
                    player.sendMessage(plugin.getMessageConfig().getComponent("filter.blocked"));
                    return null;
                }
                case "warn" -> {
                    player.sendMessage(plugin.getMessageConfig().getComponent("filter.warning"));
                    return null;
                }
                case "censor" -> {
                    return Component.text(filteredMessage);
                }
            }
        }
        return message;
    }

    private boolean checkCooldown(Player player, String chatType) {
        int cooldown = chatType.equals("global")
                ? plugin.getConfigManager().getGlobalCooldown()
                : plugin.getConfigManager().getLocalCooldown();

        if (cooldown > 0 && !player.hasPermission("chat.bypass.cooldown")) {
            if (plugin.getCooldownManager().isOnCooldown(player.getUniqueId(), chatType, cooldown)) {
                int remaining = plugin.getCooldownManager()
                        .getRemainingCooldown(player.getUniqueId(), chatType, cooldown);

                player.sendMessage(
                        plugin.getMessageConfig()
                                .getComponent("cooldown.wait", "{time}", String.valueOf(remaining))
                );
                return false;
            }
        }
        return true;
    }

    private boolean checkAntiSpam(Player player, String message) {
        if (player.hasPermission("chat.bypass.antispam")) {
            return true;
        }

        AntiSpamManager.SpamResult result = plugin.getAntiSpamManager()
                .checkMessage(player.getUniqueId(), message);

        switch (result) {
            case TOO_MANY_CAPS -> {
                player.sendMessage(plugin.getMessageConfig().getComponent("antispam.caps"));
                return false;
            }
            case REPEAT_CHARS -> {
                player.sendMessage(plugin.getMessageConfig().getComponent("antispam.repeat"));
                return false;
            }
            case SIMILAR_MESSAGE -> {
                player.sendMessage(plugin.getMessageConfig().getComponent("antispam.similar"));
                return false;
            }
            case ALLOWED -> {
                return true;
            }
        }
        return true;
    }

    private String applyFilter(String message) {
        if (!plugin.getConfigManager().isFilterEnabled()) {
            return message;
        }

        String filtered = message;
        String replacement = plugin.getConfigManager().getFilterReplacement();
        
        for (String word : plugin.getConfigManager().getFilterWords()) {
            if (word == null || word.trim().isEmpty()) continue;
            
            // Заменяем слово с учетом регистра и границ слов
            String pattern = "(?i)\\b" + java.util.regex.Pattern.quote(word.trim()) + "\\b";
            filtered = filtered.replaceAll(pattern, replacement);
        }
        
        return filtered;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        plugin.getPmManager().removeConversation(player.getUniqueId());
        plugin.getCooldownManager().removeCooldown(player.getUniqueId());
        plugin.getAntiSpamManager().clearPlayer(player.getUniqueId());
        plugin.getSpyManager().removeSpy(player.getUniqueId());
    }
}
