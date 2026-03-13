package com.loki.lochat.commands;

import com.loki.lochat.LoChat;
import com.loki.lochat.integrations.DiscordIntegration;
import com.loki.lochat.utils.ChatFormatter;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

/**
 * Команда для управления Discord интеграцией
 */
public class DiscordCommand implements CommandExecutor, TabCompleter {
    private final LoChat plugin;
    private final DiscordIntegration discord;

    public DiscordCommand(LoChat plugin, DiscordIntegration discord) {
        this.plugin = plugin;
        this.discord = discord;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("lochat.discord.admin")) {
            sender.sendMessage(ChatFormatter.parse(plugin.getMessageConfig().get("errors.no-permission")));
            return true;
        }

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "status":
                handleStatus(sender);
                break;
            case "reload":
                handleReload(sender);
                break;
            case "test":
                handleTest(sender, args);
                break;
            default:
                sendHelp(sender);
                break;
        }

        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ChatFormatter.parse("&6&l=== Discord Integration ==="));
        sender.sendMessage(ChatFormatter.parse("&e/discord status &7- Показать статус интеграции"));
        sender.sendMessage(ChatFormatter.parse("&e/discord reload &7- Перезагрузить конфигурацию"));
        sender.sendMessage(ChatFormatter.parse("&e/discord test [message] &7- Отправить тестовое сообщение"));
    }

    private void handleStatus(CommandSender sender) {
        if (discord.isEnabled()) {
            sender.sendMessage(ChatFormatter.parse("&a✓ Discord интеграция включена"));
        } else {
            sender.sendMessage(ChatFormatter.parse("&c✗ Discord интеграция отключена"));
        }
    }

    private void handleReload(CommandSender sender) {
        try {
            discord.reload();
            sender.sendMessage(ChatFormatter.parse("&a✓ Discord конфигурация перезагружена"));
        } catch (Exception e) {
            sender.sendMessage(ChatFormatter.parse("&c✗ Ошибка при перезагрузке: " + e.getMessage()));
            plugin.getLogger().severe("Error reloading Discord config: " + e.getMessage());
        }
    }

    private void handleTest(CommandSender sender, String[] args) {
        if (!discord.isEnabled()) {
            sender.sendMessage(ChatFormatter.parse("&c✗ Discord интеграция отключена"));
            return;
        }

        String message = args.length > 1 ? String.join(" ", Arrays.copyOfRange(args, 1, args.length)) : "Тестовое сообщение от " + sender.getName();
        
        sender.sendMessage(ChatFormatter.parse("&e⏳ Отправка тестового сообщения..."));
        
        // Используем CompletableFuture для асинхронной отправки (Folia-совместимо)
        java.util.concurrent.CompletableFuture.runAsync(() -> {
            try {
                discord.sendTestMessage(message, sender.getName());
                // Возвращаемся в главный поток для отправки сообщения
                if (sender instanceof org.bukkit.entity.Player player) {
                    player.getScheduler().run(plugin, (task) -> 
                        sender.sendMessage(ChatFormatter.parse("&a✓ Тестовое сообщение отправлено в Discord")), null
                    );
                } else {
                    // Для консоли можно отправить сразу
                    sender.sendMessage(ChatFormatter.parse("&a✓ Тестовое сообщение отправлено в Discord"));
                }
            } catch (Exception e) {
                if (sender instanceof org.bukkit.entity.Player player) {
                    player.getScheduler().run(plugin, (task) -> 
                        sender.sendMessage(ChatFormatter.parse("&c✗ Ошибка отправки: " + e.getMessage())), null
                    );
                } else {
                    sender.sendMessage(ChatFormatter.parse("&c✗ Ошибка отправки: " + e.getMessage()));
                }
                plugin.getLogger().severe("Discord test error: " + e.getMessage());
            }
        });
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (!sender.hasPermission("lochat.discord.admin")) {
            return null;
        }

        if (args.length == 1) {
            return Arrays.asList("status", "reload", "test");
        }

        return null;
    }
}