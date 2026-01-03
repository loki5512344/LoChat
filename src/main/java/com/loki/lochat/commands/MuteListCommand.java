package com.loki.lochat.commands;

import com.loki.lochat.LoChat;
import com.loki.lochat.managers.MuteManager;
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
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm");

    public MuteListCommand(LoChat plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("lochat.mutelist")) {
            sender.sendMessage(plugin.getMessageConfig().getComponent("errors.no-permission"));
            return true;
        }

        Map<UUID, MuteManager.MuteData> activeMutes = plugin.getMuteManager().getActiveMutes();

        if (activeMutes.isEmpty()) {
            sender.sendMessage("§aНет активных мутов");
            return true;
        }

        sender.sendMessage("§6§l=== Активные муты (" + activeMutes.size() + ") ===");
        
        int index = 1;
        for (Map.Entry<UUID, MuteManager.MuteData> entry : activeMutes.entrySet()) {
            UUID uuid = entry.getKey();
            MuteManager.MuteData data = entry.getValue();
            
            // Получаем имя игрока
            String playerName = data.playerName;
            if (playerName == null) {
                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
                playerName = offlinePlayer.getName() != null ? offlinePlayer.getName() : uuid.toString().substring(0, 8);
            }
            
            // Форматируем время
            String timeStr;
            if (data.isPermanent()) {
                timeStr = "§4Навсегда";
            } else {
                long remaining = data.endTime - System.currentTimeMillis();
                timeStr = "§e" + plugin.getMuteManager().formatTime(remaining);
            }
            
            sender.sendMessage("§7" + index + ". §f" + playerName + " §7- " + timeStr);
            sender.sendMessage("   §7Причина: §f" + (data.reason != null ? data.reason : "Не указана"));
            sender.sendMessage("   §7Выдал: §f" + data.mutedBy + " §7(" + dateFormat.format(new Date(data.mutedAt)) + ")");
            
            index++;
        }

        return true;
    }
}
