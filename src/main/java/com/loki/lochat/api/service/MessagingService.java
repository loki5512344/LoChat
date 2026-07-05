package com.loki.lochat.api.service;

import net.kyori.adventure.text.Component;

import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Объединённый сервис для управления общением между игроками
 * Включает: личные сообщения (PM), шпионаж (Spy), игнорирование (Ignore)
 */
public interface MessagingService {
    
    // ========== PM (Private Messages) ==========
    
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
    
    // ========== Spy (Шпионаж) ==========
    
    /**
     * Переключает режим шпиона для игрока
     * @return true если шпион включен, false если выключен
     */
    boolean toggleSpy(UUID player);
    
    /**
     * Проверяет, находится ли игрок в режиме шпиона
     */
    boolean isSpying(UUID player);
    
    /**
     * Отправляет сообщение всем шпионам (для обычного чата)
     */
    void sendToSpies(Player sender, Component message, boolean isGlobal);
    
    /**
     * Отправляет информацию о личном сообщении шпионам
     */
    void broadcastPM(Player sender, Player receiver, String message);
    
    /**
     * Удаляет игрока из списка шпионов
     */
    void removeSpy(UUID player);
    
    // ========== Ignore (Игнорирование) ==========
    
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
    
    // ========== Persistence ==========
    
    /**
     * Сохраняет данные на диск
     */
    void save();
    
    /**
     * Загружает данные с диска
     */
    void load();
}
