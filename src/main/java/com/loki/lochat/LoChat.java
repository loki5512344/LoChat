package com.loki.lochat;

import com.loki.lochat.api.service.IgnoreService;
import com.loki.lochat.api.service.MuteService;
import com.loki.lochat.api.service.NickService;
import com.loki.lochat.commands.*;
import com.loki.lochat.config.ConfigManager;
import com.loki.lochat.config.MessageConfig;
import com.loki.lochat.core.registry.ServiceRegistry;
import com.loki.lochat.gradient.GradientModule;
import com.loki.lochat.integrations.LibertyBansHook;
import com.loki.lochat.integrations.PlaceholderAPIHook;
import com.loki.lochat.listener.ChatEventListener;
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
        registerCommands();
        registerListeners();

        // Автосообщения
        autoMessageManager.start();

        getLogger().info("LoChat успешно запущен!");
    }

    @Override
    public void onDisable() {
        if (autoMessageManager != null) autoMessageManager.stop();

        // Сохранение данных через сервисы
        IgnoreService ignoreService = serviceRegistry.get(IgnoreService.class);
        if (ignoreService != null) ignoreService.save();

        MuteService muteService = serviceRegistry.get(MuteService.class);
        if (muteService != null) muteService.save();

        NickService nickService = serviceRegistry.get(NickService.class);
        if (nickService != null) nickService.save();

        if (gradientModule != null) gradientModule.shutdown();
        getLogger().info("LoChat отключен!");
    }

    private void initIntegrations() {
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

    private void registerCommands() {
        getCommand("g").setExecutor(new GlobalChatCommand(this));
        getCommand("l").setExecutor(new LocalChatCommand(this));
        getCommand("msg").setExecutor(new MsgCommand(this));
        getCommand("reply").setExecutor(new ReplyCommand(this));
        getCommand("ignore").setExecutor(new IgnoreCommand(this));
        getCommand("unignore").setExecutor(new UnignoreCommand(this));
        getCommand("announce").setExecutor(new AnnounceCommand(this));
        getCommand("chatspy").setExecutor(new ChatSpyCommand(this));
        getCommand("clearchat").setExecutor(new ClearChatCommand(this));

        NickCommand nickCmd = new NickCommand(this);
        getCommand("nick").setExecutor(nickCmd);
        getCommand("nick").setTabCompleter(nickCmd);

        LoChatCommand loChatCmd = new LoChatCommand(this);
        getCommand("lochat").setExecutor(loChatCmd);
        getCommand("lochat").setTabCompleter(loChatCmd);

        MuteCommand muteCmd = new MuteCommand(this);
        getCommand("lmute").setExecutor(muteCmd);
        getCommand("lmute").setTabCompleter(muteCmd);

        UnmuteCommand unmuteCmd = new UnmuteCommand(this);
        getCommand("lunmute").setExecutor(unmuteCmd);
        getCommand("lunmute").setTabCompleter(unmuteCmd);

        getCommand("lmutelist").setExecutor(new MuteListCommand(this));

        MuteHistoryCommand historyCmd = new MuteHistoryCommand(this);
        getCommand("lmutehistory").setExecutor(historyCmd);
        getCommand("lmutehistory").setTabCompleter(historyCmd);

        MuteBlameCommand blameCmd = new MuteBlameCommand(this);
        getCommand("lmuteblame").setExecutor(blameCmd);
        getCommand("lmuteblame").setTabCompleter(blameCmd);

        ClearChatConfigCommand clearCfgCmd = new ClearChatConfigCommand(this);
        getCommand("clearchatconfig").setExecutor(clearCfgCmd);
        getCommand("clearchatconfig").setTabCompleter(clearCfgCmd);
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new ChatEventListener(this, serviceRegistry), this);
        getServer().getPluginManager().registerEvents(new PlayerEventListener(serviceRegistry), this);
        getLogger().info("Слушатели зарегистрированы (новая архитектура)");
    }

    public void reload() {
        configManager.reload();
        messageConfig.reload();
        autoMessageManager.reload();
        if (gradientModule != null) gradientModule.reload();
        if (customCommandManager != null) customCommandManager.reload();
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
}
