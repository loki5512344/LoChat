package com.loki.lochat.gradient.data;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class GradientPlayerDataTest {

    private GradientPlayerData data;
    private UUID testUuid;

    @BeforeEach
    void setUp() {
        testUuid = UUID.randomUUID();
        data = new GradientPlayerData(testUuid);
    }

    @Test
    void constructor_defaultValues() {
        assertEquals(testUuid, data.getUuid());
        assertNull(data.getPrefix());
        assertNotNull(data.getColors());
        assertTrue(data.getColors().isEmpty());
        assertTrue(data.isColorEnabled());
        assertTrue(data.isPrefixEnabled());
        assertFalse(data.isPrefixPurchased());
        assertEquals(0, data.getLastColorChange());
        assertEquals(0, data.getLastPrefixChange());
    }

    @Test
    void hasPrefix_false() {
        assertFalse(data.hasPrefix());
        data.setPrefix("");
        assertFalse(data.hasPrefix());
    }

    @Test
    void hasPrefix_true() {
        data.setPrefix("VIP");
        assertTrue(data.hasPrefix());
    }

    @Test
    void hasColors_false() {
        assertFalse(data.hasColors());
    }

    @Test
    void hasColors_true() {
        data.setColors(List.of("#FF0000", "#00FF00"));
        assertTrue(data.hasColors());
    }

    @Test
    void setColors_null() {
        data.setColors(null);
        assertNotNull(data.getColors());
        assertTrue(data.getColors().isEmpty());
    }

    @Test
    void setColors_copiesList() {
        List<String> original = List.of("#FF0000");
        data.setColors(original);
        assertEquals(1, data.getColors().size());
        assertEquals("#FF0000", data.getColors().get(0));
    }

    @Test
    void colorEnabled_toggle() {
        assertTrue(data.isColorEnabled());
        data.setColorEnabled(false);
        assertFalse(data.isColorEnabled());
        data.setColorEnabled(true);
        assertTrue(data.isColorEnabled());
    }

    @Test
    void prefixEnabled_toggle() {
        assertTrue(data.isPrefixEnabled());
        data.setPrefixEnabled(false);
        assertFalse(data.isPrefixEnabled());
    }

    @Test
    void prefixPurchased() {
        assertFalse(data.isPrefixPurchased());
        data.setPrefixPurchased(true);
        assertTrue(data.isPrefixPurchased());
    }

    @Test
    void lastColorChange() {
        long time = System.currentTimeMillis();
        data.setLastColorChange(time);
        assertEquals(time, data.getLastColorChange());
    }

    @Test
    void lastPrefixChange() {
        long time = System.currentTimeMillis();
        data.setLastPrefixChange(time);
        assertEquals(time, data.getLastPrefixChange());
    }
}
