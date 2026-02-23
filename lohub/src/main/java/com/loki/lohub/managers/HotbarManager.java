package com.loki.lohub.managers;

import com.loki.lohub.LoHub;
import com.loki.lohub.utils.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HotbarManager {

    private final LoHub plugin;
    private final Map<Integer, HotbarItem> items = new HashMap<>();

    public HotbarManager(LoHub plugin) {
        this.plugin = plugin;
        loadItems();
    }

    private void loadItems() {
        items.clear();
        ConfigurationSection section = plugin.getConfig().getConfigurationSection("custom_join_items.items");
        if (section == null) {
            return;
        }

        for (String key : section.getKeys(false)) {
            ConfigurationSection itemSection = section.getConfigurationSection(key);
            if (itemSection == null) {
                continue;
            }

            Material material = Material.getMaterial(itemSection.getString("material", "STONE"));
            if (material == null) {
                continue;
            }

            int slot = itemSection.getInt("slot", 0);
            int amount = itemSection.getInt("amount", 1);
            String displayName = itemSection.getString("display_name", "");
            List<String> lore = itemSection.getStringList("lore");
            List<String> actions = itemSection.getStringList("actions");

            ItemStack item = new ItemBuilder(material)
                    .amount(amount)
                    .name(displayName)
                    .lore(lore)
                    .build();

            items.put(slot, new HotbarItem(item, actions));
        }
    }

    public void giveItems(Player player) {
        if (!plugin.getConfig().getBoolean("custom_join_items.enabled", false)) {
            return;
        }

        for (Map.Entry<Integer, HotbarItem> entry : items.entrySet()) {
            player.getInventory().setItem(entry.getKey(), entry.getValue().item());
        }
    }

    public void handleClick(Player player, int slot) {
        HotbarItem item = items.get(slot);
        if (item == null) {
            return;
        }

        plugin.getActionManager().executeActions(player, item.actions(), false);
    }

    public boolean isHotbarItem(int slot) {
        return items.containsKey(slot);
    }

    public void reload() {
        loadItems();
    }

    private record HotbarItem(ItemStack item, List<String> actions) {
    }
}
