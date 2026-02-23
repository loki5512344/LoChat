package com.loki.lohub.listeners;

import com.loki.lohub.LoHub;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;

import java.util.List;

public class LaunchpadListener implements Listener {

    private final LoHub plugin;

    public LaunchpadListener(LoHub plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!plugin.getConfig().getBoolean("launchpad.enabled", true)) {
            return;
        }

        if (event.getFrom().getBlockX() == event.getTo().getBlockX()
                && event.getFrom().getBlockY() == event.getTo().getBlockY()
                && event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
            return;
        }

        Player player = event.getPlayer();
        Block block = player.getLocation().getBlock();

        String topBlockStr = plugin.getConfig().getString("launchpad.top_block", "STONE_PRESSURE_PLATE");
        String bottomBlockStr = plugin.getConfig().getString("launchpad.bottom_block", "REDSTONE_BLOCK");

        Material topMaterial = Material.getMaterial(topBlockStr);
        Material bottomMaterial = Material.getMaterial(bottomBlockStr);

        if (topMaterial == null || bottomMaterial == null) {
            return;
        }

        if (block.getType() == topMaterial) {
            Block below = block.getRelative(0, -1, 0);
            if (below.getType() == bottomMaterial) {
                launchPlayer(player);
            }
        }
    }

    private void launchPlayer(Player player) {
        double power = plugin.getConfig().getDouble("launchpad.launch_power", 3.0);
        double powerY = plugin.getConfig().getDouble("launchpad.launch_power_y", 1.0);

        Vector velocity = player.getLocation().getDirection().multiply(power);
        velocity.setY(powerY);
        player.setVelocity(velocity);

        List<String> actions = plugin.getConfig().getStringList("launchpad.actions");
        if (!actions.isEmpty()) {
            plugin.getActionManager().executeActions(player, actions, false);
        }
    }
}
