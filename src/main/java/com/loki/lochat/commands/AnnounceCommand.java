package com.loki.lochat.commands;

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

        // /announce reload
        if (args[0].equalsIgnoreCase("reload") && sender.hasPermission("chat.announce.reload")) {
            plugin.reload();
            sender.sendMessage(ChatFormatter.parse(plugin.getMessageConfig().get("announce.reloaded")));
            return true;
        }

        String message = String.join(" ", args);
        String format = plugin.getConfigManager().getAnnouncementFormat();
        Component formatted = ChatFormatter.formatAnnouncement(format, message);

        // Отправка в чат
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendMessage(formatted);
            
            // ActionBar
            player.sendActionBar(formatted);
            
            // Title
            Title title = Title.title(
                    ChatFormatter.parse("<gold>ОБЪЯВЛЕНИЕ</gold>"),
                    ChatFormatter.parse(message),
                    Title.Times.times(Duration.ofMillis(500), Duration.ofSeconds(3), Duration.ofMillis(500))
            );
            player.showTitle(title);
        }

        plugin.getLogger().info("[ANNOUNCE] " + sender.getName() + ": " + ChatFormatter.stripTags(message));
        return true;
    }
}
