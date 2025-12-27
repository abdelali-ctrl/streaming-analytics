package com.streaming.analytics.service;

import com.streaming.analytics.model.UserProfile;
import com.streaming.analytics.model.Video;
import com.streaming.analytics.model.VideoStats;
import com.streaming.analytics.model.ViewEvent;
import com.streaming.analytics.repository.EventRepository;
import com.streaming.analytics.repository.UserProfileRepository;
import com.streaming.analytics.repository.VideoRepository;
import com.streaming.analytics.repository.VideoStatsRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Main service for processing view events
 * Handles event ingestion, statistics updates, and user profile updates
 */
@ApplicationScoped
public class EventProcessorService {

    private static final Logger logger = LoggerFactory.getLogger(EventProcessorService.class);

    @Inject
    private EventRepository eventRepository;

    @Inject
    private VideoStatsRepository statsRepository;

    @Inject
    private UserProfileRepository userProfileRepository;

    @Inject
    private VideoRepository videoRepository;

    /**
     * Processes a single view event
     * 1. Saves the event
     * 2. Updates video statistics
     * 3. Updates user profile
     */
    public void processEvent(ViewEvent event) {
        try {
            // 1. Save the event
            if (event.getTimestamp() == null) {
                event.setTimestamp(Instant.now());
            }
            eventRepository.save(event);

            // 2. Update video statistics (only for WATCH actions)
            if ("WATCH".equals(event.getAction())) {
                statsRepository.updateStats(event.getVideoId(), event.getDuration());
            }

            // 3. Update user profile
            updateUserProfile(event);

            logger.debug("Processed event: {} for user: {}", event.getEventId(), event.getUserId());

        } catch (Exception e) {
            logger.error("Error processing event: {}", event.getEventId(), e);
            throw new RuntimeException("Failed to process event", e);
        }
    }

    /**
     * Processes a batch of events efficiently
     * Uses bulk insert for better performance
     */
    public int processBatch(List<ViewEvent> events) {
        if (events == null || events.isEmpty()) {
            return 0;
        }

        try {
            long startTime = System.currentTimeMillis();

            // Set timestamps for events without one
            for (ViewEvent event : events) {
                if (event.getTimestamp() == null) {
                    event.setTimestamp(Instant.now());
                }
            }

            // 1. Bulk save all events
            eventRepository.saveBatch(events);

            // 2. Update statistics for each video (batch optimization)
            for (ViewEvent event : events) {
                if ("WATCH".equals(event.getAction())) {
                    statsRepository.updateStats(event.getVideoId(), event.getDuration());
                }
            }

            // 3. Update user profiles
            for (ViewEvent event : events) {
                updateUserProfile(event);
            }

            long duration = System.currentTimeMillis() - startTime;
            logger.info("Processed batch of {} events in {}ms ({} events/sec)",
                    events.size(), duration, (events.size() * 1000.0 / duration));

            return events.size();

        } catch (Exception e) {
            logger.error("Error processing batch of {} events", events.size(), e);
            throw new RuntimeException("Failed to process batch", e);
        }
    }

    /**
     * Updates user profile with watch event data
     */
    private void updateUserProfile(ViewEvent event) {
        try {
            // Get video category for preferences
            Video video = videoRepository.findByVideoId(event.getVideoId());
            String category = (video != null) ? video.getCategory() : "Unknown";

            // Update user profile
            userProfileRepository.updateWithWatch(
                    event.getUserId(),
                    event.getVideoId(),
                    category,
                    event.getDuration());
        } catch (Exception e) {
            logger.warn("Failed to update user profile for user: {}", event.getUserId(), e);
        }
    }

    /**
     * Gets the top videos by total views
     */
    public List<VideoStats> getTopVideos(int limit) {
        return statsRepository.getTopVideos(limit);
    }

    /**
     * Gets statistics for a specific video
     */
    public VideoStats getVideoStats(String videoId) {
        return statsRepository.getStats(videoId);
    }

    /**
     * Generates personalized recommendations for a user
     * Based on: watch history, preferences, and popular videos in preferred
     * categories
     */
    public List<Video> getRecommendations(String userId, int limit) {
        try {
            // 1. Get user profile
            UserProfile profile = userProfileRepository.findByUserId(userId);
            if (profile == null) {
                // New user - return popular videos
                return videoRepository.findMostPopular(limit);
            }

            // 2. Get user's top categories
            List<String> topCategories = profile.getTopCategories(3);
            if (topCategories.isEmpty()) {
                return videoRepository.findMostPopular(limit);
            }

            // 3. Get popular videos from preferred categories
            List<Video> recommendations = new ArrayList<>();
            List<String> watchHistory = profile.getWatchHistory();

            for (String category : topCategories) {
                List<Video> categoryVideos = videoRepository.findByCategory(category, limit);

                // Filter out already watched videos
                for (Video video : categoryVideos) {
                    if (!watchHistory.contains(video.getVideoId()) &&
                            recommendations.size() < limit) {
                        recommendations.add(video);
                    }
                }

                if (recommendations.size() >= limit) {
                    break;
                }
            }

            // 4. If not enough recommendations, add popular videos
            if (recommendations.size() < limit) {
                List<Video> popular = videoRepository.findMostPopular(limit);
                for (Video video : popular) {
                    if (!watchHistory.contains(video.getVideoId()) &&
                            !recommendations.contains(video) &&
                            recommendations.size() < limit) {
                        recommendations.add(video);
                    }
                }
            }

            logger.debug("Generated {} recommendations for user: {}", recommendations.size(), userId);
            return recommendations;

        } catch (Exception e) {
            logger.error("Error generating recommendations for user: {}", userId, e);
            return videoRepository.findMostPopular(limit);
        }
    }

    /**
     * Gets total event count
     */
    public long getTotalEventCount() {
        return eventRepository.count();
    }
}
