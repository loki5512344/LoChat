package com.loki.lochat.api.service;

import java.util.Optional;
import java.util.UUID;

/**
 * Сервис для управления личными сообщениями
 */
public interface PMService {
    /**
     * Устанавливает последнего собеседника для игрока
     */
    void setLastConversation(UUID player, UUID target);

    /**
     * Получает последнего собеседника игрока
     */
    Optional<UUID> getLastConversation(UUID player);

    /**
     * Удаляет информацию о последнем разговоре
     */
    void removeConversation(UUID player);

    /**
     * Проверяет, есть ли у игрока активный разговор
     */
    boolean hasConversation(UUID player);
}
