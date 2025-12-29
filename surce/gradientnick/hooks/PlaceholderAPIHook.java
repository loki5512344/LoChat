package ru.lovar.gradientnick.hooks;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.lovar.gradientnick.GradientNick;
import ru.lovar.gradientnick.data.PlayerData;
import ru.lovar.gradientnick.util.GradientUtil;

public class PlaceholderAPIHook extends PlaceholderExpansion {

    private final GradientNick plugin;

    public PlaceholderAPIHook(GradientNick plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "lopreff";
    }

    @Override
    public @NotNull String getAuthor() {
        return "loki";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public @Nullable String onRequest(OfflinePlayer player, @NotNull String params) {
        if (player == null) return null;

        PlayerData data = plugin.getDataManager().getPlayerData(player.getUniqueId());
        org.bukkit.entity.Player onlinePlayer = player.getPlayer();

        return switch (params.toLowerCase()) {
            case "name" -> {
                if (!data.isColorEnabled() || !data.hasColors()) {
                    yield player.getName();
                }
                yield GradientUtil.applyGradient(
                    player.getName(),
                    data.getColors(),
                    plugin.getConfigManager().isUseLegacyRgbFormat()
                );
            }
            case "prefix" -> {
                if (!data.isPrefixEnabled() || !data.hasPrefix()) {
                    yield "";
                }
                String format = plugin.getConfigManager().getPrefixFormat();
                String prefix = format.replace("{prefix}", data.getPrefix());
                if (data.isColorEnabled() && data.hasColors() && plugin.getConfigManager().isGradientOnPrefix()) {
                    yield GradientUtil.applyGradient(prefix, data.getColors(), plugin.getConfigManager().isUseLegacyRgbFormat());
                }
                yield prefix;
            }
            case "full", "displayname" -> {
                yield GradientUtil.buildDisplayName(
                    data.isPrefixEnabled() ? data.getPrefix() : null,
                    player.getName(),
                    data.isColorEnabled() ? data.getColors() : null,
                    plugin.getConfigManager().isGradientOnPrefix(),
                    plugin.getConfigManager().isContinuousGradient(),
                    plugin.getConfigManager().getPrefixFormat(),
                    plugin.getConfigManager().isUseLegacyRgbFormat()
                );
            }
            case "tabname" -> {
                if (!data.isColorEnabled() || !data.hasColors()) {
                    yield player.getName();
                }
                yield GradientUtil.applyGradient(
                    player.getName(),
                    data.getColors(),
                    plugin.getConfigManager().isUseLegacyRgbFormat()
                );
            }
            case "tab" -> {
                String prefix = null;
                boolean hasCustomPrefix = data.isPrefixEnabled() && data.hasPrefix();
                
                if (hasCustomPrefix) {
                    prefix = plugin.getConfigManager().getPrefixFormat()
                            .replace("{prefix}", data.getPrefix());
                } else if (onlinePlayer != null) {
                    String lpPrefix = plugin.getLuckPermsHook().getActivePrefix(onlinePlayer);
                    if (lpPrefix != null && !lpPrefix.isEmpty()) {
                        prefix = stripColors(lpPrefix);
                    }
                }
                
                String nick = player.getName();
                
                if (data.isColorEnabled() && data.hasColors()) {
                    if (prefix != null && !prefix.isEmpty()) {
                        String fullText = prefix + nick;
                        yield GradientUtil.applyGradient(fullText, data.getColors(), 
                                plugin.getConfigManager().isUseLegacyRgbFormat());
                    } else {
                        yield GradientUtil.applyGradient(nick, data.getColors(), 
                                plugin.getConfigManager().isUseLegacyRgbFormat());
                    }
                } else {
                    if (prefix != null && !prefix.isEmpty()) {
                        if (!hasCustomPrefix && onlinePlayer != null) {
                            String lpPrefix = plugin.getLuckPermsHook().getActivePrefix(onlinePlayer);
                            if (lpPrefix != null) {
                                yield lpPrefix + nick;
                            }
                        }
                        yield prefix + nick;
                    }
                    yield nick;
                }
            }
            case "hasprefix" -> (data.isPrefixEnabled() && data.hasPrefix()) ? "true" : "false";
            case "colors" -> data.hasColors() ? String.join(", ", data.getColors()) : "нет";
            case "color_enabled" -> data.isColorEnabled() ? "да" : "нет";
            case "prefix_enabled" -> data.isPrefixEnabled() ? "да" : "нет";
            default -> null;
        };
    }
    
    private String stripColors(String text) {
        if (text == null) return null;
        return text.replaceAll("(?i)(§x(§[0-9a-f]){6}|§[0-9a-fk-or]|&[0-9a-fk-or]|&#[0-9a-f]{6})", "");
    }
}
