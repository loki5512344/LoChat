package ru.lovar.gradientnick.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import ru.lovar.gradientnick.GradientNick;
import ru.lovar.gradientnick.config.MessagesManager;
import ru.lovar.gradientnick.data.PlayerData;
import ru.lovar.gradientnick.gui.ConfirmGUI;
import ru.lovar.gradientnick.util.Constants;
import ru.lovar.gradientnick.util.DisplayNameUtil;
import ru.lovar.gradientnick.util.FoliaUtil;

import java.util.Map;

public class GUIListener implements Listener {

    private final GradientNick plugin;
    
    private static final Map<ConfirmGUI.ConfirmType, String> PRICE_MSG_KEYS = Map.of(
            ConfirmGUI.ConfirmType.COLOR, "not-enough-points-color",
            ConfirmGUI.ConfirmType.PREFIX, "not-enough-points-prefix"
    );

    public GUIListener(GradientNick plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof ConfirmGUI gui)) return;
        
        event.setCancelled(true);
        
        if (!(event.getWhoClicked() instanceof Player player) || !player.equals(gui.getPlayer())) return;

        switch (event.getRawSlot()) {
            case Constants.GUI_CONFIRM_SLOT -> handleConfirm(player, gui);
            case Constants.GUI_CANCEL_SLOT -> handleCancel(player);
        }
    }

    private void handleConfirm(Player player, ConfirmGUI gui) {
        player.closeInventory();
        
        MessagesManager msg = plugin.getMessagesManager();
        int price = gui.getPrice();
        
        if (price > 0 && !player.hasPermission("gradient.bypass.cost")) {
            int balance = plugin.getPlayerPointsAPI().look(player.getUniqueId());
            if (balance < price) {
                player.sendMessage(msg.get(PRICE_MSG_KEYS.get(gui.getType()), "price", String.valueOf(price)));
                return;
            }
            plugin.getPlayerPointsAPI().take(player.getUniqueId(), price);
        }

        PlayerData data = plugin.getDataManager().getPlayerData(player.getUniqueId());

        switch (gui.getType()) {
            case COLOR -> {
                data.setColors(gui.getColors());
                data.setColorEnabled(true);
                data.setLastColorChange(System.currentTimeMillis());
                player.sendMessage(msg.get("color-success", "price", String.valueOf(price)));
            }
            case PREFIX -> {
                data.setPrefix(gui.getPrefix());
                data.setPrefixEnabled(true);
                data.setPrefixPurchased(true);
                data.setLastPrefixChange(System.currentTimeMillis());
                plugin.getLuckPermsHook().setPrefix(player, DisplayNameUtil.buildColoredPrefix(plugin, data));
                player.sendMessage(msg.get(price > 0 ? "prefix-success" : "prefix-success-free", "price", String.valueOf(price)));
            }
        }

        FoliaUtil.runEntityTask(plugin, player, () -> DisplayNameUtil.updateDisplayName(plugin, player, data));
        FoliaUtil.runAsync(plugin, () -> plugin.getDataManager().savePlayerData(player.getUniqueId()));
    }

    private void handleCancel(Player player) {
        player.closeInventory();
        player.sendMessage("§7Действие отменено.");
    }
}
