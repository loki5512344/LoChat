package com.loki.lohub.config;

import com.loki.lohub.LoHub;
import org.bukkit.configuration.file.FileConfiguration;

public class ConfigManager {
    
    private final LoHub plugin;
    private FileConfiguration config;
    
    public ConfigManager(LoHub plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
    }
    
    public void reload() {
        plugin.reloadConfig();
        this.config = plugin.getConfig();
    }
    
    public FileConfiguration getConfig() {
        return config;
    }
    
    public boolean isProtectionEnabled() {
        return config.getBoolean("protection.enabled", true);
    }
    
    public boolean isBlockBreakProtected() {
        return config.getBoolean("protection.block-break", true);
    }
    
    public boolean isBlockPlaceProtected() {
        return config.getBoolean("protection.block-place", true);
    }
    
    public boolean isPvPProtected() {
        return config.getBoolean("protection.pvp", true);
    }
    
    public boolean isItemDropProtected() {
        return config.getBoolean("protection.item-drop", true);
    }
    
    public boolean isItemPickupProtected() {
        return config.getBoolean("protection.item-pickup", true);
    }
    
    public boolean isFoodLevelProtected() {
        return config.getBoolean("protection.food-level", true);
    }
    
    public boolean isMobSpawningProtected() {
        return config.getBoolean("protection.mob-spawning", true);
    }
}
