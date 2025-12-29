package com.loki.lochat.gradient.commands;

import com.loki.lochat.gradient.GradientModule;
import com.loki.lochat.gradient.config.GradientConfig;
import com.loki.lochat.gradient.config.GradientMessages;
import com.loki.lochat.gradient.data.GradientPlayerData;
import com.loki.lochat.gradient.util.DisplayNameUtil;
import com.loki.lochat.gradient.util.FoliaUtil;
import com.loki.lochat.gradient.util.GradientUtil;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Админ команда /aprefix для управления градиентами
 */
public class GradientAdminCommand implements CommandExecutor, TabCompleter {

    private final GradientModule module;
    private static final List<String> PRESET_COLORS = Arrays.asList(
            "#FF0000", "#FF7F00", "#FFFF00", "#00FF00", "#0000FF", "#4B0082", "#9400D3"
    );

    public GradientAdminCommand(GradientModule module) {
        this.module = module;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        GradientMessages msg = module.getMessages();

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
                module.reload();
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
                handleSetColor(sender, args, msg);
            }
            case "setprefix" -> {
                if (args.length < 3) {
                    sender.sendMessage("§cИспользование: /aprefix setprefix <игрок> <префикс>");
                    return true;
                }
                handleSetPrefix(sender, args, msg);
            }
            case "coloron" -> handleColorToggle(sender, args, true, msg);
            case "coloroff" -> handleColorToggle(sender, args, false, msg);
            case "prefixon" -> handlePrefixToggle(sender, args, true, msg);
            case "prefixoff" -> handlePrefixToggle(sender, args, false, msg);
            case "resetcolor" -> handleResetColor(sender, args, msg);
            case "resetprefix" -> handleResetPrefix(sender, args, msg);
            default -> sendHelp(sender);
        }

        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage("§e=== LoChat Gradient Admin ===");
        sender.sendMessage("§e/aprefix reload §7— перезагрузить конфиг");
        sender.sendMessage("§e/aprefix info <игрок> §7— информация об игроке");
        sender.sendMessage("§e/aprefix setcolor <игрок> <цвета> §7— установить цвет");
        sender.sendMessage("§e/aprefix setprefix <игрок> <префикс> §7— установить префикс");
        sender.sendMessage("§e/aprefix coloron/coloroff <игрок> §7— вкл/выкл цвет");
        sender.sendMessage("§e/aprefix prefixon/prefixoff <игрок> §7— вкл/выкл префикс");
        sender.sendMessage("§e/aprefix resetcolor <игрок> §7— сбросить цвет");
        sender.sendMessage("§e/aprefix resetprefix <игрок> §7— сбросить префикс");
    }

    private void handleInfo(CommandSender sender, String playerName, GradientMessages msg) {
        OfflinePlayer target = Bukkit.getOfflinePlayer(playerName);
        if (!target.hasPlayedBefore() && !target.isOnline()) {
            sender.sendMessage(msg.get("player-not-found"));
            return;
        }

        GradientPlayerData data = module.getDataManager().getPlayerData(target.getUniqueId());

        sender.sendMessage(msg.get("info-header", "player", playerName));
        sender.sendMessage(msg.get("info-prefix", "prefix", data.hasPrefix() ? data.getPrefix() : "нет"));
        sender.sendMessage(msg.get("info-prefix-enabled", "enabled", data.isPrefixEnabled() ? "да" : "нет"));
        sender.sendMessage(msg.get("info-colors", "colors", data.hasColors() ? String.join(", ", data.getColors()) : "нет"));
        sender.sendMessage(msg.get("info-color-enabled", "enabled", data.isColorEnabled() ? "да" : "нет"));
        sender.sendMessage(msg.get("info-prefix-purchased", "purchased", data.isPrefixPurchased() ? "да" : "нет"));
    }

    private void handleSetColor(CommandSender sender, String[] args, GradientMessages msg) {
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

        GradientConfig cfg = module.getConfig();
        if (colors.size() > cfg.getMaxColors()) {
            sender.sendMessage(msg.get("too-many-colors", "max", String.valueOf(cfg.getMaxColors())));
            return;
        }

        GradientPlayerData data = module.getDataManager().getPlayerData(target.getUniqueId());
        data.setColors(colors);
        data.setColorEnabled(true);

        Player onlineTarget = target.getPlayer();
        if (onlineTarget != null) {
            FoliaUtil.runEntityTask(module.getPlugin(), onlineTarget, 
                    () -> DisplayNameUtil.updateDisplayName(module, onlineTarget, data));
        }

        FoliaUtil.runAsync(module.getPlugin(), 
                () -> module.getDataManager().savePlayerData(target.getUniqueId()));

        sender.sendMessage("§aЦвет для игрока §f" + args[1] + " §aустановлен: §f" + String.join(" ", colors));
    }

    private void handleSetPrefix(CommandSender sender, String[] args, GradientMessages msg) {
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

        GradientConfig cfg = module.getConfig();
        if (prefix.length() > cfg.getMaxPrefixLength()) {
            sender.sendMessage(msg.get("prefix-too-long", "max", String.valueOf(cfg.getMaxPrefixLength())));
            return;
        }

        GradientPlayerData data = module.getDataManager().getPlayerData(target.getUniqueId());
        data.setPrefix(prefix);
        data.setPrefixEnabled(true);
        data.setPrefixPurchased(true);

        module.getLuckPermsHook().setPrefix(target.getUniqueId(), DisplayNameUtil.buildColoredPrefix(module, data));

        Player onlineTarget = target.getPlayer();
        if (onlineTarget != null) {
            FoliaUtil.runEntityTask(module.getPlugin(), onlineTarget, 
                    () -> DisplayNameUtil.updateDisplayName(module, onlineTarget, data));
        }

        FoliaUtil.runAsync(module.getPlugin(), 
                () -> module.getDataManager().savePlayerData(target.getUniqueId()));

        sender.sendMessage("§aПрефикс для игрока §f" + args[1] + " §aустановлен: §f" + prefix);
    }

    private void handleColorToggle(CommandSender sender, String[] args, boolean enable, GradientMessages msg) {
        if (args.length < 2) {
            sender.sendMessage("§cИспользование: /aprefix " + (enable ? "coloron" : "coloroff") + " <игрок>");
            return;
        }
        
        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
        if (!target.hasPlayedBefore() && !target.isOnline()) {
            sender.sendMessage(msg.get("player-not-found"));
            return;
        }

        GradientPlayerData data = module.getDataManager().getPlayerData(target.getUniqueId());
        data.setColorEnabled(enable);

        Player onlineTarget = target.getPlayer();
        if (onlineTarget != null) {
            FoliaUtil.runEntityTask(module.getPlugin(), onlineTarget, 
                    () -> DisplayNameUtil.updateDisplayName(module, onlineTarget, data));
        }

        FoliaUtil.runAsync(module.getPlugin(), 
                () -> module.getDataManager().savePlayerData(target.getUniqueId()));

        sender.sendMessage("§aЦвет для игрока §f" + args[1] + (enable ? " §aвключён" : " §cвыключен"));
    }

    private void handlePrefixToggle(CommandSender sender, String[] args, boolean enable, GradientMessages msg) {
        if (args.length < 2) {
            sender.sendMessage("§cИспользование: /aprefix " + (enable ? "prefixon" : "prefixoff") + " <игрок>");
            return;
        }
        
        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
        if (!target.hasPlayedBefore() && !target.isOnline()) {
            sender.sendMessage(msg.get("player-not-found"));
            return;
        }

        GradientPlayerData data = module.getDataManager().getPlayerData(target.getUniqueId());
        data.setPrefixEnabled(enable);

        if (enable && data.hasPrefix()) {
            module.getLuckPermsHook().setPrefix(target.getUniqueId(), DisplayNameUtil.buildColoredPrefix(module, data));
        } else {
            module.getLuckPermsHook().removePrefix(target.getUniqueId());
        }

        Player onlineTarget = target.getPlayer();
        if (onlineTarget != null) {
            FoliaUtil.runEntityTask(module.getPlugin(), onlineTarget, 
                    () -> DisplayNameUtil.updateDisplayName(module, onlineTarget, data));
        }

        FoliaUtil.runAsync(module.getPlugin(), 
                () -> module.getDataManager().savePlayerData(target.getUniqueId()));

        sender.sendMessage("§aПрефикс для игрока §f" + args[1] + (enable ? " §aвключён" : " §cвыключен"));
    }

    private void handleResetColor(CommandSender sender, String[] args, GradientMessages msg) {
        if (args.length < 2) {
            sender.sendMessage("§cИспользование: /aprefix resetcolor <игрок>");
            return;
        }
        
        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
        if (!target.hasPlayedBefore() && !target.isOnline()) {
            sender.sendMessage(msg.get("player-not-found"));
            return;
        }

        GradientPlayerData data = module.getDataManager().getPlayerData(target.getUniqueId());
        data.setColors(new ArrayList<>());
        data.setColorEnabled(false);

        Player onlineTarget = target.getPlayer();
        if (onlineTarget != null) {
            FoliaUtil.runEntityTask(module.getPlugin(), onlineTarget, 
                    () -> DisplayNameUtil.updateDisplayName(module, onlineTarget, data));
        }

        FoliaUtil.runAsync(module.getPlugin(), 
                () -> module.getDataManager().savePlayerData(target.getUniqueId()));

        sender.sendMessage("§aЦвет для игрока §f" + args[1] + " §aсброшен");
    }

    private void handleResetPrefix(CommandSender sender, String[] args, GradientMessages msg) {
        if (args.length < 2) {
            sender.sendMessage("§cИспользование: /aprefix resetprefix <игрок>");
            return;
        }
        
        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
        if (!target.hasPlayedBefore() && !target.isOnline()) {
            sender.sendMessage(msg.get("player-not-found"));
            return;
        }

        GradientPlayerData data = module.getDataManager().getPlayerData(target.getUniqueId());
        data.setPrefix(null);
        data.setPrefixEnabled(false);

        module.getLuckPermsHook().removePrefix(target.getUniqueId());

        Player onlineTarget = target.getPlayer();
        if (onlineTarget != null) {
            FoliaUtil.runEntityTask(module.getPlugin(), onlineTarget, 
                    () -> DisplayNameUtil.updateDisplayName(module, onlineTarget, data));
        }

        FoliaUtil.runAsync(module.getPlugin(), 
                () -> module.getDataManager().savePlayerData(target.getUniqueId()));

        sender.sendMessage("§aПрефикс для игрока §f" + args[1] + " §aсброшен");
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                                 @NotNull String alias, @NotNull String[] args) {
        if (!sender.hasPermission("gradient.admin")) return new ArrayList<>();

        if (args.length == 1) {
            return Arrays.asList("reload", "info", "setcolor", "setprefix", "coloron", "coloroff", 
                    "prefixon", "prefixoff", "resetcolor", "resetprefix").stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .toList();
        }

        if (args.length == 2) {
            String sub = args[0].toLowerCase();
            if (Arrays.asList("info", "setcolor", "setprefix", "coloron", "coloroff", 
                    "prefixon", "prefixoff", "resetcolor", "resetprefix").contains(sub)) {
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
