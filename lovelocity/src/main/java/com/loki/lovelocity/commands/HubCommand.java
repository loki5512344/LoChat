package com.loki.lovelocity.commands;

import com.loki.lovelocity.LoVelocity;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class HubCommand implements SimpleCommand {
    
    private final LoVelocity plugin;
    private final List<String> hubServers = List.of("hub", "lobby");
    
    public HubCommand(LoVelocity plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public void execute(Invocation invocation) {
        if (!(invocation.source() instanceof Player player)) {
            invocation.source().sendMessage(Component.text("This command is only for players!", NamedTextColor.RED));
            return;
        }
        
        Optional<RegisteredServer> currentServer = player.getCurrentServer().map(s -> s.getServer());
        
        if (currentServer.isEmpty()) {
            player.sendMessage(Component.text("Failed to determine current server!", NamedTextColor.RED));
            return;
        }
        
        String currentServerName = currentServer.get().getServerInfo().getName().toLowerCase();
        
        if (isHubServer(currentServerName)) {
            player.sendMessage(Component.text("You are already on the hub!", NamedTextColor.YELLOW));
            return;
        }
        
        Optional<RegisteredServer> hubServer = findHubServer();
        
        if (hubServer.isEmpty()) {
            player.sendMessage(Component.text("Hub server not found!", NamedTextColor.RED));
            plugin.getLogger().warn("Hub server not found! Available servers: " + 
                plugin.getServer().getAllServers().stream()
                    .map(s -> s.getServerInfo().getName())
                    .toList());
            return;
        }
        
        player.sendMessage(Component.text("Teleporting to hub...", NamedTextColor.GREEN));
        
        player.createConnectionRequest(hubServer.get()).fireAndForget();
    }
    
    private boolean isHubServer(String serverName) {
        return hubServers.stream().anyMatch(hub -> serverName.contains(hub));
    }
    
    private Optional<RegisteredServer> findHubServer() {
        return plugin.getServer().getAllServers().stream()
            .filter(server -> isHubServer(server.getServerInfo().getName().toLowerCase()))
            .findFirst();
    }
    
    @Override
    public CompletableFuture<List<String>> suggestAsync(Invocation invocation) {
        return CompletableFuture.completedFuture(List.of());
    }
    
    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("lovelocity.hub");
    }
}
