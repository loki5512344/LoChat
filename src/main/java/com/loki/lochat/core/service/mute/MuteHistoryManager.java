package com.loki.lochat.core.service.mute;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.loki.lochat.config.RatConfig;
import com.loki.lochat.data.model.MuteData;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class MuteHistoryManager {

    private final Map<UUID, List<MuteData.MuteHistoryEntry>> history = new ConcurrentHashMap<>();
    private final File historyFile;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public MuteHistoryManager(File historyFile) {
        this.historyFile = historyFile;
        load();
    }

    public void addEntry(UUID uuid, String playerName, long duration, String reason, String mutedBy) {
        List<MuteData.MuteHistoryEntry> entries = history.computeIfAbsent(uuid, k -> new ArrayList<>());
        entries.add(new MuteData.MuteHistoryEntry(
                playerName,
                duration,
                reason,
                mutedBy,
                System.currentTimeMillis(),
                false,
                null,
                0
        ));
        
        // ✅ FIX: Удаляем старые записи если превышен лимит
        if (entries.size() > RatConfig.MAX_HISTORY_PER_PLAYER) {
            entries.remove(0); // Удаляем самую старую запись
        }
    }

    public void updateUnmute(UUID uuid, String unmutedBy) {
        List<MuteData.MuteHistoryEntry> entries = history.get(uuid);
        if (entries != null && !entries.isEmpty()) {
            MuteData.MuteHistoryEntry lastEntry = entries.get(entries.size() - 1);
            if (lastEntry.unmutedBy == null) {
                lastEntry.unmuted = true;
                lastEntry.unmutedBy = unmutedBy;
                lastEntry.unmutedAt = System.currentTimeMillis();
            }
        }
    }

    public List<MuteData.MuteHistoryEntry> getHistory(UUID uuid) {
        return history.getOrDefault(uuid, Collections.emptyList());
    }

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

    public void save() {
        try (FileWriter writer = new FileWriter(historyFile)) {
            gson.toJson(history, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void load() {
        if (!historyFile.exists()) {
            return;
        }

        try (FileReader reader = new FileReader(historyFile)) {
            Type type = new TypeToken<Map<UUID, List<MuteData.MuteHistoryEntry>>>() {
            }.getType();
            Map<UUID, List<MuteData.MuteHistoryEntry>> loaded = gson.fromJson(reader, type);
            if (loaded != null) {
                history.putAll(loaded);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
