package com.loki.lohub.listeners;

import com.loki.lohub.LoHub;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;

public class HubProtectionListener implements Listener {
    
    private final LoHub plugin;
    
    public HubProtectionListener(LoHub plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (plugin.getConfig().getBoolean("spawn.teleport-on-join", true)) {
            plugin.getSpawnManager().teleportToSpawn(event.getPlayer());
        }
    }
    
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (!plugin.getConfigManager().isProtectionEnabled()) return;
        
        Player player = event.getPlayer();
        if (player.hasPermission("lohub.bypass.protection")) return;
        
        if (plugin.getConfigManager().isBlockBreakProtected()) {
            event.setCancelled(true);
        }
    }
    
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!plugin.getConfigManager().isProtectionEnabled()) return;
        
        Player player = event.getPlayer();
        if (player.hasPermission("lohub.bypass.protection")) return;
        
        if (plugin.getConfigManager().isBlockPlaceProtected()) {
            event.setCancelled(true);
        }
    }
    
    @EventHandler
    public void onPvP(EntityDamageByEntityEvent event) {
        if (!plugin.getConfigManager().isProtectionEnabled()) return;
        if (!(event.getDamager() instanceof Player)) return;
        if (!(event.getEntity() instanceof Player)) return;
        
        Player damager = (Player) event.getDamager();
        if (damager.hasPermission("lohub.bypass.protection")) return;
        
        if (plugin.getConfigManager().isPvPProtected()) {
            event.setCancelled(true);
        }
    }
    
    @EventHandler
    public void onItemDrop(PlayerDropItemEvent event) {
        if (!plugin.getConfigManager().isProtectionEnabled()) return;
        
        Player player = event.getPlayer();
        if (player.hasPermission("lohub.bypass.protection")) return;
        
        if (plugin.getConfigManager().isItemDropProtected()) {
            event.setCancelled(true);
        }
    }
    
    @EventHandler
    public void onItemPickup(PlayerPickupItemEvent event) {
        if (!plugin.getConfigManager().isProtectionEnabled()) return;
        
        Player player = event.getPlayer();
        if (player.hasPermission("lohub.bypass.protection")) return;
        
        if (plugin.getConfigManager().isItemPickupProtected()) {
            event.setCancelled(true);
        }
    }
    
    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        if (!plugin.getConfigManager().isProtectionEnabled()) return;
        if (!(event.getEntity() instanceof Player)) return;
        
        Player player = (Player) event.getEntity();
        if (player.hasPermission("lohub.bypass.protection")) return;
        
        if (plugin.getConfigManager().isFoodLevelProtected()) {
            event.setCancelled(true);
        }
    }
}
