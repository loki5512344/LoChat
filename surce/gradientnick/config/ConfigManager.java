package ru.lovar.gradientnick.config;

import org.bukkit.configuration.file.FileConfiguration;
import ru.lovar.gradientnick.GradientNick;

import java.util.List;

public class ConfigManager {

    private final GradientNick plugin;
    private FileConfiguration config;

    public ConfigManager(GradientNick plugin) {
        this.plugin = plugin;
        plugin.saveDefaultConfig();
        this.config = plugin.getConfig();
        updateConfig();
    }

    public void reload() {
        plugin.reloadConfig();
        this.config = plugin.getConfig();
        updateConfig();
    }

    private void updateConfig() {
        boolean needsSave = false;
        
        // Автодополнение новых параметров
        if (!config.contains("prefix-blacklist")) {
            config.set("prefix-blacklist", List.of("admin", "owner", "moder", "helper", "staff"));
            needsSave = true;
        }
        if (!config.contains("history-max-entries")) {
            config.set("history-max-entries", 10);
            needsSave = true;
        }
        if (!config.contains("history-free-apply")) {
            config.set("history-free-apply", true);
            needsSave = true;
        }
        
        if (needsSave) {
            plugin.saveConfig();
        }
    }

    public int getMaxColors() {
        return config.getInt("max-colors", 7);
    }

    public int getMinColors() {
        return config.getInt("min-colors", 1);
    }

    public int getPricePerColor() {
        return config.getInt("price-per-color", 50);
    }

    public int getPrefixPrice() {
        return config.getInt("prefix-price", 500);
    }

    public boolean isPrefixOneTimePurchase() {
        return config.getBoolean("prefix-one-time-purchase", true);
    }

    public int getAnimatedPrice() {
        return config.getInt("animated-price", 1000);
    }

    public int getAnimatedIntervalTicks() {
        return config.getInt("animated-interval-ticks", 5);
    }

    public int getColorCooldown() {
        return config.getInt("color-cooldown", 60);
    }

    public int getPrefixCooldown() {
        return config.getInt("prefix-cooldown", 300);
    }

    public int getMaxPrefixLength() {
        return config.getInt("max-prefix-length", 7);
    }

    public String getPrefixFormat() {
        return config.getString("prefix-format", "[{prefix}] ");
    }

    public boolean isGradientOnPrefix() {
        return config.getBoolean("gradient-on-prefix", true);
    }

    public boolean isContinuousGradient() {
        return config.getBoolean("continuous-gradient", true);
    }

    public boolean isUseLegacyRgbFormat() {
        return config.getBoolean("use-legacy-rgb-format", true);
    }

    public String getStorageType() {
        return config.getString("storage-type", "YAML");
    }

    public String getSqliteFile() {
        return config.getString("sqlite-file", "data.db");
    }

    public List<String> getPrefixBlacklist() {
        return config.getStringList("prefix-blacklist");
    }

    public boolean isPrefixBlacklisted(String prefix) {
        String lowerPrefix = prefix.toLowerCase();
        return getPrefixBlacklist().stream()
                .anyMatch(blacklisted -> lowerPrefix.contains(blacklisted.toLowerCase()));
    }

    public int getHistoryMaxEntries() {
        return config.getInt("history-max-entries", 10);
    }

    public boolean isHistoryFreeApply() {
        return config.getBoolean("history-free-apply", true);
    }
}
