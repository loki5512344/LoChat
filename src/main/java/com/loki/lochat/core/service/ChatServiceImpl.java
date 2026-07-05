package com.loki.lochat.core.service;

import com.loki.lochat.api.service.ChatService;
import com.loki.lochat.renderer.EnhancedChatRenderer;
import com.loki.lochat.utils.format.ChatFormatter;
import com.loki.lochat.utils.persistence.FilePersistence;
import com.loki.lochat.utils.platform.FoliaUtil;
import com.loki.lochat.utils.player.DistanceUtil;

import net.kyori.adventure.text.Component;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Реализация сервиса чата.
 * globalChatDisabled персистируется в data/players.yml.
 */
public class ChatServiceImpl implements ChatService {

    private final JavaPlugin plugin;
    private final Set<UUID> globalChatDisabled = ConcurrentHashMap.newKeySet();

    public ChatServiceImpl(JavaPlugin plugin) {
        this.plugin = plugin;
        loadDisabled();
    }

    // ──────────────────────────────────────────────────────────────────────────
    //  Chat
    // ──────────────────────────────────────────────────────────────────────────

    @Override
    public void sendGlobalMessage(Player sender, Object message) {
        Component msg = toComponent(message);
        EnhancedChatRenderer renderer = new EnhancedChatRenderer(plugin, true);
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (!isGlobalChatDisabled(p.getUniqueId())) {
                p.sendMessage(renderer.render(sender, sender.displayName(), msg, p));
            }
        }
    }

    @Override
    public void sendLocalMessage(Player sender, Object message) {
        int radius = plugin.getConfig().getInt("chat.local.radius", 100);
        Component msg = toComponent(message);
        EnhancedChatRenderer renderer = new EnhancedChatRenderer(plugin, false);

        int count = 0;
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (DistanceUtil.isInRange(sender, p, radius)) {
                p.sendMessage(renderer.render(sender, sender.displayName(), msg, p));
                count++;
            }
        }
        if (count <= 1) {
            sender.sendMessage(ChatFormatter.parse(
                    plugin.getConfig().getString("messages.local.nobody-heard",
                            "<color:#9878C9>Вас никто не услышал — рядом нет игроков</color>")));
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    //  Toggle global chat — с персистенцией
    // ──────────────────────────────────────────────────────────────────────────

    @Override
    public boolean toggleGlobalChat(UUID player) {
        boolean nowEnabled;
        if (globalChatDisabled.contains(player)) {
            globalChatDisabled.remove(player);
            nowEnabled = true;
        } else {
            globalChatDisabled.add(player);
            nowEnabled = false;
        }
        FoliaUtil.runAsync(plugin, this::saveDisabled);
        return nowEnabled;
    }

    @Override
    public boolean isGlobalChatDisabled(UUID player) {
        return globalChatDisabled.contains(player);
    }

    // ──────────────────────────────────────────────────────────────────────────
    //  Персистенция
    // ──────────────────────────────────────────────────────────────────────────

    private void loadDisabled() {
        FileConfiguration cfg = FilePersistence.loadYaml(plugin, "data/players.yml");
        for (String uuidStr : cfg.getStringList("global-chat-disabled")) {
            try {
                globalChatDisabled.add(UUID.fromString(uuidStr));
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    private void saveDisabled() {
        YamlConfiguration cfg = new YamlConfiguration();
        cfg.set("global-chat-disabled",
                globalChatDisabled.stream().map(UUID::toString).toList());
        FilePersistence.saveYaml(plugin, "data/players.yml", cfg);
    }

    private Component toComponent(Object message) {
        return message instanceof Component c ? c : ChatFormatter.parse(message.toString());
    }
}
