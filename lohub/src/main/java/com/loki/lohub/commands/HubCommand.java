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
            sender.sendMessage(TextUtil.colorize("&cThis command is only for players!"));
            return true;
        }
        
        if (plugin.getSpawnManager().getSpawn() == null) {
            player.sendMessage(TextUtil.colorize(plugin.getConfig().getString("messages.no-spawn")));
            return true;
        }
        
        plugin.getSpawnManager().teleportToSpawn(player);
        player.sendMessage(TextUtil.colorize(plugin.getConfig().getString("messages.spawn-teleport")));
        
        return true;
    }
}
