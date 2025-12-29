package com.loki.lochat.gradient.util;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GradientUtilTest {

    @Test
    void isValidHex_valid() {
        assertTrue(GradientUtil.isValidHex("#FF0000"));
        assertTrue(GradientUtil.isValidHex("#00ff00"));
        assertTrue(GradientUtil.isValidHex("#3BA8FF"));
        assertTrue(GradientUtil.isValidHex("#000000"));
        assertTrue(GradientUtil.isValidHex("#FFFFFF"));
    }

    @Test
    void isValidHex_invalid() {
        assertFalse(GradientUtil.isValidHex("FF0000"));      // без #
        assertFalse(GradientUtil.isValidHex("#FF00"));       // короткий
        assertFalse(GradientUtil.isValidHex("#FF00000"));    // длинный
        assertFalse(GradientUtil.isValidHex("#GGGGGG"));     // невалидные символы
        assertFalse(GradientUtil.isValidHex(""));
    }

    @Test
    void isValidHex_null() {
        assertThrows(NullPointerException.class, () -> GradientUtil.isValidHex(null));
    }

    @Test
    void applyGradient_singleColor_legacy() {
        String result = GradientUtil.applyGradient("Hi", List.of("#FF0000"), true);
        assertNotNull(result);
        assertTrue(result.contains("&#ff0000"));
        assertEquals("&#ff0000H&#ff0000i", result);
    }

    @Test
    void applyGradient_singleColor_minimessage() {
        String result = GradientUtil.applyGradient("Hi", List.of("#FF0000"), false);
        assertNotNull(result);
        assertTrue(result.contains("<#ff0000>"));
    }

    @Test
    void applyGradient_twoColors() {
        String result = GradientUtil.applyGradient("AB", List.of("#FF0000", "#0000FF"), true);
        assertNotNull(result);
        // Первый символ должен быть красным, последний синим
        assertTrue(result.startsWith("&#ff0000A"));
        assertTrue(result.contains("&#0000ffB"));
    }

    @Test
    void applyGradient_emptyText() {
        String result = GradientUtil.applyGradient("", List.of("#FF0000"), true);
        assertEquals("", result);
    }

    @Test
    void applyGradient_nullText() {
        String result = GradientUtil.applyGradient(null, List.of("#FF0000"), true);
        assertNull(result);
    }

    @Test
    void applyGradient_emptyColors() {
        String result = GradientUtil.applyGradient("Test", List.of(), true);
        assertEquals("Test", result);
    }

    @Test
    void applyGradient_nullColors() {
        String result = GradientUtil.applyGradient("Test", null, true);
        assertEquals("Test", result);
    }

    @Test
    void applyGradient_multipleColors() {
        String result = GradientUtil.applyGradient("ABC", List.of("#FF0000", "#00FF00", "#0000FF"), true);
        assertNotNull(result);
        // Должен содержать цветовые коды для каждого символа
        assertTrue(result.contains("A"));
        assertTrue(result.contains("B"));
        assertTrue(result.contains("C"));
    }

    @Test
    void buildDisplayName_noPrefix_noColors() {
        String result = GradientUtil.buildDisplayName(null, "Player", null, true, true, "[{prefix}] ", true);
        assertEquals("Player", result);
    }

    @Test
    void buildDisplayName_withPrefix_noColors() {
        String result = GradientUtil.buildDisplayName("VIP", "Player", null, true, true, "[{prefix}] ", true);
        assertEquals("[VIP] Player", result);
    }

    @Test
    void buildDisplayName_noPrefix_withColors() {
        String result = GradientUtil.buildDisplayName(null, "AB", List.of("#FF0000", "#0000FF"), true, true, "[{prefix}] ", true);
        assertNotNull(result);
        assertTrue(result.contains("&#"));
    }

    @Test
    void buildDisplayName_withPrefix_withColors_continuous() {
        String result = GradientUtil.buildDisplayName("V", "AB", List.of("#FF0000", "#0000FF"), true, true, "[{prefix}] ", true);
        assertNotNull(result);
        // Единый градиент на всё
        assertTrue(result.contains("&#"));
    }

    @Test
    void buildDisplayName_withPrefix_withColors_separate() {
        String result = GradientUtil.buildDisplayName("V", "AB", List.of("#FF0000", "#0000FF"), true, false, "[{prefix}] ", true);
        assertNotNull(result);
        // Отдельные градиенты
        assertTrue(result.contains("&#"));
    }

    @Test
    void buildDisplayName_gradientOnlyOnNick() {
        String result = GradientUtil.buildDisplayName("VIP", "AB", List.of("#FF0000", "#0000FF"), false, false, "[{prefix}] ", true);
        assertNotNull(result);
        // Префикс без градиента
        assertTrue(result.startsWith("[VIP] "));
    }
}
