package com.loki.lochat.core.service;

import com.loki.lochat.api.service.SpyService;
import com.loki.lochat.config.MessageConfig;
import com.loki.lochat.utils.ChatFormatter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Реализация сервиса шпионажа за чатом
 */
public class SpyServiceImpl implements SpyService {
    private final JavaPlugin plugin;
    private final MessageConfig messageConfig;
    private final Set<UUID> spyEnabled = ConcurrentHashMap.newKeySet();

    public SpyServiceImpl(JavaPlugin plugin, MessageConfig messageConfig) {
        this.plugin = plugin;
        this.messageConfig = messageConfig;
    }

    @Override
    public boolean toggleSpy(UUID player) {
        if (spyEnabled.contains(player)) {
            spyEnabled.remove(player);
            return false;
        } else {
            spyEnabled.add(player);
            return true;
        }
    }

    @Override
    public boolean isSpying(UUID player) {
        return spyEnabled.contains(player);
    }

    @Override
    public void broadcastPM(Player sender, Player receiver, String message) {
        String format = messageConfig.get("spy.format");

        for (UUID spyUuid : spyEnabled) {
            Player spy = Bukkit.getPlayer(spyUuid);
            if (spy != null && spy.isOnline() && !spy.equals(sender) && !spy.equals(receiver)) {
                spy.sendMessage(ChatFormatter.parse(format
                        .replace("{sender}", sender.getName())
                        .replace("{receiver}", receiver.getName())
                        .replace("{message}", message)));
            }
        }
    }

    @Override
    public void sendToSpies(Player sender, Component message, boolean isGlobal) {
        if (spyEnabled.isEmpty()) return;

        String chatType = isGlobal ? "Global" : "Local";
        String plainMessage = PlainTextComponentSerializer.plainText().serialize(message);
        String format = messageConfig.get("spy.chat-format",
                "§7[SPY] §e{type} §7{sender}: §f{message}");

        for (UUID spyUuid : spyEnabled) {
            Player spy = Bukkit.getPlayer(spyUuid);
            if (spy != null && spy.isOnline() && !spy.equals(sender)) {
                spy.sendMessage(ChatFormatter.parse(format
                        .replace("{type}", chatType)
                        .replace("{sender}", sender.getName())
                        .replace("{message}", plainMessage)));
            }
        }
    }

    @Override
    public void removeSpy(UUID player) {
        spyEnabled.remove(player);
    }
}
