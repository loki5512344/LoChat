package com.loki.lochat.listeners;

import com.loki.lochat.LoChat;
import com.loki.lochat.managers.AntiSpamManager;
import com.loki.lochat.managers.FilterManager;
import com.loki.lochat.utils.ChatFormatter;
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
    private static final PlainTextComponentSerializer PLAIN = PlainTextComponentSerializer.plainText();

    public ChatListener(LoChat plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChat(AsyncChatEvent event) {
        event.setCancelled(true);

        Player player = event.getPlayer();
        Component message = event.message();
        
        // Plain текст только для проверок
        String plainMessage = PLAIN.serialize(message);

        // Обработка через RegionScheduler для Folia
        player.getScheduler().run(plugin, task -> {
            processChat(player, plainMessage);
        }, null);
    }

    private void processChat(Player player, String message) {
        // Проверка мута
        if (plugin.getMuteManager().isMuted(player.getUniqueId())) {
            String timeLeft = plugin.getMuteManager().formatRemainingTime(player.getUniqueId());
            if (timeLeft.equals("навсегда")) {
                player.sendMessage(plugin.getMessageConfig().getComponent("mute.permanent"));
            } else {
                player.sendMessage(plugin.getMessageConfig().getComponent("mute.you-muted", "{time}", timeLeft));
            }
            return;
        }

        String globalSymbol = plugin.getConfigManager().getGlobalSymbol();
        boolean isGlobal = message.startsWith(globalSymbol) && player.hasPermission("chat.global.use");
        String chatType = isGlobal ? "global" : "local";
        String processedMessage = isGlobal ? message.substring(globalSymbol.length()).trim() : message;

        if (processedMessage.isEmpty()) return;

        // Проверка кулдауна
        int cooldown = isGlobal ? plugin.getConfigManager().getGlobalCooldown() : plugin.getConfigManager().getLocalCooldown();
        if (cooldown > 0 && !player.hasPermission("chat.bypass.cooldown")) {
            if (plugin.getCooldownManager().isOnCooldown(player.getUniqueId(), chatType, cooldown)) {
                int remaining = plugin.getCooldownManager().getRemainingCooldown(player.getUniqueId(), chatType, cooldown);
                player.sendMessage(plugin.getMessageConfig().getComponent("cooldown.wait", "{time}", String.valueOf(remaining)));
                return;
            }
        }

        // Проверка анти-спама
        if (!player.hasPermission("chat.bypass.antispam")) {
            AntiSpamManager.SpamResult spamResult = plugin.getAntiSpamManager().checkMessage(player.getUniqueId(), processedMessage);
            switch (spamResult) {
                case TOO_MANY_CAPS -> {
                    player.sendMessage(plugin.getMessageConfig().getComponent("antispam.caps"));
                    return;
                }
                case REPEAT_CHARS -> {
                    player.sendMessage(plugin.getMessageConfig().getComponent("antispam.repeat"));
                    return;
                }
                case SIMILAR_MESSAGE -> {
                    player.sendMessage(plugin.getMessageConfig().getComponent("antispam.similar"));
                    return;
                }
            }
        }

        // Проверка фильтра
        if (!player.hasPermission("chat.bypass.filter")) {
            FilterManager.FilterResult filterResult = plugin.getFilterManager().checkMessage(processedMessage);
            switch (filterResult) {
                case BLOCKED -> {
                    player.sendMessage(plugin.getMessageConfig().getComponent("filter.blocked"));
                    return;
                }
                case WARNED -> {
                    player.sendMessage(plugin.getMessageConfig().getComponent("filter.warned"));
                    processedMessage = plugin.getFilterManager().censorMessage(processedMessage);
                }
                case CENSORED -> processedMessage = plugin.getFilterManager().censorMessage(processedMessage);
            }
        }

        // Устанавливаем кулдаун
        plugin.getCooldownManager().setCooldown(player.getUniqueId(), chatType);

        // Отправка сообщения
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
