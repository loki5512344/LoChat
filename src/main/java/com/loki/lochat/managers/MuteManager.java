package com.loki.lochat.managers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.loki.lochat.LoChat;

import java.io.*;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Менеджер мутов для LoChat
 */
public class MuteManager {

    private final LoChat plugin;
    private final Map<UUID, MuteData> mutes = new ConcurrentHashMap<>();
    private final Map<UUID, List<MuteHistoryEntry>> history = new ConcurrentHashMap<>();
    private final File dataFile;
    private final File historyFile;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public MuteManager(LoChat plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "mutes.json");
        this.historyFile = new File(plugin.getDataFolder(), "mute-history.json");
        load();
        loadHistory();
    }

    /**
     * Замутить игрока
     * @param uuid UUID игрока
     * @param playerName имя игрока
     * @param duration длительность в миллисекундах (0 = перманентный)
     * @param reason причина
     * @param mutedBy кто замутил
     */
    public void mute(UUID uuid, String playerName, long duration, String reason, String mutedBy) {
        long endTime = duration > 0 ? System.currentTimeMillis() + duration : 0;
        MuteData data = new MuteData(endTime, reason, mutedBy, System.currentTimeMillis(), playerName);
        mutes.put(uuid, data);
        
        // Добавляем в историю
        addToHistory(uuid, playerName, duration, reason, mutedBy);
        
        saveAsync();
    }

    /**
     * Замутить игрока (без имени - для совместимости)
     */
    public void mute(UUID uuid, long duration, String reason, String mutedBy) {
        mute(uuid, null, duration, reason, mutedBy);
    }

    /**
     * Размутить игрока
     * @param uuid UUID игрока
     * @param unmutedBy кто размутил (null если автоматически)
     * @return true если игрок был замучен
     */
    public boolean unmute(UUID uuid, String unmutedBy) {
        MuteData removed = mutes.remove(uuid);
        if (removed != null) {
            // Обновляем историю - помечаем как размученный
            updateHistoryUnmute(uuid, unmutedBy);
            saveAsync();
            return true;
        }
        return false;
    }

    /**
     * Размутить игрока (для совместимости)
     */
    public boolean unmute(UUID uuid) {
        return unmute(uuid, null);
    }

    /**
     * Проверить, замучен ли игрок
     */
    public boolean isMuted(UUID uuid) {
        MuteData data = mutes.get(uuid);
        if (data == null) return false;
        
        // Проверяем истёк ли мут
        if (data.endTime > 0 && System.currentTimeMillis() > data.endTime) {
            mutes.remove(uuid);
            saveAsync();
            return false;
        }
        return true;
    }

    /**
     * Получить данные мута
     */
    public MuteData getMuteData(UUID uuid) {
        if (!isMuted(uuid)) return null;
        return mutes.get(uuid);
    }

    /**
     * Получить оставшееся время мута
     */
    public long getRemainingTime(UUID uuid) {
        MuteData data = mutes.get(uuid);
        if (data == null) return 0;
        if (data.endTime == 0) return -1; // Перманентный
        return Math.max(0, data.endTime - System.currentTimeMillis());
    }

    /**
     * Форматировать время
     */
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

    /**
     * Парсинг времени из строки (1d, 2h, 30m, 60s)
     */
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
                // Если без суффикса - считаем минутами
                return Long.parseLong(timeStr) * 60 * multiplier;
            }
        } catch (NumberFormatException e) {
            return -1; // Ошибка парсинга
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

    private void loadHistory() {
        if (!historyFile.exists()) return;
        
        try (Reader reader = new FileReader(historyFile)) {
            Type type = new TypeToken<Map<String, List<MuteHistoryEntry>>>(){}.getType();
            Map<String, List<MuteHistoryEntry>> loaded = gson.fromJson(reader, type);
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

    public void saveHistory() {
        try {
            if (!historyFile.getParentFile().exists()) {
                historyFile.getParentFile().mkdirs();
            }
            
            Map<String, List<MuteHistoryEntry>> toSave = new ConcurrentHashMap<>();
            history.forEach((uuid, data) -> toSave.put(uuid.toString(), data));
            
            try (Writer writer = new FileWriter(historyFile)) {
                gson.toJson(toSave, writer);
            }
        } catch (IOException e) {
            plugin.getLogger().warning("Ошибка сохранения истории мутов: " + e.getMessage());
        }
    }

    private void saveHistoryAsync() {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, this::saveHistory);
    }

    private void saveAsync() {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            save();
            saveHistory();
        });
    }

    /**
     * Добавить запись в историю
     */
    private void addToHistory(UUID uuid, String playerName, long duration, String reason, String mutedBy) {
        List<MuteHistoryEntry> playerHistory = history.computeIfAbsent(uuid, k -> new ArrayList<>());
        playerHistory.add(new MuteHistoryEntry(
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

    /**
     * Обновить историю при размуте
     */
    private void updateHistoryUnmute(UUID uuid, String unmutedBy) {
        List<MuteHistoryEntry> playerHistory = history.get(uuid);
        if (playerHistory != null && !playerHistory.isEmpty()) {
            // Находим последний активный мут
            for (int i = playerHistory.size() - 1; i >= 0; i--) {
                MuteHistoryEntry entry = playerHistory.get(i);
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

    /**
     * Получить список всех активных мутов
     */
    public Map<UUID, MuteData> getActiveMutes() {
        // Очищаем истёкшие муты
        mutes.entrySet().removeIf(entry -> {
            MuteData data = entry.getValue();
            return data.endTime > 0 && System.currentTimeMillis() > data.endTime;
        });
        return new HashMap<>(mutes);
    }

    /**
     * Получить историю мутов игрока
     */
    public List<MuteHistoryEntry> getPlayerHistory(UUID uuid) {
        return history.getOrDefault(uuid, new ArrayList<>());
    }

    /**
     * Получить UUID по имени из истории
     */
    public UUID getUUIDByName(String name) {
        for (Map.Entry<UUID, List<MuteHistoryEntry>> entry : history.entrySet()) {
            for (MuteHistoryEntry historyEntry : entry.getValue()) {
                if (historyEntry.playerName != null && historyEntry.playerName.equalsIgnoreCase(name)) {
                    return entry.getKey();
                }
            }
        }
        return null;
    }

    /**
     * Данные о муте
     */
    public static class MuteData {
        public long endTime; // 0 = перманентный
        public String reason;
        public String mutedBy;
        public long mutedAt;
        public String playerName;

        public MuteData() {}

        public MuteData(long endTime, String reason, String mutedBy, long mutedAt) {
            this(endTime, reason, mutedBy, mutedAt, null);
        }

        public MuteData(long endTime, String reason, String mutedBy, long mutedAt, String playerName) {
            this.endTime = endTime;
            this.reason = reason;
            this.mutedBy = mutedBy;
            this.mutedAt = mutedAt;
            this.playerName = playerName;
        }

        public boolean isPermanent() {
            return endTime == 0;
        }
    }

    /**
     * Запись истории мута
     */
    public static class MuteHistoryEntry {
        public String playerName;
        public long duration; // 0 = перманентный
        public String reason;
        public String mutedBy;
        public long mutedAt;
        public boolean unmuted;
        public String unmutedBy;
        public long unmutedAt;

        public MuteHistoryEntry() {}

        public MuteHistoryEntry(String playerName, long duration, String reason, String mutedBy, 
                                long mutedAt, boolean unmuted, String unmutedBy, long unmutedAt) {
            this.playerName = playerName;
            this.duration = duration;
            this.reason = reason;
            this.mutedBy = mutedBy;
            this.mutedAt = mutedAt;
            this.unmuted = unmuted;
            this.unmutedBy = unmutedBy;
            this.unmutedAt = unmutedAt;
        }

        public boolean isPermanent() {
            return duration == 0;
        }
    }
}
