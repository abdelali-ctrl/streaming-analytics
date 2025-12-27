package com.streaming.analytics.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ViewEvent model
 */
class ViewEventTest {

    @Test
    @DisplayName("ViewEvent should store and return all fields correctly")
    void testViewEventFields() {
        ViewEvent event = new ViewEvent();
        Instant now = Instant.now();

        event.setEventId("evt_abc123");
        event.setUserId("user_456");
        event.setVideoId("video_789");
        event.setTimestamp(now);
        event.setAction("WATCH");
        event.setDuration(1800);
        event.setQuality("1080p");
        event.setDeviceType("desktop");

        assertEquals("evt_abc123", event.getEventId());
        assertEquals("user_456", event.getUserId());
        assertEquals("video_789", event.getVideoId());
        assertEquals(now, event.getTimestamp());
        assertEquals("WATCH", event.getAction());
        assertEquals(1800, event.getDuration());
        assertEquals("1080p", event.getQuality());
        assertEquals("desktop", event.getDeviceType());
    }

    @Test
    @DisplayName("ViewEvent should handle null values gracefully")
    void testViewEventNullValues() {
        ViewEvent event = new ViewEvent();

        assertNull(event.getEventId());
        assertNull(event.getUserId());
        assertNull(event.getVideoId());
        assertNull(event.getTimestamp());
        assertNull(event.getAction());
        assertEquals(0, event.getDuration());
        assertNull(event.getQuality());
        assertNull(event.getDeviceType());
    }

    @Test
    @DisplayName("ViewEvent duration should accept zero and positive values")
    void testViewEventDuration() {
        ViewEvent event = new ViewEvent();

        event.setDuration(0);
        assertEquals(0, event.getDuration());

        event.setDuration(3600);
        assertEquals(3600, event.getDuration());
    }
}
