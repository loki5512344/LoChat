package com.loki.lochat.config;

import org.bukkit.plugin.java.JavaPlugin;

/**
 * Конфигурация мутов (config/mute.yml)
 */
public class MuteConfig extends BaseConfig {

    public MuteConfig(JavaPlugin plugin) {
        super(plugin, "mute.yml", true);
        init(); // ✅ Явная инициализация
    }

    // ── Основные настройки ──────────────────────────────────────────────────────
    
    public boolean isEnabled() {
        return config.getBoolean("enabled", true);
    }

    public String getDefaultDuration() {
        return config.getString("default-duration", "7d");
    }

    public String getDefaultReason() {
        return config.getString("default-reason", "Без причины");
    }

    public int getMaxHistoryPerPlayer() {
        return config.getInt("max-history-per-player", 50);
    }

    // ── Права на длительность ───────────────────────────────────────────────────
    
    public boolean isPermissionBasedDuration() {
        return config.getBoolean("permission-based-duration", true);
    }

    // ── Уведомления ─────────────────────────────────────────────────────────────
    
    public boolean isNotifyOnMute() {
        return config.getBoolean("notify.on-mute", true);
    }

    public boolean isNotifyOnUnmute() {
        return config.getBoolean("notify.on-unmute", true);
    }

    public boolean isNotifyOnExpire() {
        return config.getBoolean("notify.on-expire", true);
    }

    // ── Тихий мут ───────────────────────────────────────────────────────────────
    
    public boolean isSilentMuteEnabled() {
        return config.getBoolean("silent-mute.enabled", true);
    }

    public boolean isSilentMuteLogToConsole() {
        return config.getBoolean("silent-mute.log-to-console", true);
    }
}
