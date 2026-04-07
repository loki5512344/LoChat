package com.loki.lochat.core.service.mute;

import com.loki.lochat.utils.format.TimeFormatter;
import org.bukkit.entity.Player;

/**
 * Проверка прав на муты
 */
public class MutePermissionChecker {
    
    private static final String[] DURATIONS = {
        "30d", "14d", "7d", "3d", "1d", "12h", "6h", "1h", "30m", "10m"
    };
    
    public long getMaxDuration(Player player) {
        if (player.hasPermission("lochat.mute.dur.perm")) {
            return 0; // Permanent
        }

        for (String dur : DURATIONS) {
            if (player.hasPermission("lochat.mute.dur." + dur)) {
                return TimeFormatter.parse(dur);
            }
        }

        return -1; // No permission
    }
    
    public boolean canMuteForDuration(Player player, long duration) {
        if (player.hasPermission("lochat.mute.dur.perm")) {
            return true;
        }

        if (duration == 0) {
            return false; // Permanent mute requires special permission
        }

        long maxDuration = getMaxDuration(player);
        if (maxDuration == -1) {
            return false; // No mute permission
        }
        if (maxDuration == 0) {
            return true; // Can mute permanently
        }

        return duration <= maxDuration;
    }
}
