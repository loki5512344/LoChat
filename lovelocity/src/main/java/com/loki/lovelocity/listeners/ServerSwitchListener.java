package com.loki.lovelocity.listeners;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.loki.lovelocity.LoVelocity;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;

import java.util.Optional;

public class ServerSwitchListener {

    private final LoVelocity plugin;
    private static final String CHANNEL = "lohub:switch";

    public ServerSwitchListener(LoVelocity plugin) {
        this.plugin = plugin;
    }

    @Subscribe(order = PostOrder.LAST)
    public void onServerConnected(ServerConnectedEvent event) {
        Player player = event.getPlayer();
        RegisteredServer toServer = event.getServer();
        Optional<RegisteredServer> previousServer = event.getPreviousServer();

        // If player is switching from another server (not first join)
        if (previousServer.isPresent()) {
            String fromServerName = previousServer.get().getServerInfo().getName();
            String toServerName = toServer.getServerInfo().getName();
            
            // Send plugin message to hub server about server switch
            sendServerSwitchMessage(player, fromServerName, toServerName);
            
            plugin.getLogger().info(player.getUsername() + " switched from " + fromServerName + " to " + toServerName);
        }
    }

    @Subscribe(order = PostOrder.LAST)
    public void onDisconnect(DisconnectEvent event) {
        // Player left the network - handled by backend server
    }

    private void sendServerSwitchMessage(Player player, String fromServer, String toServer) {
        // Create plugin message
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("ServerSwitch");
        out.writeUTF(player.getUsername());
        out.writeUTF(fromServer);
        out.writeUTF(toServer);

        byte[] data = out.toByteArray();

        // Send to hub server (assuming hub server name is "hub")
        plugin.getServer().getServer("hub").ifPresent(hubServer -> {
            hubServer.sendPluginMessage(() -> CHANNEL, data);
        });
    }
}
