package com.loki.lohub.managers;

import com.loki.lohub.LoHub;
import com.loki.lohub.utils.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class PlayerHiderManager {

    private final LoHub plugin;
    private final Set<UUID> hiddenPlayers = new HashSet<>();

    public PlayerHiderManager(LoHub plugin) {
        this.plugin = plugin;
    }

    public void giveItem(Player player) {
        if (!plugin.getConfig().getBoolean("player_hider.enabled", true)) {
            return;
        }

        int slot = plugin.getConfig().getInt("player_hider.slot", 8);
        player.getInventory().setItem(slot, getItem(player));
    }

    public ItemStack getItem(Player player) {
        boolean hidden = hiddenPlayers.contains(player.getUniqueId());
        String path = hidden ? "player_hider.hidden" : "player_hider.not_hidden";

        Material material = Material.getMaterial(plugin.getConfig().getString(path + ".material", "LIME_DYE"));
        if (material == null) {
            material = Material.LIME_DYE;
        }

        int amount = plugin.getConfig().getInt(path + ".amount", 1);
        String displayName = plugin.getConfig().getString(path + ".display_name", "");
        List<String> lore = plugin.getConfig().getStringList(path + ".lore");

        return new ItemBuilder(material)
                .amount(amount)
                .name(displayName)
                .lore(lore)
                .build();
    }

    public void toggle(Player player) {
        if (hiddenPlayers.contains(player.getUniqueId())) {
            showPlayers(player);
        } else {
            hidePlayers(player);
        }

        updateItem(player);
    }

    private void hidePlayers(Player player) {
        hiddenPlayers.add(player.getUniqueId());
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (!online.equals(player)) {
                player.hidePlayer(plugin, online);
            }
        }
    }

    private void showPlayers(Player player) {
        hiddenPlayers.remove(player.getUniqueId());
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (!online.equals(player)) {
                player.showPlayer(plugin, online);
            }
        }
    }

    private void updateItem(Player player) {
        int slot = plugin.getConfig().getInt("player_hider.slot", 8);
        player.getInventory().setItem(slot, getItem(player));
    }

    public boolean isPlayerHiderSlot(int slot) {
        return plugin.getConfig().getBoolean("player_hider.enabled", true)
                && slot == plugin.getConfig().getInt("player_hider.slot", 8);
    }

    public void removePlayer(Player player) {
        hiddenPlayers.remove(player.getUniqueId());
    }
}
