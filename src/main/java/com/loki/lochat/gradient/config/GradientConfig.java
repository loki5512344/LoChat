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
    private FileConfiguration mainConfig;
    private File configFile;

    public GradientConfig(JavaPlugin plugin) {
        this.plugin = plugin;
        this.mainConfig = plugin.getConfig();
        loadConfig();
    }

    private void loadConfig() {
        // Читаем из главного config.yml вместо отдельного файла
        config = plugin.getConfig();
        plugin.getLogger().info("Gradient конфиг загружен из config.yml");
    }

    public void reload() {
        plugin.reloadConfig();
        this.mainConfig = plugin.getConfig();
        this.config = plugin.getConfig();
    }

    public void save() {
        // Сохранение не требуется, используем главный конфиг
        plugin.saveConfig();
    }

    public boolean isEnabled() {
        return config.getBoolean("gradient.enabled", true);
    }

    public int getMaxColors() {
        return config.getInt("gradient.max-colors", 7);
    }

    public int getMinColors() {
        return config.getInt("gradient.min-colors", 1);
    }

    public int getPricePerColor() {
        return config.getInt("gradient.price-per-color", 50);
    }

    public int getPrefixPrice() {
        return config.getInt("gradient.prefix-price", 500);
    }

    public boolean isPrefixOneTimePurchase() {
        return config.getBoolean("gradient.prefix-one-time-purchase", true);
    }

    public int getColorCooldown() {
        return config.getInt("gradient.color-cooldown", 60);
    }

    public int getPrefixCooldown() {
        return config.getInt("gradient.prefix-cooldown", 120);
    }

    public int getMaxPrefixLength() {
        return config.getInt("gradient.max-prefix-length", 16);
    }

    public String getPrefixFormat() {
        return config.getString("gradient.prefix-format", "[{prefix}] ");
    }

    public boolean isGradientOnPrefix() {
        return config.getBoolean("gradient.gradient-on-prefix", true);
    }

    public boolean isGradientOnLuckPermsPrefix() {
        return config.getBoolean("gradient.gradient-on-luckperms-prefix", true);
    }

    public boolean isContinuousGradient() {
        return config.getBoolean("gradient.continuous-gradient", true);
    }

    public boolean isUseLegacyRgbFormat() {
        return config.getBoolean("gradient.use-legacy-rgb-format", true);
    }

    public String getStorageType() {
        return config.getString("gradient.storage-type", "file");
    }

    public String getSqliteFile() {
        return config.getString("gradient.sqlite-file", "gradient-data.db");
    }

    public List<String> getPrefixBlacklist() {
        return config.getStringList("gradient.prefix-blacklist");
    }

    public boolean isPrefixBlacklisted(String prefix) {
        String lowerPrefix = prefix.toLowerCase();
        return getPrefixBlacklist().stream()
                .anyMatch(blacklisted -> lowerPrefix.contains(blacklisted.toLowerCase()));
    }

    public boolean isUpdateDisplayName() {
        if (mainConfig.contains("display.update-display-name")) {
            return mainConfig.getBoolean("display.update-display-name", true);
        }
        return config.getBoolean("display.update-display-name", true);
    }

    public boolean isUpdateTabList() {
        if (mainConfig.contains("display.update-tab-list")) {
            return mainConfig.getBoolean("display.update-tab-list", true);
        }
        return config.getBoolean("display.update-tab-list", true);
    }
}
