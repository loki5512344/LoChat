package com.loki.lochat.config;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

/**
 * Конфигурация фильтров (config/filters.yml)
 */
public class FiltersConfig extends BaseConfig {

    public FiltersConfig(JavaPlugin plugin) {
        super(plugin, "filters.yml", true);
    }

    // ── Капс ────────────────────────────────────────────────────────────────────
    
    public boolean isCapsEnabled() {
        return config.getBoolean("caps.enabled", true);
    }

    public int getCapsMaxPercent() {
        return config.getInt("caps.max-percent", 70);
    }

    public int getCapsMinLength() {
        return config.getInt("caps.min-length", 5);
    }

    public boolean isCapsAutoLowercase() {
        return config.getBoolean("caps.auto-lowercase", true);
    }

    public boolean isCapsBlock() {
        return config.getBoolean("caps.block", false);
    }

    // ── Мат ─────────────────────────────────────────────────────────────────────
    
    public boolean isSwearEnabled() {
        return config.getBoolean("swear.enabled", true);
    }

    public String getSwearAction() {
        return config.getString("swear.action", "replace");
    }

    public String getSwearReplacementChar() {
        return config.getString("swear.replacement-char", "*");
    }

    public boolean isSwearCheckFragments() {
        return config.getBoolean("swear.check-fragments", true);
    }

    public boolean isSwearIgnoreCase() {
        return config.getBoolean("swear.ignore-case", true);
    }

    public boolean isSwearUseExternalFile() {
        return config.getBoolean("swear.use-external-file", true);
    }

    public List<String> getSwearWords() {
        return config.getStringList("swear.words");
    }

    public String getSwearBlockMessage() {
        return config.getString("swear.block-message", "&#CF6679Мат запрещён");
    }

    // ── Реклама ─────────────────────────────────────────────────────────────────
    
    public boolean isAdvertisingEnabled() {
        return config.getBoolean("advertising.enabled", true);
    }

    public boolean isBlockHiddenUrls() {
        return config.getBoolean("advertising.block-hidden-urls", true);
    }

    public boolean isBlockDomains() {
        return config.getBoolean("advertising.block-domains", true);
    }

    public List<String> getWhitelistedDomains() {
        return config.getStringList("advertising.whitelist");
    }

    public List<String> getBlacklistedDomains() {
        return config.getStringList("advertising.blacklist");
    }

    public String getAdvertisingBlockMessage() {
        return config.getString("advertising.blocked-message", "&#CF6679Реклама запрещена!");
    }

    // ── IP адреса ───────────────────────────────────────────────────────────────
    
    public boolean isIpEnabled() {
        return config.getBoolean("ip.enabled", true);
    }

    public boolean isIpBlock() {
        return config.getBoolean("ip.block", false);
    }

    public String getIpReplacement() {
        return config.getString("ip.replacement", "[IP скрыт]");
    }

    public String getIpBlockMessage() {
        return config.getString("ip.blocked-message", "&#CF6679IP адреса запрещены!");
    }

    // ── Повторяющиеся символы ───────────────────────────────────────────────────
    
    public boolean isRepeatEnabled() {
        return config.getBoolean("repeat.enabled", true);
    }

    public int getRepeatMaxRepeats() {
        return config.getInt("repeat.max-repeats", 3);
    }

    // ── Флуд ────────────────────────────────────────────────────────────────────
    
    public boolean isFloodEnabled() {
        return config.getBoolean("flood.enabled", true);
    }

    public int getFloodMaxMessages() {
        return config.getInt("flood.max-messages", 5);
    }

    public int getFloodTimePeriod() {
        return config.getInt("flood.time-period", 10);
    }

    public String getFloodBlockMessage() {
        return config.getString("flood.block-message", "&#CF6679Не флудите!");
    }

    // ── Спам ────────────────────────────────────────────────────────────────────
    
    public boolean isSpamEnabled() {
        return config.getBoolean("spam.enabled", true);
    }

    public int getSpamMaxSimilarMessages() {
        return config.getInt("spam.max-similar-messages", 3);
    }

    public int getSpamSimilarityThreshold() {
        return config.getInt("spam.similarity-threshold", 80);
    }

    public String getSpamBlockMessage() {
        return config.getString("spam.block-message", "&#CF6679Не отправляйте одинаковые сообщения");
    }
}
