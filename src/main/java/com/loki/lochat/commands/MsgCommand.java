package com.loki.lochat.commands;

import com.loki.lochat.LoChat;
import com.loki.lochat.api.service.IgnoreService;
import com.loki.lochat.api.service.PMService;
import com.loki.lochat.api.service.SpyService;
import com.loki.lochat.utils.ChatFormatter;
import com.loki.lochat.utils.PlayerUtil;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Команда для отправки личных сообщений
 * /msg <ник> <сообщение>
 */
public class MsgCommand implements CommandExecutor {

    private final LoChat plugin;
    private final PMService pmService;
    private final IgnoreService ignoreService;
    private final SpyService spyService;

    public MsgCommand(LoChat plugin) {
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

        if (args.length < 2) {
            player.sendMessage(ChatFormatter.parse(plugin.getMessageConfig().getInvalidUsage("/msg <ник> <сообщение>")));
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            player.sendMessage(ChatFormatter.parse(plugin.getMessageConfig().get("pm.player-offline")));
            return true;
        }

        if (target.equals(player)) {
            player.sendMessage(ChatFormatter.parse(plugin.getMessageConfig().get("pm.self")));
            return true;
        }

        // Проверка игнора
        if (ignoreService.isIgnoring(target.getUniqueId(), player.getUniqueId())) {
            player.sendMessage(ChatFormatter.parse(plugin.getMessageConfig().get("pm.ignored")));
            return true;
        }

        // Собираем сообщение
        StringBuilder messageBuilder = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            if (i > 1) messageBuilder.append(" ");
            messageBuilder.append(args[i]);
        }
        String message = messageBuilder.toString();

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
            Sound sound = PlayerUtil.parseSound(plugin.getConfigManager().getPmSoundType(), null);
            if (sound != null) {
                target.playSound(target.getLocation(), sound, 1.0f, 1.0f);
            }
        }

        // Шпион
        spyService.broadcastPM(player, target, message);

        // Сохраняем последнего собеседника
        pmService.setLastConversation(player.getUniqueId(), target.getUniqueId());
        pmService.setLastConversation(target.getUniqueId(), player.getUniqueId());

        // Логирование
        if (plugin.getConfigManager().isPmLogEnabled()) {
            plugin.getLogger().info("[PM] " + player.getName() + " -> " + target.getName() + ": " + message);
        }

        return true;
    }
}
