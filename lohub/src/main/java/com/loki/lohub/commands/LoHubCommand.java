package com.loki.lohub.commands;

import com.loki.lohub.LoHub;
import com.loki.lohub.utils.TextUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class LoHubCommand implements CommandExecutor {
    
    private final LoHub plugin;
    
    public LoHubCommand(LoHub plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("lohub.admin")) {
            sender.sendMessage(TextUtil.colorize(plugin.getConfig().getString("messages.no-permission")));
            return true;
        }
        
        if (args.length == 0) {
            sender.sendMessage(TextUtil.colorize("&7LoHub v" + plugin.getDescription().getVersion()));
            sender.sendMessage(TextUtil.colorize("&7Use: &e/lohub reload"));
            return true;
        }
        
        if (args[0].equalsIgnoreCase("reload")) {
            plugin.getConfigManager().reload();
            sender.sendMessage(TextUtil.colorize("&aConfiguration reloaded!"));
            return true;
        }
        
        return true;
    }
}
