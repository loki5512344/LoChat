package ru.lovar.gradientnick.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.lovar.gradientnick.GradientNick;
import ru.lovar.gradientnick.config.ConfigManager;
import ru.lovar.gradientnick.config.MessagesManager;
import ru.lovar.gradientnick.data.PlayerData;
import ru.lovar.gradientnick.gui.ConfirmGUI;
import ru.lovar.gradientnick.util.Constants;
import ru.lovar.gradientnick.util.DisplayNameUtil;
import ru.lovar.gradientnick.util.FoliaUtil;

import java.util.*;
import java.util.function.BiFunction;
import java.util.regex.Pattern;

public class PrefixCommand implements CommandExecutor, TabCompleter {

    private final GradientNick plugin;
    private final Map<String, BiFunction<Player, String[], Boolean>> subCommands = new HashMap<>();
    
    private static final Pattern COLOR_CODE_PATTERN = Pattern.compile("(?i)(&[0-9a-fk-or]|§[0-9a-fk-or]|#[0-9a-f]{6}|&#[0-9a-f]{6}|<#[0-9a-f]{6}>)");
    private static final Pattern ALLOWED_PREFIX_PATTERN = Pattern.compile("^[a-zA-Zа-яА-ЯёЁ0-9 _\\-]+$");
    private static final Set<String> SUB_COMMANDS = Set.of("on", "off", "reset");

    public PrefixCommand(GradientNick plugin) {
        this.plugin = plugin;
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
            sender.sendMessage(plugin.getMessagesManager().get("only-player"));
            return true;
        }

        if (!player.hasPermission("gradient.prefix")) {
            player.sendMessage(plugin.getMessagesManager().get("no-permission"));
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(plugin.getMessagesManager().get("prefix-help"));
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
        PlayerData data = plugin.getDataManager().getPlayerData(player.getUniqueId());
        MessagesManager msg = plugin.getMessagesManager();
        
        if (!data.hasPrefix()) {
            player.sendMessage(msg.get("prefix-no-prefix"));
            return true;
        }
        
        data.setPrefixEnabled(true);
        plugin.getLuckPermsHook().setPrefix(player, DisplayNameUtil.buildColoredPrefix(plugin, data));
        saveAndUpdate(player, data);
        player.sendMessage(msg.get("prefix-enabled"));
        return true;
    }

    private boolean handleOff(Player player, String[] args) {
        PlayerData data = plugin.getDataManager().getPlayerData(player.getUniqueId());
        data.setPrefixEnabled(false);
        plugin.getLuckPermsHook().removePrefix(player);
        saveAndUpdate(player, data);
        player.sendMessage(plugin.getMessagesManager().get("prefix-disabled"));
        return true;
    }

    private boolean handleReset(Player player, String[] args) {
        PlayerData data = plugin.getDataManager().getPlayerData(player.getUniqueId());
        MessagesManager msg = plugin.getMessagesManager();
        
        if (!data.hasPrefix()) {
            player.sendMessage(msg.get("prefix-no-prefix"));
            return true;
        }
        
        data.setPrefix(null);
        data.setPrefixEnabled(false);
        plugin.getLuckPermsHook().removePrefix(player);
        saveAndUpdate(player, data);
        player.sendMessage(msg.get("prefix-reset-success"));
        return true;
    }

    private boolean handleSetPrefix(Player player, String[] args) {
        MessagesManager msg = plugin.getMessagesManager();
        ConfigManager cfg = plugin.getConfigManager();
        String prefix = String.join(" ", args);

        if (prefix.trim().isEmpty()) {
            player.sendMessage(msg.get("prefix-help"));
            return true;
        }

        if (COLOR_CODE_PATTERN.matcher(prefix).find()) {
            player.sendMessage(msg.get("prefix-no-colors"));
            return true;
        }

        if (!ALLOWED_PREFIX_PATTERN.matcher(prefix).matches()) {
            player.sendMessage(msg.get("prefix-invalid-chars"));
            return true;
        }

        if (cfg.isPrefixBlacklisted(prefix)) {
            player.sendMessage(msg.get("prefix-blacklisted"));
            return true;
        }

        if (prefix.length() > cfg.getMaxPrefixLength()) {
            player.sendMessage(msg.get("prefix-too-long", "max", String.valueOf(cfg.getMaxPrefixLength())));
            return true;
        }

        PlayerData data = plugin.getDataManager().getPlayerData(player.getUniqueId());

        if (!player.hasPermission("gradient.bypass.cooldown") && !checkCooldown(player, cfg.getPrefixCooldown(), data.getLastPrefixChange())) {
            return true;
        }

        int price = calculatePrice(player, cfg, data);

        if (price > 0 && !checkBalance(player, price, "not-enough-points-prefix")) {
            return true;
        }

        ConfirmGUI gui = new ConfirmGUI(plugin, player, ConfirmGUI.ConfirmType.PREFIX, data.getColors(), prefix, price);
        FoliaUtil.runEntityTask(plugin, player, gui::open);
        return true;
    }

    private int calculatePrice(Player player, ConfigManager cfg, PlayerData data) {
        if (player.hasPermission("gradient.bypass.cost")) return 0;
        return (cfg.isPrefixOneTimePurchase() && data.isPrefixPurchased()) ? 0 : cfg.getPrefixPrice();
    }

    private boolean checkCooldown(Player player, int cooldownSec, long lastChange) {
        long cooldownMs = cooldownSec * Constants.MILLIS_PER_SECOND;
        long timePassed = System.currentTimeMillis() - lastChange;
        if (timePassed < cooldownMs) {
            long remaining = (cooldownMs - timePassed) / Constants.MILLIS_PER_SECOND;
            player.sendMessage(plugin.getMessagesManager().get("cooldown", "time", String.valueOf(remaining)));
            return false;
        }
        return true;
    }

    private boolean checkBalance(Player player, int price, String msgKey) {
        int balance = plugin.getPlayerPointsAPI().look(player.getUniqueId());
        if (balance < price) {
            player.sendMessage(plugin.getMessagesManager().get(msgKey, "price", String.valueOf(price)));
            return false;
        }
        return true;
    }

    private void saveAndUpdate(Player player, PlayerData data) {
        FoliaUtil.runEntityTask(plugin, player, () -> DisplayNameUtil.updateDisplayName(plugin, player, data));
        FoliaUtil.runAsync(plugin, () -> plugin.getDataManager().savePlayerData(player.getUniqueId()));
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
