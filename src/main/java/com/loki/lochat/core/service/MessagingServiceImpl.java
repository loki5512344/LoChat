package com.loki.lochat.core.service;

import com.loki.lochat.api.service.MessagingService;
import com.loki.lochat.config.MessageConfig;
import com.loki.lochat.core.service.messaging.IgnoreService;
import com.loki.lochat.core.service.messaging.PrivateMessageService;
import com.loki.lochat.core.service.messaging.SpyService;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Фасад для сервисов общения
 * Делегирует вызовы: PrivateMessageService, SpyService, IgnoreService
 */
public class MessagingServiceImpl implements MessagingService {

    private final PrivateMessageService pmService;
    private final SpyService spyService;
    private final IgnoreService ignoreService;

    public MessagingServiceImpl(JavaPlugin plugin, MessageConfig messageConfig) {
        this.pmService = new PrivateMessageService();
        this.spyService = new SpyService(messageConfig);
        this.ignoreService = new IgnoreService(plugin);
        this.ignoreService.init();
    }

    // ========== PM Delegation ==========

    @Override
    public void setLastConversation(UUID player, UUID target) {
        pmService.setLastConversation(player, target);
    }

    @Override
    public Optional<UUID> getLastConversation(UUID player) {
        return pmService.getLastConversation(player);
    }

    @Override
    public void removeConversation(UUID player) {
        pmService.removeConversation(player);
    }

    @Override
    public boolean hasConversation(UUID player) {
        return pmService.hasConversation(player);
    }

    // ========== Spy Delegation ==========

    @Override
    public boolean toggleSpy(UUID player) {
        return spyService.toggleSpy(player);
    }

    @Override
    public boolean isSpying(UUID player) {
        return spyService.isSpying(player);
    }

    @Override
    public void broadcastPM(Player sender, Player receiver, String message) {
        spyService.broadcastPM(sender, receiver, message);
    }

    @Override
    public void sendToSpies(Player sender, Component message, boolean isGlobal) {
        spyService.sendToSpies(sender, message, isGlobal);
    }

    @Override
    public void removeSpy(UUID player) {
        spyService.removeSpy(player);
    }

    // ========== Ignore Delegation ==========

    @Override
    public boolean isIgnoring(UUID player, UUID target) {
        return ignoreService.isIgnoring(player, target);
    }

    @Override
    public boolean addIgnore(UUID player, UUID target) {
        return ignoreService.addIgnore(player, target);
    }

    @Override
    public boolean removeIgnore(UUID player, UUID target) {
        return ignoreService.removeIgnore(player, target);
    }

    @Override
    public Set<UUID> getIgnoredPlayers(UUID player) {
        return ignoreService.getIgnoredPlayers(player);
    }

    @Override
    public int getIgnoredCount(UUID player) {
        return ignoreService.getIgnoredCount(player);
    }

    @Override
    public void clearIgnores(UUID player) {
        ignoreService.clearIgnores(player);
    }

    // ========== Persistence ==========

    @Override
    public void load() {
        ignoreService.load();
    }

    @Override
    public void save() {
        ignoreService.save();
    }
}
