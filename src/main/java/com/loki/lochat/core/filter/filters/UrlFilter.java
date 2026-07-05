package com.loki.lochat.core.filter.filters;

import com.loki.lochat.core.filter.FilterResult;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UrlFilter {
    private static final Pattern URL_PATTERN = Pattern.compile(
        "(https?://)?([\\w-]+\\.)+[\\w-]+(/[\\w-./?%&=]*)?", Pattern.CASE_INSENSITIVE
    );
    
    private static final Pattern IP_PATTERN = Pattern.compile(
        "\\b(?:\\d{1,3}\\.){3}\\d{1,3}\\b"
    );
    
    private static final Pattern HIDDEN_URL_PATTERN = Pattern.compile(
        "(?i)(h\\s*t\\s*t\\s*p|w\\s*w\\s*w|d\\s*o\\s*t\\s*c\\s*o\\s*m)"
    );

    private final FileConfiguration config;
    private final Set<String> whitelistedDomains;
    private final Set<String> blacklistedDomains;

    public UrlFilter(FileConfiguration config) {
        this.config = config;
        this.whitelistedDomains = new HashSet<>(config.getStringList("filters.advertising.whitelist"));
        this.blacklistedDomains = new HashSet<>(config.getStringList("filters.advertising.blacklist"));
    }

    public FilterResult filterUrls(Player player, String message) {
        if (player.hasPermission("lochat.bypass.urlfilter")) {
            return FilterResult.ok(message);
        }
        
        Matcher matcher = URL_PATTERN.matcher(message);
        while (matcher.find()) {
            String domain = extractDomain(matcher.group());
            
            if (!whitelistedDomains.isEmpty()) {
                boolean ok = whitelistedDomains.stream()
                    .anyMatch(d -> domain.toLowerCase().contains(d.toLowerCase()));
                if (!ok) {
                    return FilterResult.blocked(config.getString("filters.url.blocked-message",
                        "&#CF6679Ссылки запрещены!"));
                }
            }
            
            if (!blacklistedDomains.isEmpty()) {
                boolean bad = blacklistedDomains.stream()
                    .anyMatch(d -> domain.toLowerCase().contains(d.toLowerCase()));
                if (bad) {
                    return FilterResult.blocked(config.getString("filters.url.blocked-message",
                        "&#CF6679Эта ссылка запрещена!"));
                }
            }
        }
        
        return FilterResult.ok(message);
    }

    public FilterResult filterIPs(Player player, String message) {
        if (player.hasPermission("lochat.bypass.ipfilter")) {
            return FilterResult.ok(message);
        }
        
        Matcher matcher = IP_PATTERN.matcher(message);
        if (matcher.find()) {
            if (config.getBoolean("filters.ip.block", false)) {
                return FilterResult.blocked(config.getString("filters.ip.blocked-message",
                    "&#CF6679IP адреса запрещены!"));
            }
            return FilterResult.ok(matcher.replaceAll(
                config.getString("filters.ip.replacement", "[IP скрыт]")));
        }
        
        return FilterResult.ok(message);
    }

    public FilterResult filterHiddenUrls(Player player, String message) {
        if (!config.getBoolean("filters.advertising.block-hidden-urls", true)) {
            return FilterResult.ok(message);
        }
        if (player.hasPermission("lochat.bypass.hiddenurls")) {
            return FilterResult.ok(message);
        }
        
        if (HIDDEN_URL_PATTERN.matcher(message).find()) {
            return FilterResult.blocked(config.getString("filters.advertising.blocked-message",
                "&#CF6679Реклама запрещена!"));
        }
        
        return FilterResult.ok(message);
    }

    private String extractDomain(String url) {
        url = url.replaceFirst("^https?://", "");
        int slash = url.indexOf('/');
        return slash != -1 ? url.substring(0, slash) : url;
    }
}
