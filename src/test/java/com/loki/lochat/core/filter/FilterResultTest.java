package com.loki.lochat.core.filter;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FilterResultTest {

    @Test
    void ok_allowed() {
        FilterResult r = FilterResult.ok("hello");
        assertTrue(r.allowed());
        assertNull(r.blockReason());
        assertEquals("hello", r.filteredMessage());
    }

    @Test
    void ok_getMessage() {
        FilterResult r = FilterResult.ok("hello");
        assertEquals("hello", r.getMessage());
    }

    @Test
    void blocked_notAllowed() {
        FilterResult r = FilterResult.blocked("bad word");
        assertFalse(r.allowed());
        assertEquals("bad word", r.blockReason());
        assertNull(r.filteredMessage());
    }

    @Test
    void ok_nullMessage() {
        FilterResult r = FilterResult.ok(null);
        assertTrue(r.allowed());
        assertNull(r.filteredMessage());
    }

    @Test
    void blocked_nullReason() {
        FilterResult r = FilterResult.blocked(null);
        assertFalse(r.allowed());
        assertNull(r.blockReason());
    }

    @Test
    void record_equals() {
        FilterResult a = FilterResult.ok("hello");
        FilterResult b = FilterResult.ok("hello");
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void record_notEquals() {
        FilterResult a = FilterResult.ok("hello");
        FilterResult b = FilterResult.blocked("bad");
        assertNotEquals(a, b);
    }

    @Test
    void record_toString() {
        FilterResult r = FilterResult.ok("test");
        assertTrue(r.toString().contains("test"));
    }
}
