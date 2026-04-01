package com.loki.lochat.commands.rp;

import com.loki.lochat.LoChat;
import com.loki.lochat.api.service.CooldownService;
import com.loki.lochat.commands.base.PlayerCommand;
import com.loki.lochat.utils.ChatFormatter;
import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

/**
 * /try <действие>
 * Случайный исход: успех или провал (50/50).
 * Формат: * ИгрокИмя пытается действие... [Успех! / Провал!]
 */
public class TryCommand extends PlayerCommand {

    private static final Random RANDOM = new Random();
    private final CooldownService cooldownService;

    public TryCommand(LoChat plugin) {
        super(plugin);
        this.cooldownService = plugin.getServiceRegistry().get(CooldownService.class);
    }

    @Override
    protected boolean executePlayerCommand(@NotNull Player player, @NotNull Command command,
                                           @NotNull String label, @NotNull String[] args) {
        if (!requirePermission(player, "lochat.rp.try")) return true;

        int cooldown = plugin.getConfigManager().getInt("rp.cooldowns.try", 0);
        if (cooldown > 0 && cooldownService.isOnCooldown(player.getUniqueId(), "rp_try", cooldown)) {
            int remaining = cooldownService.getRemainingCooldown(player.getUniqueId(), "rp_try", cooldown);
            String msg = plugin.getConfigManager().getMessagesConfig().getCooldownMessage().replace("{remaining}", String.valueOf(remaining));
            player.sendMessage(ChatFormatter.parse(msg));
            return true;
        }

        if (args.length == 0) {
            sendUsage(player, "/try <действие>");
            return true;
        }

        String action = String.join(" ", args);
        boolean success = RANDOM.nextBoolean();

        String resultKey = success ? "rp.try-success" : "rp.try-fail";
        String resultDefault = success ? "<green>[Успех!]</green>" : "<red>[Провал!]</red>";
        String resultTag = plugin.getConfigManager().getString(resultKey, resultDefault);

        String format = plugin.getConfigManager().getString("rp.try-format",
                "<gray>* </gray><white>{player}</white><gray> пытается {action}...</gray> {result}");

        String displayName = RpUtil.getDisplayName(plugin, player);
        Component message = ChatFormatter.parse(
                format.replace("{player}", displayName)
                      .replace("{action}", action)
                      .replace("{result}", resultTag)
        );

        int radius = plugin.getConfigManager().getInt("rp.radius", 100);
        RpUtil.sendToRadius(player, message, radius);

        if (cooldown > 0) cooldownService.setCooldown(player.getUniqueId(), "rp_try");
        plugin.getLogger().info("[TRY] " + player.getName() + ": " + action + " -> " + (success ? "SUCCESS" : "FAIL"));
        return true;
    }
}
