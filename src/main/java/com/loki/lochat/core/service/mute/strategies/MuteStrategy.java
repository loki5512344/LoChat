package com.loki.lochat.core.service.mute.strategies;

import java.util.UUID;

/**
 * Стратегия применения мута
 * Strategy pattern для разных типов мутов (чат, голос)
 */
public interface MuteStrategy {

    /**
     * Применить мут к игроку
     */
    void apply(UUID playerUuid);

    /**
     * Снять мут с игрока
     */
    void remove(UUID playerUuid);

    /**
     * Название стратегии для логирования
     */
    String getName();
}
