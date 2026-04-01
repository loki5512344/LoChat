package com.loki.lochat.config;

import org.bukkit.plugin.java.JavaPlugin;

/**
 * Конфигурация звуков (config/sounds.yml)
 */
public class SoundsConfig extends BaseConfig {

    public SoundsConfig(JavaPlugin plugin) {
        super(plugin, "sounds.yml", true);
        init(); // ✅ Явная инициализация
    }

    // ── Звуки упоминаний ────────────────────────────────────────────────────────
    
    public boolean isMentionSoundEnabled() {
        return config.getBoolean("mention.enabled", true);
    }

    public String getMentionSound() {
        return config.getString("mention.sound", "BLOCK_NOTE_BLOCK_PLING");
    }

    public float getMentionVolume() {
        return (float) config.getDouble("mention.volume", 1.0);
    }

    public float getMentionPitch() {
        return (float) config.getDouble("mention.pitch", 1.0);
    }

    // ── Звуки личных сообщений ──────────────────────────────────────────────────
    
    public boolean isPmSoundEnabled() {
        return config.getBoolean("pm.enabled", true);
    }

    public String getPmSound() {
        return config.getString("pm.sound", "ENTITY_EXPERIENCE_ORB_PICKUP");
    }

    public float getPmVolume() {
        return (float) config.getDouble("pm.volume", 1.0);
    }

    public float getPmPitch() {
        return (float) config.getDouble("pm.pitch", 1.0);
    }

    // ── Звуки команд ────────────────────────────────────────────────────────────
    
    public boolean isCommandSoundEnabled() {
        return config.getBoolean("command.enabled", false);
    }

    public String getCommandSound() {
        return config.getString("command.sound", "UI_BUTTON_CLICK");
    }

    public float getCommandVolume() {
        return (float) config.getDouble("command.volume", 0.5);
    }

    public float getCommandPitch() {
        return (float) config.getDouble("command.pitch", 1.0);
    }
}
