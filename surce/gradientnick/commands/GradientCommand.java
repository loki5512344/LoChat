package ru.lovar.gradientnick.commands;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
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
import ru.lovar.gradientnick.util.DisplayNameUtil;
import ru.lovar.gradientnick.util.FoliaUtil;
import ru.lovar.gradientnick.util.GradientUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GradientCommand implements CommandExecutor, TabCompleter {

    private final GradientNick plugin;
    private static final List<String> PRESET_COLORS = Arrays.asList(
            "#FF0000", "#FF7F00", "#FFFF00", "#00FF00", "#0000FF", "#4B0082", "#9400D3"
    );

    public GradientCommand(GradientNick plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        MessagesManager msg = plugin.getMessagesManager();
        ConfigManager cfg = plugin.getConfigManager();

        if (!sender.hasPermission("gradient.admin")) {
            sender.sendMessage(msg.get("no-permission"));
            return true;
        }

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "reload" -> {
                plugin.reload();
                sender.sendMessage(msg.get("reload-success"));
            }
            case "info" -> {
                if (args.length < 2) {
                    sender.sendMessage("§cИспользование: /aprefix info <игрок>");
                    return true;
                }
                handleInfo(sender, args[1], msg);
            }
            case "setcolor" -> {
                if (args.length < 3) {
                    sender.sendMessage("§cИспользование: /aprefix setcolor <игрок> <цвет1> [цвет2] ...");
                    return true;
                }
                handleSetColor(sender, args, msg, cfg);
            }
            case "setprefix" -> {
                if (args.length < 3) {
                    sender.sendMessage("§cИспользование: /aprefix setprefix <игрок> <префикс>");
                    return true;
                }
                handleSetPrefix(sender, args, msg, cfg);
            }
            case "coloron" -> {
                if (args.length < 2) {
                    sender.sendMessage("§cИспользование: /aprefix coloron <игрок>");
                    return true;
                }
                handleColorToggle(sender, args[1], true, msg);
            }
            case "coloroff" -> {
                if (args.length < 2) {
                    sender.sendMessage("§cИспользование: /aprefix coloroff <игрок>");
                    return true;
                }
                handleColorToggle(sender, args[1], false, msg);
            }
            case "prefixon" -> {
                if (args.length < 2) {
                    sender.sendMessage("§cИспользование: /aprefix prefixon <игрок>");
                    return true;
                }
                handlePrefixToggle(sender, args[1], true, msg);
            }
            case "prefixoff" -> {
                if (args.length < 2) {
                    sender.sendMessage("§cИспользование: /aprefix prefixoff <игрок>");
                    return true;
                }
                handlePrefixToggle(sender, args[1], false, msg);
            }
            case "resetcolor" -> {
                if (args.length < 2) {
                    sender.sendMessage("§cИспользование: /aprefix resetcolor <игрок>");
                    return true;
                }
                handleResetColor(sender, args[1], msg);
            }
            case "resetprefix" -> {
                if (args.length < 2) {
                    sender.sendMessage("§cИспользование: /aprefix resetprefix <игрок>");
                    return true;
                }
                handleResetPrefix(sender, args[1], msg);
            }
            default -> sendHelp(sender);
        }

        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage("§e=== LoPreff Admin ===");
        sender.sendMessage("§e/aprefix reload §7— перезагрузить конфиг");
        sender.sendMessage("§e/aprefix info <игрок> §7— информация об игроке");
        sender.sendMessage("§e/aprefix setcolor <игрок> <цвета> §7— установить цвет");
        sender.sendMessage("§e/aprefix setprefix <игрок> <префикс> §7— установить префикс");
        sender.sendMessage("§e/aprefix coloron/coloroff <игрок> §7— вкл/выкл цвет");
        sender.sendMessage("§e/aprefix prefixon/prefixoff <игрок> §7— вкл/выкл префикс");
        sender.sendMessage("§e/aprefix resetcolor <игрок> §7— сбросить цвет");
        sender.sendMessage("§e/aprefix resetprefix <игрок> §7— сбросить префикс");
    }

    private void handleInfo(CommandSender sender, String playerName, MessagesManager msg) {
        OfflinePlayer target = Bukkit.getOfflinePlayer(playerName);
        if (!target.hasPlayedBefore() && !target.isOnline()) {
            sender.sendMessage(msg.get("player-not-found"));
            return;
        }

        PlayerData data = plugin.getDataManager().getPlayerData(target.getUniqueId());

        sender.sendMessage(msg.get("info-header", "player", playerName));
        sender.sendMessage(msg.get("info-prefix", "prefix", data.hasPrefix() ? data.getPrefix() : "нет"));
        sender.sendMessage(msg.get("info-prefix-enabled", "enabled", data.isPrefixEnabled() ? "да" : "нет"));
        sender.sendMessage(msg.get("info-colors", "colors", data.hasColors() ? String.join(", ", data.getColors()) : "нет"));
        sender.sendMessage(msg.get("info-color-enabled", "enabled", data.isColorEnabled() ? "да" : "нет"));
        sender.sendMessage(msg.get("info-prefix-purchased", "purchased", data.isPrefixPurchased() ? "да" : "нет"));
    }

    private void handleSetColor(CommandSender sender, String[] args, MessagesManager msg, ConfigManager cfg) {
        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
        if (!target.hasPlayedBefore() && !target.isOnline()) {
            sender.sendMessage(msg.get("player-not-found"));
            return;
        }

        List<String> colors = new ArrayList<>();
        for (int i = 2; i < args.length; i++) {
            String color = args[i].startsWith("#") ? args[i] : "#" + args[i];
            if (!GradientUtil.isValidHex(color)) {
                sender.sendMessage(msg.get("invalid-color", "color", args[i]));
                return;
            }
            colors.add(color.toUpperCase());
        }

        if (colors.size() > cfg.getMaxColors()) {
            sender.sendMessage(msg.get("too-many-colors", "max", String.valueOf(cfg.getMaxColors())));
            return;
        }

        PlayerData data = plugin.getDataManager().getPlayerData(target.getUniqueId());
        data.setColors(colors);
        data.setColorEnabled(true);

        Player onlineTarget = target.getPlayer();
        if (onlineTarget != null) {
            FoliaUtil.runEntityTask(plugin, onlineTarget, () -> DisplayNameUtil.updateDisplayName(plugin, onlineTarget, data));
        }

        FoliaUtil.runAsync(plugin, () -> plugin.getDataManager().savePlayerData(target.getUniqueId()));

        sender.sendMessage("§aЦвет для игрока §f" + args[1] + " §aустановлен: §f" + String.join(" ", colors));
    }

    private void handleSetPrefix(CommandSender sender, String[] args, MessagesManager msg, ConfigManager cfg) {
        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
        if (!target.hasPlayedBefore() && !target.isOnline()) {
            sender.sendMessage(msg.get("player-not-found"));
            return;
        }

        StringBuilder prefixBuilder = new StringBuilder();
        for (int i = 2; i < args.length; i++) {
            if (i > 2) prefixBuilder.append(" ");
            prefixBuilder.append(args[i]);
        }
        String prefix = prefixBuilder.toString();

        if (prefix.length() > cfg.getMaxPrefixLength()) {
            sender.sendMessage(msg.get("prefix-too-long", "max", String.valueOf(cfg.getMaxPrefixLength())));
            return;
        }

        PlayerData data = plugin.getDataManager().getPlayerData(target.getUniqueId());
        data.setPrefix(prefix);
        data.setPrefixEnabled(true);
        data.setPrefixPurchased(true);

        plugin.getLuckPermsHook().setPrefix(target.getUniqueId(), DisplayNameUtil.buildColoredPrefix(plugin, data));

        Player onlineTarget = target.getPlayer();
        if (onlineTarget != null) {
            FoliaUtil.runEntityTask(plugin, onlineTarget, () -> DisplayNameUtil.updateDisplayName(plugin, onlineTarget, data));
        }

        FoliaUtil.runAsync(plugin, () -> plugin.getDataManager().savePlayerData(target.getUniqueId()));

        sender.sendMessage("§aПрефикс для игрока §f" + args[1] + " §aустановлен: §f" + prefix);
    }

    private void handleColorToggle(CommandSender sender, String playerName, boolean enable, MessagesManager msg) {
        OfflinePlayer target = Bukkit.getOfflinePlayer(playerName);
        if (!target.hasPlayedBefore() && !target.isOnline()) {
            sender.sendMessage(msg.get("player-not-found"));
            return;
        }

        PlayerData data = plugin.getDataManager().getPlayerData(target.getUniqueId());
        data.setColorEnabled(enable);

        Player onlineTarget = target.getPlayer();
        if (onlineTarget != null) {
            FoliaUtil.runEntityTask(plugin, onlineTarget, () -> DisplayNameUtil.updateDisplayName(plugin, onlineTarget, data));
        }

        FoliaUtil.runAsync(plugin, () -> plugin.getDataManager().savePlayerData(target.getUniqueId()));

        sender.sendMessage("§aЦвет для игрока §f" + playerName + (enable ? " §aвключён" : " §cвыключен"));
    }

    private void handlePrefixToggle(CommandSender sender, String playerName, boolean enable, MessagesManager msg) {
        OfflinePlayer target = Bukkit.getOfflinePlayer(playerName);
        if (!target.hasPlayedBefore() && !target.isOnline()) {
            sender.sendMessage(msg.get("player-not-found"));
            return;
        }

        PlayerData data = plugin.getDataManager().getPlayerData(target.getUniqueId());
        data.setPrefixEnabled(enable);

        if (enable && data.hasPrefix()) {
            plugin.getLuckPermsHook().setPrefix(target.getUniqueId(), DisplayNameUtil.buildColoredPrefix(plugin, data));
        } else {
            plugin.getLuckPermsHook().removePrefix(target.getUniqueId());
        }

        Player onlineTarget = target.getPlayer();
        if (onlineTarget != null) {
            FoliaUtil.runEntityTask(plugin, onlineTarget, () -> DisplayNameUtil.updateDisplayName(plugin, onlineTarget, data));
        }

        FoliaUtil.runAsync(plugin, () -> plugin.getDataManager().savePlayerData(target.getUniqueId()));

        sender.sendMessage("§aПрефикс для игрока §f" + playerName + (enable ? " §aвключён" : " §cвыключен"));
    }

    private void handleResetColor(CommandSender sender, String playerName, MessagesManager msg) {
        OfflinePlayer target = Bukkit.getOfflinePlayer(playerName);
        if (!target.hasPlayedBefore() && !target.isOnline()) {
            sender.sendMessage(msg.get("player-not-found"));
            return;
        }

        PlayerData data = plugin.getDataManager().getPlayerData(target.getUniqueId());
        data.setColors(new ArrayList<>());
        data.setColorEnabled(false);

        Player onlineTarget = target.getPlayer();
        if (onlineTarget != null) {
            FoliaUtil.runEntityTask(plugin, onlineTarget, () -> DisplayNameUtil.updateDisplayName(plugin, onlineTarget, data));
        }

        FoliaUtil.runAsync(plugin, () -> plugin.getDataManager().savePlayerData(target.getUniqueId()));

        sender.sendMessage("§aЦвет для игрока §f" + playerName + " §aсброшен");
    }

    private void handleResetPrefix(CommandSender sender, String playerName, MessagesManager msg) {
        OfflinePlayer target = Bukkit.getOfflinePlayer(playerName);
        if (!target.hasPlayedBefore() && !target.isOnline()) {
            sender.sendMessage(msg.get("player-not-found"));
            return;
        }

        PlayerData data = plugin.getDataManager().getPlayerData(target.getUniqueId());
        data.setPrefix(null);
        data.setPrefixEnabled(false);

        plugin.getLuckPermsHook().removePrefix(target.getUniqueId());

        Player onlineTarget = target.getPlayer();
        if (onlineTarget != null) {
            FoliaUtil.runEntityTask(plugin, onlineTarget, () -> DisplayNameUtil.updateDisplayName(plugin, onlineTarget, data));
        }

        FoliaUtil.runAsync(plugin, () -> plugin.getDataManager().savePlayerData(target.getUniqueId()));

        sender.sendMessage("§aПрефикс для игрока §f" + playerName + " §aсброшен");
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                                 @NotNull String alias, @NotNull String[] args) {
        if (!sender.hasPermission("gradient.admin")) return new ArrayList<>();

        if (args.length == 1) {
            return Arrays.asList("reload", "info", "setcolor", "setprefix", "coloron", "coloroff", "prefixon", "prefixoff", "resetcolor", "resetprefix").stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .toList();
        }

        if (args.length == 2) {
            String sub = args[0].toLowerCase();
            if (Arrays.asList("info", "setcolor", "setprefix", "coloron", "coloroff", "prefixon", "prefixoff", "resetcolor", "resetprefix").contains(sub)) {
                return Bukkit.getOnlinePlayers().stream()
                        .map(Player::getName)
                        .filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase()))
                        .toList();
            }
        }

        if (args.length >= 3 && args[0].equalsIgnoreCase("setcolor")) {
            return PRESET_COLORS.stream()
                    .filter(s -> s.toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
                    .toList();
        }

        return new ArrayList<>();
    }
}
