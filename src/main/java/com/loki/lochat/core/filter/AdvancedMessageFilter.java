package com.loki.lochat.core.filter;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AdvancedMessageFilter {

    private static final Pattern URL_PATTERN = Pattern.compile(
            "(https?://)?([\\w-]+\\.)+[\\w-]+(/[\\w-./?%&=]*)?",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern IP_PATTERN = Pattern.compile(
            "\\b(?:\\d{1,3}\\.){3}\\d{1,3}\\b"
    );

    private static final Pattern REPEAT_PATTERN = Pattern.compile(
            "(.)\\1{3,}"
    );

    private final FileConfiguration config;
    private final List<String> whitelistedDomains;
    private final List<String> blacklistedDomains;

    public AdvancedMessageFilter(FileConfiguration config) {
        this.config = config;
        this.whitelistedDomains = config.getStringList("filters.url.whitelist");
        this.blacklistedDomains = config.getStringList("filters.url.blacklist");
    }

    public FilterResult filterMessage(Player player, String message) {
        if (config.getBoolean("filters.url.enabled", true)) {
            FilterResult urlResult = filterUrls(player, message);
            if (!urlResult.allowed()) {
                return urlResult;
            }
            message = urlResult.filteredMessage();
        }

        if (config.getBoolean("filters.ip.enabled", true)) {
            FilterResult ipResult = filterIPs(player, message);
            if (!ipResult.allowed()) {
                return ipResult;
            }
            message = ipResult.filteredMessage();
        }

        if (config.getBoolean("filters.repeat.enabled", true)) {
            message = filterRepeatingChars(message);
        }

        return new FilterResult(true, message, null);
    }

    private FilterResult filterUrls(Player player, String message) {
        if (player.hasPermission("lochat.bypass.urlfilter")) {
            return new FilterResult(true, message, null);
        }

        Matcher matcher = URL_PATTERN.matcher(message);

        while (matcher.find()) {
            String url = matcher.group();
            String domain = extractDomain(url);

            if (!whitelistedDomains.isEmpty()) {
                boolean isWhitelisted = whitelistedDomains.stream()
                        .anyMatch(d -> domain.toLowerCase().contains(d.toLowerCase()));

                if (!isWhitelisted) {
                    return new FilterResult(false, message,
                            config.getString("filters.url.blocked-message", "&#FF6B6BСсылки запрещены!"));
                }
            }

            if (!blacklistedDomains.isEmpty()) {
                boolean isBlacklisted = blacklistedDomains.stream()
                        .anyMatch(d -> domain.toLowerCase().contains(d.toLowerCase()));

                if (isBlacklisted) {
                    return new FilterResult(false, message,
                            config.getString("filters.url.blocked-message", "&#FF6B6BЭта ссылка запрещена!"));
                }
            }
        }

        return new FilterResult(true, message, null);
    }

    private FilterResult filterIPs(Player player, String message) {
        if (player.hasPermission("lochat.bypass.ipfilter")) {
            return new FilterResult(true, message, null);
        }

        Matcher matcher = IP_PATTERN.matcher(message);

        if (matcher.find()) {
            String replacement = config.getString("filters.ip.replacement", "[IP скрыт]");
            String filtered = matcher.replaceAll(replacement);

            if (config.getBoolean("filters.ip.block", false)) {
                return new FilterResult(false, message,
                        config.getString("filters.ip.blocked-message", "&#FF6B6BIP адреса запрещены!"));
            }

            return new FilterResult(true, filtered, null);
        }

        return new FilterResult(true, message, null);
    }

    private String filterRepeatingChars(String message) {
        int maxRepeats = config.getInt("filters.repeat.max-repeats", 3);

        Matcher matcher = REPEAT_PATTERN.matcher(message);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String repeated = matcher.group(1);
            String replacement = repeated.repeat(Math.min(maxRepeats, matcher.group().length()));
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(result);

        return result.toString();
    }

    private String extractDomain(String url) {
        url = url.replaceFirst("^https?://", "");
        int slashIndex = url.indexOf('/');
        if (slashIndex != -1) {
            url = url.substring(0, slashIndex);
        }
        return url;
    }

    public record FilterResult(boolean allowed, String filteredMessage, String blockReason) {
    }
}
