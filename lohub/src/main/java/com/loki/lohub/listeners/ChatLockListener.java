package com.loki.lohub.listeners;

import com.loki.lohub.LoHub;
import com.loki.lohub.utils.TextUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.Arrays;
import java.util.List;

public class ChatLockListener implements Listener {

    private static final List<String> BLOCKED_COMMANDS = Arrays.asList(
            "me", "msg", "tell", "w", "whisper", "pm", "dm",
            "reply", "r", "say", "minecraft:me", "minecraft:msg",
            "minecraft:tell", "bukkit:me", "bukkit:msg", "bukkit:tell"
    );

    private final LoHub plugin;

    public ChatLockListener(LoHub plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        if (!plugin.getChatLockManager().isChatLocked()) {
            return;
        }

        Player player = event.getPlayer();
        if (player.hasPermission("lohub.chatlock.bypass")) {
            return;
        }

        event.setCancelled(true);
        player.sendMessage(Component.text(TextUtil.colorize(plugin.getConfigManager().getMessage("chat.locked"))));
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        if (!plugin.getChatLockManager().isChatLocked()) {
            return;
        }

        Player player = event.getPlayer();
        if (player.hasPermission("lohub.chatlock.bypass")) {
            return;
        }

        String command = event.getMessage().toLowerCase().substring(1);
        String baseCommand = command.split(" ")[0];

        if (isBlockedCommand(baseCommand)) {
            event.setCancelled(true);
            player.sendMessage(Component.text(TextUtil.colorize(plugin.getConfigManager().getMessage("chat.locked"))));
        }
    }

    private boolean isBlockedCommand(String command) {
        return BLOCKED_COMMANDS.stream().anyMatch(blocked -> command.equals(blocked));
    }
}
