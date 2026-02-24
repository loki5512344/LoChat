package com.loki.lohub.commands;

import com.loki.lohub.LoHub;
import com.loki.lohub.utils.TextUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class HubCommand implements CommandExecutor {

    private final LoHub plugin;

    public HubCommand(LoHub plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(TextUtil.colorize(plugin.getConfigManager().getMessage("general.players-only")));
            return true;
        }

        if (plugin.getSpawnManager().getSpawn() == null) {
            player.sendMessage(TextUtil.colorize(plugin.getConfigManager().getMessage("spawn.no-spawn")));
            return true;
        }

        plugin.getSpawnManager().teleportToSpawn(player);
        player.sendMessage(TextUtil.colorize(plugin.getConfigManager().getMessage("spawn.teleport")));

        return true;
    }
}
