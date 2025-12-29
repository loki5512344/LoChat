package com.loki.lochat.managers;

import com.loki.lochat.LoChat;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class MuteManager {

    private final LoChat plugin;
    private final File muteFile;
    private final Map<UUID, Long> mutedPlayers = new ConcurrentHashMap<>();
    private final Map<UUID, String> muteReasons = new ConcurrentHashMap<>();

    public MuteManager(LoChat plugin) {
        this.plugin = plugin;
        this.muteFile = new File(plugin.getDataFolder(), "mutes.yml");
        load();
    }

    public void load() {
        if (!muteFile.exists()) return;

        YamlConfiguration config = YamlConfiguration.loadConfiguration(muteFile);
        for (String key : config.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(key);
                long muteEnd = config.getLong(key + ".end");
                String reason = config.getString(key + ".reason");

                // Пропускаем истёкшие муты
                if (muteEnd != 0 && System.currentTimeMillis() > muteEnd) continue;

                mutedPlayers.put(uuid, muteEnd);
                if (reason != null) muteReasons.put(uuid, reason);
            } catch (Exception ignored) {}
        }
    }

    public void save() {
        YamlConfiguration config = new YamlConfiguration();

        for (Map.Entry<UUID, Long> entry : mutedPlayers.entrySet()) {
            String key = entry.getKey().toString();
            config.set(key + ".end", entry.getValue());
            String reason = muteReasons.get(entry.getKey());
            if (reason != null) config.set(key + ".reason", reason);
        }

        try {
            config.save(muteFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Не удалось сохранить муты: " + e.getMessage());
        }
    }

    public boolean isMuted(UUID player) {
        Long muteEnd = mutedPlayers.get(player);
        if (muteEnd == null) return false;

        if (muteEnd == 0) return true;

        if (System.currentTimeMillis() > muteEnd) {
            mutedPlayers.remove(player);
            muteReasons.remove(player);
            save();
            return false;
        }

        return true;
    }

    public void mute(UUID player, long durationSeconds, String reason) {
        long muteEnd = durationSeconds <= 0 ? 0 : System.currentTimeMillis() + (durationSeconds * 1000);
        mutedPlayers.put(player, muteEnd);
        if (reason != null && !reason.isEmpty()) {
            muteReasons.put(player, reason);
        }
        save();
    }

    public void unmute(UUID player) {
        mutedPlayers.remove(player);
        muteReasons.remove(player);
        save();
    }

    public String getRemainingTime(UUID player) {
        Long muteEnd = mutedPlayers.get(player);
        if (muteEnd == null) return null;
        if (muteEnd == 0) return "навсегда";

        long remaining = (muteEnd - System.currentTimeMillis()) / 1000;
        if (remaining <= 0) return null;

        return formatTime(remaining);
    }

    public String getReason(UUID player) {
        return muteReasons.get(player);
    }

    public boolean isAlreadyMuted(UUID player) {
        return mutedPlayers.containsKey(player);
    }

    private String formatTime(long seconds) {
        if (seconds < 60) return seconds + " сек";
        if (seconds < 3600) return (seconds / 60) + " мин";
        if (seconds < 86400) return (seconds / 3600) + " ч";
        return (seconds / 86400) + " д";
    }

    public static long parseTime(String time) {
        if (time == null || time.isEmpty()) return -1;

        try {
            char unit = time.charAt(time.length() - 1);
            long value = Long.parseLong(time.substring(0, time.length() - 1));

            return switch (Character.toLowerCase(unit)) {
                case 's' -> value;
                case 'm' -> value * 60;
                case 'h' -> value * 3600;
                case 'd' -> value * 86400;
                default -> Long.parseLong(time);
            };
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
