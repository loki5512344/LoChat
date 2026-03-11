package com.loki.lochat.listener;

import com.loki.lochat.api.service.MessageService;
import com.loki.lochat.core.filter.AdvancedMessageFilter;
import com.loki.lochat.core.registry.ServiceRegistry;
import com.loki.lochat.renderer.EnhancedChatRenderer;
import io.papermc.paper.event.player.AsyncChatEvent;
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

    public ChatEventListener(JavaPlugin plugin, ServiceRegistry registry) {
        this.plugin = plugin;
        this.messageService = registry.get(MessageService.class);
        this.advancedFilter = new AdvancedMessageFilter(plugin.getConfig());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChat(AsyncChatEvent event) {
        Player sender = event.getPlayer();
        String rawMessage = PlainTextComponentSerializer.plainText().serialize(event.message());

        // Определяем тип чата и очищаем сообщение от !
        boolean isGlobal = rawMessage.startsWith("!");
        String message = isGlobal ? rawMessage.substring(1).stripLeading() : rawMessage;

        boolean allowed = messageService.processMessage(sender, message);

        if (!allowed) {
            event.setCancelled(true);
            return;
        }

        AdvancedMessageFilter.FilterResult filterResult = advancedFilter.filterMessage(sender, message);

        if (!filterResult.allowed()) {
            event.setCancelled(true);
            sender.sendMessage(com.loki.lochat.utils.ChatFormatter.parse(filterResult.blockReason()));
            return;
        }

        String filteredMessage = filterResult.filteredMessage();
        
        // Убеждаемся что ! удален из отображаемого сообщения
        if (isGlobal && filteredMessage.startsWith("!")) {
            filteredMessage = filteredMessage.substring(1).stripLeading();
        }
        
        event.message(com.loki.lochat.utils.ChatFormatter.parse(filteredMessage));

        // Фильтруем получателей для локального чата
        if (!isGlobal) {
            int radius = plugin.getConfig().getInt("chat.local.radius", 100);
            event.viewers().removeIf(viewer ->
                viewer instanceof Player p && !com.loki.lochat.util.DistanceUtil.isInRange(sender, p, radius)
            );
        }

        event.renderer(new EnhancedChatRenderer(plugin, sender, event.message(), isGlobal));
    }
}