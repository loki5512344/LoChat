package com.loki.lochat.commands.base;

import com.loki.lochat.LoChat;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Базовый класс для команд, доступных только игрокам
 */
public abstract class PlayerCommand extends BaseCommand {
    
    public PlayerCommand(LoChat plugin) {
        super(plugin);
    }
    
    @Override
    protected final boolean executeCommand(@NotNull CommandSender sender, @NotNull Command command, 
                                         @NotNull String label, @NotNull String[] args) {
        if (!requirePlayer(sender)) {
            return true;
        }
        
        Player player = (Player) sender;
        return executePlayerCommand(player, command, label, args);
    }
    
    /**
     * Выполнить команду для игрока
     */
    protected abstract boolean executePlayerCommand(@NotNull Player player, @NotNull Command command, 
                                                  @NotNull String label, @NotNull String[] args);
}
