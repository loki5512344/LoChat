package com.loki.lochat.core.service;

import com.loki.lochat.api.service.MessagingService;
import com.loki.lochat.config.MessageConfig;
import com.loki.lochat.utils.FoliaUtil;
import com.loki.lochat.utils.ChatFormatter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Объединённая реализация сервиса общения
 * Включает: PM, Spy, Ignore
 */
public class MessagingServiceImpl implements MessagingService {
    
    private final JavaPlugin plugin;
    private final MessageConfig messageConfig;
    private final File ignoreFile;
    
    // PM state
    private final Map<UUID, UUID> lastConversation = new ConcurrentHashMap<>();
    
    // Spy state
    private final Set<UUID> spyEnabled = ConcurrentHashMap.newKeySet();
    
    // Ignore state
    private final Map<UUID, Set<UUID>> ignoreMap = new ConcurrentHashMap<>();
    
    public MessagingServiceImpl(JavaPlugin plugin, MessageConfig messageConfig) {
        this.plugin = plugin;
        this.messageConfig = messageConfig;
        this.ignoreFile = new File(plugin.getDataFolder(), "ignores.yml");
        load();
    }
    
    // ========== PM Implementation ==========
    
    @Override
    public void setLastConversation(UUID player, UUID target) {
        lastConversation.put(player, target);
    }
    
    @Override
    public Optional<UUID> getLastConversation(UUID player) {
        return Optional.ofNullable(lastConversation.get(player));
    }
    
    @Override
    public void removeConversation(UUID player) {
        lastConversation.remove(player);
    }
    
    @Override
    public boolean hasConversation(UUID player) {
        return lastConversation.containsKey(player);
    }
    
    // ========== Spy Implementation ==========
    
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
    
    // ========== Ignore Implementation ==========
    
    @Override
    public boolean isIgnoring(UUID player, UUID target) {
        Set<UUID> ignored = ignoreMap.get(player);
        return ignored != null && ignored.contains(target);
    }
    
    @Override
    public boolean addIgnore(UUID player, UUID target) {
        Set<UUID> ignored = ignoreMap.computeIfAbsent(player, k -> ConcurrentHashMap.newKeySet());
        boolean added = ignored.add(target);
        if (added) saveAsync();
        return added;
    }
    
    @Override
    public boolean removeIgnore(UUID player, UUID target) {
        Set<UUID> ignored = ignoreMap.get(player);
        if (ignored == null) return false;
        boolean removed = ignored.remove(target);
        if (removed) saveAsync();
        return removed;
    }
    
    @Override
    public Set<UUID> getIgnoredPlayers(UUID player) {
        return ignoreMap.getOrDefault(player, Collections.emptySet());
    }
    
    @Override
    public int getIgnoredCount(UUID player) {
        Set<UUID> ignored = ignoreMap.get(player);
        return ignored == null ? 0 : ignored.size();
    }
    
    @Override
    public void clearIgnores(UUID player) {
        ignoreMap.remove(player);
        saveAsync();
    }
    
    // ========== Persistence ==========
    
    @Override
    public void load() {
        if (!ignoreFile.exists()) return;
        
        YamlConfiguration config = YamlConfiguration.loadConfiguration(ignoreFile);
        for (String key : config.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(key);
                List<String> ignoredList = config.getStringList(key);
                Set<UUID> ignored = ConcurrentHashMap.newKeySet();
                
                for (String ignoredStr : ignoredList) {
                    try {
                        ignored.add(UUID.fromString(ignoredStr));
                    } catch (Exception e) {
                        // Ignore invalid UUID
                    }
                }
                
                if (!ignored.isEmpty()) {
                    ignoreMap.put(uuid, ignored);
                }
            } catch (Exception e) {
                // Ignore invalid entry
            }
        }
    }
    
    @Override
    public void save() {
        YamlConfiguration config = new YamlConfiguration();
        
        for (Map.Entry<UUID, Set<UUID>> entry : ignoreMap.entrySet()) {
            List<String> ignoredList = new ArrayList<>();
            for (UUID ignored : entry.getValue()) {
                ignoredList.add(ignored.toString());
            }
            config.set(entry.getKey().toString(), ignoredList);
        }
        
        try {
            config.save(ignoreFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Не удалось сохранить игноры: " + e.getMessage());
        }
    }
    
    private void saveAsync() {
        FoliaUtil.runAsync(plugin, this::save);
    }
}
