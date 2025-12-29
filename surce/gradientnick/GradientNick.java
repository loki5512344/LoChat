package ru.lovar.gradientnick;

import org.black_ixx.playerpoints.PlayerPoints;
import org.black_ixx.playerpoints.PlayerPointsAPI;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import ru.lovar.gradientnick.commands.ColorCommand;
import ru.lovar.gradientnick.commands.GradientCommand;
import ru.lovar.gradientnick.commands.PrefixCommand;
import ru.lovar.gradientnick.config.ConfigManager;
import ru.lovar.gradientnick.config.MessagesManager;
import ru.lovar.gradientnick.data.DataManager;
import ru.lovar.gradientnick.hooks.LuckPermsHook;
import ru.lovar.gradientnick.hooks.PlaceholderAPIHook;
import ru.lovar.gradientnick.listeners.GUIListener;
import ru.lovar.gradientnick.listeners.PlayerListener;

public class GradientNick extends JavaPlugin {

    private static GradientNick instance;
    private PlayerPointsAPI playerPointsAPI;
    private ConfigManager configManager;
    private MessagesManager messagesManager;
    private DataManager dataManager;
    private LuckPermsHook luckPermsHook;

    @Override
    public void onEnable() {
        instance = this;
        
        // Инициализация конфигов
        this.configManager = new ConfigManager(this);
        this.messagesManager = new MessagesManager(this);
        
        // Инициализация хранилища данных
        this.dataManager = new DataManager(this);
        
        // Подключение PlayerPoints
        if (Bukkit.getPluginManager().isPluginEnabled("PlayerPoints")) {
            this.playerPointsAPI = PlayerPoints.getInstance().getAPI();
            getLogger().info("PlayerPoints подключен!");
        } else {
            getLogger().severe("PlayerPoints не найден! Плагин отключается.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        
        // Подключение LuckPerms
        this.luckPermsHook = new LuckPermsHook(this);
        
        // Подключение PlaceholderAPI
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new PlaceholderAPIHook(this).register();
            getLogger().info("PlaceholderAPI подключен!");
        }
        
        // Регистрация команд
        ColorCommand colorCommand = new ColorCommand(this);
        getCommand("color").setExecutor(colorCommand);
        getCommand("color").setTabCompleter(colorCommand);
        
        PrefixCommand prefixCommand = new PrefixCommand(this);
        getCommand("prefix").setExecutor(prefixCommand);
        getCommand("prefix").setTabCompleter(prefixCommand);
        
        GradientCommand gradientCommand = new GradientCommand(this);
        getCommand("aprefix").setExecutor(gradientCommand);
        getCommand("aprefix").setTabCompleter(gradientCommand);
        
        // Регистрация слушателей
        Bukkit.getPluginManager().registerEvents(new PlayerListener(this), this);
        Bukkit.getPluginManager().registerEvents(new GUIListener(this), this);
        
        getLogger().info("LoPreff успешно запущен!");
    }

    @Override
    public void onDisable() {
        if (dataManager != null) {
            dataManager.saveAll();
        }
        getLogger().info("LoPreff отключен!");
    }

    public static GradientNick getInstance() {
        return instance;
    }

    public PlayerPointsAPI getPlayerPointsAPI() {
        return playerPointsAPI;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public MessagesManager getMessagesManager() {
        return messagesManager;
    }

    public DataManager getDataManager() {
        return dataManager;
    }

    public LuckPermsHook getLuckPermsHook() {
        return luckPermsHook;
    }

    public void reload() {
        configManager.reload();
        messagesManager.reload();
    }
}
