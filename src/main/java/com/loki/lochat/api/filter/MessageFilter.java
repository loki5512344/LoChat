package com.loki.lochat.api.filter;

import com.loki.lochat.data.model.ChatMessage;
import org.bukkit.entity.Player;

/**
 * Интерфейс фильтра сообщений (Chain of Responsibility)
 */
public interface MessageFilter {
    /**
     * Применить фильтр к сообщению
     * @return true если сообщение прошло фильтр, false если заблокировано
     */
    boolean apply(Player player, ChatMessage message);
}
