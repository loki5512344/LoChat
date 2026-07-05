package com.loki.lochat.commands.admin.broadcast;

import com.loki.lochat.LoChat;
import com.loki.lochat.integrations.DiscordIntegration;
import com.loki.lochat.utils.format.ChatFormatter;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

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
            case "status" -> sender.sendMessage(ChatFormatter.parse(discord.isEnabled() ? "&#9878C9Discord &#7858E9включён" : "&#9878C9Discord &#CF6679отключён"));
            case "reload" -> {
                try {
                    discord.reload();
                    sender.sendMessage(ChatFormatter.parse("&#9878C9Discord &#7858E9перезагружен"));
                } catch (Exception e) {
                    sender.sendMessage(ChatFormatter.parse("&#CF6679Ошибка: " + e.getMessage()));
                }
            }
            case "test" -> {
                if (!discord.isEnabled()) {
                    sender.sendMessage(ChatFormatter.parse("&#CF6679Discord отключён"));
                    return true;
                }
                String msg = args.length > 1 ? String.join(" ", Arrays.copyOfRange(args, 1, args.length)) : "Тест от " + sender.getName();
                sender.sendMessage(ChatFormatter.parse("&#B798A8Отправка..."));
                java.util.concurrent.CompletableFuture.runAsync(() -> {
                    discord.sendTestMessage(msg, sender.getName());
                    if (sender instanceof org.bukkit.entity.Player p) {
                        p.getScheduler().run(plugin, t -> sender.sendMessage(ChatFormatter.parse("&#9878C9Отправлено &#7858E9✓")), null);
                    } else {
                        sender.sendMessage(ChatFormatter.parse("&#9878C9Отправлено &#7858E9✓"));
                    }
                });
            }
            default -> sendHelp(sender);
        }
        return true;
    }

    private void sendHelp(CommandSender s) {
        s.sendMessage(ChatFormatter.parse("&#B798A8/discordadmin status|reload|test"));
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (!sender.hasPermission("lochat.discord.admin")) {
            return null;
        }
        if (args.length == 1) {
            return List.of("status", "reload", "test");
        }
        return null;
    }
}
