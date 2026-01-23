package com.loki.lochat.commands;

import com.loki.lochat.LoChat;
import com.loki.lochat.utils.ChatFormatter;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Команда /mute nick [time] [-s] [причина]
 * Поддержка -s в любом месте до причины
 */
public class MuteCommand implements CommandExecutor, TabCompleter {

    private final LoChat plugin;

    public MuteCommand(LoChat plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("lochat.mute")) {
            sender.sendMessage(plugin.getMessageConfig().getComponent("errors.no-permission"));
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage("§cИспользование: /mute <ник> [время] [-s] [причина]");
            sender.sendMessage("§7Время: 1d, 2h, 30m, 60s или 0/perm для перманентного");
            sender.sendMessage("§7-s - тихий мут (требует право lochat.mute.silent)");
            return true;
        }

        // Парсим аргументы
        String targetName = null;
        String timeStr = null;
        boolean silent = false;
        StringBuilder reasonBuilder = new StringBuilder();

        for (String arg : args) {
            if (arg.equalsIgnoreCase("-s")) {
                silent = true;
            } else if (targetName == null) {
                targetName = arg;
            } else if (timeStr == null && isTimeFormat(arg)) {
                timeStr = arg;
            } else {
                if (reasonBuilder.length() > 0) reasonBuilder.append(" ");
                reasonBuilder.append(arg);
            }
        }

        // Проверка права на тихий мут
        if (silent && !sender.hasPermission("lochat.mute.silent")) {
            sender.sendMessage("§cУ вас нет права на тихий мут!");
            return true;
        }

        // Получаем настройки из конфига
        String defaultReason = plugin.getConfigManager().getString("mute.default-reason", "Нету.");
        String defaultDuration = plugin.getConfigManager().getString("mute.default-duration", "7d");
        
        String reason = reasonBuilder.length() > 0 ? reasonBuilder.toString() : defaultReason;

        // Определяем длительность
        long duration;
        if (timeStr == null || timeStr.isEmpty()) {
            // Используем максимальное доступное время из прав
            if (sender instanceof Player player) {
                duration = plugin.getMuteManager().getMaxDuration(player);
                if (duration == -1) {
                    // Нет прав на длительность - используем дефолт
                    duration = plugin.getMuteManager().parseTime(defaultDuration);
                }
            } else {
                duration = 0; // Консоль = перманентный
            }
        } else if (timeStr.equalsIgnoreCase("perm") || timeStr.equals("0")) {
            duration = 0; // Перманентный
        } else {
            duration = plugin.getMuteManager().parseTime(timeStr);
            if (duration < 0) {
                sender.sendMessage("§cНеверный формат времени! Используйте: 1d, 2h, 30m, 60s, perm");
                return true;
            }
        }

        // Проверяем право на длительность
        if (sender instanceof Player player) {
            if (!plugin.getMuteManager().canMuteForDuration(player, duration)) {
                long maxDur = plugin.getMuteManager().getMaxDuration(player);
                String maxStr = maxDur == 0 ? "перманентный" : plugin.getMuteManager().formatTime(maxDur);
                sender.sendMessage("§cВы не можете мутить на такой срок! Максимум: " + maxStr);
                return true;
            }
        }

        // Ищем игрока
        UUID targetUUID = com.loki.lochat.utils.PlayerUtil.findPlayerUUID(targetName);
        if (targetUUID == null) {
            sender.sendMessage(plugin.getMessageConfig().getComponent("errors.player-not-found"));
            return true;
        }

        Player target = Bukkit.getPlayer(targetUUID);
        String finalTargetName = com.loki.lochat.utils.PlayerUtil.getPlayerName(targetUUID);

        // Проверяем, не замучен ли уже
        if (plugin.getMuteManager().isMuted(targetUUID)) {
            sender.sendMessage("§cИгрок уже замучен!");
            return true;
        }

        // Мутим игрока
        String operatorName = sender.getName();
        plugin.getMuteManager().mute(targetUUID, finalTargetName, duration, reason, operatorName);

        String timeDisplay = duration == 0 ? "навсегда" : plugin.getMuteManager().formatTime(duration);

        // Уведомляем отправителя
        sender.sendMessage("§aИгрок §e" + finalTargetName + " §aзамучен на §e" + timeDisplay);
        sender.sendMessage("§7Причина: §f" + reason);

        // Уведомляем замученного (если онлайн)
        if (target != null) {
            String msgKey = duration == 0 ? "mute.messages.you-muted-permanent" : "mute.messages.you-muted";
            String msg = plugin.getConfigManager().getString(msgKey, 
                    duration == 0 ? "§cВы замучены навсегда! Причина: %reason%" : "§cВы замучены на %duration%! Причина: %reason%");
            msg = plugin.getMuteManager().formatMessage(msg, finalTargetName, operatorName, timeDisplay, reason);
            target.sendMessage(ChatFormatter.parse(msg));
        }

        // Broadcast
        if (silent) {
            // Тихий мут - только тем кто видит
            String silentMsg = plugin.getConfigManager().getString("mute.messages.silent-muted",
                    "§8[Тихо] §c%player% §7замучен на §c%duration% §7(%operator%). Причина: %reason%");
            silentMsg = plugin.getMuteManager().formatMessage(silentMsg, finalTargetName, operatorName, timeDisplay, reason);
            
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.hasPermission("lochat.mute.see-silent")) {
                    p.sendMessage(ChatFormatter.parse(silentMsg));
                }
            }
        } else {
            // Обычный broadcast
            String broadcastMsg = plugin.getConfigManager().getString("mute.messages.muted",
                    "§c%player% §7был замучен на §c%duration% §7модератором §c%operator%§7. Причина: §c%reason%");
            broadcastMsg = plugin.getMuteManager().formatMessage(broadcastMsg, finalTargetName, operatorName, timeDisplay, reason);
            Bukkit.broadcast(ChatFormatter.parse(broadcastMsg));
        }

        return true;
    }

    private boolean isTimeFormat(String str) {
        if (str.equalsIgnoreCase("perm")) return true;
        return str.matches("\\d+[dhms]?");
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                                 @NotNull String alias, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            String prefix = args[0].toLowerCase();
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.getName().toLowerCase().startsWith(prefix)) {
                    completions.add(player.getName());
                }
            }
        } else if (args.length == 2) {
            completions.add("10m");
            completions.add("30m");
            completions.add("1h");
            completions.add("6h");
            completions.add("12h");
            completions.add("1d");
            completions.add("7d");
            completions.add("30d");
            completions.add("perm");
            completions.add("-s");
        } else if (args.length == 3) {
            completions.add("-s");
        }

        return completions;
    }
}
