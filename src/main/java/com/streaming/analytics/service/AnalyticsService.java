package com.streaming.analytics.service;

import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Accumulators;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import com.streaming.analytics.model.Video;
import com.streaming.analytics.model.VideoStats;
import com.streaming.analytics.repository.VideoRepository;
import com.streaming.analytics.repository.VideoStatsRepository;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.Date;

/**
 * Analytics service for aggregations and trend detection
 * Provides MapReduce-like operations for Big Data processing
 */
@ApplicationScoped
public class AnalyticsService {

    private static final Logger logger = LoggerFactory.getLogger(AnalyticsService.class);

    @Inject
    private MongoDatabase database;

    @Inject
    private VideoRepository videoRepository;

    @Inject
    private VideoStatsRepository statsRepository;

    private MongoCollection<Document> eventsCollection;

    @PostConstruct
    public void init() {
        this.eventsCollection = database.getCollection("events");
        logger.info("AnalyticsService initialized");
    }

    /**
     * Aggregates statistics by video category
     * Uses MongoDB aggregation pipeline (MapReduce-like)
     */
    public Map<String, CategoryStats> aggregateByCategory() {
        Map<String, CategoryStats> result = new HashMap<>();

        try {
            // Get all categories from videos
            List<String> categories = videoRepository.getAllCategories();

            for (String category : categories) {
                // Get videos in this category
                List<Video> videos = videoRepository.findByCategory(category);

                long totalViews = 0;
                double totalDuration = 0;
                int videoCount = videos.size();

                // Aggregate stats for each video in category
                for (Video video : videos) {
                    VideoStats stats = statsRepository.getStats(video.getVideoId());
                    if (stats != null) {
                        totalViews += stats.getTotalViews();
                        totalDuration += stats.getAvgDuration() * stats.getTotalViews();
                    }
                }

                double avgDuration = totalViews > 0 ? totalDuration / totalViews : 0;

                result.put(category, new CategoryStats(category, videoCount, totalViews, avgDuration));
            }

            logger.debug("Aggregated stats for {} categories", result.size());

        } catch (Exception e) {
            logger.error("Error aggregating by category", e);
        }

        return result;
    }

    /**
     * Aggregates events by time period using MongoDB aggregation
     */
    public List<Document> aggregateByTimePeriod(String period) {
        List<Document> results = new ArrayList<>();

        try {
            String dateFormat;
            switch (period.toLowerCase()) {
                case "hour":
                    dateFormat = "%Y-%m-%d %H:00";
                    break;
                case "day":
                    dateFormat = "%Y-%m-%d";
                    break;
                case "month":
                    dateFormat = "%Y-%m";
                    break;
                default:
                    dateFormat = "%Y-%m-%d";
            }

            AggregateIterable<Document> aggregation = eventsCollection.aggregate(Arrays.asList(
                    Aggregates.group(
                            new Document("$dateToString",
                                    new Document("format", dateFormat)
                                            .append("date", "$timestamp")),
                            Accumulators.sum("count", 1),
                            Accumulators.avg("avgDuration", "$duration")),
                    Aggregates.sort(Sorts.descending("_id")),
                    Aggregates.limit(24)));

            for (Document doc : aggregation) {
                results.add(doc);
            }

        } catch (Exception e) {
            logger.error("Error aggregating by time period", e);
        }

        return results;
    }

    /**
     * Detects trending videos
     * Compares views in last 24 hours vs previous 7 days average
     */
    public List<TrendingVideo> detectTrending(int limit) {
        List<TrendingVideo> trending = new ArrayList<>();

        try {
            Instant now = Instant.now();
            Instant last24h = now.minus(24, ChronoUnit.HOURS);
            Instant last7d = now.minus(7, ChronoUnit.DAYS);

            // Get recently updated videos
            List<VideoStats> recentStats = statsRepository.findRecentlyUpdated(last24h);

            for (VideoStats stats : recentStats) {
                // Count views in last 24 hours
                long views24h = countViewsInPeriod(stats.getVideoId(), last24h, now);

                // Count views in previous 7 days (daily average)
                long views7d = countViewsInPeriod(stats.getVideoId(), last7d, last24h);
                double dailyAvg7d = views7d / 7.0;

                // Calculate trending score (how much above average)
                double trendScore = dailyAvg7d > 0 ? (views24h / dailyAvg7d) : views24h;

                if (trendScore > 1.5) { // 50% above average = trending
                    Video video = videoRepository.findByVideoId(stats.getVideoId());
                    if (video != null) {
                        trending.add(new TrendingVideo(
                                video,
                                views24h,
                                trendScore,
                                stats.getTotalViews()));
                    }
                }
            }

            // Sort by trend score and limit
            trending.sort((a, b) -> Double.compare(b.getTrendScore(), a.getTrendScore()));
            if (trending.size() > limit) {
                trending = trending.subList(0, limit);
            }

            logger.debug("Detected {} trending videos", trending.size());

        } catch (Exception e) {
            logger.error("Error detecting trending videos", e);
        }

        return trending;
    }

    /**
     * Counts views for a video in a time period
     */
    private long countViewsInPeriod(String videoId, Instant start, Instant end) {
        return eventsCollection.countDocuments(
                Filters.and(
                        Filters.eq("videoId", videoId),
                        Filters.eq("action", "WATCH"),
                        Filters.gte("timestamp", start),
                        Filters.lt("timestamp", end)));
    }

    /**
     * Gets dashboard summary statistics
     */
    public DashboardSummary getDashboardSummary() {
        try {
            long totalEvents = eventsCollection.countDocuments();
            long totalVideos = videoRepository.count();

            // Get top 5 videos
            List<VideoStats> topVideos = statsRepository.getTopVideos(5);

            // Get category breakdown
            Map<String, CategoryStats> categoryStats = aggregateByCategory();

            return new DashboardSummary(totalEvents, totalVideos, topVideos, categoryStats);

        } catch (Exception e) {
            logger.error("Error getting dashboard summary", e);
            return new DashboardSummary(0, 0, new ArrayList<>(), new HashMap<>());
        }
    }

    /**
     * Aggregates events by device type for chart
     */
    public Map<String, Long> aggregateByDevice() {
        Map<String, Long> result = new LinkedHashMap<>();

        try {
            AggregateIterable<Document> aggregation = eventsCollection.aggregate(Arrays.asList(
                    Aggregates.group("$deviceType", Accumulators.sum("count", 1)),
                    Aggregates.sort(Sorts.descending("count"))));

            for (Document doc : aggregation) {
                String device = doc.getString("_id");
                Number count = (Number) doc.get("count");
                if (device != null && count != null) {
                    result.put(device, count.longValue());
                }
            }

            logger.debug("Aggregated {} device types", result.size());

        } catch (Exception e) {
            logger.error("Error aggregating by device", e);
        }

        return result;
    }

    /**
     * Gets hourly event counts for the last 24 hours (for line chart)
     */
    public Map<String, Long> getHourlyStats() {
        Map<String, Long> result = new LinkedHashMap<>();

        try {
            Instant now = Instant.now();
            Instant last24h = now.minus(24, ChronoUnit.HOURS);

            AggregateIterable<Document> aggregation = eventsCollection.aggregate(Arrays.asList(
                    Aggregates.match(Filters.gte("timestamp", Date.from(last24h))),
                    Aggregates.group(
                            new Document("$dateToString",
                                    new Document("format", "%H:00")
                                            .append("date", "$timestamp")),
                            Accumulators.sum("count", 1)),
                    Aggregates.sort(Sorts.ascending("_id"))));

            for (Document doc : aggregation) {
                String hour = doc.getString("_id");
                Number count = (Number) doc.get("count");
                if (hour != null && count != null) {
                    result.put(hour, count.longValue());
                }
            }

            logger.debug("Got {} hourly data points", result.size());

        } catch (Exception e) {
            logger.error("Error getting hourly stats", e);
        }

        return result;
    }

    /**
     * Aggregates events by video quality (360p, 720p, 1080p, 4K)
     */
    public Map<String, Long> aggregateByQuality() {
        Map<String, Long> result = new LinkedHashMap<>();

        try {
            AggregateIterable<Document> aggregation = eventsCollection.aggregate(Arrays.asList(
                    Aggregates.group("$quality", Accumulators.sum("count", 1)),
                    Aggregates.sort(Sorts.descending("count"))));

            for (Document doc : aggregation) {
                String quality = doc.getString("_id");
                Number count = (Number) doc.get("count");
                if (quality != null && count != null) {
                    result.put(quality, count.longValue());
                }
            }

            logger.debug("Aggregated {} quality levels", result.size());

        } catch (Exception e) {
            logger.error("Error aggregating by quality", e);
        }

        return result;
    }

    /**
     * Aggregates events by action type (WATCH, PAUSE, STOP, etc.)
     */
    public Map<String, Long> aggregateByAction() {
        Map<String, Long> result = new LinkedHashMap<>();

        try {
            AggregateIterable<Document> aggregation = eventsCollection.aggregate(Arrays.asList(
                    Aggregates.group("$action", Accumulators.sum("count", 1)),
                    Aggregates.sort(Sorts.descending("count"))));

            for (Document doc : aggregation) {
                String action = doc.getString("_id");
                Number count = (Number) doc.get("count");
                if (action != null && count != null) {
                    result.put(action, count.longValue());
                }
            }

            logger.debug("Aggregated {} action types", result.size());

        } catch (Exception e) {
            logger.error("Error aggregating by action", e);
        }

        return result;
    }

    /**
     * Gets average watch duration by category (engagement metric)
     */
    public Map<String, Double> getAvgDurationByCategory() {
        Map<String, Double> result = new LinkedHashMap<>();

        try {
            Map<String, CategoryStats> categoryStats = aggregateByCategory();
            for (Map.Entry<String, CategoryStats> entry : categoryStats.entrySet()) {
                result.put(entry.getKey(), entry.getValue().getAvgDuration());
            }

            logger.debug("Got avg duration for {} categories", result.size());

        } catch (Exception e) {
            logger.error("Error getting avg duration by category", e);
        }

        return result;
    }

    /**
     * Inner class for category statistics
     */
    public static class CategoryStats {
        private final String category;
        private final int videoCount;
        private final long totalViews;
        private final double avgDuration;

        public CategoryStats(String category, int videoCount, long totalViews, double avgDuration) {
            this.category = category;
            this.videoCount = videoCount;
            this.totalViews = totalViews;
            this.avgDuration = avgDuration;
        }

        public String getCategory() {
            return category;
        }

        public int getVideoCount() {
            return videoCount;
        }

        public long getTotalViews() {
            return totalViews;
        }

        public double getAvgDuration() {
            return avgDuration;
        }
    }

    /**
     * Inner class for trending video data
     */
    public static class TrendingVideo {
        private final Video video;
        private final long views24h;
        private final double trendScore;
        private final long totalViews;

        public TrendingVideo(Video video, long views24h, double trendScore, long totalViews) {
            this.video = video;
            this.views24h = views24h;
            this.trendScore = trendScore;
            this.totalViews = totalViews;
        }

        public Video getVideo() {
            return video;
        }

        public long getViews24h() {
            return views24h;
        }

        public double getTrendScore() {
            return trendScore;
        }

        public long getTotalViews() {
            return totalViews;
        }
    }

    /**
     * Inner class for dashboard summary
     */
    public static class DashboardSummary {
        private final long totalEvents;
        private final long totalVideos;
        private final List<VideoStats> topVideos;
        private final Map<String, CategoryStats> categoryStats;

        public DashboardSummary(long totalEvents, long totalVideos,
                List<VideoStats> topVideos, Map<String, CategoryStats> categoryStats) {
            this.totalEvents = totalEvents;
            this.totalVideos = totalVideos;
            this.topVideos = topVideos;
            this.categoryStats = categoryStats;
        }

        public long getTotalEvents() {
            return totalEvents;
        }

        public long getTotalVideos() {
            return totalVideos;
        }

        public List<VideoStats> getTopVideos() {
            return topVideos;
        }

        public Map<String, CategoryStats> getCategoryStats() {
            return categoryStats;
        }
    }
}
