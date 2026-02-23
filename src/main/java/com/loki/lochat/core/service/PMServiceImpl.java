package com.loki.lochat.core.service;

import com.loki.lochat.api.service.PMService;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Реализация сервиса личных сообщений
 */
public class PMServiceImpl implements PMService {
    private final Map<UUID, UUID> lastConversation = new ConcurrentHashMap<>();

    @Override
    public void setLastConversation(UUID player, UUID target) {
        lastConversation.put(player, target);
    }

    @Override
    public Optional<UUID> getLastConversation(UUID player) {
        return Optional.ofNullable(lastConversation.get(player));
    }

    @Override
    public void removeConversation(UUID player) {
        lastConversation.remove(player);
    }

    @Override
    public boolean hasConversation(UUID player) {
        return lastConversation.containsKey(player);
    }
}
