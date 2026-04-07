package com.loki.lochat.gradient.commands;

import com.loki.lochat.config.RatConfig;
import com.loki.lochat.gradient.GradientModule;
import com.loki.lochat.gradient.commands.handlers.color.*;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Команда /color для установки градиента ника
 * Делегирует обработку подкоманд соответствующим handlers
 */
public class GradientColorCommand implements CommandExecutor, TabCompleter {

    private static final Set<String> SUB_COMMANDS = Set.of("on", "off", "reset", "copy");
    private final GradientModule module;
    private final Map<String, SubCommandHandler> handlers = new HashMap<>();
    private final ColorSetHandler setHandler;

    public GradientColorCommand(GradientModule module) {
        this.module = module;
        this.setHandler = new ColorSetHandler(module);
        registerHandlers();
    }

    private void registerHandlers() {
        handlers.put("on", new ColorOnHandler(module));
        handlers.put("off", new ColorOffHandler(module));
        handlers.put("reset", new ColorResetHandler(module));
        handlers.put("copy", new ColorCopyHandler(module));
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
        SubCommandHandler handler = handlers.get(subCommand);

        if (handler != null) {
            return handler.handle(player, args);
        }

        return setHandler.handle(player, args);
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                                @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            String input = args[0].toLowerCase();
            List<String> completions = new ArrayList<>(SUB_COMMANDS);
            completions.addAll(RatConfig.PRESET_COLORS);
            return completions.stream().filter(s -> s.toLowerCase().startsWith(input)).toList();
        }

        String firstArg = args[0].toLowerCase();

        if (args.length == 2 && firstArg.equals("copy")) {
            return Bukkit.getOnlinePlayers().stream().map(Player::getName)
                    .filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase())).toList();
        }

        if (!SUB_COMMANDS.contains(firstArg) && args.length <= module.getConfig().getMaxColors()) {
            return RatConfig.PRESET_COLORS.stream()
                    .filter(s -> s.toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
                    .toList();
        }

        return List.of();
    }
}
