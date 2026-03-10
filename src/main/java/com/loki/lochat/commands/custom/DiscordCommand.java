package com.loki.lochat.commands.custom;

import com.loki.lochat.LoChat;
import com.loki.lochat.utils.ChatFormatter;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

/**
 * Команда Discord сервера
 */
public class DiscordCommand implements CommandExecutor {

    private final LoChat plugin;

    public DiscordCommand(LoChat plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        String discordLink = plugin.getConfig().getString("discord.invite-link", "discord.gg/example");
        
        String message = String.format("""
            <gradient:#7289DA:#5865F2>═══════ Наш Discord ═══════</gradient>
            <gray>Присоединяйтесь к нашему сообществу!</gray>
            
            <click:open_url:'https://%s'><gradient:#87CEEB:#4682B4>🔗 %s</gradient></click>
            
            <gray>В Discord вы можете:</gray>
            <white>•</white> <gray>Общаться с игроками</gray>
            <white>•</white> <gray>Получать новости сервера</gray>
            <white>•</white> <gray>Участвовать в событиях</gray>
            <white>•</white> <gray>Получать поддержку</gray>
            """, discordLink, discordLink);

        sender.sendMessage(ChatFormatter.parse(message));
        return true;
    }
}