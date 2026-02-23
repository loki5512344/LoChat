package com.loki.lochat.core.registry;

import com.loki.lochat.api.service.*;
import com.loki.lochat.config.ConfigManager;
import com.loki.lochat.config.MessageConfig;
import com.loki.lochat.core.service.*;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

/**
 * Реестр сервисов - Dependency Injection контейнер (SOLID DIP)
 */
public class ServiceRegistry {
    private final Map<Class<?>, Object> services = new HashMap<>();
    private final JavaPlugin plugin;
    
    public ServiceRegistry(JavaPlugin plugin) {
        this.plugin = plugin;
        registerServices();
    }
    
    public ServiceRegistry(JavaPlugin plugin, ConfigManager configManager, MessageConfig messageConfig) {
        this.plugin = plugin;
        registerServicesWithDeps(configManager, messageConfig);
    }
    
    private void registerServices() {
        // Регистрируем все сервисы (базовая версия)
        register(ChatService.class, new ChatServiceImpl(plugin, this));
        register(MuteService.class, new MuteServiceImpl(plugin));
        register(CooldownService.class, new CooldownServiceImpl());
        register(MessageService.class, new MessageServiceImpl(plugin, this));
        register(PlayerDataService.class, new PlayerDataServiceImpl(plugin));
    }
    
    private void registerServicesWithDeps(ConfigManager configManager, MessageConfig messageConfig) {
        // Регистрируем все сервисы с зависимостями
        register(ChatService.class, new ChatServiceImpl(plugin, this));
        register(MuteService.class, new MuteServiceImpl(plugin));
        register(CooldownService.class, new CooldownServiceImpl());
        register(MessageService.class, new MessageServiceImpl(plugin, this));
        register(PlayerDataService.class, new PlayerDataServiceImpl(plugin));
        
        // Новые сервисы
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
