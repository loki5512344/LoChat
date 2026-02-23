package com.loki.lochat.api.service;

import org.bukkit.entity.Player;

/**
 * Сервис обработки сообщений
 */
public interface MessageService {
    /**
     * Обработать входящее сообщение чата
     * @return true если сообщение обработано успешно
     */
    boolean processMessage(Player player, String message);
}
