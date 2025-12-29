package com.loki.lochat.managers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PMManagerTest {

    private PMManager pmManager;
    private UUID player1;
    private UUID player2;

    @BeforeEach
    void setUp() {
        pmManager = new PMManager();
        player1 = UUID.randomUUID();
        player2 = UUID.randomUUID();
    }

    @Test
    void setAndGetLastConversation() {
        pmManager.setLastConversation(player1, player2);
        assertEquals(player2, pmManager.getLastConversation(player1));
    }

    @Test
    void getLastConversation_noConversation() {
        assertNull(pmManager.getLastConversation(player1));
    }

    @Test
    void hasConversation() {
        assertFalse(pmManager.hasConversation(player1));
        pmManager.setLastConversation(player1, player2);
        assertTrue(pmManager.hasConversation(player1));
    }

    @Test
    void removeConversation() {
        pmManager.setLastConversation(player1, player2);
        pmManager.removeConversation(player1);
        assertFalse(pmManager.hasConversation(player1));
    }
}
