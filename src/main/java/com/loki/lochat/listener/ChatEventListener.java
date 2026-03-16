package com.loki.lochat.listener;

import com.loki.lochat.LoChat;
import com.loki.lochat.api.service.MessageService;
import com.loki.lochat.core.filter.AdvancedMessageFilter;
import com.loki.lochat.core.registry.ServiceRegistry;
import com.loki.lochat.renderer.EnhancedChatRenderer;
import com.loki.lochat.utils.ChatFormatter;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

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

        // plain нужен только для фильтров — цвета игрока обрабатываем ниже отдельно
        String rawMessage = PlainTextComponentSerializer.plainText().serialize(event.message());

        boolean isGlobal = rawMessage.startsWith("!");
        String plainMessage = isGlobal ? rawMessage.substring(1).stripLeading() : rawMessage;

        // Мут, кулдаун
        if (!messageService.processMessage(sender, plainMessage)) {
            event.setCancelled(true);
            return;
        }

        // Капс, реклама, повторы
        AdvancedMessageFilter.FilterResult filterResult = advancedFilter.filterMessage(sender, plainMessage);
        if (!filterResult.allowed()) {
            event.setCancelled(true);
            sender.sendMessage(ChatFormatter.parse(filterResult.blockReason()));
            return;
        }

        String filteredMessage = filterResult.filteredMessage();

        // ── Фиксируем цвет текста ──────────────────────────────────────────────
        // Проблема: Paper наследует цвет от предыдущего компонента если явный не задан.
        // Синий цвет в LOCAL — это цвет последней буквы градиента "LOCAL" (#4169E1),
        // который просачивается в текст сообщения.
        // Решение: всегда явно задаём цвет сообщения.
        //   • lochat.chat.colors → парсим &7 / &#RRGGBB / MiniMessage из самого текста
        //   • без права          → берём message-color из конфига
        LoChat loChat = (LoChat) plugin;
        Component messageComponent;

        if (sender.hasPermission("lochat.chat.colors")) {
            messageComponent = ChatFormatter.parse(filteredMessage);
        } else {
            String defaultColor = isGlobal
                    ? loChat.getConfigManager().getAppearanceConfig().getGlobalMessageColor()
                    : loChat.getConfigManager().getAppearanceConfig().getLocalMessageColor();
            String hex = defaultColor.startsWith("#") ? defaultColor : "#" + defaultColor;
            messageComponent = ChatFormatter.parse("<color:" + hex + ">" + filteredMessage + "</color>");
        }

        event.message(messageComponent);

        // Радиус для локального чата
        if (!isGlobal) {
            int radius = loChat.getConfigManager().getAppearanceConfig().getLocalRadius();
            event.viewers().removeIf(v ->
                    v instanceof Player p && !com.loki.lochat.util.DistanceUtil.isInRange(sender, p, radius)
            );

            long recipients = event.viewers().stream()
                    .filter(v -> v instanceof Player p && !p.equals(sender))
                    .count();

            if (recipients == 0) {
                String msg = loChat.getConfigManager().getHardcodedMessages().getNobodyHeard();
                sender.getScheduler().run(plugin, t ->
                        sender.sendMessage(ChatFormatter.parse(msg)), null
                );
            }
        }

        event.renderer(new EnhancedChatRenderer(plugin, sender, messageComponent, isGlobal));

        loChat.getDiscordIntegration().sendChatMessage(sender, filteredMessage, isGlobal);

        // Статистика
        com.loki.lochat.api.service.PlayerDataService pds =
                loChat.getServiceRegistry().get(com.loki.lochat.api.service.PlayerDataService.class);
        if (pds instanceof com.loki.lochat.core.service.PlayerDataServiceImpl impl) {
            impl.recordMessage(sender.getUniqueId(), isGlobal ? "global" : "local");
        }
    }
}
