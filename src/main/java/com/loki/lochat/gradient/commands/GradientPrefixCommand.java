package com.loki.lochat.gradient.commands;

import com.loki.lochat.gradient.GradientModule;
import com.loki.lochat.gradient.commands.handlers.prefix.PrefixOffHandler;
import com.loki.lochat.gradient.commands.handlers.prefix.PrefixOnHandler;
import com.loki.lochat.gradient.commands.handlers.prefix.PrefixResetHandler;
import com.loki.lochat.gradient.commands.handlers.prefix.PrefixSetHandler;
import com.loki.lochat.gradient.commands.handlers.prefix.PrefixSubCommandHandler;

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

/**
 * Команда /prefix для установки кастомного префикса
 * Делегирует обработку подкоманд соответствующим handlers
 */
public class GradientPrefixCommand implements CommandExecutor, TabCompleter {

    private static final Set<String> SUB_COMMANDS = Set.of("on", "off", "reset");
    private final GradientModule module;
    private final Map<String, PrefixSubCommandHandler> handlers = new HashMap<>();
    private final PrefixSetHandler setHandler;

    public GradientPrefixCommand(GradientModule module) {
        this.module = module;
        this.setHandler = new PrefixSetHandler(module);
        registerHandlers();
    }

    private void registerHandlers() {
        handlers.put("on", new PrefixOnHandler(module));
        handlers.put("off", new PrefixOffHandler(module));
        handlers.put("reset", new PrefixResetHandler(module));
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
        PrefixSubCommandHandler handler = handlers.get(subCommand);

        if (handler != null) {
            return handler.handle(player, args);
        }

        return setHandler.handle(player, args);
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
