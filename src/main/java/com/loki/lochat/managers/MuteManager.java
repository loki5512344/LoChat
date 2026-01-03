package com.loki.lochat.managers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.loki.lochat.LoChat;

import java.io.*;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Менеджер мутов для LoChat
 */
public class MuteManager {

    private final LoChat plugin;
    private final Map<UUID, MuteData> mutes = new ConcurrentHashMap<>();
    private final File dataFile;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public MuteManager(LoChat plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "mutes.json");
        load();
    }

    /**
     * Замутить игрока
     * @param uuid UUID игрока
     * @param duration длительность в миллисекундах (0 = перманентный)
     * @param reason причина
     * @param mutedBy кто замутил
     */
    public void mute(UUID uuid, long duration, String reason, String mutedBy) {
        long endTime = duration > 0 ? System.currentTimeMillis() + duration : 0;
        mutes.put(uuid, new MuteData(endTime, reason, mutedBy, System.currentTimeMillis()));
        saveAsync();
    }

    /**
     * Размутить игрока
     * @param uuid UUID игрока
     * @return true если игрок был замучен
     */
    public boolean unmute(UUID uuid) {
        MuteData removed = mutes.remove(uuid);
        if (removed != null) {
            saveAsync();
            return true;
        }
        return false;
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

    private void saveAsync() {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, this::save);
    }

    /**
     * Данные о муте
     */
    public static class MuteData {
        public long endTime; // 0 = перманентный
        public String reason;
        public String mutedBy;
        public long mutedAt;

        public MuteData() {}

        public MuteData(long endTime, String reason, String mutedBy, long mutedAt) {
            this.endTime = endTime;
            this.reason = reason;
            this.mutedBy = mutedBy;
            this.mutedAt = mutedAt;
        }

        public boolean isPermanent() {
            return endTime == 0;
        }
    }
}
