package com.loki.lochat.managers;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MuteManagerTest {

    @Test
    void parseTime_seconds() {
        assertEquals(30, MuteManager.parseTime("30s"));
    }

    @Test
    void parseTime_minutes() {
        assertEquals(300, MuteManager.parseTime("5m"));
    }

    @Test
    void parseTime_hours() {
        assertEquals(3600, MuteManager.parseTime("1h"));
    }

    @Test
    void parseTime_days() {
        assertEquals(86400, MuteManager.parseTime("1d"));
    }

    @Test
    void parseTime_invalid() {
        assertEquals(-1, MuteManager.parseTime("abc"));
        assertEquals(-1, MuteManager.parseTime(""));
        assertEquals(-1, MuteManager.parseTime(null));
    }

    @Test
    void parseTime_numberOnly() {
        assertEquals(60, MuteManager.parseTime("60"));
    }
}
