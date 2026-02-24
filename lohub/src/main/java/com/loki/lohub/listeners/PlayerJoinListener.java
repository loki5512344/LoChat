package com.loki.lohub.listeners;

import com.loki.lohub.LoHub;
import com.loki.lohub.features.FireworkHandler;
import com.loki.lohub.features.JoinMessageHandler;
import com.loki.lohub.features.JoinSettingsHandler;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import java.util.List;

public class PlayerJoinListener implements Listener {

    private final LoHub plugin;

    public PlayerJoinListener(LoHub plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (isWorldDisabled(player.getWorld().getName())) {
            return;
        }

        JoinMessageHandler.handleJoin(event, player, plugin.getConfig());
        JoinSettingsHandler.apply(player, plugin);
        executeJoinActions(player);
        FireworkHandler.spawn(player, plugin.getConfig());

        plugin.getScoreboardManager().setScoreboard(player, true);
        plugin.getTablistManager().setTablist(player);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        if (isWorldDisabled(player.getWorld().getName())) {
            return;
        }

        plugin.getPlayerHiderManager().removePlayer(player);
        plugin.getScoreboardManager().removePlayer(player);

        JoinMessageHandler.handleQuit(event, player, plugin.getConfig());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();

        if (isWorldDisabled(player.getWorld().getName())) {
            return;
        }

        if (plugin.getConfig().getBoolean("spawn.teleport-on-respawn", true)) {
            Location spawn = plugin.getSpawnManager().getSpawn();
            if (spawn != null) {
                event.setRespawnLocation(spawn);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (isWorldDisabled(event.getPlayer().getWorld().getName())) {
            return;
        }

        if (plugin.getConfig().getBoolean("world_settings.disable_death_message", true)) {
            event.deathMessage(null);
        }
    }

    private void executeJoinActions(Player player) {
        List<String> actions = plugin.getConfig().getStringList("join_events");
        if (!actions.isEmpty()) {
            plugin.getActionManager().executeActions(player, actions, false);
        }
    }

    private boolean isWorldDisabled(String worldName) {
        List<String> disabledWorlds = plugin.getConfig().getStringList("disabled-worlds.worlds");
        boolean invert = plugin.getConfig().getBoolean("disabled-worlds.invert", false);
        boolean isInList = disabledWorlds.contains(worldName);

        return invert ? !isInList : isInList;
    }
}
