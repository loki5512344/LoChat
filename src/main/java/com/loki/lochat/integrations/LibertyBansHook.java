package com.loki.lochat.integrations;

import com.loki.lochat.LoChat;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.concurrent.CompletableFuture;

/**
 * Интеграция с LibertyBans для проверки мутов
 * Простая версия без API зависимости
 */
public class LibertyBansHook {

    private final LoChat plugin;
    private boolean enabled = false;
    private Plugin libertyBansPlugin;

    public LibertyBansHook(LoChat plugin) {
        this.plugin = plugin;
        initialize();
    }

    private void initialize() {
        libertyBansPlugin = plugin.getServer().getPluginManager().getPlugin("LibertyBans");
        if (libertyBansPlugin == null || !libertyBansPlugin.isEnabled()) {
            plugin.getLogger().info("LibertyBans не найден, интеграция отключена");
            return;
        }

        this.enabled = true;
        plugin.getLogger().info("LibertyBans найден, но API интеграция недоступна. Используйте команды LibertyBans для управления мутами.");
    }

    /**
     * Проверяет, замучен ли игрок
     * В простой версии всегда возвращает false, так как нет доступа к API
     * @param player игрок для проверки
     * @return CompletableFuture<Boolean> - всегда false в простой версии
     */
    public CompletableFuture<Boolean> isMuted(Player player) {
        // В простой версии без API мы не можем проверить мут
        // Пользователи должны использовать команды LibertyBans
        return CompletableFuture.completedFuture(false);
    }

    /**
     * В простой версии без API недоступно
     */
    public CompletableFuture<Object> getActiveMute(Player player) {
        return CompletableFuture.completedFuture(null);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isPluginPresent() {
        return libertyBansPlugin != null && libertyBansPlugin.isEnabled();
    }
}