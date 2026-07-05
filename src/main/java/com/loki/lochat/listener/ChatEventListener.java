package com.loki.lochat.listener;

import com.loki.lochat.LoChat;
import com.loki.lochat.api.service.MessageService;
import com.loki.lochat.core.filter.AdvancedMessageFilter;
import com.loki.lochat.core.filter.FilterResult;
import com.loki.lochat.core.registry.ServiceRegistry;
import com.loki.lochat.renderer.EnhancedChatRenderer;
import com.loki.lochat.utils.format.ChatFormatter;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import io.papermc.paper.event.player.AsyncChatEvent;

public class ChatEventListener implements Listener {
    private final JavaPlugin plugin;
    private final MessageService messageService;
    private final AdvancedMessageFilter advancedFilter;

    public ChatEventListener(JavaPlugin plugin, ServiceRegistry registry, com.loki.lochat.core.filter.AdvancedMessageFilter filter) {
        this.plugin = plugin;
        this.messageService = registry.get(MessageService.class);
        this.advancedFilter = filter;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChat(AsyncChatEvent event) {
        Player sender = event.getPlayer();
        String rawMessage = PlainTextComponentSerializer.plainText().serialize(event.message());
        LoChat loChat = (LoChat) plugin;

        // Определяем режим чата (глобальный/локальный)
        ChatMode mode = determineChatMode(loChat, rawMessage);
        if (mode == null) {
            event.setCancelled(true);
            sender.sendMessage(ChatFormatter.parse("&#CF6679Этот режим чата отключен!"));
            return;
        }

        String plainMessage = mode.message;
        boolean isGlobal = mode.isGlobal;

        // Проверки: мут, кулдаун, фильтры
        if (!processFilters(event, sender, plainMessage, isGlobal)) {
            return;
        }

        // Обработка локального чата (радиус)
        if (!isGlobal) {
            handleLocalChat(event, sender, loChat);
        }

        // Рендеринг и интеграции
        event.renderer(new EnhancedChatRenderer(plugin, isGlobal));
        loChat.getDiscordIntegration().sendChatMessage(sender, plainMessage, isGlobal);
        recordStatistics(loChat, sender, isGlobal);
    }

    private ChatMode determineChatMode(LoChat loChat, String rawMessage) {
        boolean localEnabled = loChat.getConfigManager().isLocalEnabled();
        boolean isGlobal;
        String message;

        if (localEnabled) {
            isGlobal = rawMessage.startsWith("!");
            message = isGlobal ? rawMessage.substring(1).stripLeading() : rawMessage;
        } else {
            isGlobal = true;
            message = rawMessage;
        }

        // Проверка: включен ли режим
        if (isGlobal && !loChat.getConfigManager().isGlobalEnabled()) {
            return null;
        }
        if (!isGlobal && !localEnabled) {
            return null;
        }

        return new ChatMode(isGlobal, message);
    }

    private boolean processFilters(AsyncChatEvent event, Player sender, String plainMessage, boolean isGlobal) {
        LoChat loChat = (LoChat) plugin;

        // Мут, кулдаун
        if (!messageService.processMessage(sender, plainMessage)) {
            event.setCancelled(true);
            return false;
        }

        // Капс, реклама, повторы
        FilterResult filterResult = advancedFilter.filterMessage(sender, plainMessage);
        if (!filterResult.allowed()) {
            event.setCancelled(true);
            sender.sendMessage(ChatFormatter.parse(filterResult.blockReason()));
            return false;
        }

        String filteredMessage = filterResult.filteredMessage();

        // Цвет текста сообщения
        Component messageComponent;
        if (sender.hasPermission("lochat.chat.colors")) {
            messageComponent = ChatFormatter.parse(filteredMessage);
        } else {
            String defaultColor = isGlobal
                    ? loChat.getConfigManager().getAppearanceConfig().getGlobalMessageColor()
                    : loChat.getConfigManager().getAppearanceConfig().getLocalMessageColor();
            messageComponent = ChatFormatter.parseWithDefaultMessageColor(filteredMessage, defaultColor);
        }

        event.message(messageComponent);
        return true;
    }

    private void handleLocalChat(AsyncChatEvent event, Player sender, LoChat loChat) {
        int radius = loChat.getConfigManager().getAppearanceConfig().getLocalRadius();

        event.viewers().removeIf(v -> {
            if (!(v instanceof Player p)) {
                return false;
            }
            try {
                return !com.loki.lochat.utils.player.DistanceUtil.isInRange(sender, p, radius);
            } catch (Exception e) {
                return false;
            }
        });

        long recipients = event.viewers().stream()
                .filter(v -> v instanceof Player p && !p.equals(sender))
                .count();

        if (recipients == 0) {
            String msg = loChat.getConfigManager().getMessagesConfig().getNobodyHeard();
            sender.getScheduler().run(plugin, t ->
                    sender.sendMessage(ChatFormatter.parse(msg)), null
            );
        }
    }

    private void recordStatistics(LoChat loChat, Player sender, boolean isGlobal) {
        com.loki.lochat.api.service.PlayerService playerService =
                loChat.getServiceRegistry().get(com.loki.lochat.api.service.PlayerService.class);
        if (playerService instanceof com.loki.lochat.core.service.PlayerServiceImpl impl) {
            impl.recordMessage(sender.getUniqueId(), isGlobal ? "global" : "local");
        }
    }

    private record ChatMode(boolean isGlobal, String message) {
    }
}
