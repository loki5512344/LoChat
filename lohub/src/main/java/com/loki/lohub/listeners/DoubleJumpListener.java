package com.loki.lohub.listeners;

import com.loki.lohub.LoHub;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.util.Vector;

import java.util.List;

public class DoubleJumpListener implements Listener {

    private final LoHub plugin;

    public DoubleJumpListener(LoHub plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!plugin.getConfig().getBoolean("double_jump.enabled", true)) {
            return;
        }

        Player player = event.getPlayer();

        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) {
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
        if (!plugin.getConfig().getBoolean("double_jump.enabled", true)) {
            return;
        }

        Player player = event.getPlayer();

        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) {
            return;
        }

        if (plugin.getCooldownManager().hasCooldown("double_jump", player.getUniqueId())) {
            event.setCancelled(true);
            player.setAllowFlight(false);
            player.setFlying(false);
            return;
        }

        event.setCancelled(true);
        player.setAllowFlight(false);
        player.setFlying(false);

        double power = plugin.getConfig().getDouble("double_jump.launch_power", 1.0);
        double powerY = plugin.getConfig().getDouble("double_jump.launch_power_y", 0.9);

        Vector velocity = player.getLocation().getDirection().multiply(power);
        velocity.setY(powerY);
        player.setVelocity(velocity);

        int cooldown = plugin.getConfig().getInt("double_jump.cooldown", 3);
        plugin.getCooldownManager().setCooldown("double_jump", player.getUniqueId(), cooldown);

        List<String> actions = plugin.getConfig().getStringList("double_jump.actions");
        if (!actions.isEmpty()) {
            plugin.getActionManager().executeActions(player, actions, false);
        }
    }
}
