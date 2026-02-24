package com.loki.lohub.listeners;

import com.loki.lohub.LoHub;
import com.loki.lohub.managers.CustomCommandManager;
import com.loki.lohub.utils.TextUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class CustomCommandListener implements Listener {

    private final LoHub plugin;

    public CustomCommandListener(LoHub plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage().toLowerCase();

        if (!message.startsWith("/")) {
            return;
        }

        String command = message.substring(1).split(" ")[0];
        CustomCommandManager.CustomCommand customCommand = plugin.getCustomCommandManager().getCommand(command);

        if (customCommand == null) {
            return;
        }

        event.setCancelled(true);

        if (customCommand.permission() != null && !customCommand.permission().isEmpty()) {
            if (!player.hasPermission(customCommand.permission())) {
                player.sendMessage(TextUtil.colorize(plugin.getConfig().getString("messages.no-permission")));
                return;
            }
        }

        plugin.getActionManager().executeActions(player, customCommand.actions(), false);
    }
}
