package com.loki.lochat.commands.admin;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.loki.lochat.LoChat;
import com.loki.lochat.utils.ChatFormatter;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class HubCommand implements CommandExecutor {

    private final LoChat plugin;

    public HubCommand(LoChat plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatFormatter.parse(plugin.getMessageConfig().get("general.players-only")));
            return true;
        }
        if (!plugin.getConfigManager().getBoolean("hub_command.enabled", true)) {
            player.sendMessage(ChatFormatter.parse(plugin.getMessageConfig().get("general.command-disabled")));
            return true;
        }
        String server = plugin.getConfigManager().getString("hub_command.server", "hub");
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("Connect");
        out.writeUTF(server);
        player.sendPluginMessage(plugin, "BungeeCord", out.toByteArray());
        player.sendMessage(ChatFormatter.parse(plugin.getMessageConfig().get("hub.connecting").replace("%server%", server)));
        return true;
    }
}
