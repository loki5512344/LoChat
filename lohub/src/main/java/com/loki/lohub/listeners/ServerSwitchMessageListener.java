package com.loki.lohub.listeners;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import com.loki.lohub.LoHub;
import com.loki.lohub.utils.TextUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;

public class ServerSwitchMessageListener implements PluginMessageListener {

    private final LoHub plugin;
    private static final String CHANNEL = "lohub:switch";

    public ServerSwitchMessageListener(LoHub plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onPluginMessageReceived(@NotNull String channel, @NotNull Player player, byte @NotNull [] message) {
        if (!channel.equals(CHANNEL)) {
            return;
        }

        ByteArrayDataInput in = ByteStreams.newDataInput(message);
        String subChannel = in.readUTF();

        if (subChannel.equals("ServerSwitch")) {
            String playerName = in.readUTF();
            String fromServer = in.readUTF();
            String toServer = in.readUTF();

            handleServerSwitch(playerName, fromServer, toServer);
        }
    }

    private void handleServerSwitch(String playerName, String fromServer, String toServer) {
        if (!plugin.getConfig().getBoolean("server_switch_messages.enabled", true)) {
            return;
        }

        String messageTemplate = plugin.getConfig().getString("server_switch_messages.switch_message",
                "&#FFD700%player% &#AAAAAA connected to &#6BCB77%server%");

        String serverDisplayName = getServerDisplayName(toServer);
        String formattedMessage = messageTemplate
                .replace("%player%", playerName)
                .replace("%server%", serverDisplayName);

        Component component = Component.text(TextUtil.colorize(formattedMessage));
        Bukkit.broadcast(component);
    }

    private String getServerDisplayName(String serverName) {
        String path = "server_switch_messages.server_names." + serverName;
        return plugin.getConfig().getString(path, serverName);
    }

    public static String getChannel() {
        return CHANNEL;
    }
}
