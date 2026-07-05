package com.loki.lochat.commands.base;

import com.loki.lochat.LoChat;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

/**
 * Базовый класс для административных команд
 */
public abstract class AdminCommand extends BaseCommand {
    
    private final String requiredPermission;
    
    public AdminCommand(LoChat plugin, String requiredPermission) {
        super(plugin);
        this.requiredPermission = requiredPermission;
    }
    
    @Override
    protected final boolean executeCommand(@NotNull CommandSender sender, @NotNull Command command, 
                                         @NotNull String label, @NotNull String[] args) {
        if (!requirePermission(sender, requiredPermission)) {
            return true;
        }
        
        return executeAdminCommand(sender, command, label, args);
    }
    
    /**
     * Выполнить административную команду
     */
    protected abstract boolean executeAdminCommand(@NotNull CommandSender sender, @NotNull Command command, 
                                                 @NotNull String label, @NotNull String[] args);
}
