package com.loki.lohub.features;

import com.loki.lohub.utils.TextUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public final class JoinMessageHandler {

    private static final String CONFIG_PATH = "join_leave_messages";

    private JoinMessageHandler() {
    }

    public static void handleJoin(PlayerJoinEvent event, Player player, FileConfiguration config) {
        if (!config.getBoolean(CONFIG_PATH + ".enabled", true)) {
            return;
        }

        String message = config.getString(CONFIG_PATH + ".join_message", "");
        if (message.isEmpty()) {
            event.joinMessage(null);
        } else {
            String formatted = TextUtil.colorize(message.replace("%player%", player.getName()));
            event.joinMessage(Component.text(formatted));
        }
    }

    public static void handleQuit(PlayerQuitEvent event, Player player, FileConfiguration config) {
        if (!config.getBoolean(CONFIG_PATH + ".enabled", true)) {
            return;
        }

        String message = config.getString(CONFIG_PATH + ".quit_message", "");
        if (message.isEmpty()) {
            event.quitMessage(null);
        } else {
            String formatted = TextUtil.colorize(message.replace("%player%", player.getName()));
            event.quitMessage(Component.text(formatted));
        }
    }
}
