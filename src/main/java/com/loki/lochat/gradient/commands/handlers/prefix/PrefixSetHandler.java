package com.loki.lochat.gradient.commands.handlers.prefix;

import com.loki.lochat.config.RatConfig;
import com.loki.lochat.gradient.GradientModule;
import com.loki.lochat.gradient.config.GradientConfig;
import com.loki.lochat.gradient.config.GradientMessages;
import com.loki.lochat.gradient.data.GradientPlayerData;
import com.loki.lochat.gradient.gui.GradientConfirmGUI;
import com.loki.lochat.utils.platform.FoliaUtil;
import org.bukkit.entity.Player;

import java.util.regex.Pattern;

/**
 * Обработчик команды /prefix <текст>
 * Устанавливает кастомный префикс
 */
public class PrefixSetHandler implements PrefixSubCommandHandler {

    private static final Pattern COLOR_CODE_PATTERN = Pattern.compile(
            "(?i)(&[0-9a-fk-or]|§[0-9a-fk-or]|#[0-9a-f]{6}|&#[0-9a-f]{6}|<#[0-9a-f]{6}>)");
    private static final Pattern ALLOWED_PREFIX_PATTERN = Pattern.compile("^[a-zA-Zа-яА-ЯёЁ0-9 _\\-]+$");

    private final GradientModule module;

    public PrefixSetHandler(GradientModule module) {
        this.module = module;
    }

    @Override
    public boolean handle(Player player, String[] args) {
        GradientMessages msg = module.getMessages();
        GradientConfig cfg = module.getConfig();
        String prefix = String.join(" ", args);

        if (prefix.trim().isEmpty()) {
            msg.send(player, "prefix-help");
            return true;
        }

        if (COLOR_CODE_PATTERN.matcher(prefix).find()) {
            msg.send(player, "prefix-no-colors");
            return true;
        }

        if (!ALLOWED_PREFIX_PATTERN.matcher(prefix).matches()) {
            msg.send(player, "prefix-invalid-chars");
            return true;
        }

        if (cfg.isPrefixBlacklisted(prefix)) {
            msg.send(player, "prefix-blacklisted");
            return true;
        }

        if (prefix.length() > cfg.getMaxPrefixLength()) {
            msg.send(player, "prefix-too-long", "max", String.valueOf(cfg.getMaxPrefixLength()));
            return true;
        }

        GradientPlayerData data = module.getDataManager().getPlayerData(player.getUniqueId());

        if (!player.hasPermission("gradient.bypass.cooldown") &&
                !checkCooldown(player, cfg.getPrefixCooldown(), data.getLastPrefixChange())) {
            return true;
        }

        int price = calculatePrice(player, cfg, data);

        if (price > 0 && !checkBalance(player, price)) {
            return true;
        }

        GradientConfirmGUI gui = new GradientConfirmGUI(module, player,
                GradientConfirmGUI.ConfirmType.PREFIX,
                data.hasColors() && data.isColorEnabled() ? data.getColors() : null,
                prefix, price);
        FoliaUtil.runEntityTask(module.getPlugin(), player, gui::open);
        return true;
    }

    private int calculatePrice(Player player, GradientConfig cfg, GradientPlayerData data) {
        if (player.hasPermission("gradient.bypass.cost")) return 0;
        return (cfg.isPrefixOneTimePurchase() && data.isPrefixPurchased()) ? 0 : cfg.getPrefixPrice();
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
            module.getMessages().send(player, "not-enough-points-prefix", "price", String.valueOf(price));
            return false;
        }
        return true;
    }
}
