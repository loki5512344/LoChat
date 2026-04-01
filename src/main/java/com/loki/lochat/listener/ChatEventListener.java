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

        // ✅ РЕЖИМ ЕДИНОГО ЧАТА: если локальный отключен, весь чат = глобальный
        LoChat loChat = (LoChat) plugin;
        boolean localEnabled = loChat.getConfigManager().isLocalEnabled();
        
        boolean isGlobal;
        String plainMessage;
        
        if (localEnabled) {
            // Старая логика: "!" = глобальный, без "!" = локальный
            isGlobal = rawMessage.startsWith("!");
            plainMessage = isGlobal ? rawMessage.substring(1).stripLeading() : rawMessage;
        } else {
            // Новая логика: весь чат глобальный, "!" не нужен
            isGlobal = true;
            plainMessage = rawMessage;
        }

        // ✅ Проверка: включен ли глобальный/локальный чат
        if (isGlobal && !loChat.getConfigManager().isGlobalEnabled()) {
            event.setCancelled(true);
            sender.sendMessage(ChatFormatter.parse("&#CF6679Глобальный чат отключен!"));
            return;
        }
        
        if (!isGlobal && !localEnabled) {
            event.setCancelled(true);
            sender.sendMessage(ChatFormatter.parse("&#CF6679Локальный чат отключен!"));
            return;
        }

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

        // ── Цвет текста сообщения ({message}) ─────────────────────────────────
        // Если стиль не задан явно, Paper может унаследовать цвет последнего символа градиента
        // префикса в строке чата (например у LOCAL последняя буква давала синеватый #4169E1) —
        // из‑за этого весь текст сообщения «подтягивался» под этот тон. Намеренный цвет текста
        // задаётся в appearance.yml: prefixes.global.message-color / prefixes.local.message-color (#E8E0F0 по умолчанию).
        // Решение: всегда выставлять стиль тела сообщения:
        //   • lochat.chat.colors → MiniMessage / &# / & из текста
        //   • иначе → parseWithDefaultMessageColor(..., message-color) + экранирование < (см. ChatFormatter)
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

        // ✅ FIX: Радиус для локального чата - кэшируем локации в async потоке
        // Paper позволяет читать Location в async, но не модифицировать World
        if (!isGlobal) {
            int radius = loChat.getConfigManager().getAppearanceConfig().getLocalRadius();
            
            // Безопасно: AsyncChatEvent.viewers() thread-safe, Location.distance() тоже
            event.viewers().removeIf(v -> {
                if (!(v instanceof Player p)) return false;
                
                // Проверяем мир и расстояние (чтение Location безопасно в async)
                try {
                    return !com.loki.lochat.util.DistanceUtil.isInRange(sender, p, radius);
                } catch (Exception e) {
                    // На всякий случай ловим исключения
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

        event.renderer(new EnhancedChatRenderer(plugin, isGlobal));

        loChat.getDiscordIntegration().sendChatMessage(sender, filteredMessage, isGlobal);

        // Статистика
        com.loki.lochat.api.service.PlayerDataService pds =
                loChat.getServiceRegistry().get(com.loki.lochat.api.service.PlayerDataService.class);
        if (pds instanceof com.loki.lochat.core.service.PlayerDataServiceImpl impl) {
            impl.recordMessage(sender.getUniqueId(), isGlobal ? "global" : "local");
        }
    }
}
