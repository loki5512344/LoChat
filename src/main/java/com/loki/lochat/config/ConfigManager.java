package com.loki.lochat.config;

import com.loki.lochat.LoChat;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;

public class ConfigManager {

    private final LoChat plugin;
    private FileConfiguration config;

    public ConfigManager(LoChat plugin) {
        this.plugin = plugin;
        plugin.saveDefaultConfig();
        this.config = plugin.getConfig();
    }

    public void reload() {
        plugin.reloadConfig();
        this.config = plugin.getConfig();
    }

    // Global chat
    public boolean isGlobalEnabled() {
        return config.getBoolean("chat.global.enabled", true);
    }

    public String getGlobalPrefix() {
        return config.getString("chat.global.prefix", "[G]");
    }

    public String getGlobalFormat() {
        return config.getString("chat.global.format", "<prefix><player>: <message>");
    }

    public String getGlobalSymbol() {
        return config.getString("chat.global.symbol", "!");
    }

    public int getGlobalCooldown() {
        return config.getInt("chat.global.cooldown", 3);
    }

    // Local chat
    public boolean isLocalEnabled() {
        return config.getBoolean("chat.local.enabled", true);
    }

    public int getLocalRadius() {
        return config.getInt("chat.local.radius", 100);
    }

    public String getLocalPrefix() {
        return config.getString("chat.local.prefix", "[L]");
    }

    public String getLocalFormat() {
        return config.getString("chat.local.format", "<player>: <message>");
    }

    public int getLocalCooldown() {
        return config.getInt("chat.local.cooldown", 1);
    }

    // PM
    public boolean isPmEnabled() {
        return config.getBoolean("chat.pm.enabled", true);
    }

    public boolean isPmLogEnabled() {
        return config.getBoolean("chat.pm.log", false);
    }

    public boolean isPmSoundEnabled() {
        return config.getBoolean("chat.pm.sound", true);
    }

    public String getPmSoundType() {
        return config.getString("chat.pm.sound-type", "ENTITY_EXPERIENCE_ORB_PICKUP");
    }

    // Mentions
    public boolean isMentionsEnabled() {
        return config.getBoolean("mentions.enabled", true);
    }

    public boolean isMentionSoundEnabled() {
        return config.getBoolean("mentions.sound", true);
    }

    public String getMentionSoundType() {
        return config.getString("mentions.sound-type", "BLOCK_NOTE_BLOCK_PLING");
    }

    public String getMentionHighlight() {
        return config.getString("mentions.highlight", "<yellow>@{player}</yellow>");
    }

    public String getSelfMentionHighlight() {
        return config.getString("mentions.self-highlight", "<gold><bold>{player}</bold></gold>");
    }

    // Filter
    public boolean isFilterEnabled() {
        return config.getBoolean("filter.enabled", true);
    }

    public List<String> getFilterWords() {
        return config.getStringList("filter.words");
    }

    public String getFilterAction() {
        return config.getString("filter.action", "censor");
    }

    public String getFilterReplacement() {
        return config.getString("filter.replacement", "***");
    }

    // Anti-spam
    public boolean isAntiSpamEnabled() {
        return config.getBoolean("antispam.enabled", true);
    }

    public int getMaxCapsPercent() {
        return config.getInt("antispam.max-caps-percent", 70);
    }

    public int getMaxRepeatChars() {
        return config.getInt("antispam.max-repeat-chars", 5);
    }

    public int getSimilarMessageDelay() {
        return config.getInt("antispam.similar-message-delay", 30);
    }

    // Auto messages
    public boolean isAutoMessagesEnabled() {
        return config.getBoolean("automessages.enabled", true);
    }

    public int getAutoMessagesInterval() {
        return config.getInt("automessages.interval", 300);
    }

    public boolean isAutoMessagesRandom() {
        return config.getBoolean("automessages.random", false);
    }

    // Formats
    public String getAnnouncementFormat() {
        return config.getString("formats.announcement", "<gold>[ОБЪЯВЛЕНИЕ]</gold> <message>");
    }

    // LoPreff integration
    public boolean isLopreffEnabled() {
        return config.getBoolean("lopreff.enabled", true);
    }

    public boolean useGradientName() {
        return config.getBoolean("lopreff.use-gradient-name", true);
    }

    public boolean useCustomPrefix() {
        return config.getBoolean("lopreff.use-custom-prefix", true);
    }
}
