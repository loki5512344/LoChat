package com.loki.lochat.core.service.mute.strategies;

import java.util.UUID;

/**
 * Стратегия мута в чате
 * Базовая реализация - мут проверяется через MuteService.isMuted()
 */
public class ChatMuteStrategy implements MuteStrategy {

    @Override
    public void apply(UUID playerUuid) {
        // Чат мут работает через проверку в фильтрах
        // Не требует дополнительных действий при применении
    }

    @Override
    public void remove(UUID playerUuid) {
        // Чат мут работает через проверку в фильтрах
        // Не требует дополнительных действий при снятии
    }

    @Override
    public String getName() {
        return "Chat";
    }
}
