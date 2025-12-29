package com.loki.lochat.managers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CooldownManagerTest {

    private CooldownManager cooldownManager;
    private UUID testPlayer;

    @BeforeEach
    void setUp() {
        cooldownManager = new CooldownManager();
        testPlayer = UUID.randomUUID();
    }

    @Test
    void isOnCooldown_noCooldown() {
        assertFalse(cooldownManager.isOnCooldown(testPlayer, "global", 5));
    }

    @Test
    void isOnCooldown_afterSet() {
        cooldownManager.setCooldown(testPlayer, "global");
        assertTrue(cooldownManager.isOnCooldown(testPlayer, "global", 5));
    }

    @Test
    void getRemainingCooldown_noCooldown() {
        assertEquals(0, cooldownManager.getRemainingCooldown(testPlayer, "global", 5));
    }

    @Test
    void removeCooldown() {
        cooldownManager.setCooldown(testPlayer, "global");
        cooldownManager.removeCooldown(testPlayer);
        assertFalse(cooldownManager.isOnCooldown(testPlayer, "global", 5));
    }

    @Test
    void separateCooldowns() {
        cooldownManager.setCooldown(testPlayer, "global");
        assertTrue(cooldownManager.isOnCooldown(testPlayer, "global", 5));
        assertFalse(cooldownManager.isOnCooldown(testPlayer, "local", 5));
    }
}
