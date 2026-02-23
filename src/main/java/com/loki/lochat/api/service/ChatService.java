package com.loki.lochat.api.service;

import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * Сервис для отправки сообщений в чат
 */
public interface ChatService {
    void sendGlobalMessage(Player sender, Object message);

    void sendLocalMessage(Player sender, Object message);

    boolean toggleGlobalChat(UUID player);

    boolean isGlobalChatDisabled(UUID player);
}
