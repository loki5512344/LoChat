package com.loki.lochat.gradient.commands.handlers.color;

import com.loki.lochat.config.RatConfig;
import com.loki.lochat.gradient.GradientModule;
import com.loki.lochat.gradient.config.GradientConfig;
import com.loki.lochat.gradient.config.GradientMessages;
import com.loki.lochat.gradient.data.GradientPlayerData;
import com.loki.lochat.gradient.gui.GradientConfirmGUI;
import com.loki.lochat.gradient.util.GradientUtil;
import com.loki.lochat.utils.platform.FoliaUtil;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Обработчик команды /color <цвета...>
 * Устанавливает градиент из указанных цветов
 */
public class ColorSetHandler implements SubCommandHandler {

    private final GradientModule module;

    public ColorSetHandler(GradientModule module) {
        this.module = module;
    }

    @Override
    public boolean handle(Player player, String[] args) {
        GradientMessages msg = module.getMessages();
        GradientConfig cfg = module.getConfig();

        if (args.length > cfg.getMaxColors()) {
            msg.send(player, "too-many-colors", "max", String.valueOf(cfg.getMaxColors()));
            return true;
        }

        if (args.length < cfg.getMinColors()) {
            msg.send(player, "not-enough-colors", "min", String.valueOf(cfg.getMinColors()));
            return true;
        }

        GradientPlayerData data = module.getDataManager().getPlayerData(player.getUniqueId());

        if (!player.hasPermission("gradient.bypass.cooldown") &&
                !checkCooldown(player, cfg.getColorCooldown(), data.getLastColorChange())) {
            return true;
        }

        List<String> colors = new ArrayList<>();
        for (String arg : args) {
            String color = arg.startsWith("#") ? arg : "#" + arg;
            if (!GradientUtil.isValidHex(color)) {
                msg.send(player, "invalid-color", "color", arg);
                return true;
            }
            colors.add(color.toUpperCase());
        }

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
