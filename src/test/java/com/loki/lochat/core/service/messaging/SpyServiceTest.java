package com.loki.lochat.core.service.messaging;

import com.loki.lochat.config.MessageConfig;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SpyServiceTest {

    private MessageConfig messageConfig;
    private SpyService spyService;

    @BeforeEach
    void setUp() {
        messageConfig = mock(MessageConfig.class);
        spyService = new SpyService(messageConfig);
    }

    @Test
    void isSpying_defaultFalse() {
        assertFalse(spyService.isSpying(UUID.randomUUID()));
    }

    @Test
    void toggleSpy_turnsOn() {
        UUID player = UUID.randomUUID();
        assertTrue(spyService.toggleSpy(player));
        assertTrue(spyService.isSpying(player));
    }

    @Test
    void toggleSpy_turnsOff() {
        UUID player = UUID.randomUUID();
        spyService.toggleSpy(player);
        assertFalse(spyService.toggleSpy(player));
        assertFalse(spyService.isSpying(player));
    }

    @Test
    void toggleSpy_toggleOnOff() {
        UUID player = UUID.randomUUID();
        assertTrue(spyService.toggleSpy(player));
        assertFalse(spyService.toggleSpy(player));
        assertTrue(spyService.toggleSpy(player));
        assertTrue(spyService.isSpying(player));
    }

    @Test
    void removeSpy_removesFromSet() {
        UUID player = UUID.randomUUID();
        spyService.toggleSpy(player);
        spyService.removeSpy(player);
        assertFalse(spyService.isSpying(player));
    }

    @Test
    void removeSpy_notSpying() {
        spyService.removeSpy(UUID.randomUUID());
        assertFalse(spyService.isSpying(UUID.randomUUID()));
    }

    @Test
    void multiplePlayersIndepedent() {
        UUID a = UUID.randomUUID();
        UUID b = UUID.randomUUID();
        spyService.toggleSpy(a);
        assertTrue(spyService.isSpying(a));
        assertFalse(spyService.isSpying(b));
    }
}
