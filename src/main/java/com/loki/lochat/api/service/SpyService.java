package com.loki.lochat.api.service;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

/**
 * Сервис для шпионажа за чатом
 */
public interface SpyService {
    /**
     * Переключает режим шпиона для игрока
     *
     * @return true если шпион включен, false если выключен
     */
    boolean toggleSpy(java.util.UUID player);

    /**
     * Проверяет, находится ли игрок в режиме шпиона
     */
    boolean isSpying(java.util.UUID player);

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
    void removeSpy(java.util.UUID player);
}
