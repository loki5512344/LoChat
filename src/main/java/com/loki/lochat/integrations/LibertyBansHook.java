package com.loki.lochat.integrations;

import com.loki.lochat.LoChat;
import com.loki.lochat.api.service.MuteService;

import org.bukkit.entity.Player;

/**
 * Заглушка для совместимости с LibertyBans
 * Теперь используется встроенная система мутов LoChat
 */
public class LibertyBansHook {

    private final MuteService muteService;

    public LibertyBansHook(LoChat plugin) {
        this.muteService = plugin.getServiceRegistry().get(MuteService.class);

        // Проверяем наличие LibertyBans
        if (plugin.getServer().getPluginManager().getPlugin("LibertyBans") != null) {
            plugin.getLogger().info("LibertyBans обнаружен. Используйте встроенную систему мутов LoChat (/lmute, /lunmute)");
        }
    }

    /**
     * Проверяет, замучен ли игрок через встроенную систему LoChat
     */
    public boolean isMuted(Player player) {
        return muteService.isMuted(player.getUniqueId());
    }

    public boolean isEnabled() {
        return true; // Всегда используем встроенную систему
    }

    public boolean isPluginPresent() {
        return true;
    }
}
