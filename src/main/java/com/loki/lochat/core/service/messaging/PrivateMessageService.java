package com.loki.lochat.core.service.messaging;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Сервис личных сообщений (PM)
 * Отслеживает последние диалоги между игроками
 */
public class PrivateMessageService {

    private final Map<UUID, UUID> lastConversation = new ConcurrentHashMap<>();

    public void setLastConversation(UUID player, UUID target) {
        lastConversation.put(player, target);
    }

    public Optional<UUID> getLastConversation(UUID player) {
        return Optional.ofNullable(lastConversation.get(player));
    }

    public void removeConversation(UUID player) {
        lastConversation.remove(player);
    }

    public boolean hasConversation(UUID player) {
        return lastConversation.containsKey(player);
    }
}
