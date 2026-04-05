package com.loki.lochat.core.service;

import com.loki.lochat.api.service.MuteService;
import com.loki.lochat.data.model.MuteData;
import com.loki.lochat.utils.FoliaUtil;
import com.loki.lochat.utils.TimeFormatter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

/**
 * Сервис мутов с интеграцией Simple Voice Chat
 */
public class MuteServiceImpl implements MuteService {

    private final JavaPlugin plugin;
    private final Map<UUID, MuteData> mutes = new HashMap<>();
    private final Map<UUID, List<MuteData.MuteHistoryEntry>> history = new HashMap<>();
    private Object voicechatService; // Используем Object для опциональной зависимости

    public MuteServiceImpl(JavaPlugin plugin) {
        this.plugin = plugin;
        initVoiceChat();
        load();
    }

    private void initVoiceChat() {
        if (Bukkit.getPluginManager().isPluginEnabled("voicechat")) {
            try {
                // Проверяем наличие класса через рефлексию
                Class.forName("de.maxhenkel.voicechat.api.BukkitVoicechatService");
                voicechatService = Bukkit.getServicesManager().load(
                    Class.forName("de.maxhenkel.voicechat.api.BukkitVoicechatService")
                );
                if (voicechatService != null) {
                    plugin.getLogger().info("Simple Voice Chat integration enabled!");
                }
            } catch (ClassNotFoundException e) {
                plugin.getLogger().info("VoiceChat API not found, voice mute disabled");
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to hook into Simple Voice Chat: " + e.getMessage());
            }
        }
    }

    @Override
    public void mute(UUID uuid, String playerName, long duration, String reason, String mutedBy) {
        if (uuid == null || playerName == null || playerName.isEmpty()) {
            plugin.getLogger().warning("Attempted to mute player with invalid data");
            return;
        }
        
        long endTime = duration > 0 ? System.currentTimeMillis() + duration : 0;
        MuteData data = new MuteData(uuid, playerName, endTime, reason, mutedBy);
        
        mutes.put(uuid, data);
        addToHistory(uuid, playerName, duration, reason, mutedBy);
        
        // Мутим в голосовом чате
        muteVoiceChat(uuid, true);
        
        saveAsync();
        
        plugin.getLogger().info("Player " + playerName + " muted by " + mutedBy + 
                " for " + (duration > 0 ? formatTime(duration) : "permanent") + 
                (reason != null ? " (reason: " + reason + ")" : ""));
    }

    @Override
    public boolean unmute(UUID uuid, String unmutedBy) {
        if (uuid == null) {
            plugin.getLogger().warning("Attempted to unmute player with null UUID");
            return false;
        }
        
        MuteData removed = mutes.remove(uuid);
        if (removed != null) {
            updateUnmuteInHistory(uuid, unmutedBy);
            
            // Размутим в голосовом чате
            muteVoiceChat(uuid, false);
            
            saveAsync();
            plugin.getLogger().info("Player " + removed.getPlayerName() + " unmuted by " + unmutedBy);
            return true;
        }
        return false;
    }

    @Override
    public boolean isMuted(UUID uuid) {
        if (uuid == null) return false;

        MuteData data = mutes.get(uuid);
        if (data == null) return false;

        if (!isExpired(data)) return true;

        mutes.remove(uuid);
        muteVoiceChat(uuid, false);
        saveAsync();
        plugin.getLogger().info("Mute expired for player " + data.getPlayerName());
        return false;
    }

    @Override
    public MuteData getMuteData(UUID uuid) {
        return isMuted(uuid) ? mutes.get(uuid) : null;
    }

    @Override
    public long getRemainingTime(UUID uuid) {
        MuteData data = mutes.get(uuid);
        if (data == null) return 0;
        if (data.getEndTime() == 0) return -1;
        return Math.max(0, data.getEndTime() - System.currentTimeMillis());
    }

    @Override
    public String formatTime(long millis) {
        return TimeFormatter.format(millis);
    }

    @Override
    public long parseTime(String timeStr) {
        return TimeFormatter.parse(timeStr);
    }

    @Override
    public long getMaxDuration(Player player) {
        if (player.hasPermission("lochat.mute.dur.perm")) return 0;

        String[] durations = {"30d", "14d", "7d", "3d", "1d", "12h", "6h", "1h", "30m", "10m"};
        for (String dur : durations) {
            if (player.hasPermission("lochat.mute.dur." + dur)) {
                return parseTime(dur);
            }
        }
        return -1;
    }

    @Override
    public boolean canMuteForDuration(Player player, long duration) {
        if (player.hasPermission("lochat.mute.dur.perm")) return true;
        if (duration == 0) return false;

        long maxDuration = getMaxDuration(player);
        if (maxDuration == -1) return false;
        if (maxDuration == 0) return true;

        return duration <= maxDuration;
    }

    @Override
    public List<MuteData.MuteHistoryEntry> getPlayerHistory(UUID uuid) {
        return history.getOrDefault(uuid, new ArrayList<>());
    }

    @Override
    public List<MuteData.MuteHistoryEntry> getMutesByOperator(String operatorName) {
        List<MuteData.MuteHistoryEntry> result = new ArrayList<>();
        history.values().forEach(entries -> 
            entries.stream()
                .filter(e -> e.mutedBy != null && e.mutedBy.equalsIgnoreCase(operatorName))
                .forEach(result::add)
        );
        result.sort((a, b) -> Long.compare(b.mutedAt, a.mutedAt));
        return result;
    }

    @Override
    public UUID getUUIDByName(String name) {
        for (Map.Entry<UUID, List<MuteData.MuteHistoryEntry>> entry : history.entrySet()) {
            for (MuteData.MuteHistoryEntry e : entry.getValue()) {
                if (e.playerName != null && e.playerName.equalsIgnoreCase(name)) {
                    return entry.getKey();
                }
            }
        }
        return null;
    }

    @Override
    public Map<UUID, MuteData> getActiveMutes() {
        mutes.entrySet().removeIf(e -> isExpired(e.getValue()));
        return new HashMap<>(mutes);
    }

    @Override
    public String formatMessage(String message, String player, String operator, String duration, String reason) {
        return message
                .replace("%player%", player != null ? player : "")
                .replace("%operator%", operator != null ? operator : "")
                .replace("%duration%", duration != null ? duration : "")
                .replace("%reason%", reason != null ? reason : "");
    }

    @Override
    public void save() {
        // TODO: Implement save to file
    }

    // ========== Voice Chat Integration ==========

    private void muteVoiceChat(UUID uuid, boolean mute) {
        if (voicechatService == null) return;

        try {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) {
                // Используем рефлексию для вызова метода
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

    // ========== Private Methods ==========

    private boolean isExpired(MuteData data) {
        return data.getEndTime() > 0 && System.currentTimeMillis() > data.getEndTime();
    }

    private void addToHistory(UUID uuid, String playerName, long duration, String reason, String mutedBy) {
        List<MuteData.MuteHistoryEntry> entries = history.computeIfAbsent(uuid, k -> new ArrayList<>());
        entries.add(new MuteData.MuteHistoryEntry(
            playerName, duration, reason, mutedBy, System.currentTimeMillis(), false, null, 0
        ));
    }

    private void updateUnmuteInHistory(UUID uuid, String unmutedBy) {
        List<MuteData.MuteHistoryEntry> entries = history.get(uuid);
        if (entries != null && !entries.isEmpty()) {
            MuteData.MuteHistoryEntry last = entries.get(entries.size() - 1);
            if (!last.unmuted) {
                entries.set(entries.size() - 1, new MuteData.MuteHistoryEntry(
                    last.playerName, last.duration, last.reason, last.mutedBy, last.mutedAt, 
                    true, unmutedBy, System.currentTimeMillis()
                ));
            }
        }
    }

    private void load() {
        // TODO: Implement load from file
    }

    private void saveAsync() {
        FoliaUtil.runAsync(plugin, this::save);
    }
}
