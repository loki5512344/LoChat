package com.loki.lochat.integrations;

import com.loki.lochat.LoChat;
import com.loki.lochat.gradient.GradientModule;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class PlaceholderAPIHook extends PlaceholderExpansion {

    private final LoChat plugin;

    public PlaceholderAPIHook(LoChat plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "lochat";
    }

    @Override
    public @NotNull String getAuthor() {
        return "loki";
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public @Nullable String onRequest(OfflinePlayer player, @NotNull String params) {
        if (player == null) return "";

        UUID uuid = player.getUniqueId();
        GradientModule gradient = plugin.getGradientModule();

        return switch (params.toLowerCase()) {
            case "ignored_count" -> String.valueOf(plugin.getIgnoreManager().getIgnoredCount(uuid));
            case "global_enabled" -> String.valueOf(!plugin.getChatManager().isGlobalChatDisabled(uuid));
            case "last_pm" -> {
                UUID last = plugin.getPmManager().getLastConversation(uuid);
                if (last == null) yield "";
                var lastPlayer = Bukkit.getOfflinePlayer(last);
                yield lastPlayer.getName() != null ? lastPlayer.getName() : "";
            }
            case "spy_enabled" -> String.valueOf(plugin.getSpyManager().isSpying(uuid));
            
            // Gradient placeholders (совместимость с LoPreff)
            case "gradient_full", "full" -> {
                Player onlinePlayer = player.getPlayer();
                if (onlinePlayer == null) yield player.getName() != null ? player.getName() : "";
                
                // Если gradient модуль выключен — берём только LuckPerms префикс + ник
                if (gradient == null || !gradient.isEnabled()) {
                    yield player.getName() != null ? player.getName() : "";
                }
                
                yield gradient.getFormattedName(onlinePlayer);
            }
            case "gradient_name", "name" -> {
                Player onlinePlayer = player.getPlayer();
                if (onlinePlayer == null) yield player.getName() != null ? player.getName() : "";
                
                if (gradient == null || !gradient.isEnabled()) {
                    yield player.getName();
                }
                yield gradient.getGradientNick(onlinePlayer);
            }
            case "gradient_prefix", "prefix" -> {
                Player onlinePlayer = player.getPlayer();
                if (onlinePlayer == null) yield "";
                
                if (gradient == null || !gradient.isEnabled()) yield "";
                yield gradient.getPrefix(onlinePlayer);
            }
            case "lp_prefix" -> {
                Player onlinePlayer = player.getPlayer();
                if (onlinePlayer == null) yield "";
                
                if (gradient == null || !gradient.isEnabled()) yield "";
                yield gradient.getLuckPermsPrefix(onlinePlayer);
            }
            default -> null;
        };
    }
}
