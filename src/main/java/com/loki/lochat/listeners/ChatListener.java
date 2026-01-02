package com.loki.lochat.listeners;

import com.loki.lochat.LoChat;
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

        // Folia-safe
        player.getScheduler().run(plugin, task -> {
            processChat(player, originalMessage, plainMessage);
        }, null);
    }

    private void processChat(Player player, Component message, String plainMessage) {
        // ===== LIBERTYBANS MUTE CHECK =====
        if (plugin.getLibertyBansHook().isPluginPresent()) {
            // В простой версии без API мы не можем проверить мут автоматически
            // Пользователи должны полагаться на команды LibertyBans для управления мутами
            // LibertyBans сам заблокирует чат если игрок замучен
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

        // ===== COOLDOWN =====
        int cooldown = isGlobal
                ? plugin.getConfigManager().getGlobalCooldown()
                : plugin.getConfigManager().getLocalCooldown();

        if (cooldown > 0 && !player.hasPermission("chat.bypass.cooldown")) {
            if (plugin.getCooldownManager()
                    .isOnCooldown(player.getUniqueId(), chatType, cooldown)) {

                int remaining = plugin.getCooldownManager()
                        .getRemainingCooldown(player.getUniqueId(), chatType, cooldown);

                player.sendMessage(
                        plugin.getMessageConfig()
                                .getComponent("cooldown.wait", "{time}", String.valueOf(remaining))
                );
                return;
            }
        }

        // ===== ANTI SPAM =====
        if (!player.hasPermission("chat.bypass.antispam")) {
            AntiSpamManager.SpamResult result =
                    plugin.getAntiSpamManager()
                            .checkMessage(player.getUniqueId(), processedPlain);

            switch (result) {
                case TOO_MANY_CAPS -> {
                    player.sendMessage(
                            plugin.getMessageConfig().getComponent("antispam.caps")
                    );
                    return;
                }
                case REPEAT_CHARS -> {
                    player.sendMessage(
                            plugin.getMessageConfig().getComponent("antispam.repeat")
                    );
                    return;
                }
                case SIMILAR_MESSAGE -> {
                    player.sendMessage(
                            plugin.getMessageConfig().getComponent("antispam.similar")
                    );
                    return;
                }
            }
        }


        plugin.getCooldownManager().setCooldown(player.getUniqueId(), chatType);

        // ===== SEND =====
        if (isGlobal) {
            plugin.getChatManager().sendGlobalMessage(player, processedMessage);
        } else {
            plugin.getChatManager().sendLocalMessage(player, processedMessage);
        }
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
