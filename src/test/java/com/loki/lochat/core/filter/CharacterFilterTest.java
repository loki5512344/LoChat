package com.loki.lochat.core.filter;

import com.loki.lochat.core.filter.filters.CharacterFilter;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CharacterFilterTest {

    @Test
    void filter_limitsRepeatingChars() {
        YamlConfiguration config = new YamlConfiguration();
        config.set("filters.repeating-chars.enabled", true);
        config.set("filters.repeating-chars.max", 3);
        CharacterFilter filter = new CharacterFilter(config);
        assertEquals("привееет", filter.filter("привеееееет"));
    }

    @Test
    void filter_noChangeWhenUnderLimit() {
        YamlConfiguration config = new YamlConfiguration();
        config.set("filters.repeating-chars.enabled", true);
        config.set("filters.repeating-chars.max", 3);
        CharacterFilter filter = new CharacterFilter(config);
        assertEquals("привет", filter.filter("привет"));
    }

    @Test
    void filter_shortRepeatsAllowed() {
        YamlConfiguration config = new YamlConfiguration();
        config.set("filters.repeating-chars.enabled", true);
        config.set("filters.repeating-chars.max", 3);
        CharacterFilter filter = new CharacterFilter(config);
        assertEquals("ууупс", filter.filter("ууупс"));
    }

    @Test
    void filter_disabledReturnsOriginal() {
        YamlConfiguration config = new YamlConfiguration();
        config.set("filters.repeating-chars.enabled", false);
        CharacterFilter filter = new CharacterFilter(config);
        assertEquals("мноооого букв", filter.filter("мноооого букв"));
    }

    @Test
    void filter_zeroMaxDisabled() {
        YamlConfiguration config = new YamlConfiguration();
        config.set("filters.repeating-chars.enabled", true);
        config.set("filters.repeating-chars.max", 0);
        CharacterFilter filter = new CharacterFilter(config);
        assertEquals("теееекст", filter.filter("теееекст"));
    }

    @Test
    void filter_defaultConfig() {
        CharacterFilter filter = new CharacterFilter(new YamlConfiguration());
        assertEquals("тееест", filter.filter("теееест"));
    }

    @Test
    void filter_differentMax() {
        YamlConfiguration config = new YamlConfiguration();
        config.set("filters.repeating-chars.enabled", true);
        config.set("filters.repeating-chars.max", 1);
        CharacterFilter filter = new CharacterFilter(config);
        assertEquals("ап", filter.filter("ааап"));
    }

    @Test
    void filter_multiplePatterns() {
        YamlConfiguration config = new YamlConfiguration();
        config.set("filters.repeating-chars.enabled", true);
        config.set("filters.repeating-chars.max", 2);
        CharacterFilter filter = new CharacterFilter(config);
        assertEquals("aa bb", filter.filter("aaa bbb"));
    }
}
