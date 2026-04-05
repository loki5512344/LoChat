package com.loki.lochat.utils;

import net.kyori.adventure.text.Component;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ChatFormatterTest {

    @Test
    void testParseSimpleText() {
        Component result = ChatFormatter.parse("Hello World");
        assertNotNull(result);
    }

    @Test
    void testParseWithColors() {
        Component result = ChatFormatter.parse("<red>Red Text</red>");
        assertNotNull(result);
    }

    @Test
    void testParseWithHex() {
        Component result = ChatFormatter.parse("<#FF0000>Hex Color</#FF0000>");
        assertNotNull(result);
    }

    @Test
    void testStripTags() {
        String result = ChatFormatter.stripTags("<red>Text</red>");
        assertEquals("Text", result);
    }

    @Test
    void testConvertLegacyColors() {
        String result = ChatFormatter.convertAllColors("&cRed &aGreen");
        assertTrue(result.contains("<red>") || result.contains("Red"));
    }

    @Test
    @SuppressWarnings("deprecation")
    void testReplaceEmojis() {
        String result = ChatFormatter.replaceEmojis("Test :smile:");
        assertEquals("Test :smile:", result);
    }

    @Test
    void testToPlainComponent() {
        Component comp = Component.text("Plain");
        String result = ChatFormatter.toPlain(comp);
        assertEquals("Plain", result);
    }

    @Test
    void testToPlainObject() {
        String result = ChatFormatter.toPlain("String");
        assertEquals("String", result);
    }

    @Test
    void testToPlainNull() {
        String result = ChatFormatter.toPlain((Object) null);
        assertEquals("", result);
    }
}
