package com.loki.lochat.core.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.loki.lochat.api.service.MuteService;
import com.loki.lochat.data.model.MuteData;
import com.loki.lochat.util.FoliaUtil;
import com.loki.lochat.utils.TimeFormatter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class MuteServiceImpl implements MuteService {

    private final JavaPlugin plugin;
    private final Map<UUID, MuteData> mutes = new ConcurrentHashMap<>();
    private final MuteHistoryManager historyManager;
    private final File dataFile;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public MuteServiceImpl(JavaPlugin plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "mutes.json");
        this.historyManager = new MuteHistoryManager(new File(plugin.getDataFolder(), "mute-history.json"));
        load();
    }

    @Override
    public void mute(UUID uuid, String playerName, long duration, String reason, String mutedBy) {
        if (uuid == null) {
            plugin.getLogger().warning("Attempted to mute player with null UUID");
            return;
        }
        
        if (playerName == null || playerName.isEmpty()) {
            plugin.getLogger().warning("Attempted to mute player with null or empty name");
            return;
        }
        
        long endTime = duration > 0 ? System.currentTimeMillis() + duration : 0;
        MuteData data = new MuteData(uuid, playerName, endTime, reason, mutedBy);
        mutes.put(uuid, data);

        historyManager.addEntry(uuid, playerName, duration, reason, mutedBy);
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
            historyManager.updateUnmute(uuid, unmutedBy);
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

        // Атомарно удаляем только если значение не изменилось между get() и remove().
        // ConcurrentHashMap.remove(key, value) удаляет запись лишь когда она точно та же.
        boolean removed = mutes.remove(uuid, data);
        if (removed) {
            saveAsync();
            plugin.getLogger().info("Mute expired for player " + data.getPlayerName());
        }
        return false;
    }

    @Override
    public MuteData getMuteData(UUID uuid) {
        return isMuted(uuid) ? mutes.get(uuid) : null;
    }

    @Override
    public long getRemainingTime(UUID uuid) {
        MuteData data = mutes.get(uuid);
        if (data == null) {
            return 0;
        }
        if (data.getEndTime() == 0) {
            return -1;
        }
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
        if (player.hasPermission("lochat.mute.dur.perm")) {
            return 0;
        }

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
        if (player.hasPermission("lochat.mute.dur.perm")) {
            return true;
        }

        if (duration == 0) {
            return false;
        }

        long maxDuration = getMaxDuration(player);
        if (maxDuration == -1) {
            return false;
        }
        if (maxDuration == 0) {
            return true;
        }

        return duration <= maxDuration;
    }

    @Override
    public List<MuteData.MuteHistoryEntry> getPlayerHistory(UUID uuid) {
        return historyManager.getHistory(uuid);
    }

    @Override
    public List<MuteData.MuteHistoryEntry> getMutesByOperator(String operatorName) {
        List<MuteData.MuteHistoryEntry> result = new ArrayList<>();
        mutes.keySet().forEach(uuid -> {
            historyManager.getHistory(uuid).stream()
                    .filter(entry -> entry.mutedBy != null && entry.mutedBy.equalsIgnoreCase(operatorName))
                    .forEach(result::add);
        });
        result.sort((a, b) -> Long.compare(b.mutedAt, a.mutedAt));
        return result;
    }

    @Override
    public UUID getUUIDByName(String name) {
        for (UUID uuid : mutes.keySet()) {
            for (MuteData.MuteHistoryEntry entry : historyManager.getHistory(uuid)) {
                if (entry.playerName != null && entry.playerName.equalsIgnoreCase(name)) {
                    return uuid;
                }
            }
        }
        return null;
    }

    @Override
    public Map<UUID, MuteData> getActiveMutes() {
        mutes.entrySet().removeIf(entry -> isExpired(entry.getValue()));
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
            ensureDataFolderExists();
            saveMutes();
            historyManager.save();
        } catch (IOException e) {
            plugin.getLogger().warning("Error saving mutes: " + e.getMessage());
        }
    }

    private boolean isExpired(MuteData data) {
        return data.getEndTime() > 0 && System.currentTimeMillis() > data.getEndTime();
    }

    private void ensureDataFolderExists() {
        if (!dataFile.getParentFile().exists()) {
            dataFile.getParentFile().mkdirs();
        }
    }

    private void saveMutes() throws IOException {
        Map<String, MuteData> toSave = new ConcurrentHashMap<>();
        mutes.forEach((uuid, data) -> toSave.put(uuid.toString(), data));

        try (Writer writer = new FileWriter(dataFile)) {
            gson.toJson(toSave, writer);
        }
    }

    private void load() {
        if (!dataFile.exists()) {
            return;
        }

        try (Reader reader = new FileReader(dataFile)) {
            Type type = new TypeToken<Map<String, MuteData>>() {
            }.getType();
            Map<String, MuteData> loaded = gson.fromJson(reader, type);
            if (loaded != null) {
                loaded.forEach((key, value) -> {
                    try {
                        mutes.put(UUID.fromString(key), value);
                    } catch (IllegalArgumentException ignored) {
                    }
                });
            }
        } catch (IOException e) {
            plugin.getLogger().warning("Error loading mutes: " + e.getMessage());
        }
    }

    private void saveAsync() {
        FoliaUtil.runAsync(plugin, this::save);
    }
}
