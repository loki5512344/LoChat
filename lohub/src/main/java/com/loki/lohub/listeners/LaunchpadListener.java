package com.loki.lohub.listeners;

import com.loki.lohub.LoHub;
import com.loki.lohub.common.ConfigHelper;
import com.loki.lohub.common.LaunchHelper;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.List;

public class LaunchpadListener implements Listener {

    private static final String CONFIG_PATH = "launchpad";

    private final LoHub plugin;

    public LaunchpadListener(LoHub plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!ConfigHelper.isEnabled(plugin.getConfig(), CONFIG_PATH)) {
            return;
        }

        if (!hasMoved(event)) {
            return;
        }

        Player player = event.getPlayer();
        Block block = player.getLocation().getBlock();

        if (isLaunchpad(block)) {
            launchPlayer(player);
        }
    }

    private boolean hasMoved(PlayerMoveEvent event) {
        return event.getFrom().getBlockX() != event.getTo().getBlockX()
                || event.getFrom().getBlockY() != event.getTo().getBlockY()
                || event.getFrom().getBlockZ() != event.getTo().getBlockZ();
    }

    private boolean isLaunchpad(Block block) {
        Material topMaterial = getMaterial("top_block", "STONE_PRESSURE_PLATE");
        Material bottomMaterial = getMaterial("bottom_block", "REDSTONE_BLOCK");

        if (topMaterial == null || bottomMaterial == null) {
            return false;
        }

        if (block.getType() != topMaterial) {
            return false;
        }

        Block below = block.getRelative(0, -1, 0);
        return below.getType() == bottomMaterial;
    }

    private Material getMaterial(String key, String defaultValue) {
        String materialName = plugin.getConfig().getString(CONFIG_PATH + "." + key, defaultValue);
        return Material.getMaterial(materialName);
    }

    private void launchPlayer(Player player) {
        double power = ConfigHelper.getPower(plugin.getConfig(), CONFIG_PATH, "launch_power", 3.0);
        double powerY = ConfigHelper.getPower(plugin.getConfig(), CONFIG_PATH, "launch_power_y", 1.0);

        LaunchHelper.launch(player, power, powerY);
        executeActions(player);
    }

    private void executeActions(Player player) {
        List<String> actions = plugin.getConfig().getStringList(CONFIG_PATH + ".actions");
        if (!actions.isEmpty()) {
            plugin.getActionManager().executeActions(player, actions, false);
        }
    }
}
