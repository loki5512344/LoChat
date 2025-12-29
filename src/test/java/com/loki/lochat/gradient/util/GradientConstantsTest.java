package com.loki.lochat.gradient.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GradientConstantsTest {

    @Test
    void millisPerSecond() {
        assertEquals(1000L, GradientConstants.MILLIS_PER_SECOND);
    }

    @Test
    void guiSlots() {
        assertEquals(11, GradientConstants.GUI_CONFIRM_SLOT);
        assertEquals(15, GradientConstants.GUI_CANCEL_SLOT);
        assertEquals(13, GradientConstants.GUI_PREVIEW_SLOT);
    }

    @Test
    void luckPermsPriority() {
        assertEquals(100, GradientConstants.LUCKPERMS_PREFIX_PRIORITY);
    }
}
