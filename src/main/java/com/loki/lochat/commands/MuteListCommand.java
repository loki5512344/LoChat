package com.loki.lochat.commands;

import com.loki.lochat.LoChat;
import com.loki.lochat.api.service.MuteService;
import com.loki.lochat.data.model.MuteData;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

/**
 * Команда /lmutelist - список активных мутов
 */
public class MuteListCommand implements CommandExecutor {

    private final LoChat plugin;
    private final MuteService muteService;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm");

    public MuteListCommand(LoChat plugin) {
        this.plugin = plugin;
        this.muteService = plugin.getServiceRegistry().get(MuteService.class);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("lochat.mutelist")) {
            sender.sendMessage(plugin.getMessageConfig().getComponent("errors.no-permission"));
            return true;
        }

        Map<UUID, MuteData> activeMutes = muteService.getActiveMutes();

        if (activeMutes.isEmpty()) {
            sender.sendMessage("§aНет активных мутов");
            return true;
        }

        sender.sendMessage("§6§l=== Активные муты (" + activeMutes.size() + ") ===");

        int index = 1;
        for (Map.Entry<UUID, MuteData> entry : activeMutes.entrySet()) {
            UUID uuid = entry.getKey();
            MuteData data = entry.getValue();

            // Получаем имя игрока
            String playerName = data.getPlayerName();
            if (playerName == null) {
                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
                playerName = offlinePlayer.getName() != null ? offlinePlayer.getName() : uuid.toString().substring(0, 8);
            }

            // Форматируем время
            String timeStr;
            if (data.isPermanent()) {
                timeStr = "§4Навсегда";
            } else {
                long remaining = data.getEndTime() - System.currentTimeMillis();
                timeStr = "§e" + muteService.formatTime(remaining);
            }

            sender.sendMessage("§7" + index + ". §f" + playerName + " §7- " + timeStr);
            sender.sendMessage("   §7Причина: §f" + (data.getReason() != null ? data.getReason() : "Не указана"));
            sender.sendMessage("   §7Выдал: §f" + data.getMutedBy() + " §7(" + dateFormat.format(new Date(data.getMutedAt())) + ")");

            index++;
        }

        return true;
    }
}
