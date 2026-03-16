package com.loki.lochat.config;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.List;

/**
 * Конфигурация хардкодированных сообщений
 */
public class HardcodedMessages {
    private final Plugin plugin;
    private YamlConfiguration config;
    
    public HardcodedMessages(Plugin plugin) {
        this.plugin = plugin;
        // Не вызываем loadConfig() в конструкторе для избежания this-escape
    }
    
    /**
     * Инициализировать конфигурацию (вызывать после создания объекта)
     */
    public void init() {
        loadConfig();
    }
    
    /**
     * Загрузить конфигурацию
     */
    public void loadConfig() {
        File configFile = new File(plugin.getDataFolder(), "config/hardcoded-messages.yml");
        if (!configFile.exists()) {
            plugin.saveResource("config/hardcoded-messages.yml", false);
        }
        
        config = YamlConfiguration.loadConfiguration(configFile);
    }
    
    /**
     * Перезагрузить конфигурацию
     */
    public void reload() {
        loadConfig();
    }
    
    // ========== КОМАНДЫ ==========
    
    /**
     * Получить сообщение об использовании команды мута
     */
    public String getMuteUsage() {
        return config.getString("commands.mute.usage", "§cИспользование: /mute <ник> [время] [-s] [причина]");
    }
    
    /**
     * Получить помощь по времени мута
     */
    public String getMuteTimeHelp() {
        return config.getString("commands.mute.time_help", "§7Время: 1d, 2h, 30m, 60s или 0/perm для перманентного");
    }
    
    /**
     * Получить помощь по тихому муту
     */
    public String getMuteSilentHelp() {
        return config.getString("commands.mute.silent_help", "§7-s - тихий мут (требует право lochat.mute.silent)");
    }
    
    /**
     * Получить сообщение об отсутствии прав на тихий мут
     */
    public String getNoSilentPermission() {
        return config.getString("commands.mute.no_silent_permission", "§cУ вас нет права на тихий мут!");
    }
    
    /**
     * Получить причину мута по умолчанию
     */
    public String getDefaultMuteReason() {
        return config.getString("commands.mute.default_reason", "Нету.");
    }
    
    /**
     * Получить длительность мута по умолчанию
     */
    public String getDefaultMuteDuration() {
        return config.getString("commands.mute.default_duration", "7d");
    }
    
    /**
     * Получить сообщение кулдауна
     */
    public String getCooldownMessage() {
        return config.getString("commands.cooldown.message", "§cПодождите {remaining} сек.");
    }
    
    // ========== ЛОКАЛЬНЫЙ ЧАТ ==========
    
    /**
     * Получить сообщение "никто не услышал"
     */
    public String getNobodyHeard() {
        return config.getString("local_chat.nobody_heard", "&#FFA726Вас никто не услышал - рядом нет игроков");
    }
    
    /**
     * Получить сообщение "рядом нет игроков"
     */
    public String getNoPlayersNearby() {
        return config.getString("local_chat.no_players_nearby", "&#FFA726Рядом нет игроков");
    }
    
    // ========== ФИЛЬТРЫ ==========
    
    /**
     * Получить минимальную длину для проверки капса
     */
    public int getCapsMinLength() {
        return config.getInt("filters.caps.min_length", 3);
    }
    
    /**
     * Получить сообщение о блокировке капса
     */
    public String getCapsBlockedMessage() {
        return config.getString("filters.caps.blocked_message", "§cСлишком много заглавных букв!");
    }
    
    /**
     * Получить уведомление об автоисправлении
     */
    public String getAutoLowercaseNotice() {
        return config.getString("filters.caps.auto_lowercase_notice", "§7Ваше сообщение было автоматически исправлено");
    }
    
    // ========== МУТИРОВАНИЕ ==========
    
    /**
     * Получить сообщение о том что игрок замучен
     */
    public String getMutedMessage() {
        return config.getString("mute.muted_message", "§cВы замучены! Причина: {reason}. Осталось: {time}");
    }
    
    /**
     * Получить сообщение о перманентном муте
     */
    public String getMutedPermanent() {
        return config.getString("mute.muted_permanent", "§cВы замучены навсегда! Причина: {reason}");
    }
    
    /**
     * Получить уведомление о муте игрока
     */
    public String getPlayerMuted() {
        return config.getString("mute.player_muted", "§aИгрок {player} замучен на {time}. Причина: {reason}");
    }
    
    /**
     * Получить уведомление о перманентном муте игрока
     */
    public String getPlayerMutedPermanent() {
        return config.getString("mute.player_muted_permanent", "§aИгрок {player} замучен навсегда. Причина: {reason}");
    }
    
    /**
     * Получить уведомление о тихом муте
     */
    public String getPlayerMutedSilent() {
        return config.getString("mute.player_muted_silent", "§7[ТИХИЙ МУТ] {player} замучен на {time}");
    }
    
    // ========== ИГРОВЫЕ РЕЖИМЫ ==========
    
    /**
     * Получить название игрового режима
     */
    public String getGamemodeName(String gamemode) {
        return config.getString("gamemodes." + gamemode.toLowerCase(), gamemode);
    }
    
    // ========== ОБЩИЕ СООБЩЕНИЯ ==========
    
    /**
     * Получить сообщение об отсутствии прав
     */
    public String getNoPermission() {
        return config.getString("general.no_permission", "§cУ вас нет прав для выполнения этой команды!");
    }
    
    /**
     * Получить сообщение "только для игроков"
     */
    public String getPlayerOnly() {
        return config.getString("general.player_only", "§cЭта команда доступна только игрокам!");
    }
    
    /**
     * Получить сообщение о неверных аргументах
     */
    public String getInvalidArguments() {
        return config.getString("general.invalid_arguments", "§cНеверные аргументы команды!");
    }
    
    /**
     * Получить сообщение об отключенной команде
     */
    public String getCommandDisabled() {
        return config.getString("general.command_disabled", "§cЭта команда отключена!");
    }
    
    /**
     * Получить сообщение об отключенной функции
     */
    public String getFeatureDisabled() {
        return config.getString("general.feature_disabled", "§cЭта функция отключена в конфигурации!");
    }
    
    /**
     * Получить формат времени
     */
    public String getTimeFormat(String unit) {
        return config.getString("general.time_formats." + unit, unit);
    }
    
    /**
     * Получить сообщение об игроке не найден
     */
    public String getPlayerNotFound() {
        return config.getString("general.player_not_found", "§cИгрок не найден!");
    }
    
    /**
     * Получить сообщение игнорирования
     */
    public String getIgnore(String key) {
        return config.getString("ignore." + key, "§cОшибка игнорирования!");
    }
    
    /**
     * Получить сообщение личных сообщений
     */
    public String getPrivateMessage(String key) {
        return config.getString("private_messages." + key, "§cОшибка личных сообщений!");
    }
    
    /**
     * Получить сообщение об отсутствии прав на тихий размут
     */
    public String getNoSilentUnmutePermission() {
        return config.getString("mute.no_silent_unmute_permission", "&#CF6679Нет права на тихий размут!");
    }
    
    /**
     * Получить сообщение о том что игрок не замучен
     */
    public String getPlayerNotMutedSimple() {
        return config.getString("mute.player_not_muted_simple", "&#CF6679Игрок не замучен!");
    }
}