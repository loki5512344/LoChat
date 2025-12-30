package com.loki.lochat.gradient.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Конфигурация градиентного модуля
 */
public class GradientConfig {

    private final JavaPlugin plugin;
    private FileConfiguration config;
    private File configFile;

    public GradientConfig(JavaPlugin plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    private void loadConfig() {
        configFile = new File(plugin.getDataFolder(), "gradient-config.yml");
        if (!configFile.exists()) {
            plugin.saveResource("gradient-config.yml", false);
        }
        config = YamlConfiguration.loadConfiguration(configFile);
        
        // Загружаем дефолты
        InputStream defStream = plugin.getResource("gradient-config.yml");
        if (defStream != null) {
            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(
                    new InputStreamReader(defStream, StandardCharsets.UTF_8));
            config.setDefaults(defConfig);
        }
    }

    public void reload() {
        loadConfig();
    }

    public void save() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            plugin.getLogger().warning("Ошибка сохранения gradient-config.yml: " + e.getMessage());
        }
    }

    public boolean isEnabled() { return config.getBoolean("enabled", true); }
    public int getMaxColors() { return config.getInt("max-colors", 7); }
    public int getMinColors() { return config.getInt("min-colors", 1); }
    public int getPricePerColor() { return config.getInt("price-per-color", 50); }
    public int getPrefixPrice() { return config.getInt("prefix-price", 500); }
    public boolean isPrefixOneTimePurchase() { return config.getBoolean("prefix-one-time-purchase", true); }
    public int getColorCooldown() { return config.getInt("color-cooldown", 60); }
    public int getPrefixCooldown() { return config.getInt("prefix-cooldown", 300); }
    public int getMaxPrefixLength() { return config.getInt("max-prefix-length", 7); }
    public String getPrefixFormat() { return config.getString("prefix-format", "[{prefix}] "); }
    public boolean isGradientOnPrefix() { return config.getBoolean("gradient-on-prefix", true); }
    public boolean isGradientOnLuckPermsPrefix() { return config.getBoolean("gradient-on-luckperms-prefix", true); }
    public boolean isContinuousGradient() { return config.getBoolean("continuous-gradient", true); }
    public boolean isUseLegacyRgbFormat() { return config.getBoolean("use-legacy-rgb-format", true); }
    public String getStorageType() { return config.getString("storage-type", "YAML"); }
    public String getSqliteFile() { return config.getString("sqlite-file", "gradient-data.db"); }
    
    public List<String> getPrefixBlacklist() {
        return config.getStringList("prefix-blacklist");
    }

    public boolean isPrefixBlacklisted(String prefix) {
        String lowerPrefix = prefix.toLowerCase();
        return getPrefixBlacklist().stream()
                .anyMatch(blacklisted -> lowerPrefix.contains(blacklisted.toLowerCase()));
    }

    // Display settings
    public boolean isUpdateDisplayName() { return config.getBoolean("display.update-display-name", true); }
    public boolean isUpdateTabList() { return config.getBoolean("display.update-tab-list", true); }
}
