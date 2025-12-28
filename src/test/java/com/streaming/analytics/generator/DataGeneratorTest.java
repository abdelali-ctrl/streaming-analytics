package com.streaming.analytics.generator;

import com.streaming.analytics.model.Video;
import com.streaming.analytics.model.ViewEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for DataGenerator
 */
class DataGeneratorTest {

    private DataGenerator generator;

    @BeforeEach
    void setUp() {
        generator = new DataGenerator();
    }

    @Test
    @DisplayName("Should generate a valid ViewEvent with all fields populated")
    void testGenerateEvent() {
        ViewEvent event = generator.generateEvent();

        assertNotNull(event, "Event should not be null");
        assertNotNull(event.getEventId(), "Event ID should not be null");
        assertTrue(event.getEventId().startsWith("evt_"), "Event ID should start with 'evt_'");

        assertNotNull(event.getUserId(), "User ID should not be null");
        assertTrue(event.getUserId().startsWith("user_"), "User ID should start with 'user_'");

        assertNotNull(event.getVideoId(), "Video ID should not be null");
        assertTrue(event.getVideoId().startsWith("video_"), "Video ID should start with 'video_'");

        assertNotNull(event.getTimestamp(), "Timestamp should not be null");
        assertTrue(event.getTimestamp().isBefore(Instant.now().plus(1, ChronoUnit.MINUTES)),
                "Timestamp should be in the past or present");

        assertNotNull(event.getAction(), "Action should not be null");
        assertTrue(List.of("WATCH", "PAUSE", "STOP", "RESUME", "SEEK").contains(event.getAction()),
                "Action should be one of the valid actions");

        assertTrue(event.getDuration() >= 0, "Duration should be non-negative");

        assertNotNull(event.getQuality(), "Quality should not be null");
        assertTrue(List.of("360p", "480p", "720p", "1080p", "4K").contains(event.getQuality()),
                "Quality should be one of the valid qualities");

        assertNotNull(event.getDeviceType(), "Device type should not be null");
        assertTrue(List.of("mobile", "desktop", "tablet", "tv", "console").contains(event.getDeviceType()),
                "Device type should be one of the valid types");
    }

    @Test
    @DisplayName("Should generate a valid Video with all fields populated")
    void testGenerateVideo() {
        Video video = generator.generateVideo(123);

        assertNotNull(video, "Video should not be null");
        assertEquals("video_123", video.getVideoId(), "Video ID should match input");

        assertNotNull(video.getTitle(), "Title should not be null");
        assertFalse(video.getTitle().isEmpty(), "Title should not be empty");

        assertNotNull(video.getCategory(), "Category should not be null");
        assertTrue(List
                .of("Action", "Comedy", "Drama", "Documentary", "Sci-Fi", "Horror", "Romance", "Thriller", "Animation",
                        "Adventure")
                .contains(video.getCategory()), "Category should be valid");

        assertTrue(video.getDuration() >= 5400 && video.getDuration() <= 10800,
                "Duration should be between 1.5 hours and 3 hours");

        assertNotNull(video.getUploadDate(), "Upload date should not be null");
        assertTrue(video.getUploadDate().isBefore(Instant.now().plus(1, ChronoUnit.MINUTES)),
                "Upload date should be in the past or present");

        assertTrue(video.getViews() >= 100, "Views should be at least 100");
        assertTrue(video.getLikes() >= 10, "Likes should be at least 10");
    }

    @Test
    @DisplayName("Should generate the specified number of events")
    void testGenerateEvents() {
        int count = 50;
        List<ViewEvent> events = generator.generateEvents(count);

        assertNotNull(events, "Events list should not be null");
        assertEquals(count, events.size(), "Should generate exact number of events");

        // Verify all events are unique
        long uniqueEventIds = events.stream()
                .map(ViewEvent::getEventId)
                .distinct()
                .count();
        assertEquals(count, uniqueEventIds, "All event IDs should be unique");
    }

    @Test
    @DisplayName("Should generate video catalog with correct size")
    void testGenerateVideoCatalog() {
        int count = 20;
        List<Video> videos = generator.generateVideoCatalog(count);

        assertNotNull(videos, "Videos list should not be null");
        assertEquals(count, videos.size(), "Should generate exact number of videos");

        // Verify video IDs are sequential
        for (int i = 0; i < count; i++) {
            assertEquals("video_" + (i + 1), videos.get(i).getVideoId(),
                    "Video IDs should be sequential starting from 1");
        }
    }

    @Test
    @DisplayName("WATCH action should have longer duration than other actions")
    void testWatchActionDuration() {
        int watchEvents = 0;
        int otherEvents = 0;
        long totalWatchDuration = 0;
        long totalOtherDuration = 0;

        // Generate enough events to get statistical significance
        for (int i = 0; i < 1000; i++) {
            ViewEvent event = generator.generateEvent();
            if ("WATCH".equals(event.getAction())) {
                watchEvents++;
                totalWatchDuration += event.getDuration();
            } else {
                otherEvents++;
                totalOtherDuration += event.getDuration();
            }
        }

        if (watchEvents > 0 && otherEvents > 0) {
            double avgWatchDuration = (double) totalWatchDuration / watchEvents;
            double avgOtherDuration = (double) totalOtherDuration / otherEvents;

            assertTrue(avgWatchDuration > avgOtherDuration,
                    "WATCH actions should have longer average duration");
        }
    }
}
