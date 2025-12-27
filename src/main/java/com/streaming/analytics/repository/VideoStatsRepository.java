package com.streaming.analytics.repository;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.ReturnDocument;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.model.Updates;
import com.streaming.analytics.model.VideoStats;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * CDI Repository for VideoStats entities
 * Uses Document-based queries to avoid POJO codec issues with aggregated data
 */
@ApplicationScoped
public class VideoStatsRepository {

        private static final Logger logger = LoggerFactory.getLogger(VideoStatsRepository.class);
        private static final String COLLECTION_NAME = "video_stats";

        @Inject
        private MongoDatabase database;

        private MongoCollection<Document> collection;

        @PostConstruct
        public void init() {
                this.collection = database.getCollection(COLLECTION_NAME);
                logger.info("VideoStatsRepository initialized with collection: {}", COLLECTION_NAME);
        }

        /**
         * Converts a Document to VideoStats
         */
        private VideoStats documentToVideoStats(Document doc) {
                if (doc == null)
                        return null;

                VideoStats stats = new VideoStats();
                stats.setVideoId(doc.getString("videoId"));

                // Handle numeric types flexibly (can be Integer, Long, or Double)
                Number totalViews = (Number) doc.get("totalViews");
                stats.setTotalViews(totalViews != null ? totalViews.longValue() : 0L);

                Number avgDuration = (Number) doc.get("avgDuration");
                stats.setAvgDuration(avgDuration != null ? avgDuration.doubleValue() : 0.0);

                Number uniqueViewers = (Number) doc.get("uniqueViewers");
                stats.setUniqueViewers(uniqueViewers != null ? uniqueViewers.longValue() : 0L);

                // Handle lastUpdated - can be Date or other types
                Object lastUpdated = doc.get("lastUpdated");
                if (lastUpdated instanceof Date) {
                        stats.setLastUpdated(((Date) lastUpdated).toInstant());
                } else if (lastUpdated instanceof Instant) {
                        stats.setLastUpdated((Instant) lastUpdated);
                } else {
                        stats.setLastUpdated(Instant.now());
                }

                return stats;
        }

        /**
         * Gets statistics for a specific video
         */
        public VideoStats getStats(String videoId) {
                Document doc = collection.find(Filters.eq("videoId", videoId)).first();
                return documentToVideoStats(doc);
        }

        /**
         * Updates statistics when a new view event is processed
         */
        public VideoStats updateStats(String videoId, int watchDuration) {
                VideoStats currentStats = getStats(videoId);

                double newAvgDuration;
                long newTotalViews;

                if (currentStats == null) {
                        newTotalViews = 1;
                        newAvgDuration = watchDuration;
                } else {
                        long previousTotalDuration = (long) (currentStats.getAvgDuration()
                                        * currentStats.getTotalViews());
                        newTotalViews = currentStats.getTotalViews() + 1;
                        newAvgDuration = (previousTotalDuration + watchDuration) / (double) newTotalViews;
                }

                FindOneAndUpdateOptions options = new FindOneAndUpdateOptions()
                                .upsert(true)
                                .returnDocument(ReturnDocument.AFTER);

                Document result = collection.findOneAndUpdate(
                                Filters.eq("videoId", videoId),
                                Updates.combine(
                                                Updates.set("videoId", videoId),
                                                Updates.set("totalViews", newTotalViews),
                                                Updates.set("avgDuration", newAvgDuration),
                                                Updates.set("lastUpdated", new Date())),
                                options);

                return documentToVideoStats(result);
        }

        /**
         * Gets the top videos by total views
         */
        public List<VideoStats> getTopVideos(int limit) {
                List<VideoStats> results = new ArrayList<>();

                for (Document doc : collection.find()
                                .sort(Sorts.descending("totalViews"))
                                .limit(limit)) {
                        results.add(documentToVideoStats(doc));
                }

                return results;
        }

        /**
         * Gets videos updated within a time range (for trending detection)
         */
        public List<VideoStats> findRecentlyUpdated(Instant since) {
                List<VideoStats> results = new ArrayList<>();

                for (Document doc : collection.find(Filters.gte("lastUpdated", Date.from(since)))
                                .sort(Sorts.descending("totalViews"))) {
                        results.add(documentToVideoStats(doc));
                }

                return results;
        }

        /**
         * Increments the unique viewers count
         */
        public void incrementUniqueViewers(String videoId) {
                collection.updateOne(
                                Filters.eq("videoId", videoId),
                                Updates.inc("uniqueViewers", 1));
        }

        /**
         * Saves or updates video stats
         */
        public void save(VideoStats stats) {
                FindOneAndUpdateOptions options = new FindOneAndUpdateOptions()
                                .upsert(true);

                collection.findOneAndUpdate(
                                Filters.eq("videoId", stats.getVideoId()),
                                Updates.combine(
                                                Updates.set("videoId", stats.getVideoId()),
                                                Updates.set("totalViews", stats.getTotalViews()),
                                                Updates.set("avgDuration", stats.getAvgDuration()),
                                                Updates.set("uniqueViewers", stats.getUniqueViewers()),
                                                Updates.set("lastUpdated",
                                                                stats.getLastUpdated() != null
                                                                                ? Date.from(stats.getLastUpdated())
                                                                                : new Date())),
                                options);
        }
}
