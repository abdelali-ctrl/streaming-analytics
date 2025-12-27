package com.streaming.analytics.repository;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import com.streaming.analytics.model.Video;
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
 * CDI Repository for Video entities
 * Uses Document-based queries to avoid POJO codec issues
 */
@ApplicationScoped
public class VideoRepository {

    private static final Logger logger = LoggerFactory.getLogger(VideoRepository.class);
    private static final String COLLECTION_NAME = "videos";

    @Inject
    private MongoDatabase database;

    private MongoCollection<Document> collection;

    @PostConstruct
    public void init() {
        this.collection = database.getCollection(COLLECTION_NAME);
        logger.info("VideoRepository initialized with collection: {}", COLLECTION_NAME);
    }

    /**
     * Converts a Document to Video
     */
    private Video documentToVideo(Document doc) {
        if (doc == null)
            return null;

        Video video = new Video();
        video.setVideoId(doc.getString("videoId"));
        video.setTitle(doc.getString("title"));
        video.setCategory(doc.getString("category"));

        Number duration = (Number) doc.get("duration");
        video.setDuration(duration != null ? duration.intValue() : 0);

        Number views = (Number) doc.get("views");
        video.setViews(views != null ? views.intValue() : 0);

        Number likes = (Number) doc.get("likes");
        video.setLikes(likes != null ? likes.intValue() : 0);

        Object uploadDate = doc.get("uploadDate");
        if (uploadDate instanceof Date) {
            video.setUploadDate(((Date) uploadDate).toInstant());
        } else if (uploadDate instanceof Instant) {
            video.setUploadDate((Instant) uploadDate);
        }

        return video;
    }

    /**
     * Gets a video by videoId
     */
    public Video findByVideoId(String videoId) {
        Document doc = collection.find(Filters.eq("videoId", videoId)).first();
        return documentToVideo(doc);
    }

    /**
     * Gets videos by category
     */
    public List<Video> findByCategory(String category) {
        List<Video> results = new ArrayList<>();

        for (Document doc : collection.find(Filters.eq("category", category))
                .sort(Sorts.descending("views"))) {
            results.add(documentToVideo(doc));
        }

        return results;
    }

    /**
     * Gets videos by category with limit
     */
    public List<Video> findByCategory(String category, int limit) {
        List<Video> results = new ArrayList<>();

        for (Document doc : collection.find(Filters.eq("category", category))
                .sort(Sorts.descending("views"))
                .limit(limit)) {
            results.add(documentToVideo(doc));
        }

        return results;
    }

    /**
     * Gets the most popular videos
     */
    public List<Video> findMostPopular(int limit) {
        List<Video> results = new ArrayList<>();

        for (Document doc : collection.find()
                .sort(Sorts.descending("views"))
                .limit(limit)) {
            results.add(documentToVideo(doc));
        }

        return results;
    }

    /**
     * Gets videos by list of IDs (for recommendations)
     */
    public List<Video> findByVideoIds(List<String> videoIds) {
        List<Video> results = new ArrayList<>();

        for (Document doc : collection.find(Filters.in("videoId", videoIds))) {
            results.add(documentToVideo(doc));
        }

        return results;
    }

    /**
     * Counts total videos
     */
    public long count() {
        return collection.countDocuments();
    }

    /**
     * Gets all categories
     */
    public List<String> getAllCategories() {
        return collection.distinct("category", String.class).into(new ArrayList<>());
    }

    /**
     * Saves a video
     */
    public void save(Video video) {
        Document doc = new Document()
                .append("videoId", video.getVideoId())
                .append("title", video.getTitle())
                .append("category", video.getCategory())
                .append("duration", video.getDuration())
                .append("views", video.getViews())
                .append("likes", video.getLikes())
                .append("uploadDate", video.getUploadDate() != null ? Date.from(video.getUploadDate()) : new Date());

        collection.insertOne(doc);
        logger.debug("Saved video: {}", video.getVideoId());
    }

    /**
     * Saves a batch of videos
     */
    public void saveBatch(List<Video> videos) {
        if (videos != null && !videos.isEmpty()) {
            List<Document> docs = new ArrayList<>();
            for (Video video : videos) {
                Document doc = new Document()
                        .append("videoId", video.getVideoId())
                        .append("title", video.getTitle())
                        .append("category", video.getCategory())
                        .append("duration", video.getDuration())
                        .append("views", video.getViews())
                        .append("likes", video.getLikes())
                        .append("uploadDate",
                                video.getUploadDate() != null ? Date.from(video.getUploadDate()) : new Date());
                docs.add(doc);
            }
            collection.insertMany(docs);
            logger.info("Saved batch of {} videos", videos.size());
        }
    }
}
