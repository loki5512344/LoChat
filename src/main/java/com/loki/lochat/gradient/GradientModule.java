package com.loki.lochat.gradient;

import com.loki.lochat.gradient.commands.GradientAdminCommand;
import com.loki.lochat.gradient.commands.GradientColorCommand;
import com.loki.lochat.gradient.commands.GradientPrefixCommand;
import com.loki.lochat.gradient.config.GradientConfig;
import com.loki.lochat.gradient.config.GradientMessages;
import com.loki.lochat.gradient.data.GradientDataManager;
import com.loki.lochat.gradient.data.GradientPlayerData;
import com.loki.lochat.gradient.hooks.GradientLuckPermsHook;
import com.loki.lochat.gradient.listeners.GradientGUIListener;
import com.loki.lochat.gradient.listeners.GradientPlayerListener;
import com.loki.lochat.gradient.util.DisplayNameUtil;
import com.loki.lochat.gradient.util.GradientUtil;
import org.black_ixx.playerpoints.PlayerPoints;
import org.black_ixx.playerpoints.PlayerPointsAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Модуль градиентных ников (интегрированный LoPreff)
 */
public class GradientModule {

    private final JavaPlugin plugin;
    private GradientConfig config;
    private GradientMessages messages;
    private GradientDataManager dataManager;
    private GradientLuckPermsHook luckPermsHook;
    private PlayerPointsAPI playerPointsAPI;
    private boolean enabled;

    public GradientModule(JavaPlugin plugin) {
        this.plugin = plugin;
        this.enabled = false;
    }

    /**
     * Инициализация модуля
     * @return true если модуль успешно инициализирован
     */
    public boolean init() {
        // Загружаем конфиги
        config = new GradientConfig(plugin);
        messages = new GradientMessages(plugin);
        
        if (!config.isEnabled()) {
            plugin.getLogger().info("Gradient модуль отключен в конфиге.");
            return false;
        }
        
        // Проверяем PlayerPoints (опционально теперь)
        if (Bukkit.getPluginManager().isPluginEnabled("PlayerPoints")) {
            playerPointsAPI = PlayerPoints.getInstance().getAPI();
            plugin.getLogger().info("Gradient: PlayerPoints подключен!");
        } else {
            plugin.getLogger().warning("Gradient: PlayerPoints не найден. Покупки будут бесплатными.");
        }
        
        // Инициализация хранилища
        dataManager = new GradientDataManager(plugin, config);
        
        // Подключение LuckPerms
        luckPermsHook = new GradientLuckPermsHook(plugin);
        
        // Регистрация команд
        registerCommands();
        
        // Регистрация слушателей
        Bukkit.getPluginManager().registerEvents(new GradientPlayerListener(this), plugin);
        Bukkit.getPluginManager().registerEvents(new GradientGUIListener(this), plugin);
        
        enabled = true;
        plugin.getLogger().info("Gradient модуль успешно загружен!");
        return true;
    }

    private void registerCommands() {
        GradientColorCommand colorCommand = new GradientColorCommand(this);
        if (plugin.getCommand("color") != null) {
            plugin.getCommand("color").setExecutor(colorCommand);
            plugin.getCommand("color").setTabCompleter(colorCommand);
        }
        
        GradientPrefixCommand prefixCommand = new GradientPrefixCommand(this);
        if (plugin.getCommand("prefix") != null) {
            plugin.getCommand("prefix").setExecutor(prefixCommand);
            plugin.getCommand("prefix").setTabCompleter(prefixCommand);
        }
        
        GradientAdminCommand adminCommand = new GradientAdminCommand(this);
        if (plugin.getCommand("aprefix") != null) {
            plugin.getCommand("aprefix").setExecutor(adminCommand);
            plugin.getCommand("aprefix").setTabCompleter(adminCommand);
        }
    }

    /**
     * Выключение модуля
     */
    public void shutdown() {
        if (dataManager != null) {
            dataManager.saveAll();
        }
    }

    /**
     * Перезагрузка модуля
     */
    public void reload() {
        config.reload();
        messages.reload();
    }

    /**
     * Получает отформатированное имя игрока с градиентом
     */
    public String getFormattedName(Player player) {
        if (!enabled) return player.getName();
        return DisplayNameUtil.getFullDisplayName(this, player);
    }

    /**
     * Получает только градиентный ник (без префикса)
     */
    public String getGradientNick(Player player) {
        if (!enabled) return player.getName();
        
        GradientPlayerData data = dataManager.getPlayerData(player.getUniqueId());
        if (!data.hasColors() || !data.isColorEnabled()) {
            return player.getName();
        }
        
        return GradientUtil.applyGradient(player.getName(), data.getColors(), config.isUseLegacyRgbFormat());
    }

    /**
     * Получает только префикс игрока
     */
    public String getPrefix(Player player) {
        if (!enabled) return "";
        
        GradientPlayerData data = dataManager.getPlayerData(player.getUniqueId());
        if (!data.hasPrefix() || !data.isPrefixEnabled()) {
            return "";
        }
        
        String prefix = config.getPrefixFormat().replace("{prefix}", data.getPrefix());
        if (data.hasColors() && data.isColorEnabled() && config.isGradientOnPrefix()) {
            return GradientUtil.applyGradient(prefix, data.getColors(), config.isUseLegacyRgbFormat());
        }
        return prefix;
    }

    // Геттеры
    public JavaPlugin getPlugin() { return plugin; }
    public GradientConfig getConfig() { return config; }
    public GradientMessages getMessages() { return messages; }
    public GradientDataManager getDataManager() { return dataManager; }
    public GradientLuckPermsHook getLuckPermsHook() { return luckPermsHook; }
    public PlayerPointsAPI getPlayerPointsAPI() { return playerPointsAPI; }
    public boolean hasPlayerPoints() { return playerPointsAPI != null; }
    public boolean isEnabled() { return enabled; }
}
