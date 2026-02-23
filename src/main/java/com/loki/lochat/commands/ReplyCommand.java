package com.loki.lochat.commands;

import com.loki.lochat.LoChat;
import com.loki.lochat.api.service.IgnoreService;
import com.loki.lochat.api.service.PMService;
import com.loki.lochat.api.service.SpyService;
import com.loki.lochat.utils.ChatFormatter;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;

/**
 * Команда для ответа на последнее личное сообщение
 * /reply <сообщение>
 */
public class ReplyCommand implements CommandExecutor {

    private final LoChat plugin;
    private final PMService pmService;
    private final IgnoreService ignoreService;
    private final SpyService spyService;

    public ReplyCommand(LoChat plugin) {
        this.plugin = plugin;
        this.pmService = plugin.getServiceRegistry().get(PMService.class);
        this.ignoreService = plugin.getServiceRegistry().get(IgnoreService.class);
        this.spyService = plugin.getServiceRegistry().get(SpyService.class);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Только для игроков!");
            return true;
        }

        if (!plugin.getConfigManager().isPmEnabled()) {
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(ChatFormatter.parse(plugin.getMessageConfig().getInvalidUsage("/reply <сообщение>")));
            return true;
        }

        Optional<UUID> lastUuidOpt = pmService.getLastConversation(player.getUniqueId());
        if (lastUuidOpt.isEmpty()) {
            player.sendMessage(ChatFormatter.parse(plugin.getMessageConfig().get("pm.no-reply")));
            return true;
        }

        UUID lastUuid = lastUuidOpt.get();
        Player target = Bukkit.getPlayer(lastUuid);
        if (target == null) {
            player.sendMessage(ChatFormatter.parse(plugin.getMessageConfig().get("pm.player-offline")));
            return true;
        }

        // Проверка игнора
        if (ignoreService.isIgnoring(target.getUniqueId(), player.getUniqueId())) {
            player.sendMessage(ChatFormatter.parse(plugin.getMessageConfig().get("pm.ignored")));
            return true;
        }

        String message = String.join(" ", args);

        // Отправляем сообщения с градиентными именами
        player.sendMessage(ChatFormatter.formatPmSentNew(
                plugin.getMessageConfig().getPmFormatSent(),
                player,
                target,
                message
        ));

        target.sendMessage(ChatFormatter.formatPmReceivedNew(
                plugin.getMessageConfig().getPmFormatReceived(),
                player,
                target,
                message
        ));

        // Звук для получателя
        if (plugin.getConfigManager().isPmSoundEnabled()) {
            try {
                Sound sound = Sound.valueOf(plugin.getConfigManager().getPmSoundType());
                target.playSound(target.getLocation(), sound, 1.0f, 1.0f);
            } catch (IllegalArgumentException ignored) {}
        }

        // Шпион
        spyService.broadcastPM(player, target, message);

        // Обновляем последнего собеседника
        pmService.setLastConversation(target.getUniqueId(), player.getUniqueId());

        // Логирование
        if (plugin.getConfigManager().isPmLogEnabled()) {
            plugin.getLogger().info("[PM] " + player.getName() + " -> " + target.getName() + ": " + message);
        }

        return true;
    }
}
