package com.loki.lochat.core.factory;

import com.loki.lochat.api.service.MessagingService;
import com.loki.lochat.api.service.MuteService;
import com.loki.lochat.config.MessageConfig;
import com.loki.lochat.core.service.MessagingServiceImpl;
import com.loki.lochat.core.service.MuteServiceImpl;
import com.loki.lochat.core.service.mute.MuteDataStorage;
import com.loki.lochat.core.service.mute.MuteHistoryManager;
import com.loki.lochat.core.service.mute.strategies.ChatMuteStrategy;
import com.loki.lochat.core.service.mute.strategies.MuteStrategy;
import com.loki.lochat.core.service.mute.strategies.VoiceMuteStrategy;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.List;

/**
 * Фабрика для создания сервисов
 * Factory pattern для инициализации сложных объектов
 */
public class ServiceFactory {

    /**
     * Создать MuteService со всеми зависимостями
     */
    public static MuteService createMuteService(JavaPlugin plugin) {
        // Создаем стратегии
        List<MuteStrategy> strategies = List.of(
                new ChatMuteStrategy(),
                new VoiceMuteStrategy(plugin)
        );

        // Создаем хранилище данных
        MuteDataStorage storage = new MuteDataStorage(plugin);
        storage.load();

        // Создаем менеджер истории
        File historyFile = new File(plugin.getDataFolder(), "mute-history.json");
        MuteHistoryManager historyManager = new MuteHistoryManager(historyFile);

        // Собираем сервис
        return new MuteServiceImpl(plugin, strategies, storage, historyManager);
    }

    /**
     * Создать MessagingService со всеми зависимостями
     */
    public static MessagingService createMessagingService(JavaPlugin plugin, MessageConfig messageConfig) {
        return new MessagingServiceImpl(plugin, messageConfig);
    }
}
