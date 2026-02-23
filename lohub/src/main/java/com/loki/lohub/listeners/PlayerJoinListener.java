package com.loki.lohub.listeners;

import com.loki.lohub.LoHub;
import com.loki.lohub.utils.TextUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.meta.FireworkMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class PlayerJoinListener implements Listener {

    private static final Map<String, Color> COLOR_MAP = new HashMap<>();

    static {
        COLOR_MAP.put("AQUA", Color.AQUA);
        COLOR_MAP.put("BLACK", Color.BLACK);
        COLOR_MAP.put("BLUE", Color.BLUE);
        COLOR_MAP.put("FUCHSIA", Color.FUCHSIA);
        COLOR_MAP.put("GRAY", Color.GRAY);
        COLOR_MAP.put("GREEN", Color.GREEN);
        COLOR_MAP.put("LIME", Color.LIME);
        COLOR_MAP.put("MAROON", Color.MAROON);
        COLOR_MAP.put("NAVY", Color.NAVY);
        COLOR_MAP.put("OLIVE", Color.OLIVE);
        COLOR_MAP.put("ORANGE", Color.ORANGE);
        COLOR_MAP.put("PURPLE", Color.PURPLE);
        COLOR_MAP.put("RED", Color.RED);
        COLOR_MAP.put("SILVER", Color.SILVER);
        COLOR_MAP.put("TEAL", Color.TEAL);
        COLOR_MAP.put("WHITE", Color.WHITE);
        COLOR_MAP.put("YELLOW", Color.YELLOW);
    }

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

        handleJoinMessage(event, player);
        handleJoinSettings(player);
        handleJoinEvents(player);
        handleFirework(player);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        if (isWorldDisabled(player.getWorld().getName())) {
            return;
        }

        if (plugin.getConfig().getBoolean("join_leave_messages.enabled", true)) {
            String quitMessage = plugin.getConfig().getString("join_leave_messages.quit_message", "");
            if (quitMessage.isEmpty()) {
                event.quitMessage(null);
            } else {
                event.quitMessage(Component.text(TextUtil.colorize(quitMessage.replace("%player%", player.getName()))));
            }
        }
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

    private void handleJoinMessage(PlayerJoinEvent event, Player player) {
        if (plugin.getConfig().getBoolean("join_leave_messages.enabled", true)) {
            String joinMessage = plugin.getConfig().getString("join_leave_messages.join_message", "");
            if (joinMessage.isEmpty()) {
                event.joinMessage(null);
            } else {
                event.joinMessage(Component.text(TextUtil.colorize(joinMessage.replace("%player%", player.getName()))));
            }
        }
    }

    private void handleJoinSettings(Player player) {
        if (plugin.getConfig().getBoolean("join_settings.spawn_join", true)) {
            Location spawn = plugin.getSpawnManager().getSpawn();
            if (spawn != null) {
                player.teleport(spawn);
            }
        }

        if (plugin.getConfig().getBoolean("join_settings.heal", true)) {
            player.setHealth(player.getMaxHealth());
            player.setFoodLevel(20);
            player.setSaturation(20);
        }

        if (plugin.getConfig().getBoolean("join_settings.extinguish", true)) {
            player.setFireTicks(0);
        }

        if (plugin.getConfig().getBoolean("join_settings.clear_inventory", false)) {
            player.getInventory().clear();
        }
    }

    private void handleJoinEvents(Player player) {
        List<String> actions = plugin.getConfig().getStringList("join_events");
        if (!actions.isEmpty()) {
            plugin.getActionManager().executeActions(player, actions, false);
        }
    }

    private void handleFirework(Player player) {
        if (!plugin.getConfig().getBoolean("join_settings.firework.enabled", false)) {
            return;
        }

        boolean firstJoinOnly = plugin.getConfig().getBoolean("join_settings.firework.first_join_only", true);
        if (firstJoinOnly && player.hasPlayedBefore()) {
            return;
        }

        Location loc = player.getLocation();
        Firework firework = (Firework) loc.getWorld().spawnEntity(loc, EntityType.FIREWORK_ROCKET);
        FireworkMeta meta = firework.getFireworkMeta();

        String typeStr = plugin.getConfig().getString("join_settings.firework.type", "BALL_LARGE");
        FireworkEffect.Type type;
        try {
            type = FireworkEffect.Type.valueOf(typeStr);
        } catch (IllegalArgumentException e) {
            type = FireworkEffect.Type.BALL_LARGE;
        }

        List<Color> colors = new ArrayList<>();
        for (String colorStr : plugin.getConfig().getStringList("join_settings.firework.colors")) {
            Color color = parseColor(colorStr);
            if (color != null) {
                colors.add(color);
            }
        }

        if (colors.isEmpty()) {
            colors.add(Color.AQUA);
        }

        FireworkEffect effect = FireworkEffect.builder()
                .with(type)
                .withColor(colors)
                .flicker(plugin.getConfig().getBoolean("join_settings.firework.flicker", true))
                .trail(plugin.getConfig().getBoolean("join_settings.firework.trail", true))
                .build();

        meta.addEffect(effect);
        meta.setPower(plugin.getConfig().getInt("join_settings.firework.power", 1));
        firework.setFireworkMeta(meta);
    }

    private Color parseColor(String colorStr) {
        return COLOR_MAP.get(colorStr.toUpperCase());
    }

    private boolean isWorldDisabled(String worldName) {
        List<String> disabledWorlds = plugin.getConfig().getStringList("disabled-worlds.worlds");
        boolean invert = plugin.getConfig().getBoolean("disabled-worlds.invert", false);
        boolean isInList = disabledWorlds.contains(worldName);

        return invert ? !isInList : isInList;
    }
}
