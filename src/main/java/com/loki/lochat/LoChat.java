package com.loki.lochat;

import com.loki.lochat.commands.*;
import com.loki.lochat.config.ConfigManager;
import com.loki.lochat.config.MessageConfig;
import com.loki.lochat.gradient.GradientModule;
import com.loki.lochat.integrations.PlaceholderAPIHook;
import com.loki.lochat.listeners.ChatListener;
import com.loki.lochat.managers.*;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class LoChat extends JavaPlugin {

    private static LoChat instance;
    
    private ConfigManager configManager;
    private MessageConfig messageConfig;
    private ChatManager chatManager;
    private PMManager pmManager;
    private IgnoreManager ignoreManager;
    private AutoMessageManager autoMessageManager;
    private FilterManager filterManager;
    private AntiSpamManager antiSpamManager;
    private CooldownManager cooldownManager;
    private MentionManager mentionManager;
    private SpyManager spyManager;
    private EmojiManager emojiManager;
    private CustomCommandManager customCommandManager;
    private MuteManager muteManager;
    private GradientModule gradientModule;

    @Override
    public void onEnable() {
        instance = this;
        
        // Инициализация конфигов
        configManager = new ConfigManager(this);
        messageConfig = new MessageConfig(this);
        
        // Инициализация менеджеров
        emojiManager = new EmojiManager(this);
        ignoreManager = new IgnoreManager(this);
        pmManager = new PMManager();
        filterManager = new FilterManager(this);
        antiSpamManager = new AntiSpamManager(this);
        cooldownManager = new CooldownManager();
        mentionManager = new MentionManager(this);
        spyManager = new SpyManager(this);
        customCommandManager = new CustomCommandManager(this);
        muteManager = new MuteManager(this);
        chatManager = new ChatManager(this);
        autoMessageManager = new AutoMessageManager(this);
        
        // Инициализация модуля градиентов (интегрированный LoPreff)
        gradientModule = new GradientModule(this);
        if (gradientModule.init()) {
            getLogger().info("Gradient модуль (LoPreff) загружен!");
        }
        
        // Регистрация команд
        registerCommands();
        
        // Регистрация листенеров
        getServer().getPluginManager().registerEvents(new ChatListener(this), this);
        
        // Запуск автосообщений
        autoMessageManager.start();
        
        // PlaceholderAPI
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new PlaceholderAPIHook(this).register();
            getLogger().info("PlaceholderAPI подключен!");
        }
        
        getLogger().info("LoChat успешно запущен!");
    }

    @Override
    public void onDisable() {
        if (autoMessageManager != null) {
            autoMessageManager.stop();
        }
        if (ignoreManager != null) {
            ignoreManager.save();
        }
        if (muteManager != null) {
            muteManager.saveData();
        }
        if (gradientModule != null) {
            gradientModule.shutdown();
        }
        getLogger().info("LoChat отключен!");
    }
    
    private void registerCommands() {
        getCommand("g").setExecutor(new GlobalChatCommand(this));
        getCommand("globalchat").setExecutor(new GlobalChatCommand(this));
        getCommand("l").setExecutor(new LocalChatCommand(this));
        getCommand("msg").setExecutor(new MsgCommand(this));
        getCommand("reply").setExecutor(new ReplyCommand(this));
        getCommand("ignore").setExecutor(new IgnoreCommand(this));
        getCommand("unignore").setExecutor(new UnignoreCommand(this));
        getCommand("announce").setExecutor(new AnnounceCommand(this));
        getCommand("chatspy").setExecutor(new ChatSpyCommand(this));
        getCommand("clearchat").setExecutor(new ClearChatCommand(this));
        getCommand("mute").setExecutor(new MuteCommand(this));
        getCommand("unmute").setExecutor(new UnmuteCommand(this));
        
        LoChatCommand loChatCommand = new LoChatCommand(this);
        getCommand("lochat").setExecutor(loChatCommand);
        getCommand("lochat").setTabCompleter(loChatCommand);
    }
    
    public void reload() {
        configManager.reload();
        messageConfig.reload();
        emojiManager.reload();
        autoMessageManager.stop();
        autoMessageManager.start();
        if (gradientModule != null) {
            gradientModule.reload();
        }
        if (customCommandManager != null) {
            customCommandManager.reload();
        }
        if (filterManager != null) {
            filterManager.reload();
        }
    }

    public static LoChat getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public MessageConfig getMessageConfig() {
        return messageConfig;
    }

    public ChatManager getChatManager() {
        return chatManager;
    }

    public PMManager getPmManager() {
        return pmManager;
    }

    public IgnoreManager getIgnoreManager() {
        return ignoreManager;
    }

    public AutoMessageManager getAutoMessageManager() {
        return autoMessageManager;
    }

    public FilterManager getFilterManager() {
        return filterManager;
    }

    public AntiSpamManager getAntiSpamManager() {
        return antiSpamManager;
    }

    public CooldownManager getCooldownManager() {
        return cooldownManager;
    }

    public MentionManager getMentionManager() {
        return mentionManager;
    }

    public SpyManager getSpyManager() {
        return spyManager;
    }

    public EmojiManager getEmojiManager() {
        return emojiManager;
    }

    public GradientModule getGradientModule() {
        return gradientModule;
    }

    public CustomCommandManager getCustomCommandManager() {
        return customCommandManager;
    }

    public MuteManager getMuteManager() {
        return muteManager;
    }
}
