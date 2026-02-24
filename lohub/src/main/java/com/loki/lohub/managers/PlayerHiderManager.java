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

    private static final String CONFIG_PATH = "player_hider";
    private static final String HIDDEN_PATH = CONFIG_PATH + ".hidden";
    private static final String NOT_HIDDEN_PATH = CONFIG_PATH + ".not_hidden";

    private final LoHub plugin;
    private final Set<UUID> hiddenPlayers = new HashSet<>();

    public PlayerHiderManager(LoHub plugin) {
        this.plugin = plugin;
    }

    public void giveItem(Player player) {
        if (!isEnabled()) {
            return;
        }

        player.getInventory().setItem(getSlot(), getItem(player));
    }

    public ItemStack getItem(Player player) {
        boolean hidden = isHidden(player);
        String path = hidden ? HIDDEN_PATH : NOT_HIDDEN_PATH;

        Material material = getMaterial(path);
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
        if (isHidden(player)) {
            showPlayers(player);
        } else {
            hidePlayers(player);
        }

        updateItem(player);
    }

    private void hidePlayers(Player player) {
        hiddenPlayers.add(player.getUniqueId());
        applyVisibility(player, false);
    }

    private void showPlayers(Player player) {
        hiddenPlayers.remove(player.getUniqueId());
        applyVisibility(player, true);
    }

    private void applyVisibility(Player player, boolean visible) {
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (!online.equals(player)) {
                if (visible) {
                    player.showPlayer(plugin, online);
                } else {
                    player.hidePlayer(plugin, online);
                }
            }
        }
    }

    private void updateItem(Player player) {
        player.getInventory().setItem(getSlot(), getItem(player));
    }

    public boolean isPlayerHiderSlot(int slot) {
        return isEnabled() && slot == getSlot();
    }

    public void removePlayer(Player player) {
        hiddenPlayers.remove(player.getUniqueId());
    }

    private boolean isEnabled() {
        return plugin.getConfig().getBoolean(CONFIG_PATH + ".enabled", true);
    }

    private int getSlot() {
        return plugin.getConfig().getInt(CONFIG_PATH + ".slot", 8);
    }

    private boolean isHidden(Player player) {
        return hiddenPlayers.contains(player.getUniqueId());
    }

    private Material getMaterial(String path) {
        String materialName = plugin.getConfig().getString(path + ".material", "LIME_DYE");
        Material material = Material.getMaterial(materialName);
        return material != null ? material : Material.LIME_DYE;
    }
}
