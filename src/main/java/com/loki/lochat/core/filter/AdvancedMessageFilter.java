package com.loki.lochat.core.filter;

import com.loki.lochat.core.filter.filters.CharacterFilter;
import com.loki.lochat.core.filter.filters.FloodFilter;
import com.loki.lochat.core.filter.filters.SpamFilter;
import com.loki.lochat.core.filter.filters.SwearFilter;
import com.loki.lochat.core.filter.filters.UrlFilter;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

/**
 * Главный фильтр сообщений - координирует работу всех фильтров
 */
public class AdvancedMessageFilter {

    private final FileConfiguration config;
    private final CapsFilter capsFilter;
    private final SwearFilter swearFilter;
    private final FloodFilter floodFilter;
    private final SpamFilter spamFilter;
    private final UrlFilter urlFilter;
    private final CharacterFilter characterFilter;

    public AdvancedMessageFilter(FileConfiguration config) {
        this(config, null);
    }

    public AdvancedMessageFilter(FileConfiguration config, JavaPlugin plugin) {
        this.config = config;
        
        // Инициализация CapsFilter
        com.loki.lochat.config.FiltersConfig filtersConfig = null;
        if (plugin instanceof com.loki.lochat.LoChat loChat) {
            filtersConfig = loChat.getConfigManager().getFiltersConfig();
        }
        
        if (filtersConfig != null) {
            int maxCapsPercent = filtersConfig.getCapsMaxPercent();
            int minLength = filtersConfig.getCapsMinLength();
            boolean autoLower = filtersConfig.isCapsAutoLowercase();
            boolean blockCaps = filtersConfig.isCapsBlock();
            this.capsFilter = new CapsFilter(maxCapsPercent, minLength, autoLower, blockCaps);
        } else {
            int maxCapsPercent = config.getInt("filters.caps.max-percent", 70);
            int minLength = config.getInt("filters.caps.min-length", 5);
            boolean autoLower = config.getBoolean("filters.caps.auto-lowercase", true);
            boolean blockCaps = config.getBoolean("filters.caps.block", false);
            this.capsFilter = new CapsFilter(maxCapsPercent, minLength, autoLower, blockCaps);
        }
        
        // Инициализация остальных фильтров
        this.swearFilter = new SwearFilter(config, plugin);
        this.floodFilter = new FloodFilter(config);
        this.spamFilter = new SpamFilter(config);
        this.urlFilter = new UrlFilter(config);
        this.characterFilter = new CharacterFilter(config);
    }

    /**
     * Проверяет, может ли игрок обойти фильтр
     */
    private boolean canBypassFilter(Player player, String filterType) {
        return player.hasPermission("lochat.bypass." + filterType) 
            || player.hasPermission("lochat.bypass.filter");
    }
    
    /**
     * Главный метод фильтрации
     */
    public FilterResult filterMessage(Player player, String message) {
        
        // 1. Капс
        if (config.getBoolean("filters.caps.enabled", true)) {
            String filtered = capsFilter.filter(player, message);
            if (filtered == null) {
                return FilterResult.blocked("&#CF6679Не кричите в чате");
            }
            message = filtered;
        }

        // 2. Мат
        if (config.getBoolean("filters.swear.enabled", true) && !canBypassFilter(player, "swear")) {
            FilterResult swear = swearFilter.filter(player, message);
            if (!swear.allowed()) {
                return swear;
            }
            message = swear.filteredMessage();
        }

        // 3. URL фильтры
        if (config.getBoolean("filters.advertising.enabled", true)) {
            FilterResult r = urlFilter.filterUrls(player, message);
            if (!r.allowed()) {
                return r;
            }
            message = r.filteredMessage();

            r = urlFilter.filterHiddenUrls(player, message);
            if (!r.allowed()) {
                return r;
            }
        }

        // 4. IP
        if (config.getBoolean("filters.ip.enabled", true)) {
            FilterResult r = urlFilter.filterIPs(player, message);
            if (!r.allowed()) {
                return r;
            }
            message = r.filteredMessage();
        }

        // 5. Повторяющиеся символы
        if (config.getBoolean("filters.repeat.enabled", true)) {
            message = characterFilter.filter(message);
        }

        // 6. Anti-flood
        if (config.getBoolean("filters.flood.enabled", true) && !canBypassFilter(player, "flood")) {
            FilterResult r = floodFilter.filter(player);
            if (!r.allowed()) {
                return r;
            }
        }

        // 7. Anti-spam
        if (config.getBoolean("filters.spam.enabled", true) && !canBypassFilter(player, "spam")) {
            FilterResult r = spamFilter.filter(player, message);
            if (!r.allowed()) {
                return r;
            }
        }

        return FilterResult.ok(message);
    }

    /**
     * Очистка данных игрока при выходе
     */
    public void clearPlayer(UUID uuid) {
        floodFilter.clearPlayer(uuid);
        spamFilter.clearPlayer(uuid);
    }
}
