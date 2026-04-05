package com.loki.lochat.gradient;

import com.loki.lochat.gradient.commands.GradientAdminCommand;
import com.loki.lochat.gradient.commands.GradientColorCommand;
import com.loki.lochat.gradient.commands.GradientPrefixCommand;
import com.loki.lochat.gradient.config.GradientConfig;
import com.loki.lochat.gradient.config.GradientMessages;
import com.loki.lochat.gradient.data.GradientDataManager;
import com.loki.lochat.gradient.data.GradientPlayerData;
import com.loki.lochat.gradient.listeners.GradientGUIListener;
import com.loki.lochat.gradient.listeners.GradientPlayerListener;
import com.loki.lochat.gradient.service.GradientService;
import com.loki.lochat.gradient.service.PrefixService;
import org.black_ixx.playerpoints.PlayerPoints;
import org.black_ixx.playerpoints.PlayerPointsAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Модуль градиентных ников - упрощенная версия
 */
public class GradientModule {

    private final JavaPlugin plugin;
    private GradientConfig config;
    private GradientMessages messages;
    private GradientDataManager dataManager;
    private PrefixService prefixService;
    private PlayerPointsAPI playerPointsAPI;
    private boolean enabled;

    public GradientModule(JavaPlugin plugin) {
        this.plugin = plugin;
        this.enabled = false;
    }

    public boolean init() {
        // Загружаем конфиги
        config = new GradientConfig(plugin);
        messages = new GradientMessages(plugin);

        if (!config.isEnabled()) {
            plugin.getLogger().info("Gradient модуль отключен в конфиге.");
            return false;
        }

        // Инициализация сервисов
        dataManager = new GradientDataManager(plugin, config);
        prefixService = new PrefixService(config);

        // PlayerPoints (опционально)
        if (Bukkit.getPluginManager().isPluginEnabled("PlayerPoints")) {
            playerPointsAPI = PlayerPoints.getInstance().getAPI();
            plugin.getLogger().info("Gradient: PlayerPoints подключен!");
        } else {
            plugin.getLogger().warning("Gradient: PlayerPoints не найден. Покупки будут бесплатными.");
        }

        // LuckPerms проверка
        if (prefixService.isLuckPermsAvailable()) {
            plugin.getLogger().info("Gradient: LuckPerms подключен!");
        } else {
            plugin.getLogger().info("Gradient: LuckPerms не найден. Префиксы LuckPerms будут недоступны.");
        }

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

    public void shutdown() {
        if (dataManager != null) {
            dataManager.saveAll();
        }
    }

    public void reload() {
        config.reload();
        messages.reload();
    }

    // ========== Публичные методы для получения отформатированных имен ==========

    /**
     * Получает отформатированное имя игрока (префикс + ник с градиентом)
     */
    public String getFormattedName(Player player) {
        if (!enabled) return player.getName();
        
        GradientPlayerData data = dataManager.getPlayerData(player.getUniqueId());
        return prefixService.getFullName(player, data);
    }

    /**
     * Получает отформатированное имя для TAB плагина
     */
    public String getFormattedNameForTab(Player player) {
        if (!enabled) return player.getName();
        
        GradientPlayerData data = dataManager.getPlayerData(player.getUniqueId());
        return prefixService.getFullNameForTab(player, data);
    }

    /**
     * Получает только градиентный ник (без префикса)
     */
    public String getGradientNick(Player player) {
        if (!enabled) return player.getName();

        GradientPlayerData data = dataManager.getPlayerData(player.getUniqueId());
        return GradientService.applyGradient(player, player.getName(), data);
    }

    /**
     * Получает префикс игрока (кастомный или LuckPerms) с градиентом
     */
    public String getPrefix(Player player) {
        if (!enabled) return "";
        
        GradientPlayerData data = dataManager.getPlayerData(player.getUniqueId());
        return prefixService.getPrefix(player, data);
    }

    /**
     * Получает суффикс игрока из LuckPerms с градиентом
     */
    public String getSuffix(Player player) {
        if (!enabled) return "";
        
        GradientPlayerData data = dataManager.getPlayerData(player.getUniqueId());
        return prefixService.getLuckPermsSuffix(player, data);
    }

    /**
     * Получает только LuckPerms префикс
     */
    public String getLuckPermsPrefix(Player player) {
        if (!enabled) return "";
        
        GradientPlayerData data = dataManager.getPlayerData(player.getUniqueId());
        return prefixService.getLuckPermsPrefix(player, data);
    }

    // ========== Геттеры ==========

    public JavaPlugin getPlugin() {
        return plugin;
    }

    public GradientConfig getConfig() {
        return config;
    }

    public GradientMessages getMessages() {
        return messages;
    }

    public GradientDataManager getDataManager() {
        return dataManager;
    }

    public PrefixService getPrefixService() {
        return prefixService;
    }

    public PlayerPointsAPI getPlayerPointsAPI() {
        return playerPointsAPI;
    }

    public boolean hasPlayerPoints() {
        return playerPointsAPI != null;
    }

    public boolean isEnabled() {
        return enabled;
    }
}
