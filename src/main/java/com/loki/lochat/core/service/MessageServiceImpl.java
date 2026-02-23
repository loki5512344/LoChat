package com.loki.lochat.core.service;

import com.loki.lochat.api.filter.MessageFilter;
import com.loki.lochat.api.service.ChatService;
import com.loki.lochat.api.service.CooldownService;
import com.loki.lochat.api.service.MessageService;
import com.loki.lochat.api.service.MuteService;
import com.loki.lochat.core.filter.CooldownFilter;
import com.loki.lochat.core.filter.MuteFilter;
import com.loki.lochat.core.registry.ServiceRegistry;
import com.loki.lochat.data.model.ChatMessage;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

/**
 * Реализация сервиса обработки сообщений (Chain of Responsibility)
 */
public class MessageServiceImpl implements MessageService {
    private final JavaPlugin plugin;
    private final List<MessageFilter> filters;
    private final ChatService chatService;

    public MessageServiceImpl(JavaPlugin plugin, ServiceRegistry registry) {
        this.plugin = plugin;
        this.chatService = registry.get(ChatService.class);
        this.filters = new ArrayList<>();

        // Регистрируем фильтры в порядке применения
        filters.add(new MuteFilter(registry.get(MuteService.class)));
        filters.add(new CooldownFilter(registry.get(CooldownService.class), plugin));
    }

    @Override
    public boolean processMessage(Player player, String rawMessage) {
        // Создаем объект сообщения
        ChatMessage message = ChatMessage.create(player, rawMessage);

        // Применяем фильтры (мут, кулдаун)
        for (MessageFilter filter : filters) {
            if (!filter.apply(player, message)) {
                return false; // Сообщение заблокировано
            }
        }

        // Возвращаем true - сообщение разрешено
        // Рендерер сам отправит его
        return true;
    }
}
