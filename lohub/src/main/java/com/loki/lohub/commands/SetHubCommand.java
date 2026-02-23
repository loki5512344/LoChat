package com.loki.lohub.commands;

import com.loki.lohub.LoHub;
import com.loki.lohub.utils.TextUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetHubCommand implements CommandExecutor {

    private final LoHub plugin;

    public SetHubCommand(LoHub plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(TextUtil.colorize("&cThis command is only for players!"));
            return true;
        }

        if (!player.hasPermission("lohub.admin")) {
            player.sendMessage(TextUtil.colorize(plugin.getConfig().getString("messages.no-permission")));
            return true;
        }

        plugin.getSpawnManager().setSpawn(player.getLocation());
        player.sendMessage(TextUtil.colorize(plugin.getConfig().getString("messages.spawn-set")));

        plugin.getRegionManager().deleteHubRegion();
        plugin.getRegionManager().createHubRegion();

        if (plugin.getConfig().getBoolean("worldguard.auto-create-region")) {
            player.sendMessage(TextUtil.colorize(plugin.getConfig().getString("messages.region-created")));
        }

        return true;
    }
}
