package com.loki.lochat.api.service;

import java.util.UUID;

/**
 * Объединённый сервис для управления данными игроков
 * Включает: кулдауны (Cooldown), статистику (PlayerData)
 */
public interface PlayerService {
    
    // ========== Cooldown ==========
    
    /**
     * Проверяет, находится ли игрок на кулдауне
     */
    boolean isOnCooldown(UUID player, String type, int cooldownSeconds);
    
    /**
     * Получает оставшееся время кулдауна в секундах
     */
    int getRemainingCooldown(UUID player, String type, int cooldownSeconds);
    
    /**
     * Устанавливает кулдаун для игрока
     */
    void setCooldown(UUID player, String type);
    
    /**
     * Удаляет все кулдауны игрока
     */
    void removeCooldown(UUID player);
    
    // ========== Player Statistics ==========
    
    /**
     * Записывает отправленное сообщение в статистику
     * @param player UUID игрока
     * @param chatType тип чата: "global", "local", "pm"
     */
    void recordMessage(UUID player, String chatType);
    
    /**
     * Получает количество сообщений игрока за текущую сессию
     */
    long getPlayerMessages(UUID player);
    
    /**
     * Очищает данные игрока (кулдауны + сохраняет статистику)
     */
    void clearPlayerData(UUID player);
    
    /**
     * Сохраняет всю статистику на диск
     */
    void saveAll();
}
