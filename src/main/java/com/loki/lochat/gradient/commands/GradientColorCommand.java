package com.loki.lochat.gradient.commands;

import com.loki.lochat.gradient.GradientModule;
import com.loki.lochat.gradient.config.GradientConfig;
import com.loki.lochat.gradient.config.GradientMessages;
import com.loki.lochat.gradient.data.GradientPlayerData;
import com.loki.lochat.gradient.gui.GradientConfirmGUI;
import com.loki.lochat.gradient.util.DisplayNameUtil;
import com.loki.lochat.util.FoliaUtil;
import com.loki.lochat.gradient.util.GradientConstants;
import com.loki.lochat.gradient.util.GradientUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BiFunction;

/**
 * Команда /color для установки градиента ника
 */
public class GradientColorCommand implements CommandExecutor, TabCompleter {

    private static final List<String> PRESET_COLORS = List.of(
            "#FF0000", "#FF7F00", "#FFFF00", "#00FF00", "#0000FF", "#4B0082", "#9400D3",
            "#FF69B4", "#00FFFF", "#FFD700", "#FFFFFF", "#000000"
    );
    private static final Set<String> SUB_COMMANDS = Set.of("on", "off", "reset", "copy");
    private final GradientModule module;
    private final Map<String, BiFunction<Player, String[], Boolean>> subCommands = new HashMap<>();

    public GradientColorCommand(GradientModule module) {
        this.module = module;
        registerSubCommands();
    }

    private void registerSubCommands() {
        subCommands.put("on", this::handleOn);
        subCommands.put("off", this::handleOff);
        subCommands.put("reset", this::handleReset);
        subCommands.put("copy", this::handleCopy);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            module.getMessages().send(sender, "only-player");
            return true;
        }

        if (!player.hasPermission("gradient.color")) {
            module.getMessages().send(player, "no-permission");
            return true;
        }

        if (args.length == 0) {
            module.getMessages().send(player, "color-help");
            return true;
        }

        String subCommand = args[0].toLowerCase();
        var handler = subCommands.get(subCommand);
        if (handler != null) {
            return handler.apply(player, args);
        }

        return handleSetColors(player, args);
    }

    private boolean handleOn(Player player, String[] args) {
        GradientPlayerData data = module.getDataManager().getPlayerData(player.getUniqueId());
        GradientMessages msg = module.getMessages();

        if (!data.hasColors()) {
            msg.send(player, "color-no-colors");
            return true;
        }

        data.setColorEnabled(true);
        saveAndUpdate(player, data);
        msg.send(player, "color-enabled");
        return true;
    }

    private boolean handleOff(Player player, String[] args) {
        GradientPlayerData data = module.getDataManager().getPlayerData(player.getUniqueId());
        data.setColorEnabled(false);
        saveAndUpdate(player, data);
        module.getMessages().send(player, "color-disabled");
        return true;
    }

    private boolean handleReset(Player player, String[] args) {
        GradientPlayerData data = module.getDataManager().getPlayerData(player.getUniqueId());
        GradientMessages msg = module.getMessages();

        if (!data.hasColors()) {
            msg.send(player, "color-no-colors");
            return true;
        }

        data.setColors(new ArrayList<>());
        data.setColorEnabled(false);
        saveAndUpdate(player, data);
        msg.send(player, "color-reset");
        return true;
    }

    private boolean handleCopy(Player player, String[] args) {
        GradientMessages msg = module.getMessages();
        GradientConfig cfg = module.getConfig();

        if (args.length < 2) {
            msg.send(player, "color-copy-usage");
            return true;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            msg.send(player, "player-not-found");
            return true;
        }

        GradientPlayerData targetData = module.getDataManager().getPlayerData(target.getUniqueId());
        if (!targetData.hasColors()) {
            msg.send(player, "color-copy-no-colors", "player", target.getName());
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

    private boolean handleSetColors(Player player, String[] args) {
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
            module.getMessages().send(player, "not-enough-points-color", "price", String.valueOf(price));
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
            String input = args[0].toLowerCase();
            List<String> completions = new ArrayList<>(SUB_COMMANDS);
            completions.addAll(PRESET_COLORS);
            return completions.stream().filter(s -> s.toLowerCase().startsWith(input)).toList();
        }

        String firstArg = args[0].toLowerCase();

        if (args.length == 2 && firstArg.equals("copy")) {
            return Bukkit.getOnlinePlayers().stream().map(Player::getName)
                    .filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase())).toList();
        }

        if (!SUB_COMMANDS.contains(firstArg) && args.length <= module.getConfig().getMaxColors()) {
            return PRESET_COLORS.stream()
                    .filter(s -> s.toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
                    .toList();
        }

        return List.of();
    }
}
