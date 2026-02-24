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

    private static final String CONFIG_PATH = "custom_join_items";

    private final LoHub plugin;
    private final Map<Integer, HotbarItem> items = new HashMap<>();

    public HotbarManager(LoHub plugin) {
        this.plugin = plugin;
        loadItems();
    }

    private void loadItems() {
        items.clear();
        ConfigurationSection section = plugin.getConfig().getConfigurationSection(CONFIG_PATH + ".items");
        if (section == null) {
            return;
        }

        for (String key : section.getKeys(false)) {
            loadItem(section.getConfigurationSection(key));
        }
    }

    private void loadItem(ConfigurationSection itemSection) {
        if (itemSection == null) {
            return;
        }

        Material material = getMaterial(itemSection);
        if (material == null) {
            return;
        }

        int slot = itemSection.getInt("slot", 0);
        ItemStack item = buildItem(itemSection, material);
        List<String> actions = itemSection.getStringList("actions");

        items.put(slot, new HotbarItem(item, actions));
    }

    private Material getMaterial(ConfigurationSection section) {
        String materialName = section.getString("material", "STONE");
        return Material.getMaterial(materialName);
    }

    private ItemStack buildItem(ConfigurationSection section, Material material) {
        int amount = section.getInt("amount", 1);
        String displayName = section.getString("display_name", "");
        List<String> lore = section.getStringList("lore");

        return new ItemBuilder(material)
                .amount(amount)
                .name(displayName)
                .lore(lore)
                .build();
    }

    public void giveItems(Player player) {
        if (!isEnabled()) {
            return;
        }

        items.forEach((slot, item) -> player.getInventory().setItem(slot, item.item()));
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

    private boolean isEnabled() {
        return plugin.getConfig().getBoolean(CONFIG_PATH + ".enabled", false);
    }

    private record HotbarItem(ItemStack item, List<String> actions) {
    }
}
