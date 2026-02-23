package com.loki.lochat.api.service;

import org.bukkit.entity.Player;

import java.util.Set;

/**
 * Сервис для обработки упоминаний игроков в чате
 */
public interface MentionService {
    /**
     * Обрабатывает упоминания в сообщении
     * @return сообщение с подсвеченными никами
     */
    String processMentions(String message, Set<Player> mentionedPlayers);
    
    /**
     * Создаёт персонализированное сообщение для игрока
     * Если его ник упомянут — выделяет его особым образом
     */
    String getPersonalizedMessage(String message, Player viewer);
    
    /**
     * Проверяет, упомянут ли игрок в сообщении (по нику без @)
     */
    boolean isPlayerMentioned(String message, Player player);
    
    /**
     * Уведомляет упомянутых игроков звуком
     */
    void notifyMentioned(Set<Player> players);
    
    /**
     * Уведомляет игрока если его ник упомянут (без @)
     */
    void notifyIfMentioned(String message, Player viewer, Player sender);
}
