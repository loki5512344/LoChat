package com.loki.lochat.gradient.gui;

import com.loki.lochat.gradient.GradientModule;
import com.loki.lochat.gradient.util.GradientConstants;
import com.loki.lochat.gradient.util.GradientUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * GUI подтверждения для градиентного модуля
 */
public class GradientConfirmGUI implements InventoryHolder {

    public enum ConfirmType {
        COLOR, PREFIX
    }

    private static final LegacyComponentSerializer SERIALIZER = LegacyComponentSerializer.builder()
            .character('&')
            .hexCharacter('#')
            .hexColors()
            .build();

    private final GradientModule module;
    private final Player player;
    private final Inventory inventory;
    private final ConfirmType type;
    private final List<String> colors;
    private final String prefix;
    private final int price;

    public GradientConfirmGUI(GradientModule module, Player player, ConfirmType type, 
                              List<String> colors, String prefix, int price) {
        this.module = module;
        this.player = player;
        this.type = type;
        this.colors = colors;
        this.prefix = prefix;
        this.price = price;
        this.inventory = Bukkit.createInventory(this, 27, 
                Component.text("Подтверждение", NamedTextColor.DARK_GRAY));
        setupItems();
    }

    private void setupItems() {
        ItemStack glass = createItem(Material.GRAY_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < 27; i++) {
            inventory.setItem(i, glass);
        }

        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta skullMeta = (SkullMeta) head.getItemMeta();
        skullMeta.setOwningPlayer(player);
        
        String preview = buildPreview();
        Component previewComponent = SERIALIZER.deserialize(preview);
        skullMeta.displayName(previewComponent.decoration(TextDecoration.ITALIC, false));
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.empty());
        lore.add(Component.text("Так будет выглядеть ваш ник", NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        if (price > 0) {
            lore.add(Component.text("Стоимость: ", NamedTextColor.GRAY)
                    .append(Component.text(price + " PP", NamedTextColor.GOLD))
                    .decoration(TextDecoration.ITALIC, false));
        }
        skullMeta.lore(lore);
        head.setItemMeta(skullMeta);
        inventory.setItem(GradientConstants.GUI_PREVIEW_SLOT, head);

        ItemStack confirm = createItem(Material.LIME_WOOL, "§a§lПодтвердить");
        ItemMeta confirmMeta = confirm.getItemMeta();
        List<Component> confirmLore = new ArrayList<>();
        confirmLore.add(Component.empty());
        confirmLore.add(Component.text("Нажмите для подтверждения", NamedTextColor.GREEN)
                .decoration(TextDecoration.ITALIC, false));
        confirmMeta.lore(confirmLore);
        confirm.setItemMeta(confirmMeta);
        inventory.setItem(GradientConstants.GUI_CONFIRM_SLOT, confirm);

        ItemStack cancel = createItem(Material.RED_WOOL, "§c§lОтменить");
        ItemMeta cancelMeta = cancel.getItemMeta();
        List<Component> cancelLore = new ArrayList<>();
        cancelLore.add(Component.empty());
        cancelLore.add(Component.text("Нажмите для отмены", NamedTextColor.RED)
                .decoration(TextDecoration.ITALIC, false));
        cancelMeta.lore(cancelLore);
        cancel.setItemMeta(cancelMeta);
        inventory.setItem(GradientConstants.GUI_CANCEL_SLOT, cancel);
    }

    private String buildPreview() {
        String nick = player.getName();
        String prefixToUse = this.prefix;
        List<String> colorsToUse = this.colors;

        if (type == ConfirmType.PREFIX && (colorsToUse == null || colorsToUse.isEmpty())) {
            var data = module.getDataManager().getPlayerData(player.getUniqueId());
            if (data.hasColors() && data.isColorEnabled()) {
                colorsToUse = data.getColors();
            }
        }

        if (type == ConfirmType.COLOR && (prefixToUse == null || prefixToUse.isEmpty())) {
            var data = module.getDataManager().getPlayerData(player.getUniqueId());
            if (data.hasPrefix() && data.isPrefixEnabled()) {
                prefixToUse = data.getPrefix();
            }
        }

        return GradientUtil.buildDisplayName(
                prefixToUse,
                nick,
                colorsToUse,
                module.getConfig().isGradientOnPrefix(),
                module.getConfig().isContinuousGradient(),
                module.getConfig().getPrefixFormat(),
                module.getConfig().isUseLegacyRgbFormat()
        );
    }

    private ItemStack createItem(Material material, String name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(LegacyComponentSerializer.legacySection().deserialize(name)
                .decoration(TextDecoration.ITALIC, false));
        item.setItemMeta(meta);
        return item;
    }

    public void open() {
        player.openInventory(inventory);
    }

    @Override
    public @NotNull Inventory getInventory() { return inventory; }
    public Player getPlayer() { return player; }
    public ConfirmType getType() { return type; }
    public List<String> getColors() { return colors; }
    public String getPrefix() { return prefix; }
    public int getPrice() { return price; }
}
