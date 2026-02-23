package com.loki.lochat.api.service;

import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.UUID;

/**
 * Сервис для управления кастомными никами
 */
public interface NickService {

    /**
     * Устанавливает кастомный ник игроку
     *
     * @param player   UUID игрока
     * @param nickname Новый ник (с поддержкой цветов и русских символов)
     * @return true если успешно, false если ник занят или невалиден
     */
    boolean setNickname(UUID player, String nickname);

    /**
     * Сбрасывает кастомный ник игрока
     *
     * @param player UUID игрока
     */
    void resetNickname(UUID player);

    /**
     * Получает кастомный ник игрока
     *
     * @param player UUID игрока
     * @return Optional с ником или empty если нет кастомного ника
     */
    Optional<String> getNickname(UUID player);

    /**
     * Проверяет занят ли ник
     *
     * @param nickname Ник для проверки
     * @return true если ник уже используется
     */
    boolean isNicknameTaken(String nickname);

    /**
     * Валидирует ник (длина, символы)
     *
     * @param nickname Ник для проверки
     * @return true если ник валиден
     */
    boolean isValidNickname(String nickname);

    /**
     * Обновляет displayName и tabName игрока
     *
     * @param player Игрок
     */
    void updatePlayerDisplay(Player player);

    /**
     * Сохраняет данные
     */
    void save();
}
