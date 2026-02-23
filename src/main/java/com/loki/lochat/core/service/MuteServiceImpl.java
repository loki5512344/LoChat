package com.loki.lochat.core.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.loki.lochat.api.service.MuteService;
import com.loki.lochat.data.model.MuteData;
import com.loki.lochat.gradient.util.FoliaUtil;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Реализация сервиса мутов
 */
public class MuteServiceImpl implements MuteService {
    private final JavaPlugin plugin;
    private final Map<UUID, MuteData> mutes = new ConcurrentHashMap<>();
    private final Map<UUID, List<MuteData.MuteHistoryEntry>> history = new ConcurrentHashMap<>();
    private final File dataFile;
    private final File historyFile;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    
    public MuteServiceImpl(JavaPlugin plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "mutes.json");
        this.historyFile = new File(plugin.getDataFolder(), "mute-history.json");
        load();
        loadHistory();
    }
    
    @Override
    public void mute(UUID uuid, String playerName, long duration, String reason, String mutedBy) {
        long endTime = duration > 0 ? System.currentTimeMillis() + duration : 0;
        MuteData data = new MuteData(uuid, playerName, endTime, reason, mutedBy);
        mutes.put(uuid, data);
        
        // Добавляем в историю
        addToHistory(uuid, playerName, duration, reason, mutedBy);
        
        saveAsync();
    }
    
    @Override
    public boolean unmute(UUID uuid, String unmutedBy) {
        MuteData removed = mutes.remove(uuid);
        if (removed != null) {
            updateHistoryUnmute(uuid, unmutedBy);
            saveAsync();
            return true;
        }
        return false;
    }
    
    @Override
    public boolean isMuted(UUID uuid) {
        MuteData data = mutes.get(uuid);
        if (data == null) return false;
        
        if (data.getEndTime() > 0 && System.currentTimeMillis() > data.getEndTime()) {
            mutes.remove(uuid);
            saveAsync();
            return false;
        }
        return true;
    }
    
    @Override
    public MuteData getMuteData(UUID uuid) {
        if (!isMuted(uuid)) return null;
        return mutes.get(uuid);
    }
    
    @Override
    public long getRemainingTime(UUID uuid) {
        MuteData data = mutes.get(uuid);
        if (data == null) return 0;
        if (data.getEndTime() == 0) return -1; // Перманентный
        return Math.max(0, data.getEndTime() - System.currentTimeMillis());
    }
    
    @Override
    public String formatTime(long millis) {
        if (millis <= 0) return "0с";
        
        long seconds = millis / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        
        if (days > 0) {
            return days + "д " + (hours % 24) + "ч";
        } else if (hours > 0) {
            return hours + "ч " + (minutes % 60) + "м";
        } else if (minutes > 0) {
            return minutes + "м " + (seconds % 60) + "с";
        } else {
            return seconds + "с";
        }
    }
    
    @Override
    public long parseTime(String timeStr) {
        if (timeStr == null || timeStr.isEmpty()) return 0;
        
        timeStr = timeStr.toLowerCase();
        long multiplier = 1000; // базово в миллисекундах
        
        try {
            if (timeStr.endsWith("d")) {
                return Long.parseLong(timeStr.replace("d", "")) * 24 * 60 * 60 * multiplier;
            } else if (timeStr.endsWith("h")) {
                return Long.parseLong(timeStr.replace("h", "")) * 60 * 60 * multiplier;
            } else if (timeStr.endsWith("m")) {
                return Long.parseLong(timeStr.replace("m", "")) * 60 * multiplier;
            } else if (timeStr.endsWith("s")) {
                return Long.parseLong(timeStr.replace("s", "")) * multiplier;
            } else {
                return Long.parseLong(timeStr) * 60 * multiplier;
            }
        } catch (NumberFormatException e) {
            return -1;
        }
    }
    
    @Override
    public long getMaxDuration(Player player) {
        if (player.hasPermission("lochat.mute.dur.perm")) {
            return 0; // Перманентный
        }
        
        String[] durations = {"30d", "14d", "7d", "3d", "1d", "12h", "6h", "1h", "30m", "10m"};
        for (String dur : durations) {
            if (player.hasPermission("lochat.mute.dur." + dur)) {
                return parseTime(dur);
            }
        }
        
        return -1; // Нет прав
    }
    
    @Override
    public boolean canMuteForDuration(Player player, long duration) {
        if (player.hasPermission("lochat.mute.dur.perm")) {
            return true;
        }
        
        if (duration == 0) {
            return false;
        }
        
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
        for (List<MuteData.MuteHistoryEntry> playerHistory : history.values()) {
            for (MuteData.MuteHistoryEntry entry : playerHistory) {
                if (entry.mutedBy != null && entry.mutedBy.equalsIgnoreCase(operatorName)) {
                    result.add(entry);
                }
            }
        }
        result.sort((a, b) -> Long.compare(b.mutedAt, a.mutedAt));
        return result;
    }
    
    @Override
    public UUID getUUIDByName(String name) {
        for (Map.Entry<UUID, List<MuteData.MuteHistoryEntry>> entry : history.entrySet()) {
            for (MuteData.MuteHistoryEntry historyEntry : entry.getValue()) {
                if (historyEntry.playerName != null && historyEntry.playerName.equalsIgnoreCase(name)) {
                    return entry.getKey();
                }
            }
        }
        return null;
    }
    
    @Override
    public Map<UUID, MuteData> getActiveMutes() {
        mutes.entrySet().removeIf(entry -> {
            MuteData data = entry.getValue();
            return data.getEndTime() > 0 && System.currentTimeMillis() > data.getEndTime();
        });
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
        try {
            if (!dataFile.getParentFile().exists()) {
                dataFile.getParentFile().mkdirs();
            }
            
            Map<String, MuteData> toSave = new ConcurrentHashMap<>();
            mutes.forEach((uuid, data) -> toSave.put(uuid.toString(), data));
            
            try (Writer writer = new FileWriter(dataFile)) {
                gson.toJson(toSave, writer);
            }
        } catch (IOException e) {
            plugin.getLogger().warning("Ошибка сохранения мутов: " + e.getMessage());
        }
    }
    
    private void load() {
        if (!dataFile.exists()) return;
        
        try (Reader reader = new FileReader(dataFile)) {
            Type type = new TypeToken<Map<String, MuteData>>(){}.getType();
            Map<String, MuteData> loaded = gson.fromJson(reader, type);
            if (loaded != null) {
                loaded.forEach((key, value) -> {
                    try {
                        mutes.put(UUID.fromString(key), value);
                    } catch (IllegalArgumentException ignored) {}
                });
            }
        } catch (IOException e) {
            plugin.getLogger().warning("Ошибка загрузки мутов: " + e.getMessage());
        }
    }
    
    private void loadHistory() {
        if (!historyFile.exists()) return;
        
        try (Reader reader = new FileReader(historyFile)) {
            Type type = new TypeToken<Map<String, List<MuteData.MuteHistoryEntry>>>(){}.getType();
            Map<String, List<MuteData.MuteHistoryEntry>> loaded = gson.fromJson(reader, type);
            if (loaded != null) {
                loaded.forEach((key, value) -> {
                    try {
                        history.put(UUID.fromString(key), new ArrayList<>(value));
                    } catch (IllegalArgumentException ignored) {}
                });
            }
        } catch (IOException e) {
            plugin.getLogger().warning("Ошибка загрузки истории мутов: " + e.getMessage());
        }
    }
    
    private void saveHistory() {
        try {
            if (!historyFile.getParentFile().exists()) {
                historyFile.getParentFile().mkdirs();
            }
            
            Map<String, List<MuteData.MuteHistoryEntry>> toSave = new ConcurrentHashMap<>();
            history.forEach((uuid, data) -> toSave.put(uuid.toString(), data));
            
            try (Writer writer = new FileWriter(historyFile)) {
                gson.toJson(toSave, writer);
            }
        } catch (IOException e) {
            plugin.getLogger().warning("Ошибка сохранения истории мутов: " + e.getMessage());
        }
    }
    
    private void addToHistory(UUID uuid, String playerName, long duration, String reason, String mutedBy) {
        List<MuteData.MuteHistoryEntry> playerHistory = history.computeIfAbsent(uuid, k -> new ArrayList<>());
        playerHistory.add(new MuteData.MuteHistoryEntry(
                playerName,
                duration,
                reason,
                mutedBy,
                System.currentTimeMillis(),
                false,
                null,
                0
        ));
        saveHistoryAsync();
    }
    
    private void updateHistoryUnmute(UUID uuid, String unmutedBy) {
        List<MuteData.MuteHistoryEntry> playerHistory = history.get(uuid);
        if (playerHistory != null && !playerHistory.isEmpty()) {
            for (int i = playerHistory.size() - 1; i >= 0; i--) {
                MuteData.MuteHistoryEntry entry = playerHistory.get(i);
                if (!entry.unmuted) {
                    entry.unmuted = true;
                    entry.unmutedBy = unmutedBy;
                    entry.unmutedAt = System.currentTimeMillis();
                    break;
                }
            }
        }
        saveHistoryAsync();
    }
    
    private void saveHistoryAsync() {
        FoliaUtil.runAsync(plugin, this::saveHistory);
    }
    
    private void saveAsync() {
        FoliaUtil.runAsync(plugin, () -> {
            save();
            saveHistory();
        });
    }
}
