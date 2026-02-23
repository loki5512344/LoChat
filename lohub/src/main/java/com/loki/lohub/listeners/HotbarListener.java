package com.loki.lohub.listeners;

import com.loki.lohub.LoHub;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class HotbarListener implements Listener {

    private final LoHub plugin;

    public HotbarListener(LoHub plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Player player = event.getPlayer();
        int slot = player.getInventory().getHeldItemSlot();

        if (plugin.getPlayerHiderManager().isPlayerHiderSlot(slot)) {
            event.setCancelled(true);

            if (plugin.getCooldownManager().hasCooldown("player_hider", player.getUniqueId())) {
                return;
            }

            int cooldown = plugin.getConfig().getInt("player_hider.cooldown", 3);
            plugin.getCooldownManager().setCooldown("player_hider", player.getUniqueId(), cooldown);

            plugin.getPlayerHiderManager().toggle(player);
        } else if (plugin.getHotbarManager().isHotbarItem(slot)) {
            event.setCancelled(true);
            plugin.getHotbarManager().handleClick(player, slot);
        }
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        if (!plugin.getConfig().getBoolean("custom_join_items.disable_inventory_movement", true)) {
            return;
        }

        Player player = event.getPlayer();
        int slot = player.getInventory().getHeldItemSlot();

        if (plugin.getHotbarManager().isHotbarItem(slot) || plugin.getPlayerHiderManager().isPlayerHiderSlot(slot)) {
            event.setCancelled(true);
        }
    }
}
