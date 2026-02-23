package com.loki.lochat.integrations;

import com.loki.lochat.LoChat;
import com.loki.lochat.api.service.ChatService;
import com.loki.lochat.api.service.IgnoreService;
import com.loki.lochat.api.service.PMService;
import com.loki.lochat.api.service.SpyService;
import com.loki.lochat.gradient.GradientModule;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

public class PlaceholderAPIHook extends PlaceholderExpansion {

    private final LoChat plugin;
    private final IgnoreService ignoreService;
    private final ChatService chatService;
    private final PMService pmService;
    private final SpyService spyService;

    public PlaceholderAPIHook(LoChat plugin) {
        this.plugin = plugin;
        this.ignoreService = plugin.getServiceRegistry().get(IgnoreService.class);
        this.chatService = plugin.getServiceRegistry().get(ChatService.class);
        this.pmService = plugin.getServiceRegistry().get(PMService.class);
        this.spyService = plugin.getServiceRegistry().get(SpyService.class);
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
        return plugin.getPluginMeta().getVersion();
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
            case "ignored_count" -> String.valueOf(ignoreService.getIgnoredCount(uuid));
            case "global_enabled" -> String.valueOf(!chatService.isGlobalChatDisabled(uuid));
            case "last_pm" -> {
                Optional<UUID> lastOpt = pmService.getLastConversation(uuid);
                if (lastOpt.isEmpty()) yield "";
                var lastPlayer = Bukkit.getOfflinePlayer(lastOpt.get());
                yield lastPlayer.getName() != null ? lastPlayer.getName() : "";
            }
            case "spy_enabled" -> String.valueOf(spyService.isSpying(uuid));

            // Gradient placeholders (совместимость с LoPreff)
            // Используем TAB формат §x§R§R§G§G§B§B для совместимости с TAB плагином
            case "gradient_full", "full" -> {
                Player onlinePlayer = player.getPlayer();
                if (onlinePlayer == null) yield player.getName() != null ? player.getName() : "";

                // Если gradient модуль выключен — берём только LuckPerms префикс + ник
                if (gradient == null || !gradient.isEnabled()) {
                    yield player.getName() != null ? player.getName() : "";
                }

                // Используем TAB формат для плейсхолдеров
                yield gradient.getFormattedNameForTab(onlinePlayer);
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
