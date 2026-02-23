package com.loki.lohub.utils;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.List;
import java.util.stream.Collectors;

public class ItemBuilder {

    private final ItemStack itemStack;

    public ItemBuilder(Material material) {
        this.itemStack = new ItemStack(material);
    }

    public ItemBuilder(ItemStack itemStack) {
        this.itemStack = itemStack.clone();
    }

    public static ItemBuilder fromConfig(ConfigurationSection section) {
        return fromConfig(section, null);
    }

    public static ItemBuilder fromConfig(ConfigurationSection section, Player player) {
        String materialName = section.getString("material", "STONE").toUpperCase();
        Material material;

        try {
            material = Material.valueOf(materialName);
        } catch (IllegalArgumentException e) {
            material = Material.STONE;
        }

        ItemBuilder builder = new ItemBuilder(material);

        if (section.contains("amount")) {
            builder.amount(section.getInt("amount", 1));
        }

        if (section.contains("display_name")) {
            String name = section.getString("display_name");
            builder.name(player != null ? PlaceholderUtil.parse(name, player) : name);
        }

        if (section.contains("lore")) {
            List<String> lore = section.getStringList("lore");
            builder.lore(player != null ?
                    lore.stream().map(line -> PlaceholderUtil.parse(line, player)).collect(Collectors.toList()) :
                    lore);
        }

        if (section.contains("glow") && section.getBoolean("glow")) {
            builder.glow();
        }

        if (section.contains("custom_model_data")) {
            builder.customModelData(section.getInt("custom_model_data"));
        }

        if (section.contains("item_flags")) {
            section.getStringList("item_flags").forEach(flagName -> {
                try {
                    builder.flag(ItemFlag.valueOf(flagName.toUpperCase()));
                } catch (IllegalArgumentException ignored) {
                }
            });
        }

        if (material == Material.PLAYER_HEAD && section.contains("skull_owner")) {
            String owner = section.getString("skull_owner");
            builder.skullOwner(player != null ? owner.replace("%player%", player.getName()) : owner);
        }

        return builder;
    }

    public ItemBuilder amount(int amount) {
        itemStack.setAmount(Math.max(1, amount));
        return this;
    }

    public ItemBuilder name(String name) {
        ItemMeta meta = itemStack.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(TextUtil.colorize(name));
            itemStack.setItemMeta(meta);
        }
        return this;
    }

    public ItemBuilder lore(List<String> lore) {
        ItemMeta meta = itemStack.getItemMeta();
        if (meta != null) {
            List<String> coloredLore = lore.stream()
                    .map(TextUtil::colorize)
                    .collect(Collectors.toList());
            meta.setLore(coloredLore);
            itemStack.setItemMeta(meta);
        }
        return this;
    }

    public ItemBuilder lore(String... lines) {
        return lore(List.of(lines));
    }

    public ItemBuilder flag(ItemFlag... flags) {
        ItemMeta meta = itemStack.getItemMeta();
        if (meta != null) {
            meta.addItemFlags(flags);
            itemStack.setItemMeta(meta);
        }
        return this;
    }

    public ItemBuilder glow() {
        ItemMeta meta = itemStack.getItemMeta();
        if (meta != null) {
            meta.addEnchant(Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            itemStack.setItemMeta(meta);
        }
        return this;
    }

    public ItemBuilder customModelData(int data) {
        ItemMeta meta = itemStack.getItemMeta();
        if (meta != null) {
            meta.setCustomModelData(data);
            itemStack.setItemMeta(meta);
        }
        return this;
    }

    public ItemBuilder skullOwner(String owner) {
        if (itemStack.getType() == Material.PLAYER_HEAD) {
            SkullMeta meta = (SkullMeta) itemStack.getItemMeta();
            if (meta != null) {
                meta.setOwner(owner);
                itemStack.setItemMeta(meta);
            }
        }
        return this;
    }

    public ItemStack build() {
        return itemStack;
    }
}
