package com.loki.lochat.commands.admin;

import com.loki.lochat.LoChat;
import com.loki.lochat.utils.ChatFormatter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;

public class AnnounceCommand implements CommandExecutor {

    private final LoChat plugin;

    public AnnounceCommand(LoChat plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            sender.sendMessage(ChatFormatter.parse(plugin.getMessageConfig().getInvalidUsage("/announce <сообщение>")));
            return true;
        }
        if (args[0].equalsIgnoreCase("reload") && sender.hasPermission("chat.announce.reload")) {
            plugin.reload();
            sender.sendMessage(ChatFormatter.parse(plugin.getMessageConfig().get("announce.reloaded")));
            return true;
        }

        String message = String.join(" ", args);
        Component formatted = ChatFormatter.formatAnnouncement(plugin.getConfigManager().getAnnouncementFormat(), message);

        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendMessage(formatted);
            if (plugin.getConfigManager().isAnnouncementActionBarEnabled())
                player.sendActionBar(formatted);
            if (plugin.getConfigManager().isAnnouncementTitleEnabled()) {
                player.showTitle(Title.title(
                        ChatFormatter.parse(plugin.getConfigManager().getAnnouncementTitleHeader()),
                        ChatFormatter.parse(message),
                        Title.Times.times(Duration.ofMillis(500),
                                Duration.ofSeconds(plugin.getConfigManager().getAnnouncementTitleDuration()),
                                Duration.ofMillis(500))));
            }
        }
        plugin.getLogger().info("[ANNOUNCE] " + sender.getName() + ": " + ChatFormatter.stripTags(message));
        return true;
    }
}
