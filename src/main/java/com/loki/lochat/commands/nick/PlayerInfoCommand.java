package com.loki.lochat.commands.nick;

import com.loki.lochat.LoChat;
import com.loki.lochat.utils.ChatFormatter;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PlayerInfoCommand implements CommandExecutor {

    private final LoChat plugin;

    public PlayerInfoCommand(LoChat plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length < 1) return false;

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(ChatFormatter.parse("&#CF6679Игрок не найден или не в сети"));
            return true;
        }

        sender.sendMessage(ChatFormatter.parse("&#7858E9▬▬▬▬▬ &#B798A8" + target.getName() + " &#7858E9▬▬▬▬▬"));
        sender.sendMessage(ChatFormatter.parse("&#9878C9❤ &fЗдоровье: &#7858E9" + Math.round(target.getHealth()) + "/20"));
        sender.sendMessage(ChatFormatter.parse("&#9878C9✦ &fРежим: &#7858E9" + target.getGameMode().name().toLowerCase()));
        sender.sendMessage(ChatFormatter.parse("&#9878C9◆ &fМир: &#7858E9" + target.getWorld().getName()));
        if (plugin.getGradientModule() != null) {
            sender.sendMessage(ChatFormatter.parse("&#9878C9⚡ &fГрадиент: " + plugin.getGradientModule().getFormattedName(target)));
        }
        return true;
    }
}
