package com.streaming.analytics.model;

import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.codecs.pojo.annotations.BsonProperty;
import org.bson.types.ObjectId;

import java.time.Instant;
import java.util.*;

/**
 * Entity representing a user profile with watch history and preferences
 * Maps to the 'user_profiles' collection in MongoDB
 */
public class UserProfile {

    @BsonId
    private ObjectId id;

    @BsonProperty("userId")
    private String userId;

    @BsonProperty("watchHistory")
    private List<String> watchHistory; // List of videoIds watched

    @BsonProperty("preferences")
    private Map<String, Integer> preferences; // Category -> watch count

    @BsonProperty("recommendedVideos")
    private List<String> recommendedVideos; // List of recommended videoIds

    @BsonProperty("lastActive")
    private Instant lastActive;

    @BsonProperty("totalWatchTime")
    private long totalWatchTime; // Total watch time in seconds

    // Default constructor required for MongoDB POJO codec
    public UserProfile() {
        this.watchHistory = new ArrayList<>();
        this.preferences = new HashMap<>();
        this.recommendedVideos = new ArrayList<>();
    }

    // Constructor with userId
    public UserProfile(String userId) {
        this.userId = userId;
        this.watchHistory = new ArrayList<>();
        this.preferences = new HashMap<>();
        this.recommendedVideos = new ArrayList<>();
        this.lastActive = Instant.now();
        this.totalWatchTime = 0;
    }

    // Getters and Setters
    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public List<String> getWatchHistory() {
        return watchHistory;
    }

    public void setWatchHistory(List<String> watchHistory) {
        this.watchHistory = watchHistory;
    }

    public Map<String, Integer> getPreferences() {
        return preferences;
    }

    public void setPreferences(Map<String, Integer> preferences) {
        this.preferences = preferences;
    }

    public List<String> getRecommendedVideos() {
        return recommendedVideos;
    }

    public void setRecommendedVideos(List<String> recommendedVideos) {
        this.recommendedVideos = recommendedVideos;
    }

    public Instant getLastActive() {
        return lastActive;
    }

    public void setLastActive(Instant lastActive) {
        this.lastActive = lastActive;
    }

    public long getTotalWatchTime() {
        return totalWatchTime;
    }

    public void setTotalWatchTime(long totalWatchTime) {
        this.totalWatchTime = totalWatchTime;
    }

    /**
     * Adds a video to watch history and updates preferences
     */
    public void addToHistory(String videoId, String category, int watchDuration) {
        // Add to history (keep last 100 videos)
        this.watchHistory.add(0, videoId);
        if (this.watchHistory.size() > 100) {
            this.watchHistory = new ArrayList<>(this.watchHistory.subList(0, 100));
        }

        // Update category preference
        this.preferences.merge(category, 1, Integer::sum);

        // Update total watch time
        this.totalWatchTime += watchDuration;

        // Update last active
        this.lastActive = Instant.now();
    }

    /**
     * Gets the user's top preferred categories
     */
    public List<String> getTopCategories(int limit) {
        return this.preferences.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(limit)
                .map(Map.Entry::getKey)
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        UserProfile that = (UserProfile) o;
        return Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId);
    }

    @Override
    public String toString() {
        return "UserProfile{" +
                "userId='" + userId + '\'' +
                ", watchHistorySize=" + (watchHistory != null ? watchHistory.size() : 0) +
                ", totalWatchTime=" + totalWatchTime +
                '}';
    }
}
