package com.loki.lochat.config;

import org.bukkit.plugin.java.JavaPlugin;

/**
 * Конфигурация мутов (config/mute.yml)
 */
public class MuteConfig extends BaseConfig {

    public MuteConfig(JavaPlugin plugin) {
        super(plugin, "mute.yml", true);
    }

    // ── Основные настройки ──────────────────────────────────────────────────────
    
    public boolean isEnabled() {
        return getConfig().getBoolean("enabled", true);
    }

    public String getDefaultDuration() {
        return getConfig().getString("default-duration", "7d");
    }

    public String getDefaultReason() {
        return getConfig().getString("default-reason", "Без причины");
    }

    public int getMaxHistoryPerPlayer() {
        return getConfig().getInt("max-history-per-player", 50);
    }

    // ── Права на длительность ───────────────────────────────────────────────────
    
    public boolean isPermissionBasedDuration() {
        return getConfig().getBoolean("permission-based-duration", true);
    }

    // ── Уведомления ─────────────────────────────────────────────────────────────
    
    public boolean isNotifyOnMute() {
        return getConfig().getBoolean("notify.on-mute", true);
    }

    public boolean isNotifyOnUnmute() {
        return getConfig().getBoolean("notify.on-unmute", true);
    }

    public boolean isNotifyOnExpire() {
        return getConfig().getBoolean("notify.on-expire", true);
    }

    // ── Тихий мут ───────────────────────────────────────────────────────────────
    
    public boolean isSilentMuteEnabled() {
        return getConfig().getBoolean("silent-mute.enabled", true);
    }

    public boolean isSilentMuteLogToConsole() {
        return getConfig().getBoolean("silent-mute.log-to-console", true);
    }
}
