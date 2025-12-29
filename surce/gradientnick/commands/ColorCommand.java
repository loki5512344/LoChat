package ru.lovar.gradientnick.commands;

import org.bukkit.Bukkit;
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
import ru.lovar.gradientnick.util.GradientUtil;

import java.util.*;
import java.util.function.BiFunction;

public class ColorCommand implements CommandExecutor, TabCompleter {

    private final GradientNick plugin;
    private final Map<String, BiFunction<Player, String[], Boolean>> subCommands = new HashMap<>();
    
    private static final List<String> PRESET_COLORS = List.of(
            "#FF0000", "#FF7F00", "#FFFF00", "#00FF00", "#0000FF", "#4B0082", "#9400D3",
            "#FF69B4", "#00FFFF", "#FFD700", "#FFFFFF", "#000000"
    );
    private static final Set<String> SUB_COMMANDS = Set.of("on", "off", "reset", "copy");

    public ColorCommand(GradientNick plugin) {
        this.plugin = plugin;
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
            sender.sendMessage(plugin.getMessagesManager().get("only-player"));
            return true;
        }

        if (!player.hasPermission("gradient.color")) {
            player.sendMessage(plugin.getMessagesManager().get("no-permission"));
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(plugin.getMessagesManager().get("color-help"));
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
        PlayerData data = plugin.getDataManager().getPlayerData(player.getUniqueId());
        MessagesManager msg = plugin.getMessagesManager();
        
        if (!data.hasColors()) {
            player.sendMessage(msg.get("color-no-colors"));
            return true;
        }
        
        data.setColorEnabled(true);
        saveAndUpdate(player, data);
        player.sendMessage(msg.get("color-enabled"));
        return true;
    }

    private boolean handleOff(Player player, String[] args) {
        PlayerData data = plugin.getDataManager().getPlayerData(player.getUniqueId());
        data.setColorEnabled(false);
        saveAndUpdate(player, data);
        player.sendMessage(plugin.getMessagesManager().get("color-disabled"));
        return true;
    }

    private boolean handleReset(Player player, String[] args) {
        PlayerData data = plugin.getDataManager().getPlayerData(player.getUniqueId());
        MessagesManager msg = plugin.getMessagesManager();
        
        if (!data.hasColors()) {
            player.sendMessage(msg.get("color-no-colors"));
            return true;
        }
        
        data.setColors(new ArrayList<>());
        data.setColorEnabled(false);
        saveAndUpdate(player, data);
        player.sendMessage(msg.get("color-reset"));
        return true;
    }

    private boolean handleCopy(Player player, String[] args) {
        MessagesManager msg = plugin.getMessagesManager();
        ConfigManager cfg = plugin.getConfigManager();
        
        if (args.length < 2) {
            player.sendMessage(msg.get("color-copy-usage"));
            return true;
        }
        
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            player.sendMessage(msg.get("player-not-found"));
            return true;
        }
        
        PlayerData targetData = plugin.getDataManager().getPlayerData(target.getUniqueId());
        if (!targetData.hasColors()) {
            player.sendMessage(msg.get("color-copy-no-colors", "player", target.getName()));
            return true;
        }

        PlayerData data = plugin.getDataManager().getPlayerData(player.getUniqueId());
        
        if (!player.hasPermission("gradient.bypass.cooldown") && !checkCooldown(player, cfg.getColorCooldown(), data.getLastColorChange())) {
            return true;
        }

        List<String> colors = new ArrayList<>(targetData.getColors());
        int price = player.hasPermission("gradient.bypass.cost") ? 0 : colors.size() * cfg.getPricePerColor();

        if (price > 0 && !checkBalance(player, price)) {
            return true;
        }

        ConfirmGUI gui = new ConfirmGUI(plugin, player, ConfirmGUI.ConfirmType.COLOR, colors, data.getPrefix(), price);
        FoliaUtil.runEntityTask(plugin, player, gui::open);
        return true;
    }

    private boolean handleSetColors(Player player, String[] args) {
        MessagesManager msg = plugin.getMessagesManager();
        ConfigManager cfg = plugin.getConfigManager();

        if (args.length > cfg.getMaxColors()) {
            player.sendMessage(msg.get("too-many-colors", "max", String.valueOf(cfg.getMaxColors())));
            return true;
        }

        if (args.length < cfg.getMinColors()) {
            player.sendMessage(msg.get("not-enough-colors", "min", String.valueOf(cfg.getMinColors())));
            return true;
        }

        PlayerData data = plugin.getDataManager().getPlayerData(player.getUniqueId());

        if (!player.hasPermission("gradient.bypass.cooldown") && !checkCooldown(player, cfg.getColorCooldown(), data.getLastColorChange())) {
            return true;
        }

        List<String> colors = new ArrayList<>();
        for (String arg : args) {
            String color = arg.startsWith("#") ? arg : "#" + arg;
            if (!GradientUtil.isValidHex(color)) {
                player.sendMessage(msg.get("invalid-color", "color", arg));
                return true;
            }
            colors.add(color.toUpperCase());
        }

        int price = player.hasPermission("gradient.bypass.cost") ? 0 : colors.size() * cfg.getPricePerColor();

        if (price > 0 && !checkBalance(player, price)) {
            return true;
        }

        ConfirmGUI gui = new ConfirmGUI(plugin, player, ConfirmGUI.ConfirmType.COLOR, colors, data.getPrefix(), price);
        FoliaUtil.runEntityTask(plugin, player, gui::open);
        return true;
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

    private boolean checkBalance(Player player, int price) {
        int balance = plugin.getPlayerPointsAPI().look(player.getUniqueId());
        if (balance < price) {
            player.sendMessage(plugin.getMessagesManager().get("not-enough-points-color", "price", String.valueOf(price)));
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
        
        // Для цветов — показываем пресеты
        if (!SUB_COMMANDS.contains(firstArg) && args.length <= plugin.getConfigManager().getMaxColors()) {
            return PRESET_COLORS.stream()
                    .filter(s -> s.toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
                    .toList();
        }
        
        return List.of();
    }
}
