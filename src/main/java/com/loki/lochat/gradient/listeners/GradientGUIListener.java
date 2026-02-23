package com.loki.lochat.gradient.listeners;

import com.loki.lochat.gradient.GradientModule;
import com.loki.lochat.gradient.config.GradientMessages;
import com.loki.lochat.gradient.data.GradientPlayerData;
import com.loki.lochat.gradient.gui.GradientConfirmGUI;
import com.loki.lochat.gradient.util.DisplayNameUtil;
import com.loki.lochat.gradient.util.FoliaUtil;
import com.loki.lochat.gradient.util.GradientConstants;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.Map;

/**
 * Слушатель GUI градиентного модуля
 */
public class GradientGUIListener implements Listener {

    private final GradientModule module;
    
    private static final Map<GradientConfirmGUI.ConfirmType, String> PRICE_MSG_KEYS = Map.of(
            GradientConfirmGUI.ConfirmType.COLOR, "not-enough-points-color",
            GradientConfirmGUI.ConfirmType.PREFIX, "not-enough-points-prefix"
    );

    public GradientGUIListener(GradientModule module) {
        this.module = module;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof GradientConfirmGUI gui)) return;
        
        event.setCancelled(true);
        
        if (!(event.getWhoClicked() instanceof Player player) || !player.equals(gui.getPlayer())) return;

        switch (event.getRawSlot()) {
            case GradientConstants.GUI_CONFIRM_SLOT -> handleConfirm(player, gui);
            case GradientConstants.GUI_CANCEL_SLOT -> handleCancel(player);
        }
    }

    private void handleConfirm(Player player, GradientConfirmGUI gui) {
        player.closeInventory();
        
        GradientMessages msg = module.getMessages();
        int price = gui.getPrice();
        
        if (price > 0 && !player.hasPermission("gradient.bypass.cost")) {
            if (!module.hasPlayerPoints()) {
                player.sendMessage("§cPlayerPoints не установлен!");
                return;
            }
            int balance = module.getPlayerPointsAPI().look(player.getUniqueId());
            if (balance < price) {
                msg.send(player, PRICE_MSG_KEYS.get(gui.getType()), "price", String.valueOf(price));
                return;
            }
            module.getPlayerPointsAPI().take(player.getUniqueId(), price);
        }

        GradientPlayerData data = module.getDataManager().getPlayerData(player.getUniqueId());

        switch (gui.getType()) {
            case COLOR -> {
                data.setColors(gui.getColors());
                data.setColorEnabled(true);
                data.setLastColorChange(System.currentTimeMillis());
                msg.send(player, "color-success", "price", String.valueOf(price));
            }
            case PREFIX -> {
                data.setPrefix(gui.getPrefix());
                data.setPrefixEnabled(true);
                data.setPrefixPurchased(true);
                data.setLastPrefixChange(System.currentTimeMillis());
                // Сохраняем существующие цвета если они есть
                if (gui.getColors() != null && !gui.getColors().isEmpty()) {
                    data.setColors(gui.getColors());
                    data.setColorEnabled(true);
                }
                if (module.getLuckPermsHook() != null) {
                    module.getLuckPermsHook().setPrefix(player, DisplayNameUtil.buildColoredPrefix(module, data));
                }
                msg.send(player, price > 0 ? "prefix-success" : "prefix-success-free", 
                        "price", String.valueOf(price));
            }
        }

        FoliaUtil.runEntityTask(module.getPlugin(), player, 
                () -> DisplayNameUtil.updateDisplayName(module, player, data));
        FoliaUtil.runAsync(module.getPlugin(), 
                () -> module.getDataManager().savePlayerData(player.getUniqueId()));
    }

    private void handleCancel(Player player) {
        player.closeInventory();
        player.sendMessage("§7Действие отменено.");
    }
}
