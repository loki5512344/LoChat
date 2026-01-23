package com.loki.lochat.managers;

import com.loki.lochat.LoChat;
import com.loki.lochat.utils.ChatFormatter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SpyManager {

    private final LoChat plugin;
    private final Set<UUID> spyEnabled = ConcurrentHashMap.newKeySet();

    public SpyManager(LoChat plugin) {
        this.plugin = plugin;
    }

    public boolean toggleSpy(UUID player) {
        if (spyEnabled.contains(player)) {
            spyEnabled.remove(player);
            return false;
        } else {
            spyEnabled.add(player);
            return true;
        }
    }

    public boolean isSpying(UUID player) {
        return spyEnabled.contains(player);
    }

    public void broadcastPM(Player sender, Player receiver, String message) {
        String format = plugin.getMessageConfig().get("spy.format");

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

    public void sendToSpies(Player sender, net.kyori.adventure.text.Component message, boolean isGlobal) {
        if (spyEnabled.isEmpty()) return;
        
        String chatType = isGlobal ? "Global" : "Local";
        String plainMessage = net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText().serialize(message);
        String format = plugin.getMessageConfig().get("spy.chat-format", 
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

    public void removeSpy(UUID player) {
        spyEnabled.remove(player);
    }
}
