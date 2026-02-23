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
        
        // Подключение LuckPerms (опционально)
        try {
            if (Bukkit.getPluginManager().isPluginEnabled("LuckPerms")) {
                luckPermsHook = new GradientLuckPermsHook(plugin);
                plugin.getLogger().info("Gradient: LuckPerms подключен!");
            } else {
                plugin.getLogger().info("Gradient: LuckPerms не найден. Префиксы LuckPerms будут недоступны.");
            }
        } catch (NoClassDefFoundError e) {
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
     * Получает отформатированное имя игрока с градиентом и LuckPerms префиксом
     * Градиент применяется на всю строку целиком (префикс + ник)
     * Использует формат &#RRGGBB
     */
    public String getFormattedName(Player player) {
        return getFormattedName(player, false);
    }

    /**
     * Получает отформатированное имя для TAB плагина
     * Использует формат §x§R§R§G§G§B§B
     */
    public String getFormattedNameForTab(Player player) {
        return getFormattedName(player, true);
    }

    /**
     * Получает отформатированное имя игрока с градиентом и LuckPerms префиксом
     * @param useTabFormat true для §x§... формата (TAB), false для &#RRGGBB
     */
    private String getFormattedName(Player player, boolean useTabFormat) {
        if (!enabled) return player.getName();
        
        GradientPlayerData data = dataManager.getPlayerData(player.getUniqueId());
        
        // Собираем полную строку: префикс + ник
        StringBuilder fullText = new StringBuilder();
        
        // LuckPerms префикс
        String lpPrefix = null;
        if (luckPermsHook.isEnabled()) {
            lpPrefix = luckPermsHook.getActivePrefix(player);
        }
        
        // Определяем какой префикс использовать
        if (data.hasPrefix() && data.isPrefixEnabled()) {
            // Кастомный префикс
            fullText.append(config.getPrefixFormat().replace("{prefix}", data.getPrefix()));
        } else if (lpPrefix != null && !lpPrefix.isEmpty()) {
            // LuckPerms префикс (убираем его цвета если будем применять градиент)
            if (data.hasColors() && data.isColorEnabled() && config.isGradientOnLuckPermsPrefix()) {
                fullText.append(stripColors(lpPrefix));
            } else {
                // LP префикс со своими цветами, ник отдельно
                String nick = data.hasColors() && data.isColorEnabled() 
                    ? applyGradientWithFormat(player.getName(), data.getColors(), useTabFormat)
                    : player.getName();
                return lpPrefix + nick;
            }
        }
        
        // Добавляем ник
        fullText.append(player.getName());
        
        // Применяем единый градиент на всю строку
        if (data.hasColors() && data.isColorEnabled()) {
            return applyGradientWithFormat(fullText.toString(), data.getColors(), useTabFormat);
        }
        
        return fullText.toString();
    }

    /**
     * Применяет градиент с нужным форматом
     */
    private String applyGradientWithFormat(String text, java.util.List<String> colors, boolean useTabFormat) {
        if (useTabFormat) {
            // Формат §x§R§R§G§G§B§B для TAB
            return GradientUtil.applyGradientTabFormat(text, colors);
        } else {
            // Формат &#RRGGBB для GUI и чата
            return GradientUtil.applyGradient(text, colors, true);
        }
    }

    /**
     * Убирает цветовые коды из строки
     */
    private String stripColors(String text) {
        if (text == null) return "";
        return text.replaceAll("(?i)(§x(§[0-9a-f]){6}|§[0-9a-fk-or]|&[0-9a-fk-or]|&#[0-9a-f]{6}|<[^>]+>)", "");
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
     * Получает префикс игрока (кастомный или LuckPerms) с градиентом
     * Градиент применяется только на префикс
     */
    public String getPrefix(Player player) {
        if (!enabled) return "";
        
        GradientPlayerData data = dataManager.getPlayerData(player.getUniqueId());
        
        // Кастомный префикс
        if (data.hasPrefix() && data.isPrefixEnabled()) {
            String prefix = config.getPrefixFormat().replace("{prefix}", data.getPrefix());
            if (data.hasColors() && data.isColorEnabled() && config.isGradientOnPrefix()) {
                return GradientUtil.applyGradient(prefix, data.getColors(), config.isUseLegacyRgbFormat());
            }
            return prefix;
        }
        
        // LuckPerms префикс
        if (luckPermsHook.isEnabled()) {
            String lpPrefix = luckPermsHook.getActivePrefix(player);
            if (lpPrefix != null && !lpPrefix.isEmpty()) {
                if (data.hasColors() && data.isColorEnabled() && config.isGradientOnLuckPermsPrefix()) {
                    String cleanPrefix = stripColors(lpPrefix);
                    return GradientUtil.applyGradient(cleanPrefix, data.getColors(), config.isUseLegacyRgbFormat());
                }
                return lpPrefix;
            }
        }
        
        return "";
    }

    /**
     * Получает только LuckPerms префикс
     */
    public String getLuckPermsPrefix(Player player) {
        if (!enabled || !luckPermsHook.isEnabled()) return "";
        String prefix = luckPermsHook.getActivePrefix(player);
        return prefix != null ? prefix : "";
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
