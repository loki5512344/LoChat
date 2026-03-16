package com.loki.lochat;

import com.loki.lochat.api.service.IgnoreService;
import com.loki.lochat.api.service.MuteService;
import com.loki.lochat.api.service.NickService;
import com.loki.lochat.commands.*;
import com.loki.lochat.config.ConfigManager;
import com.loki.lochat.config.MessageConfig;
import com.loki.lochat.core.registry.ServiceRegistry;
import com.loki.lochat.gradient.GradientModule;
import com.loki.lochat.integrations.DiscordIntegration;
import com.loki.lochat.integrations.LibertyBansHook;
import com.loki.lochat.integrations.PlaceholderAPIHook;
import com.loki.lochat.core.filter.AdvancedMessageFilter;
import com.loki.lochat.listener.ChatEventListener;
import com.loki.lochat.listener.DiscordEventListener;
import com.loki.lochat.listener.PlayerEventListener;
import com.loki.lochat.managers.AutoMessageManager;
import com.loki.lochat.managers.CustomCommandManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Главный класс плагина LoChat
 * Использует SOLID архитектуру с Dependency Injection через ServiceRegistry
 */
public final class LoChat extends JavaPlugin {

    private static LoChat instance;
    private ServiceRegistry serviceRegistry;
    private ConfigManager configManager;
    private MessageConfig messageConfig;
    private GradientModule gradientModule;
    private AutoMessageManager autoMessageManager;
    private CustomCommandManager customCommandManager;
    private DiscordIntegration discordIntegration;
    private CommandManager commandManager;
    private AdvancedMessageFilter advancedMessageFilter;

    // Геттеры
    public static LoChat getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;
        getLogger().info("Загрузка LoChat v" + getPluginMeta().getVersion() + " для Paper/Folia " + Bukkit.getMinecraftVersion() + "...");

        // Конфиги
        configManager = new ConfigManager(this);
        messageConfig = new MessageConfig(this);

        // Service Registry (SOLID DI) - с зависимостями
        serviceRegistry = ServiceRegistry.createWithDeps(this, configManager, messageConfig);
        getLogger().info("ServiceRegistry инициализирован (SOLID DI)");

        // Менеджеры
        customCommandManager = CustomCommandManager.create(this);
        autoMessageManager = new AutoMessageManager(this);

        // Интеграции
        initIntegrations();

        // Команды и слушатели
        commandManager = new CommandManager(this);
        commandManager.registerCommands();
        registerListeners();
        registerPluginChannels();

        // Автосообщения
        autoMessageManager.start();

        getLogger().info("LoChat успешно запущен!");
    }

    @Override
    public void onDisable() {
        if (autoMessageManager != null) autoMessageManager.stop();

        // Сохраняем статистику
        com.loki.lochat.api.service.PlayerDataService pds = serviceRegistry.get(com.loki.lochat.api.service.PlayerDataService.class);
        if (pds != null) pds.saveAll();

        // Сохранение данных через сервисы
        IgnoreService ignoreService = serviceRegistry.get(IgnoreService.class);
        if (ignoreService != null) ignoreService.save();

        MuteService muteService = serviceRegistry.get(MuteService.class);
        if (muteService != null) muteService.save();

        NickService nickService = serviceRegistry.get(NickService.class);
        if (nickService != null) nickService.save();

        // Останавливаем Discord executor
        if (discordIntegration != null) discordIntegration.shutdown();

        if (gradientModule != null) gradientModule.shutdown();
        getLogger().info("LoChat отключен!");
    }

    private void initIntegrations() {
        // Discord интеграция
        discordIntegration = new DiscordIntegration(this);
        
        // Gradient модуль
        gradientModule = new GradientModule(this);
        if (gradientModule.init()) {
            getLogger().info("Gradient модуль (LoPreff) загружен!");
        }

        // LibertyBans
        new LibertyBansHook(this);

        // PlaceholderAPI
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new PlaceholderAPIHook(this).register();
            getLogger().info("PlaceholderAPI подключен!");
        }
    }

    // Старый метод регистрации команд - заменен на CommandManager
    /*
    private void registerCommands() {
        // Код перенесен в CommandManager для лучшей организации
    }
    */

    private void registerListeners() {
        // Создаём один экземпляр фильтра и передаём в оба листенера — flood/spam трекеры общие
        advancedMessageFilter = new AdvancedMessageFilter(getConfig(), this);
        getServer().getPluginManager().registerEvents(new ChatEventListener(this, serviceRegistry, advancedMessageFilter), this);
        getServer().getPluginManager().registerEvents(new PlayerEventListener(serviceRegistry, advancedMessageFilter), this);
        
        // Discord listener
        if (discordIntegration.isEnabled()) {
            getServer().getPluginManager().registerEvents(new DiscordEventListener(discordIntegration), this);
            getLogger().info("Discord event listener registered");
        }
        
        getLogger().info("Слушатели зарегистрированы (новая архитектура)");
    }

    private void registerPluginChannels() {
        getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        getLogger().info("BungeeCord plugin messaging channel registered");
    }

    public void reload() {
        configManager.reload();
        messageConfig.reload();
        autoMessageManager.reload();
        if (gradientModule != null) gradientModule.reload();
        if (customCommandManager != null) customCommandManager.reload();
        if (discordIntegration != null) discordIntegration.reload();
    }

    public ServiceRegistry getServiceRegistry() {
        return serviceRegistry;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public MessageConfig getMessageConfig() {
        return messageConfig;
    }

    public GradientModule getGradientModule() {
        return gradientModule;
    }

    public CustomCommandManager getCustomCommandManager() {
        return customCommandManager;
    }

    public CommandManager getCommandManager() {
        return commandManager;
    }

    public DiscordIntegration getDiscordIntegration() {
        return discordIntegration;
    }
}
