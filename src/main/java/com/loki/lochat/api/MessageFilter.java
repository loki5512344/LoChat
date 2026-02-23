package com.loki.lochat.api;

import org.bukkit.entity.Player;

/**
 * Интерфейс для фильтрации сообщений
 */
public interface MessageFilter {
    /**
     * Применить фильтр к сообщению
     *
     * @return отфильтрованное сообщение или null если заблокировано
     */
    Object apply(Player player, Object message, String plainMessage);
}
