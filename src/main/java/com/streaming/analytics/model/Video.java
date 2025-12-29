package com.streaming.analytics.model;

import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.codecs.pojo.annotations.BsonProperty;
import org.bson.types.ObjectId;

import java.time.Instant;
import java.util.Objects;

/**
 * Entity representing video metadata
 * Maps to the 'videos' collection in MongoDB
 */
public class Video {

    @BsonId
    private ObjectId id;

    @BsonProperty("videoId")
    private String videoId;

    @BsonProperty("title")
    private String title;

    @BsonProperty("category")
    private String category; // Action, Comedy, Drama, Documentary, SciFi, Horror, Romance, Thriller

    @BsonProperty("duration")
    private int duration; // Video duration in seconds

    @BsonProperty("uploadDate")
    private Instant uploadDate;

    @BsonProperty("views")
    private int views;

    @BsonProperty("likes")
    private int likes;

    @BsonProperty("rating")
    private double rating; // TMDB vote average (0-10)

    @BsonProperty("posterUrl")
    private String posterUrl; // Full URL to movie poster

    @BsonProperty("overview")
    private String overview; // Movie description/synopsis

    // Default constructor required for MongoDB POJO codec
    public Video() {
    }

    // Constructor with main fields
    public Video(String videoId, String title, String category, int duration, Instant uploadDate) {
        this.videoId = videoId;
        this.title = title;
        this.category = category;
        this.duration = duration;
        this.uploadDate = uploadDate;
        this.views = 0;
        this.likes = 0;
    }

    // Getters and Setters
    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public String getVideoId() {
        return videoId;
    }

    public void setVideoId(String videoId) {
        this.videoId = videoId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public Instant getUploadDate() {
        return uploadDate;
    }

    public void setUploadDate(Instant uploadDate) {
        this.uploadDate = uploadDate;
    }

    public int getViews() {
        return views;
    }

    public void setViews(int views) {
        this.views = views;
    }

    public int getLikes() {
        return likes;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public String getPosterUrl() {
        return posterUrl;
    }

    public void setPosterUrl(String posterUrl) {
        this.posterUrl = posterUrl;
    }

    public String getOverview() {
        return overview;
    }

    public void setOverview(String overview) {
        this.overview = overview;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Video video = (Video) o;
        return Objects.equals(videoId, video.videoId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(videoId);
    }

    @Override
    public String toString() {
        return "Video{" +
                "videoId='" + videoId + '\'' +
                ", title='" + title + '\'' +
                ", category='" + category + '\'' +
                ", views=" + views +
                '}';
    }
}
