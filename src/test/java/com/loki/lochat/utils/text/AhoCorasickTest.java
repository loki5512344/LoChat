package com.loki.lochat.utils.text;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AhoCorasickTest {

    @Test
    void matches_simpleWord() {
        AhoCorasick ac = new AhoCorasick(List.of("хуй"));
        assertTrue(ac.matches("это хуй"));
        assertTrue(ac.matches("хуй"));
        assertFalse(ac.matches("привет"));
    }

    @Test
    void matches_multipleWords() {
        AhoCorasick ac = new AhoCorasick(List.of("хуй", "пизда", "ебать"));
        assertTrue(ac.matches("старый хуй"));
        assertTrue(ac.matches("полная пизда"));
        assertTrue(ac.matches("ебать копать"));
        assertFalse(ac.matches("нормальный текст"));
    }

    @Test
    void matches_overlappingWords() {
        AhoCorasick ac = new AhoCorasick(List.of("хуй", "хуе"));
        assertTrue(ac.matches("хуе"));
        assertTrue(ac.matches("хуй"));
    }

    @Test
    void matches_subword() {
        AhoCorasick ac = new AhoCorasick(List.of("хуй"));
        assertFalse(ac.matches("ху"));
        assertFalse(ac.matches(""));
    }

    @Test
    void matches_latinToCyrillicSubstitution() {
        AhoCorasick ac = new AhoCorasick(List.of("хуй", "пизда"));
        assertTrue(ac.matches("xуй"));
        assertTrue(ac.matches("xyй"));
        assertTrue(ac.matches("хyй"));
    }

    @Test
    void matches_caseInsensitive() {
        AhoCorasick ac = new AhoCorasick(List.of("хуй"));
        assertTrue(ac.matches("ХУЙ"));
        assertTrue(ac.matches("Хуй"));
        assertTrue(ac.matches("хуй"));
    }

    @Test
    void matches_mixedSubstitutions() {
        AhoCorasick ac = new AhoCorasick(List.of("ебать"));
        assertTrue(ac.matches("3бать"));
        assertTrue(ac.matches("ебaть"));
    }

    @Test
    void replaceAll_replacesMatches() {
        AhoCorasick ac = new AhoCorasick(List.of("хуй", "пизда"));
        assertEquals("*** текст", ac.replaceAll("хуй текст", "***"));
        assertEquals("*** и ***", ac.replaceAll("хуй и пизда", "***"));
    }

    @Test
    void replaceAll_noMatch() {
        AhoCorasick ac = new AhoCorasick(List.of("хуй"));
        assertEquals("чистый текст", ac.replaceAll("чистый текст", "***"));
    }

    @Test
    void replaceAll_nonOverlapping() {
        AhoCorasick ac = new AhoCorasick(List.of("хуй", "пизда"));
        assertEquals("*** ***", ac.replaceAll("хуй пизда", "***"));
    }

    @Test
    void findMatches_returnsAll() {
        AhoCorasick ac = new AhoCorasick(List.of("хуй", "пизда", "ебать"));
        List<String> found = ac.findMatches("хуй и пизда и ебать");
        assertTrue(found.contains("хуй"));
        assertTrue(found.contains("пизда"));
        assertTrue(found.contains("ебать"));
    }

    @Test
    void findMatches_empty() {
        AhoCorasick ac = new AhoCorasick(List.of("хуй"));
        assertTrue(ac.findMatches("чистый текст").isEmpty());
    }

    @Test
    void emptyWordList() {
        AhoCorasick ac = new AhoCorasick(List.of());
        assertFalse(ac.matches("любой текст"));
        assertEquals("любой текст", ac.replaceAll("любой текст", "***"));
    }

    @Test
    void emptyStringInWordList() {
        AhoCorasick ac = new AhoCorasick(List.of(""));
        assertFalse(ac.matches("тест"));
    }

    @Test
    void matches_longText() {
        AhoCorasick ac = new AhoCorasick(List.of("хуй", "пизда"));
        assertTrue(ac.matches("a".repeat(1000) + "хуй" + "b".repeat(1000)));
    }

    @Test
    void matches_specialCharacters() {
        AhoCorasick ac = new AhoCorasick(List.of("хуй"));
        assertTrue(ac.matches(",хуй."));
        assertTrue(ac.matches("текст с xуй внутри"));
    }

    @Test
    void matches_insideWord() {
        AhoCorasick ac = new AhoCorasick(List.of("хуй"));
        assertTrue(ac.matches("хуйтекст"));
        assertTrue(ac.matches("текстхуйтекст"));
    }

    @Test
    void matches_punctuationAround() {
        AhoCorasick ac = new AhoCorasick(List.of("хуй"));
        assertTrue(ac.matches("хуй,"));
        assertTrue(ac.matches("(хуй)"));
        assertTrue(ac.matches("\"хуй\""));
    }

    @Test
    void replaceAll_partialWord() {
        AhoCorasick ac = new AhoCorasick(List.of("хуй"));
        assertEquals("***ня", ac.replaceAll("хуйня", "***"));
    }
}
