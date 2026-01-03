package com.loki.lochat.integrations;

import com.loki.lochat.LoChat;
import org.bukkit.entity.Player;

/**
 * Заглушка для совместимости с LibertyBans
 * Теперь используется встроенная система мутов LoChat
 */
public class LibertyBansHook {

    private final LoChat plugin;

    public LibertyBansHook(LoChat plugin) {
        this.plugin = plugin;
        
        // Проверяем наличие LibertyBans
        if (plugin.getServer().getPluginManager().getPlugin("LibertyBans") != null) {
            plugin.getLogger().info("LibertyBans обнаружен. Используйте встроенную систему мутов LoChat (/lmute, /lunmute)");
        }
    }

    /**
     * Проверяет, замучен ли игрок через встроенную систему LoChat
     */
    public boolean isMuted(Player player) {
        return plugin.getMuteManager().isMuted(player.getUniqueId());
    }

    public boolean isEnabled() {
        return true; // Всегда используем встроенную систему
    }

    public boolean isPluginPresent() {
        return true;
    }
}
