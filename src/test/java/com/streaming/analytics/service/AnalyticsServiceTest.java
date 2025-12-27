package com.streaming.analytics.service;

import com.streaming.analytics.model.Video;
import com.streaming.analytics.model.VideoStats;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for AnalyticsService inner classes and utility methods
 */
class AnalyticsServiceTest {

    @Test
    @DisplayName("CategoryStats should store and return correct values")
    void testCategoryStats() {
        AnalyticsService.CategoryStats stats = new AnalyticsService.CategoryStats(
                "Action", 150, 50000L, 125.5);

        assertEquals("Action", stats.getCategory());
        assertEquals(150, stats.getVideoCount());
        assertEquals(50000L, stats.getTotalViews());
        assertEquals(125.5, stats.getAvgDuration(), 0.01);
    }

    @Test
    @DisplayName("TrendingVideo should store and return correct values")
    void testTrendingVideo() {
        Video video = new Video();
        video.setVideoId("video_123");
        video.setTitle("Test Video");
        video.setCategory("Comedy");

        AnalyticsService.TrendingVideo trending = new AnalyticsService.TrendingVideo(
                video, 500L, 2.5, 1000L);

        assertEquals(video, trending.getVideo());
        assertEquals(500L, trending.getViews24h());
        assertEquals(2.5, trending.getTrendScore(), 0.01);
        assertEquals(1000L, trending.getTotalViews());
    }

    @Test
    @DisplayName("DashboardSummary should store and return correct values")
    void testDashboardSummary() {
        java.util.List<VideoStats> topVideos = java.util.Arrays.asList(
                createVideoStats("video_1", 1000),
                createVideoStats("video_2", 500));

        java.util.Map<String, AnalyticsService.CategoryStats> categoryStats = new java.util.HashMap<>();
        categoryStats.put("Action", new AnalyticsService.CategoryStats("Action", 10, 5000L, 120.0));
        categoryStats.put("Comedy", new AnalyticsService.CategoryStats("Comedy", 15, 7500L, 90.0));

        AnalyticsService.DashboardSummary summary = new AnalyticsService.DashboardSummary(
                100000L, 10000L, topVideos, categoryStats);

        assertEquals(100000L, summary.getTotalEvents());
        assertEquals(10000L, summary.getTotalVideos());
        assertEquals(2, summary.getTopVideos().size());
        assertEquals(2, summary.getCategoryStats().size());
    }

    @Test
    @DisplayName("VideoStats should correctly calculate unique viewers")
    void testVideoStatsUniqueViewers() {
        VideoStats stats = new VideoStats();
        stats.setVideoId("video_test");
        stats.setTotalViews(1500L);
        stats.setAvgDuration(180.5);
        stats.setUniqueViewers(120);
        stats.setLastUpdated(Instant.now());

        assertEquals("video_test", stats.getVideoId());
        assertEquals(1500L, stats.getTotalViews());
        assertEquals(180.5, stats.getAvgDuration(), 0.01);
        assertEquals(120, stats.getUniqueViewers());
        assertNotNull(stats.getLastUpdated());
    }

    @Test
    @DisplayName("Video should store all metadata correctly")
    void testVideoMetadata() {
        Video video = new Video();
        Instant uploadDate = Instant.now();

        video.setVideoId("video_456");
        video.setTitle("Epic Adventure 456");
        video.setCategory("Action");
        video.setDuration(3600);
        video.setUploadDate(uploadDate);
        video.setViews(500000);
        video.setLikes(25000);

        assertEquals("video_456", video.getVideoId());
        assertEquals("Epic Adventure 456", video.getTitle());
        assertEquals("Action", video.getCategory());
        assertEquals(3600, video.getDuration());
        assertEquals(uploadDate, video.getUploadDate());
        assertEquals(500000, video.getViews());
        assertEquals(25000, video.getLikes());
    }

    private VideoStats createVideoStats(String videoId, long views) {
        VideoStats stats = new VideoStats();
        stats.setVideoId(videoId);
        stats.setTotalViews(views);
        stats.setAvgDuration(120.0);
        stats.setUniqueViewers(views > 100 ? (int) (views / 10) : 10);
        stats.setLastUpdated(Instant.now());
        return stats;
    }
}
