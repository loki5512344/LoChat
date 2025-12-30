package com.loki.lochat.managers;

import com.loki.lochat.LoChat;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MuteManager {

    private final LoChat plugin;
    private final Map<UUID, MuteData> mutedPlayers;
    private File dataFile;
    private FileConfiguration dataConfig;

    public MuteManager(LoChat plugin) {
        this.plugin = plugin;
        this.mutedPlayers = new HashMap<>();
        loadData();
    }

    private void loadData() {
        dataFile = new File(plugin.getDataFolder(), "mutes.yml");
        if (!dataFile.exists()) {
            try {
                dataFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Не удалось создать файл mutes.yml: " + e.getMessage());
            }
        }
        
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
        
        // Загружаем муты из файла
        for (String key : dataConfig.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(key);
                long expiry = dataConfig.getLong(key + ".expiry");
                String reason = dataConfig.getString(key + ".reason", "Нарушение правил");
                String mutedBy = dataConfig.getString(key + ".muted_by", "Система");
                
                // Проверяем не истёк ли мут
                if (expiry > 0 && System.currentTimeMillis() > expiry) {
                    dataConfig.set(key, null);
                    continue;
                }
                
                mutedPlayers.put(uuid, new MuteData(expiry, reason, mutedBy));
            } catch (IllegalArgumentException ignored) {
                // Неверный UUID, пропускаем
            }
        }
        
        saveData();
    }

    public void saveData() {
        for (Map.Entry<UUID, MuteData> entry : mutedPlayers.entrySet()) {
            String key = entry.getKey().toString();
            MuteData data = entry.getValue();
            
            dataConfig.set(key + ".expiry", data.expiry);
            dataConfig.set(key + ".reason", data.reason);
            dataConfig.set(key + ".muted_by", data.mutedBy);
        }
        
        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Не удалось сохранить mutes.yml: " + e.getMessage());
        }
    }

    public void mutePlayer(UUID playerId, long duration, String reason, String mutedBy) {
        long expiry = duration == 0 ? 0 : System.currentTimeMillis() + duration;
        mutedPlayers.put(playerId, new MuteData(expiry, reason, mutedBy));
        saveData();
    }

    public void unmutePlayer(UUID playerId) {
        mutedPlayers.remove(playerId);
        dataConfig.set(playerId.toString(), null);
        saveData();
    }

    public boolean isMuted(UUID playerId) {
        MuteData data = mutedPlayers.get(playerId);
        if (data == null) return false;
        
        // Проверяем не истёк ли мут
        if (data.expiry > 0 && System.currentTimeMillis() > data.expiry) {
            unmutePlayer(playerId);
            return false;
        }
        
        return true;
    }

    public MuteData getMuteData(UUID playerId) {
        return mutedPlayers.get(playerId);
    }

    public long getRemainingTime(UUID playerId) {
        MuteData data = mutedPlayers.get(playerId);
        if (data == null || data.expiry == 0) return 0;
        
        long remaining = data.expiry - System.currentTimeMillis();
        return Math.max(0, remaining);
    }

    public String formatRemainingTime(UUID playerId) {
        long remaining = getRemainingTime(playerId);
        if (remaining == 0) {
            MuteData data = getMuteData(playerId);
            if (data != null && data.expiry == 0) {
                return "навсегда";
            }
            return "0с";
        }
        
        long seconds = remaining / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        if (days > 0) return days + "д " + (hours % 24) + "ч";
        if (hours > 0) return hours + "ч " + (minutes % 60) + "м";
        if (minutes > 0) return minutes + "м " + (seconds % 60) + "с";
        return seconds + "с";
    }

    public static class MuteData {
        public final long expiry; // 0 = permanent
        public final String reason;
        public final String mutedBy;

        public MuteData(long expiry, String reason, String mutedBy) {
            this.expiry = expiry;
            this.reason = reason;
            this.mutedBy = mutedBy;
        }
    }
}