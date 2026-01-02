package com.loki.lochat.integrations;

import com.loki.lochat.LoChat;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.Plugin;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Интеграция с LibertyBans для проверки мутов
 * Использует событийный подход для отслеживания мутов
 */
public class LibertyBansHook implements Listener {

    private final LoChat plugin;
    private boolean enabled = false;
    private Plugin libertyBansPlugin;
    private final Set<UUID> mutedPlayers = ConcurrentHashMap.newKeySet();

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
        // Регистрируем слушатель для отслеживания попыток чата замученных игроков
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        plugin.getLogger().info("LibertyBans интеграция включена (событийный режим)");
    }

    /**
     * Слушаем события чата с высоким приоритетом, чтобы отследить,
     * какие игроки не могут писать (значит они замучены)
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
        // Если событие не отменено, значит игрок не замучен
        UUID playerId = event.getPlayer().getUniqueId();
        mutedPlayers.remove(playerId);
    }

    /**
     * Проверяет, замучен ли игрок
     * В событийном режиме мы полагаемся на LibertyBans для блокировки чата
     * @param player игрок для проверки
     * @return CompletableFuture<Boolean> - результат проверки
     */
    public CompletableFuture<Boolean> isMuted(Player player) {
        if (!enabled) {
            return CompletableFuture.completedFuture(false);
        }

        // В событийном режиме мы не можем точно определить мут без API
        // Поэтому возвращаем false и полагаемся на то, что LibertyBans сам заблокирует чат
        return CompletableFuture.completedFuture(false);
    }

    /**
     * В событийном режиме недоступно без API
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

    /**
     * Добавляет игрока в список потенциально замученных
     * (используется когда LibertyBans блокирует чат)
     */
    public void markAsPotentiallyMuted(UUID playerId) {
        if (enabled) {
            mutedPlayers.add(playerId);
        }
    }

    /**
     * Проверяет, был ли игрок отмечен как потенциально замученный
     */
    public boolean isPotentiallyMuted(UUID playerId) {
        return enabled && mutedPlayers.contains(playerId);
    }
}