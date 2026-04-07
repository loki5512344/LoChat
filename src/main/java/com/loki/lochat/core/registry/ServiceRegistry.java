package com.loki.lochat.core.registry;

import com.loki.lochat.api.service.*;
import com.loki.lochat.config.ConfigManager;
import com.loki.lochat.config.MessageConfig;
import com.loki.lochat.core.factory.ServiceFactory;
import com.loki.lochat.core.service.*;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

public class ServiceRegistry {
    private final Map<Class<?>, Object> services = new HashMap<>();
    private final JavaPlugin plugin;

    private ServiceRegistry(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public static ServiceRegistry createWithDeps(JavaPlugin plugin, ConfigManager configManager, MessageConfig messageConfig) {
        ServiceRegistry registry = new ServiceRegistry(plugin);
        registry.registerServicesWithDeps(configManager, messageConfig);
        return registry;
    }

    private void registerServicesWithDeps(ConfigManager configManager, MessageConfig messageConfig) {
        register(ChatService.class, new ChatServiceImpl(plugin, this));
        register(MuteService.class, ServiceFactory.createMuteService(plugin));

        // ✅ NEW: Объединённый PlayerService (Cooldown + PlayerData)
        PlayerService playerService = new PlayerServiceImpl(plugin);
        register(PlayerService.class, playerService);

        register(MessageService.class, new MessageServiceImpl(plugin, this));

        // ✅ NEW: Объединённый MessagingService (PM + Spy + Ignore)
        MessagingService messagingService = new MessagingServiceImpl(plugin, messageConfig);
        register(MessagingService.class, messagingService);

        register(MentionService.class, new MentionServiceImpl(configManager));
        register(NickService.class, new NickServiceImpl(plugin));
        register(PunishmentService.class, new PunishmentServiceImpl(plugin, configManager.getMessagesConfig()));
    }

    public <T> void register(Class<T> serviceClass, T implementation) {
        services.put(serviceClass, implementation);
    }
    
    /**
     * Регистрирует сервис с возможностью приведения типов (для обратной совместимости)
     */
    @SuppressWarnings("unchecked")
    public void registerCompat(Class<?> serviceClass, Object implementation) {
        services.put(serviceClass, implementation);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(Class<T> serviceClass) {
        Object service = services.get(serviceClass);
        if (service == null) {
            throw new IllegalStateException("Service not registered: " + serviceClass.getName());
        }
        return (T) service;
    }

    public void shutdown() {
        services.clear();
    }
}
