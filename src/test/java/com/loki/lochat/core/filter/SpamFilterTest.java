package com.loki.lochat.core.filter;

import com.loki.lochat.core.filter.filters.SpamFilter;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SpamFilterTest {

    @Test
    void filter_firstMessageAllowed() {
        Player player = mock(Player.class);
        when(player.hasPermission("lochat.bypass.spam")).thenReturn(false);
        when(player.getUniqueId()).thenReturn(java.util.UUID.randomUUID());
        YamlConfiguration config = new YamlConfiguration();
        config.set("filters.spam.max-similar-messages", 3);
        config.set("filters.spam.similarity-threshold", 80);
        SpamFilter filter = new SpamFilter(config);
        FilterResult result = filter.filter(player, "hello");
        assertTrue(result.allowed());
    }

    @Test
    void filter_sameMessageRepeated() {
        Player player = mock(Player.class);
        when(player.hasPermission("lochat.bypass.spam")).thenReturn(false);
        when(player.getUniqueId()).thenReturn(java.util.UUID.randomUUID());
        YamlConfiguration config = new YamlConfiguration();
        config.set("filters.spam.max-similar-messages", 2);
        config.set("filters.spam.similarity-threshold", 80);
        config.set("filters.spam.block-message", "no spam");
        SpamFilter filter = new SpamFilter(config);
        filter.filter(player, "hello");
        filter.filter(player, "hello");
        FilterResult result = filter.filter(player, "hello");
        assertFalse(result.allowed());
        assertEquals("no spam", result.blockReason());
    }

    @Test
    void filter_differentMessagesAllowed() {
        Player player = mock(Player.class);
        when(player.hasPermission("lochat.bypass.spam")).thenReturn(false);
        when(player.getUniqueId()).thenReturn(java.util.UUID.randomUUID());
        YamlConfiguration config = new YamlConfiguration();
        config.set("filters.spam.max-similar-messages", 2);
        config.set("filters.spam.similarity-threshold", 80);
        SpamFilter filter = new SpamFilter(config);
        filter.filter(player, "hello");
        filter.filter(player, "world");
        FilterResult result = filter.filter(player, "foo");
        assertTrue(result.allowed());
    }

    @Test
    void filter_bypassPermission() {
        Player player = mock(Player.class);
        when(player.hasPermission("lochat.bypass.spam")).thenReturn(true);
        when(player.getUniqueId()).thenReturn(java.util.UUID.randomUUID());
        YamlConfiguration config = new YamlConfiguration();
        SpamFilter filter = new SpamFilter(config);
        FilterResult result = filter.filter(player, "spam");
        assertTrue(result.allowed());
    }

    @Test
    void filter_historyLimit() {
        Player player = mock(Player.class);
        when(player.hasPermission("lochat.bypass.spam")).thenReturn(false);
        when(player.getUniqueId()).thenReturn(java.util.UUID.randomUUID());
        YamlConfiguration config = new YamlConfiguration();
        config.set("filters.spam.max-similar-messages", 2);
        config.set("filters.spam.similarity-threshold", 100);
        config.set("filters.spam.block-message", "no spam");
        SpamFilter filter = new SpamFilter(config);
        // Send 12 different messages to fill and rotate history
        for (int i = 0; i < 12; i++) {
            filter.filter(player, "msg" + i);
        }
        // Send "hello" twice more to trigger spam detection
        filter.filter(player, "hello");
        filter.filter(player, "hello");
        FilterResult result = filter.filter(player, "hello");
        assertFalse(result.allowed());
    }

    @Test
    void filter_similarButNotExact() {
        Player player = mock(Player.class);
        when(player.hasPermission("lochat.bypass.spam")).thenReturn(false);
        when(player.getUniqueId()).thenReturn(java.util.UUID.randomUUID());
        YamlConfiguration config = new YamlConfiguration();
        config.set("filters.spam.max-similar-messages", 2);
        config.set("filters.spam.similarity-threshold", 90);
        config.set("filters.spam.block-message", "no spam");
        SpamFilter filter = new SpamFilter(config);
        filter.filter(player, "hello world");
        filter.filter(player, "hello world");
        FilterResult result = filter.filter(player, "hello world!");
        // 91% similarity (11/12 chars match), should be blocked
        assertFalse(result.allowed());
    }

    @Test
    void clearPlayer_removesData() {
        Player player = mock(Player.class);
        java.util.UUID uuid = java.util.UUID.randomUUID();
        when(player.hasPermission("lochat.bypass.spam")).thenReturn(false);
        when(player.getUniqueId()).thenReturn(uuid);
        YamlConfiguration config = new YamlConfiguration();
        config.set("filters.spam.max-similar-messages", 2);
        config.set("filters.spam.similarity-threshold", 100);
        SpamFilter filter = new SpamFilter(config);
        filter.filter(player, "hello");
        filter.clearPlayer(uuid);
        filter.filter(player, "hello");
        FilterResult result = filter.filter(player, "hello");
        assertTrue(result.allowed());
    }
}
