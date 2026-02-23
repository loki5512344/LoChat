package com.loki.lochat.api.service;

import java.util.UUID;

/**
 * Сервис для работы с данными игроков
 */
public interface PlayerDataService {
    void clearPlayerData(UUID player);

    void saveAll();
}
