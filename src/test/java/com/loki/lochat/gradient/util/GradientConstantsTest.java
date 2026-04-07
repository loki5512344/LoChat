package com.loki.lochat.gradient.util;

import com.loki.lochat.config.RatConfig;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GradientConstantsTest {

    @Test
    void millisPerSecond() {
        assertEquals(1000L, RatConfig.MILLIS_PER_SECOND);
    }

    @Test
    void guiSlots() {
        assertEquals(11, RatConfig.GUI_CONFIRM_SLOT);
        assertEquals(15, RatConfig.GUI_CANCEL_SLOT);
        assertEquals(13, RatConfig.GUI_PREVIEW_SLOT);
    }

    @Test
    void luckPermsPriority() {
        assertEquals(100, RatConfig.LUCKPERMS_PREFIX_PRIORITY);
    }
}
