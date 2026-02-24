package com.loki.lohub.listeners;

import com.loki.lohub.LoHub;
import com.loki.lohub.common.ConfigHelper;
import com.loki.lohub.common.GameModeHelper;
import com.loki.lohub.common.LaunchHelper;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;

import java.util.List;

public class DoubleJumpListener implements Listener {

    private static final String CONFIG_PATH = "double_jump";
    private static final String COOLDOWN_KEY = "double_jump";

    private final LoHub plugin;

    public DoubleJumpListener(LoHub plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!ConfigHelper.isEnabled(plugin.getConfig(), CONFIG_PATH)) {
            return;
        }

        Player player = event.getPlayer();

        if (GameModeHelper.isCreativeOrSpectator(player)) {
            return;
        }

        if (!player.isOnGround() && !player.getAllowFlight()) {
            player.setAllowFlight(true);
        } else if (player.isOnGround() && player.getAllowFlight()) {
            player.setAllowFlight(false);
            player.setFlying(false);
        }
    }

    @EventHandler
    public void onPlayerToggleFlight(PlayerToggleFlightEvent event) {
        if (!ConfigHelper.isEnabled(plugin.getConfig(), CONFIG_PATH)) {
            return;
        }

        Player player = event.getPlayer();

        if (GameModeHelper.isCreativeOrSpectator(player)) {
            return;
        }

        if (plugin.getCooldownManager().hasCooldown(COOLDOWN_KEY, player.getUniqueId())) {
            cancelFlight(event, player);
            return;
        }

        cancelFlight(event, player);
        performDoubleJump(player);
    }

    private void cancelFlight(PlayerToggleFlightEvent event, Player player) {
        event.setCancelled(true);
        player.setAllowFlight(false);
        player.setFlying(false);
    }

    private void performDoubleJump(Player player) {
        double power = ConfigHelper.getPower(plugin.getConfig(), CONFIG_PATH, "launch_power", 1.0);
        double powerY = ConfigHelper.getPower(plugin.getConfig(), CONFIG_PATH, "launch_power_y", 0.9);

        LaunchHelper.launch(player, power, powerY);

        int cooldown = ConfigHelper.getCooldown(plugin.getConfig(), CONFIG_PATH, 3);
        plugin.getCooldownManager().setCooldown(COOLDOWN_KEY, player.getUniqueId(), cooldown);

        executeActions(player);
    }

    private void executeActions(Player player) {
        List<String> actions = plugin.getConfig().getStringList(CONFIG_PATH + ".actions");
        if (!actions.isEmpty()) {
            plugin.getActionManager().executeActions(player, actions, false);
        }
    }
}
