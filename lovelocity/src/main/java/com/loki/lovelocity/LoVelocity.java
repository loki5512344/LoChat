package com.loki.lovelocity;

import com.google.inject.Inject;
import com.loki.lovelocity.commands.HubCommand;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import org.slf4j.Logger;

import java.nio.file.Path;

@Plugin(
        id = "lovelocity",
        name = "LoVelocity",
        version = "1.5.5",
        description = "Velocity integration for LoNetwork",
        authors = {"loki"}
)
public class LoVelocity {

    private final ProxyServer server;
    private final Logger logger;
    private final Path dataDirectory;

    @Inject
    public LoVelocity(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
        this.server = server;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        logger.info("LoVelocity loaded!");
        logger.info("Version: 1.5.5");

        registerCommands();
        registerListeners();
    }

    private void registerCommands() {
        CommandMeta hubMeta = server.getCommandManager().metaBuilder("hub")
                .aliases("lobby")
                .plugin(this)
                .build();

        server.getCommandManager().register(hubMeta, new HubCommand(this));

        logger.info("Commands registered: /hub, /lobby");
    }

    private void registerListeners() {
        server.getEventManager().register(this, new com.loki.lovelocity.listeners.ServerSwitchListener(this));
        logger.info("Listeners registered");
    }

    public ProxyServer getServer() {
        return server;
    }

    public Logger getLogger() {
        return logger;
    }

    public Path getDataDirectory() {
        return dataDirectory;
    }
}
