package com.loki.lochat.gradient.commands.handlers.color;

import com.loki.lochat.config.RatConfig;
import com.loki.lochat.gradient.GradientModule;
import com.loki.lochat.gradient.config.GradientConfig;
import com.loki.lochat.gradient.data.GradientPlayerData;
import com.loki.lochat.gradient.gui.GradientConfirmGUI;
import com.loki.lochat.utils.platform.FoliaUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Обработчик команды /color copy <игрок>
 */
public class ColorCopyHandler implements SubCommandHandler {

    private final GradientModule module;

    public ColorCopyHandler(GradientModule module) {
        this.module = module;
    }

    @Override
    public boolean handle(Player player, String[] args) {
        GradientConfig cfg = module.getConfig();

        if (args.length < 2) {
            module.getMessages().send(player, "color-copy-usage");
            return true;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            module.getMessages().send(player, "player-not-found");
            return true;
        }

        GradientPlayerData targetData = module.getDataManager().getPlayerData(target.getUniqueId());
        if (!targetData.hasColors()) {
            module.getMessages().send(player, "color-copy-no-colors", "player", target.getName());
            return true;
        }

        GradientPlayerData data = module.getDataManager().getPlayerData(player.getUniqueId());

        if (!player.hasPermission("gradient.bypass.cooldown") &&
                !checkCooldown(player, cfg.getColorCooldown(), data.getLastColorChange())) {
            return true;
        }

        List<String> colors = new ArrayList<>(targetData.getColors());
        int price = player.hasPermission("gradient.bypass.cost") ? 0 : colors.size() * cfg.getPricePerColor();

        if (price > 0 && !checkBalance(player, price)) {
            return true;
        }

        GradientConfirmGUI gui = new GradientConfirmGUI(module, player,
                GradientConfirmGUI.ConfirmType.COLOR, colors, data.getPrefix(), price);
        FoliaUtil.runEntityTask(module.getPlugin(), player, gui::open);
        return true;
    }

    private boolean checkCooldown(Player player, int cooldownSec, long lastChange) {
        long cooldownMs = cooldownSec * RatConfig.MILLIS_PER_SECOND;
        long timePassed = System.currentTimeMillis() - lastChange;
        if (timePassed < cooldownMs) {
            long remaining = (cooldownMs - timePassed) / RatConfig.MILLIS_PER_SECOND;
            module.getMessages().send(player, "cooldown", "time", String.valueOf(remaining));
            return false;
        }
        return true;
    }

    private boolean checkBalance(Player player, int price) {
        if (!module.hasPlayerPoints()) {
            player.sendMessage("§cPlayerPoints не установлен!");
            return false;
        }
        int balance = module.getPlayerPointsAPI().look(player.getUniqueId());
        if (balance < price) {
            module.getMessages().send(player, "not-enough-points-color", "price", String.valueOf(price));
            return false;
        }
        return true;
    }
}
