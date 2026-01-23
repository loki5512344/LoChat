package com.loki.lochat;

import com.loki.lochat.commands.*;
import com.loki.lochat.config.ConfigManager;
import com.loki.lochat.config.MessageConfig;
import com.loki.lochat.gradient.GradientModule;
import com.loki.lochat.integrations.LibertyBansHook;
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
    private AntiSpamManager antiSpamManager;
    private CooldownManager cooldownManager;
    private MentionManager mentionManager;
    private SpyManager spyManager;
    private EmojiManager emojiManager;
    private CustomCommandManager customCommandManager;
    private GradientModule gradientModule;
    private LibertyBansHook libertyBansHook;
    private MuteManager muteManager;
    private HeadEmojiManager headEmojiManager;
    private com.loki.lochat.integrations.SkinsRestorerHook skinsRestorerHook;

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
        antiSpamManager = new AntiSpamManager(this);
        cooldownManager = new CooldownManager();
        mentionManager = new MentionManager(this);
        spyManager = new SpyManager(this);
        customCommandManager = new CustomCommandManager(this);
        muteManager = new MuteManager(this);
        chatManager = new ChatManager(this);
        autoMessageManager = new AutoMessageManager(this);
        
        // Инициализация интеграций
        skinsRestorerHook = new com.loki.lochat.integrations.SkinsRestorerHook(this);
        headEmojiManager = new com.loki.lochat.managers.HeadEmojiManager(this);
        
        // Инициализация модуля градиентов (интегрированный LoPreff)
        gradientModule = new GradientModule(this);
        if (gradientModule.init()) {
            getLogger().info("Gradient модуль (LoPreff) загружен!");
        }
        
        // Инициализация LibertyBans интеграции
        libertyBansHook = new LibertyBansHook(this);
        
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
        if (gradientModule != null) {
            gradientModule.shutdown();
        }
        if (muteManager != null) {
            muteManager.save();
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
        
        LoChatCommand loChatCommand = new LoChatCommand(this);
        getCommand("lochat").setExecutor(loChatCommand);
        getCommand("lochat").setTabCompleter(loChatCommand);
        
        // Команды мутов
        MuteCommand muteCommand = new MuteCommand(this);
        getCommand("lmute").setExecutor(muteCommand);
        getCommand("lmute").setTabCompleter(muteCommand);
        
        UnmuteCommand unmuteCommand = new UnmuteCommand(this);
        getCommand("lunmute").setExecutor(unmuteCommand);
        getCommand("lunmute").setTabCompleter(unmuteCommand);
        
        getCommand("lmutelist").setExecutor(new MuteListCommand(this));
        
        MuteHistoryCommand muteHistoryCommand = new MuteHistoryCommand(this);
        getCommand("lmutehistory").setExecutor(muteHistoryCommand);
        getCommand("lmutehistory").setTabCompleter(muteHistoryCommand);
        
        MuteBlameCommand muteBlameCommand = new MuteBlameCommand(this);
        getCommand("lmuteblame").setExecutor(muteBlameCommand);
        getCommand("lmuteblame").setTabCompleter(muteBlameCommand);
        
        // Команда настройки очистки чата
        ClearChatConfigCommand clearChatConfigCommand = new ClearChatConfigCommand(this);
        getCommand("clearchatconfig").setExecutor(clearChatConfigCommand);
        getCommand("clearchatconfig").setTabCompleter(clearChatConfigCommand);
        
        // Внутренняя команда для информации об игроке
        getCommand("lochat").setExecutor(new PlayerInfoCommand(this));
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

    public LibertyBansHook getLibertyBansHook() {
        return libertyBansHook;
    }

    public MuteManager getMuteManager() {
        return muteManager;
    }

    public HeadEmojiManager getHeadEmojiManager() {
        return headEmojiManager;
    }

    public com.loki.lochat.integrations.SkinsRestorerHook getSkinsRestorerHook() {
        return skinsRestorerHook;
    }

}
