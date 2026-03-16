package com.loki.lochat.core.registry;

import com.loki.lochat.api.service.*;
import com.loki.lochat.config.ConfigManager;
import com.loki.lochat.config.MessageConfig;
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
        register(MuteService.class, new MuteServiceImpl(plugin));

        // CooldownService регистрируем раньше PlayerDataService, чтобы передать его как зависимость
        CooldownServiceImpl cooldownService = new CooldownServiceImpl();
        register(CooldownService.class, cooldownService);

        register(MessageService.class, new MessageServiceImpl(plugin, this));

        // Передаём тот же экземпляр CooldownService, а не новый — иначе очистка при выходе не работает
        register(PlayerDataService.class, new PlayerDataServiceImpl(plugin, cooldownService));

        register(PMService.class, new PMServiceImpl());
        register(IgnoreService.class, new IgnoreServiceImpl(plugin));
        register(SpyService.class, new SpyServiceImpl(plugin, messageConfig));
        register(MentionService.class, new MentionServiceImpl(configManager));
        register(NickService.class, new NickServiceImpl(plugin));
    }

    public <T> void register(Class<T> serviceClass, T implementation) {
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
