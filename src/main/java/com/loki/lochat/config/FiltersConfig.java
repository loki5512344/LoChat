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
        return getConfig().getBoolean("caps.enabled", true);
    }

    public int getCapsMaxPercent() {
        return getConfig().getInt("caps.max-percent", 70);
    }

    public int getCapsMinLength() {
        return getConfig().getInt("caps.min-length", 5);
    }

    public boolean isCapsAutoLowercase() {
        return getConfig().getBoolean("caps.auto-lowercase", true);
    }

    public boolean isCapsBlock() {
        return getConfig().getBoolean("caps.block", false);
    }

    // ── Мат ─────────────────────────────────────────────────────────────────────
    
    public boolean isSwearEnabled() {
        return getConfig().getBoolean("swear.enabled", true);
    }

    public String getSwearAction() {
        return getConfig().getString("swear.action", "replace");
    }

    public String getSwearReplacementChar() {
        return getConfig().getString("swear.replacement-char", "*");
    }

    public boolean isSwearCheckFragments() {
        return getConfig().getBoolean("swear.check-fragments", true);
    }

    public boolean isSwearIgnoreCase() {
        return getConfig().getBoolean("swear.ignore-case", true);
    }

    public boolean isSwearUseExternalFile() {
        return getConfig().getBoolean("swear.use-external-file", true);
    }

    public List<String> getSwearWords() {
        return getConfig().getStringList("swear.words");
    }

    public String getSwearBlockMessage() {
        return getConfig().getString("swear.block-message", "&#CF6679Мат запрещён");
    }

    // ── Реклама ─────────────────────────────────────────────────────────────────
    
    public boolean isAdvertisingEnabled() {
        return getConfig().getBoolean("advertising.enabled", true);
    }

    public boolean isBlockHiddenUrls() {
        return getConfig().getBoolean("advertising.block-hidden-urls", true);
    }

    public boolean isBlockDomains() {
        return getConfig().getBoolean("advertising.block-domains", true);
    }

    public List<String> getWhitelistedDomains() {
        return getConfig().getStringList("advertising.whitelist");
    }

    public List<String> getBlacklistedDomains() {
        return getConfig().getStringList("advertising.blacklist");
    }

    public String getAdvertisingBlockMessage() {
        return getConfig().getString("advertising.blocked-message", "&#CF6679Реклама запрещена!");
    }

    // ── IP адреса ───────────────────────────────────────────────────────────────
    
    public boolean isIpEnabled() {
        return getConfig().getBoolean("ip.enabled", true);
    }

    public boolean isIpBlock() {
        return getConfig().getBoolean("ip.block", false);
    }

    public String getIpReplacement() {
        return getConfig().getString("ip.replacement", "[IP скрыт]");
    }

    public String getIpBlockMessage() {
        return getConfig().getString("ip.blocked-message", "&#CF6679IP адреса запрещены!");
    }

    // ── Повторяющиеся символы ───────────────────────────────────────────────────
    
    public boolean isRepeatEnabled() {
        return getConfig().getBoolean("repeat.enabled", true);
    }

    public int getRepeatMaxRepeats() {
        return getConfig().getInt("repeat.max-repeats", 3);
    }

    // ── Флуд ────────────────────────────────────────────────────────────────────
    
    public boolean isFloodEnabled() {
        return getConfig().getBoolean("flood.enabled", true);
    }

    public int getFloodMaxMessages() {
        return getConfig().getInt("flood.max-messages", 5);
    }

    public int getFloodTimePeriod() {
        return getConfig().getInt("flood.time-period", 10);
    }

    public String getFloodBlockMessage() {
        return getConfig().getString("flood.block-message", "&#CF6679Не флудите!");
    }

    // ── Спам ────────────────────────────────────────────────────────────────────
    
    public boolean isSpamEnabled() {
        return getConfig().getBoolean("spam.enabled", true);
    }

    public int getSpamMaxSimilarMessages() {
        return getConfig().getInt("spam.max-similar-messages", 3);
    }

    public int getSpamSimilarityThreshold() {
        return getConfig().getInt("spam.similarity-threshold", 80);
    }

    public String getSpamBlockMessage() {
        return getConfig().getString("spam.block-message", "&#CF6679Не отправляйте одинаковые сообщения");
    }
}
