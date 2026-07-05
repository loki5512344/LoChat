package com.loki.lochat.core.filter;

import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CapsFilterTest {

    @Test
    void filter_noCapsReturnsSame() {
        Player player = mock(Player.class);
        when(player.hasPermission("lochat.bypass.caps")).thenReturn(false);
        CapsFilter filter = new CapsFilter(70, 5, true, false);
        assertEquals("hello world", filter.filter(player, "hello world"));
    }

    @Test
    void filter_allCapsAutoLower() {
        Player player = mock(Player.class);
        when(player.hasPermission("lochat.bypass.caps")).thenReturn(false);
        CapsFilter filter = new CapsFilter(50, 3, true, false);
        String result = filter.filter(player, "HELLO WORLD");
        assertNotNull(result);
        assertTrue(result.startsWith("H") && result.substring(1).equals("ello world"));
    }

    @Test
    void filter_allCapsBlocked() {
        Player player = mock(Player.class);
        when(player.hasPermission("lochat.bypass.caps")).thenReturn(false);
        CapsFilter filter = new CapsFilter(50, 3, false, true);
        assertNull(filter.filter(player, "HELLO WORLD"));
    }

    @Test
    void filter_shortMessageIgnored() {
        Player player = mock(Player.class);
        when(player.hasPermission("lochat.bypass.caps")).thenReturn(false);
        CapsFilter filter = new CapsFilter(70, 10, false, true);
        assertEquals("HI", filter.filter(player, "HI"));
    }

    @Test
    void filter_bypassPermission() {
        Player player = mock(Player.class);
        when(player.hasPermission("lochat.bypass.caps")).thenReturn(true);
        CapsFilter filter = new CapsFilter(50, 3, false, true);
        assertEquals("HELLO", filter.filter(player, "HELLO"));
    }

    @Test
    void filter_mixedCapsWithinLimit() {
        Player player = mock(Player.class);
        when(player.hasPermission("lochat.bypass.caps")).thenReturn(false);
        CapsFilter filter = new CapsFilter(70, 5, true, false);
        assertEquals("Hello World", filter.filter(player, "Hello World"));
    }

    @Test
    void filter_onlySpecialChars() {
        Player player = mock(Player.class);
        when(player.hasPermission("lochat.bypass.caps")).thenReturn(false);
        CapsFilter filter = new CapsFilter(50, 3, true, false);
        assertEquals("123!@#", filter.filter(player, "123!@#"));
    }

    @Test
    void filter_emptyString() {
        Player player = mock(Player.class);
        when(player.hasPermission("lochat.bypass.caps")).thenReturn(false);
        CapsFilter filter = new CapsFilter(50, 5, true, false);
        assertEquals("", filter.filter(player, ""));
    }
}
