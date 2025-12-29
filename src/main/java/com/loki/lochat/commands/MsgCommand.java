package com.loki.lochat.commands;

import com.loki.lochat.LoChat;
import com.loki.lochat.utils.ChatFormatter;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class MsgCommand implements CommandExecutor {

    private final LoChat plugin;

    public MsgCommand(LoChat plugin) {
        this.plugin = plugin;
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
        if (plugin.getIgnoreManager().isIgnoring(target.getUniqueId(), player.getUniqueId())) {
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

        // Отправляем сообщения
        player.sendMessage(ChatFormatter.formatPmSent(
                plugin.getMessageConfig().get("pm.sent"),
                target.getName(),
                message
        ));

        target.sendMessage(ChatFormatter.formatPmReceived(
                plugin.getMessageConfig().get("pm.received"),
                player.getName(),
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
        plugin.getSpyManager().broadcastPM(player, target, message);

        // Сохраняем последнего собеседника
        plugin.getPmManager().setLastConversation(player.getUniqueId(), target.getUniqueId());
        plugin.getPmManager().setLastConversation(target.getUniqueId(), player.getUniqueId());

        // Логирование
        if (plugin.getConfigManager().isPmLogEnabled()) {
            plugin.getLogger().info("[PM] " + player.getName() + " -> " + target.getName() + ": " + message);
        }

        return true;
    }
}
