package com.loki.lovelocity.listeners;

import com.loki.lovelocity.LoVelocity;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.player.ServerConnectedEvent;

public class ServerSwitchListener {

    private final LoVelocity plugin;

    public ServerSwitchListener(LoVelocity plugin) {
        this.plugin = plugin;
    }

    @Subscribe(order = PostOrder.LAST)
    public void onServerConnected(ServerConnectedEvent event) {
        // Velocity doesn't show join/leave messages by default
        // They are handled by backend servers (Paper/Spigot)
    }

    @Subscribe(order = PostOrder.LAST)
    public void onDisconnect(DisconnectEvent event) {
        // Velocity doesn't show join/leave messages by default
        // They are handled by backend servers (Paper/Spigot)
    }
}
