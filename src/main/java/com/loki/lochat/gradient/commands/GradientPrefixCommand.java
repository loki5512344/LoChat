package com.loki.lochat.gradient.commands;

import com.loki.lochat.gradient.GradientModule;
import com.loki.lochat.gradient.config.GradientConfig;
import com.loki.lochat.gradient.config.GradientMessages;
import com.loki.lochat.gradient.data.GradientPlayerData;
import com.loki.lochat.gradient.gui.GradientConfirmGUI;
import com.loki.lochat.gradient.util.DisplayNameUtil;
import com.loki.lochat.gradient.util.FoliaUtil;
import com.loki.lochat.gradient.util.GradientConstants;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.regex.Pattern;

/**
 * Команда /prefix для установки кастомного префикса
 */
public class GradientPrefixCommand implements CommandExecutor, TabCompleter {

    private final GradientModule module;
    private final Map<String, BiFunction<Player, String[], Boolean>> subCommands = new HashMap<>();
    
    private static final Pattern COLOR_CODE_PATTERN = Pattern.compile(
            "(?i)(&[0-9a-fk-or]|§[0-9a-fk-or]|#[0-9a-f]{6}|&#[0-9a-f]{6}|<#[0-9a-f]{6}>)");
    private static final Pattern ALLOWED_PREFIX_PATTERN = Pattern.compile("^[a-zA-Zа-яА-ЯёЁ0-9 _\\-]+$");
    private static final Set<String> SUB_COMMANDS = Set.of("on", "off", "reset");

    public GradientPrefixCommand(GradientModule module) {
        this.module = module;
        registerSubCommands();
    }

    private void registerSubCommands() {
        subCommands.put("on", this::handleOn);
        subCommands.put("off", this::handleOff);
        subCommands.put("reset", this::handleReset);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            module.getMessages().send(sender, "only-player");
            return true;
        }

        if (!player.hasPermission("gradient.prefix")) {
            module.getMessages().send(player, "no-permission");
            return true;
        }

        if (args.length == 0) {
            module.getMessages().send(player, "prefix-help");
            return true;
        }

        String subCommand = args[0].toLowerCase();
        var handler = subCommands.get(subCommand);
        if (handler != null) {
            return handler.apply(player, args);
        }

        return handleSetPrefix(player, args);
    }

    private boolean handleOn(Player player, String[] args) {
        GradientPlayerData data = module.getDataManager().getPlayerData(player.getUniqueId());
        GradientMessages msg = module.getMessages();
        
        if (!data.hasPrefix()) {
            msg.send(player, "prefix-no-prefix");
            return true;
        }
        
        data.setPrefixEnabled(true);
        module.getLuckPermsHook().setPrefix(player, DisplayNameUtil.buildColoredPrefix(module, data));
        saveAndUpdate(player, data);
        msg.send(player, "prefix-enabled");
        return true;
    }

    private boolean handleOff(Player player, String[] args) {
        GradientPlayerData data = module.getDataManager().getPlayerData(player.getUniqueId());
        data.setPrefixEnabled(false);
        module.getLuckPermsHook().removePrefix(player);
        saveAndUpdate(player, data);
        module.getMessages().send(player, "prefix-disabled");
        return true;
    }

    private boolean handleReset(Player player, String[] args) {
        GradientPlayerData data = module.getDataManager().getPlayerData(player.getUniqueId());
        GradientMessages msg = module.getMessages();
        
        if (!data.hasPrefix()) {
            msg.send(player, "prefix-no-prefix");
            return true;
        }
        
        data.setPrefix(null);
        data.setPrefixEnabled(false);
        module.getLuckPermsHook().removePrefix(player);
        saveAndUpdate(player, data);
        msg.send(player, "prefix-reset-success");
        return true;
    }

    private boolean handleSetPrefix(Player player, String[] args) {
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
        long cooldownMs = cooldownSec * GradientConstants.MILLIS_PER_SECOND;
        long timePassed = System.currentTimeMillis() - lastChange;
        if (timePassed < cooldownMs) {
            long remaining = (cooldownMs - timePassed) / GradientConstants.MILLIS_PER_SECOND;
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

    private void saveAndUpdate(Player player, GradientPlayerData data) {
        FoliaUtil.runEntityTask(module.getPlugin(), player, 
                () -> DisplayNameUtil.updateDisplayName(module, player, data));
        FoliaUtil.runAsync(module.getPlugin(), 
                () -> module.getDataManager().savePlayerData(player.getUniqueId()));
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                                 @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            return SUB_COMMANDS.stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .toList();
        }
        return List.of();
    }
}
