package com.streaming.analytics.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.codecs.pojo.annotations.BsonProperty;
import org.bson.types.ObjectId;

import java.time.Instant;
import java.util.Objects;

/**
 * Entity representing a viewing event from the streaming platform
 * Maps to the 'events' collection in MongoDB
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ViewEvent {

    @BsonId
    @JsonIgnore
    private ObjectId id;

    @BsonProperty("eventId")
    private String eventId;

    @BsonProperty("userId")
    private String userId;

    @BsonProperty("videoId")
    private String videoId;

    @BsonProperty("timestamp")
    private Instant timestamp;

    @BsonProperty("action")
    private String action; // WATCH, PAUSE, STOP, RESUME, SEEK

    @BsonProperty("duration")
    private int duration; // Duration in seconds

    @BsonProperty("quality")
    private String quality; // 360p, 480p, 720p, 1080p, 4K

    @BsonProperty("deviceType")
    private String deviceType; // mobile, desktop, tablet, tv, console

    // Default constructor required for MongoDB POJO codec
    public ViewEvent() {
    }

    // Constructor with all fields
    public ViewEvent(String eventId, String userId, String videoId, Instant timestamp,
            String action, int duration, String quality, String deviceType) {
        this.eventId = eventId;
        this.userId = userId;
        this.videoId = videoId;
        this.timestamp = timestamp;
        this.action = action;
        this.duration = duration;
        this.quality = quality;
        this.deviceType = deviceType;
    }

    // Getters and Setters
    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getVideoId() {
        return videoId;
    }

    public void setVideoId(String videoId) {
        this.videoId = videoId;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getQuality() {
        return quality;
    }

    public void setQuality(String quality) {
        this.quality = quality;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        ViewEvent viewEvent = (ViewEvent) o;
        return Objects.equals(eventId, viewEvent.eventId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(eventId);
    }

    @Override
    public String toString() {
        return "ViewEvent{" +
                "eventId='" + eventId + '\'' +
                ", userId='" + userId + '\'' +
                ", videoId='" + videoId + '\'' +
                ", action='" + action + '\'' +
                ", duration=" + duration +
                '}';
    }
}
