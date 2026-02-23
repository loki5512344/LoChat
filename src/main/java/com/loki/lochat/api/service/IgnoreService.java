package com.loki.lochat.api.service;

import java.util.Set;
import java.util.UUID;

/**
 * Сервис для управления списком игнорируемых игроков
 */
public interface IgnoreService {
    /**
     * Проверяет, игнорирует ли игрок другого
     */
    boolean isIgnoring(UUID player, UUID target);
    
    /**
     * Добавляет игрока в список игнорируемых
     * @return true если успешно добавлен
     */
    boolean addIgnore(UUID player, UUID target);
    
    /**
     * Удаляет игрока из списка игнорируемых
     * @return true если успешно удалён
     */
    boolean removeIgnore(UUID player, UUID target);
    
    /**
     * Получает список всех игнорируемых игроков
     */
    Set<UUID> getIgnoredPlayers(UUID player);
    
    /**
     * Получает количество игнорируемых игроков
     */
    int getIgnoredCount(UUID player);
    
    /**
     * Очищает список игнорируемых для игрока
     */
    void clearIgnores(UUID player);
    
    /**
     * Сохраняет данные на диск
     */
    void save();
    
    /**
     * Загружает данные с диска
     */
    void load();
}
