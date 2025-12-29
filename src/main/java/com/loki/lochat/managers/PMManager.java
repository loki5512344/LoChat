package com.loki.lochat.managers;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PMManager {

    // UUID игрока -> UUID последнего собеседника
    private final Map<UUID, UUID> lastConversation = new ConcurrentHashMap<>();

    public void setLastConversation(UUID player, UUID target) {
        lastConversation.put(player, target);
    }

    public UUID getLastConversation(UUID player) {
        return lastConversation.get(player);
    }

    public void removeConversation(UUID player) {
        lastConversation.remove(player);
    }

    public boolean hasConversation(UUID player) {
        return lastConversation.containsKey(player);
    }
}
