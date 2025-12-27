package com.streaming.analytics.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.bson.codecs.pojo.annotations.BsonIgnore;
import org.bson.codecs.pojo.annotations.BsonProperty;

import java.time.Instant;
import java.util.Objects;

/**
 * Entity representing aggregated statistics for a video
 * Maps to the 'video_stats' collection in MongoDB
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class VideoStats {

    // Skip id field completely - use videoId as unique identifier
    @BsonIgnore
    @JsonIgnore
    private Object id;

    @BsonProperty("videoId")
    private String videoId;

    @BsonProperty("totalViews")
    private long totalViews;

    @BsonProperty("avgDuration")
    private double avgDuration; // Average watch duration in seconds

    @BsonProperty("uniqueViewers")
    private long uniqueViewers;

    @BsonProperty("lastUpdated")
    private Instant lastUpdated;

    // Default constructor required for MongoDB POJO codec
    public VideoStats() {
    }

    // Constructor with videoId
    public VideoStats(String videoId) {
        this.videoId = videoId;
        this.totalViews = 0;
        this.avgDuration = 0.0;
        this.uniqueViewers = 0;
        this.lastUpdated = Instant.now();
    }

    // Getters and Setters
    public Object getId() {
        return id;
    }

    public void setId(Object id) {
        this.id = id;
    }

    public String getVideoId() {
        return videoId;
    }

    public void setVideoId(String videoId) {
        this.videoId = videoId;
    }

    public long getTotalViews() {
        return totalViews;
    }

    public void setTotalViews(long totalViews) {
        this.totalViews = totalViews;
    }

    public double getAvgDuration() {
        return avgDuration;
    }

    public void setAvgDuration(double avgDuration) {
        this.avgDuration = avgDuration;
    }

    public long getUniqueViewers() {
        return uniqueViewers;
    }

    public void setUniqueViewers(long uniqueViewers) {
        this.uniqueViewers = uniqueViewers;
    }

    public Instant getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Instant lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    /**
     * Updates statistics with a new view event
     */
    public void updateWithEvent(int watchDuration) {
        long previousTotalDuration = (long) (this.avgDuration * this.totalViews);
        this.totalViews++;
        this.avgDuration = (previousTotalDuration + watchDuration) / (double) this.totalViews;
        this.lastUpdated = Instant.now();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        VideoStats that = (VideoStats) o;
        return Objects.equals(videoId, that.videoId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(videoId);
    }

    @Override
    public String toString() {
        return "VideoStats{" +
                "videoId='" + videoId + '\'' +
                ", totalViews=" + totalViews +
                ", avgDuration=" + avgDuration +
                ", lastUpdated=" + lastUpdated +
                '}';
    }
}
