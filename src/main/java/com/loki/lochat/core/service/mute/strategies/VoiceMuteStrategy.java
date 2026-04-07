package com.loki.lochat.core.service.mute.strategies;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

/**
 * Стратегия мута в голосовом чате (Simple Voice Chat)
 */
public class VoiceMuteStrategy implements MuteStrategy {

    private final JavaPlugin plugin;
    private final Object voicechatService;

    public VoiceMuteStrategy(JavaPlugin plugin) {
        this.plugin = plugin;
        this.voicechatService = initVoiceChat();
    }

    private Object initVoiceChat() {
        if (!Bukkit.getPluginManager().isPluginEnabled("voicechat")) {
            return null;
        }

        try {
            Class.forName("de.maxhenkel.voicechat.api.BukkitVoicechatService");
            Object service = Bukkit.getServicesManager().load(
                    Class.forName("de.maxhenkel.voicechat.api.BukkitVoicechatService")
            );
            if (service != null) {
                plugin.getLogger().info("Simple Voice Chat integration enabled!");
            }
            return service;
        } catch (ClassNotFoundException e) {
            plugin.getLogger().info("VoiceChat API not found, voice mute disabled");
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to hook into Simple Voice Chat: " + e.getMessage());
        }
        return null;
    }

    @Override
    public void apply(UUID playerUuid) {
        muteVoiceChat(playerUuid, true);
    }

    @Override
    public void remove(UUID playerUuid) {
        muteVoiceChat(playerUuid, false);
    }

    @Override
    public String getName() {
        return "Voice";
    }

    private void muteVoiceChat(UUID uuid, boolean mute) {
        if (voicechatService == null) return;

        try {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) {
                voicechatService.getClass()
                        .getMethod("getVoicechat")
                        .invoke(voicechatService)
                        .getClass()
                        .getMethod("setMuted", UUID.class, boolean.class)
                        .invoke(voicechatService.getClass().getMethod("getVoicechat").invoke(voicechatService),
                                player.getUniqueId(), mute);
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to " + (mute ? "mute" : "unmute") +
                    " player in voice chat: " + e.getMessage());
        }
    }
}
