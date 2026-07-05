package com.loki.lochat.core.service;

import com.loki.lochat.api.filter.MessageFilter;
import com.loki.lochat.api.service.MessageService;
import com.loki.lochat.api.service.MuteService;
import com.loki.lochat.api.service.PlayerService;
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
    private final List<MessageFilter> filters;

    public MessageServiceImpl(JavaPlugin plugin, ServiceRegistry registry) {
        this.filters = new ArrayList<>();

        // Регистрируем фильтры в порядке применения
        // MuteFilter теперь получает plugin для доступа к HardcodedMessages
        filters.add(new MuteFilter(registry.get(MuteService.class), plugin));
        filters.add(new CooldownFilter(registry.get(PlayerService.class), plugin));
    }

    @Override
    public boolean processMessage(Player player, String rawMessage) {
        ChatMessage message = ChatMessage.create(player, rawMessage);

        for (MessageFilter filter : filters) {
            if (!filter.apply(player, message)) {
                return false;
            }
        }

        return true;
    }
}
