package com.loki.lochat.commands.moderation;

import com.loki.lochat.LoChat;
import com.loki.lochat.api.service.MuteService;
import com.loki.lochat.config.HardcodedMessages;
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
 * /mute <ник> [время] [-s] [причина]
 */
public class MuteCommand implements CommandExecutor, TabCompleter {

    private final LoChat plugin;
    private final MuteService muteService;

    public MuteCommand(LoChat plugin) {
        this.plugin = plugin;
        this.muteService = plugin.getServiceRegistry().get(MuteService.class);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {

        HardcodedMessages hm = plugin.getConfigManager().getHardcodedMessages();

        if (!sender.hasPermission("lochat.mute")) {
            sender.sendMessage(plugin.getMessageConfig().getComponent("errors.no-permission"));
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage(ChatFormatter.parse(hm.getMuteUsage()));
            sender.sendMessage(ChatFormatter.parse(hm.getMuteTimeHelp()));
            sender.sendMessage(ChatFormatter.parse(hm.getMuteSilentHelp()));
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

        if (silent && !sender.hasPermission("lochat.mute.silent")) {
            sender.sendMessage(ChatFormatter.parse(hm.getNoSilentPermission()));
            return true;
        }

        String reason = reasonBuilder.length() > 0 ? reasonBuilder.toString() : hm.getDefaultMuteReason();

        // Длительность
        long duration;
        if (timeStr == null || timeStr.isEmpty()) {
            duration = sender instanceof Player p ? muteService.getMaxDuration(p) : 0;
            if (duration == -1) duration = muteService.parseTime(hm.getDefaultMuteDuration());
        } else if (timeStr.equalsIgnoreCase("perm") || timeStr.equals("0")) {
            duration = 0;
        } else {
            duration = muteService.parseTime(timeStr);
            if (duration < 0) { sender.sendMessage(ChatFormatter.parse(hm.getMuteTimeHelp())); return true; }
        }

        if (sender instanceof Player p && !muteService.canMuteForDuration(p, duration)) {
            long max = muteService.getMaxDuration(p);
            sender.sendMessage(ChatFormatter.parse("&#CF6679Нельзя мутить на такой срок! &#B798A8Максимум: &#7858E9" +
                    (max == 0 ? "навсегда" : muteService.formatTime(max))));
            return true;
        }

        UUID targetUUID = com.loki.lochat.utils.PlayerUtil.findPlayerUUID(targetName);
        if (targetUUID == null) { sender.sendMessage(plugin.getMessageConfig().getComponent("errors.player-not-found")); return true; }
        if (muteService.isMuted(targetUUID)) { sender.sendMessage(ChatFormatter.parse("&#CF6679Игрок уже замучен!")); return true; }

        Player target = Bukkit.getPlayer(targetUUID);
        String finalName = com.loki.lochat.utils.PlayerUtil.getPlayerName(targetUUID);
        String op = sender.getName();

        muteService.mute(targetUUID, finalName, duration, reason, op);
        String timeDisplay = duration == 0 ? "навсегда" : muteService.formatTime(duration);

        sender.sendMessage(ChatFormatter.parse("&#9878C9Игрок &#7858E9" + finalName + " &#9878C9замучен на &#7858E9" + timeDisplay));
        sender.sendMessage(ChatFormatter.parse("&#B798A8Причина: &f" + reason));

        if (target != null) {
            String youMsg = duration == 0
                    ? hm.getMutedPermanent().replace("{reason}", reason)
                    : hm.getMutedMessage().replace("{reason}", reason).replace("{time}", timeDisplay);
            target.sendMessage(ChatFormatter.parse(youMsg));
        }

        if (silent) {
            String msgText = hm.getPlayerMutedSilent().replace("{player}", finalName).replace("{time}", timeDisplay);
            for (Player p : Bukkit.getOnlinePlayers()) if (p.hasPermission("lochat.mute.see-silent")) p.sendMessage(ChatFormatter.parse(msgText));
        } else {
            String broadcastMsg = (duration == 0 ? hm.getPlayerMutedPermanent() : hm.getPlayerMuted())
                    .replace("{player}", finalName).replace("{time}", timeDisplay).replace("{reason}", reason);
            Bukkit.broadcast(ChatFormatter.parse(broadcastMsg));
        }

        return true;
    }

    private boolean isTimeFormat(String s) {
        return s.equalsIgnoreCase("perm") || s.matches("\\d+[dhms]?");
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                                @NotNull String alias, @NotNull String[] args) {
        List<String> c = new ArrayList<>();
        if (args.length == 1) {
            for (Player p : Bukkit.getOnlinePlayers())
                if (p.getName().toLowerCase().startsWith(args[0].toLowerCase())) c.add(p.getName());
        } else if (args.length == 2) {
            c.addAll(List.of("10m", "30m", "1h", "6h", "12h", "1d", "7d", "30d", "perm", "-s"));
        } else if (args.length == 3) {
            c.add("-s");
        }
        return c;
    }
}
