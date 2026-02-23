package com.loki.lochat.api;

import org.bukkit.entity.Player;

/**
 * Сервис для отправки сообщений в чат
 */
public interface ChatService {
    void sendGlobalMessage(Player sender, Object message);
    void sendLocalMessage(Player sender, Object message);
    boolean toggleGlobalChat(java.util.UUID player);
    boolean isGlobalChatDisabled(java.util.UUID player);
}
