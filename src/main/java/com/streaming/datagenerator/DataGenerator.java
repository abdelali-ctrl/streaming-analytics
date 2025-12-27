package com.streaming.datagenerator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Générateur de données pour la plateforme de streaming vidéo
 * Génère des événements de visualisation simulés
 * 
 * Modes:
 * 1. Batch - Génère des fichiers JSON
 * 2. Continuous - Génère et insère directement dans MongoDB
 */
public class DataGenerator {

    private static final String[] ACTIONS = { "WATCH", "PAUSE", "STOP", "RESUME", "SEEK" };
    private static final String[] QUALITIES = { "360p", "480p", "720p", "1080p", "4K" };
    private static final String[] DEVICE_TYPES = { "mobile", "desktop", "tablet", "tv", "console" };
    private static final String[] CATEGORIES = { "Action", "Comedy", "Drama", "Documentary", "SciFi", "Horror",
            "Romance", "Thriller" };

    private static final int NUM_USERS = 50000;
    private static final int NUM_VIDEOS = 10000;

    // MongoDB connection settings
    private static final String MONGODB_HOST = System.getenv("MONGODB_HOST") != null
            ? System.getenv("MONGODB_HOST")
            : "localhost";
    private static final String MONGODB_PORT = System.getenv("MONGODB_PORT") != null
            ? System.getenv("MONGODB_PORT")
            : "27017";
    private static final String MONGODB_DATABASE = "streaming_analytics";
    private static final String MONGODB_USERNAME = "admin";
    private static final String MONGODB_PASSWORD = "admin123";

    private final ObjectMapper objectMapper;
    private final Random random;
    private MongoClient mongoClient;
    private MongoDatabase database;
    private MongoCollection<Document> eventsCollection;
    private MongoCollection<Document> videoStatsCollection;

    public DataGenerator() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        this.random = new Random();
    }

    /**
     * Connects to MongoDB
     */
    public void connectMongoDB() {
        String connectionString = String.format(
                "mongodb://%s:%s@%s:%s/?authSource=admin",
                MONGODB_USERNAME, MONGODB_PASSWORD, MONGODB_HOST, MONGODB_PORT);

        System.out.println("🔌 Connecting to MongoDB at " + MONGODB_HOST + ":" + MONGODB_PORT + "...");
        mongoClient = MongoClients.create(connectionString);
        database = mongoClient.getDatabase(MONGODB_DATABASE);
        eventsCollection = database.getCollection("events");
        videoStatsCollection = database.getCollection("video_stats");
        System.out.println("✅ Connected to MongoDB database: " + MONGODB_DATABASE);
    }

    /**
     * Closes MongoDB connection
     */
    public void closeMongoDB() {
        if (mongoClient != null) {
            mongoClient.close();
            System.out.println("🔌 MongoDB connection closed");
        }
    }

    /**
     * Génère un événement de visualisation aléatoire
     */
    public ViewEvent generateEvent() {
        ViewEvent event = new ViewEvent();

        event.setEventId("evt_" + UUID.randomUUID().toString().substring(0, 8));
        event.setUserId("user_" + ThreadLocalRandom.current().nextInt(1, NUM_USERS + 1));
        event.setVideoId("video_" + ThreadLocalRandom.current().nextInt(1, NUM_VIDEOS + 1));

        // Timestamp now (for continuous mode) or within last 24h (for batch)
        event.setTimestamp(Instant.now().toString());

        event.setAction(ACTIONS[random.nextInt(ACTIONS.length)]);

        // Durée en secondes
        if ("WATCH".equals(event.getAction())) {
            event.setDuration(ThreadLocalRandom.current().nextInt(30, 3600));
        } else {
            event.setDuration(ThreadLocalRandom.current().nextInt(0, 300));
        }

        event.setQuality(QUALITIES[random.nextInt(QUALITIES.length)]);
        event.setDeviceType(DEVICE_TYPES[random.nextInt(DEVICE_TYPES.length)]);

        return event;
    }

    /**
     * Génère des métadonnées vidéo
     */
    public Video generateVideo(int videoId) {
        Video video = new Video();

        video.setVideoId("video_" + videoId);
        video.setTitle("Video Title " + videoId);
        video.setCategory(CATEGORIES[random.nextInt(CATEGORIES.length)]);
        video.setDuration(ThreadLocalRandom.current().nextInt(300, 7200));

        Instant now = Instant.now();
        long randomDays = ThreadLocalRandom.current().nextLong(0, 365);
        video.setUploadDate(now.minus(randomDays, ChronoUnit.DAYS).toString());

        video.setViews(ThreadLocalRandom.current().nextInt(100, 1000000));
        video.setLikes(ThreadLocalRandom.current().nextInt(10, 50000));

        return video;
    }

    /**
     * Génère N événements et les écrit dans un fichier JSON
     */
    public void generateEventsToFile(int count, String filename) throws IOException {
        List<ViewEvent> events = new ArrayList<>();

        System.out.println("🎬 Génération de " + count + " événements...");

        for (int i = 0; i < count; i++) {
            ViewEvent event = generateEvent();
            // For batch, set timestamp in last 24 hours
            Instant now = Instant.now();
            long randomMinutes = ThreadLocalRandom.current().nextLong(0, 24 * 60);
            event.setTimestamp(now.minus(randomMinutes, ChronoUnit.MINUTES).toString());
            events.add(event);

            if ((i + 1) % 10000 == 0) {
                System.out.println("  ✓ " + (i + 1) + " événements générés");
            }
        }

        System.out.println("💾 Écriture dans le fichier " + filename + "...");

        try (FileWriter writer = new FileWriter(filename)) {
            objectMapper.writeValue(writer, events);
        }

        System.out.println("✅ " + count + " événements générés avec succès dans " + filename);
    }

    /**
     * Génère le catalogue vidéo
     */
    public void generateVideosCatalog(String filename) throws IOException {
        List<Video> videos = new ArrayList<>();

        System.out.println("📹 Génération de " + NUM_VIDEOS + " vidéos...");

        for (int i = 1; i <= NUM_VIDEOS; i++) {
            videos.add(generateVideo(i));

            if (i % 1000 == 0) {
                System.out.println("  ✓ " + i + " vidéos générées");
            }
        }

        System.out.println("💾 Écriture du catalogue dans " + filename + "...");

        try (FileWriter writer = new FileWriter(filename)) {
            objectMapper.writeValue(writer, videos);
        }

        System.out.println("✅ Catalogue de " + NUM_VIDEOS + " vidéos généré avec succès");
    }

    /**
     * Génère des événements en continu et les insère dans MongoDB
     * Les événements apparaîtront dans le dashboard en temps réel!
     */
    public void generateContinuousToMongoDB(int eventsPerSecond, int durationSeconds) {
        System.out.println("🔄 Génération continue vers MongoDB : " + eventsPerSecond + " événements/seconde pendant "
                + durationSeconds + "s");
        System.out.println("📊 Les événements apparaîtront dans le dashboard en temps réel!");
        System.out.println("   Dashboard: http://localhost:8080/streaming-analytics/dashboard\n");

        long startTime = System.currentTimeMillis();
        long endTime = startTime + (durationSeconds * 1000L);
        int totalGenerated = 0;

        while (System.currentTimeMillis() < endTime) {
            long cycleStart = System.currentTimeMillis();
            List<Document> batch = new ArrayList<>();

            // Génère les événements pour cette seconde
            for (int i = 0; i < eventsPerSecond; i++) {
                ViewEvent event = generateEvent();

                // Convert to MongoDB Document
                Document doc = new Document()
                        .append("eventId", event.getEventId())
                        .append("userId", event.getUserId())
                        .append("videoId", event.getVideoId())
                        .append("timestamp", new Date())
                        .append("action", event.getAction())
                        .append("duration", event.getDuration())
                        .append("quality", event.getQuality())
                        .append("deviceType", event.getDeviceType());

                batch.add(doc);
                totalGenerated++;
            }

            // Insert batch into MongoDB
            if (!batch.isEmpty()) {
                eventsCollection.insertMany(batch);

                // Also update video_stats for WATCH events
                for (Document doc : batch) {
                    if ("WATCH".equals(doc.getString("action"))) {
                        updateVideoStats(doc.getString("videoId"), doc.getInteger("duration"));
                    }
                }
            }

            System.out.print("\r  ✓ " + totalGenerated + " événements générés et insérés dans MongoDB");

            // Attendre le reste de la seconde
            long cycleTime = System.currentTimeMillis() - cycleStart;
            long sleepTime = 1000 - cycleTime;

            if (sleepTime > 0) {
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }

        System.out.println("\n✅ Génération terminée : " + totalGenerated + " événements générés et insérés");
    }

    /**
     * Updates video_stats when a WATCH event is inserted
     */
    private void updateVideoStats(String videoId, int duration) {
        Document query = new Document("videoId", videoId);
        Document existing = videoStatsCollection.find(query).first();

        if (existing == null) {
            // Create new stats
            Document newStats = new Document()
                    .append("videoId", videoId)
                    .append("totalViews", 1L)
                    .append("avgDuration", (double) duration)
                    .append("uniqueViewers", 1L)
                    .append("lastUpdated", new Date());
            videoStatsCollection.insertOne(newStats);
        } else {
            // Update existing stats
            long totalViews = ((Number) existing.get("totalViews")).longValue();
            double avgDuration = ((Number) existing.get("avgDuration")).doubleValue();

            long newTotalViews = totalViews + 1;
            double newAvgDuration = ((avgDuration * totalViews) + duration) / newTotalViews;

            Document update = new Document("$set", new Document()
                    .append("totalViews", newTotalViews)
                    .append("avgDuration", newAvgDuration)
                    .append("lastUpdated", new Date()));

            videoStatsCollection.updateOne(query, update);
        }
    }

    /**
     * Génère des événements en continu (mode console - original)
     */
    public void generateContinuousStreamConsole(int eventsPerSecond, int durationSeconds) {
        System.out.println("🔄 Génération continue (console) : " + eventsPerSecond + " événements/seconde pendant "
                + durationSeconds + "s");

        long startTime = System.currentTimeMillis();
        long endTime = startTime + (durationSeconds * 1000L);
        int totalGenerated = 0;

        while (System.currentTimeMillis() < endTime) {
            long cycleStart = System.currentTimeMillis();

            for (int i = 0; i < eventsPerSecond; i++) {
                ViewEvent event = generateEvent();
                System.out.println(event.toJson());
                totalGenerated++;
            }

            long cycleTime = System.currentTimeMillis() - cycleStart;
            long sleepTime = 1000 - cycleTime;

            if (sleepTime > 0) {
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }

        System.out.println("✅ Génération terminée : " + totalGenerated + " événements générés");
    }

    public static void main(String[] args) {
        DataGenerator generator = new DataGenerator();

        // Parse arguments
        String mode = args.length > 0 ? args[0] : "batch";
        int eventsPerSecond = args.length > 1 ? Integer.parseInt(args[1]) : 10;
        int durationSeconds = args.length > 2 ? Integer.parseInt(args[2]) : 60;

        try {
            switch (mode.toLowerCase()) {
                case "batch":
                    System.out.println("=== MODE 1 : GÉNÉRATION BATCH ===\n");
                    generator.generateEventsToFile(100000, "events_100k.json");
                    System.out.println("\n=== MODE 2 : GÉNÉRATION CATALOGUE VIDÉOS ===\n");
                    generator.generateVideosCatalog("videos_catalog.json");
                    break;

                case "continuous":
                case "stream":
                    System.out.println("=== MODE CONTINU VERS MONGODB ===\n");
                    generator.connectMongoDB();
                    generator.generateContinuousToMongoDB(eventsPerSecond, durationSeconds);
                    generator.closeMongoDB();
                    break;

                case "console":
                    System.out.println("=== MODE CONTINU CONSOLE ===\n");
                    generator.generateContinuousStreamConsole(eventsPerSecond, durationSeconds);
                    break;

                default:
                    System.out.println("Usage: DataGenerator [mode] [eventsPerSecond] [durationSeconds]");
                    System.out.println("Modes:");
                    System.out.println("  batch      - Generate JSON files (default)");
                    System.out.println("  continuous - Stream events to MongoDB (for dashboard)");
                    System.out.println("  console    - Stream events to console");
                    System.out.println("\nExamples:");
                    System.out.println("  DataGenerator batch");
                    System.out.println("  DataGenerator continuous 10 60  (10 events/sec for 60 seconds)");
            }

        } catch (IOException e) {
            System.err.println("❌ Erreur lors de la génération : " + e.getMessage());
            e.printStackTrace();
        }
    }
}

/**
 * Classe représentant un événement de visualisation
 */
class ViewEvent {
    private String eventId;
    private String userId;
    private String videoId;
    private String timestamp;
    private String action;
    private int duration;
    private String quality;
    private String deviceType;

    // Getters et Setters
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

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
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

    public String toJson() {
        return String.format(
                "{\"eventId\":\"%s\",\"userId\":\"%s\",\"videoId\":\"%s\",\"timestamp\":\"%s\",\"action\":\"%s\",\"duration\":%d,\"quality\":\"%s\",\"deviceType\":\"%s\"}",
                eventId, userId, videoId, timestamp, action, duration, quality, deviceType);
    }

    @Override
    public String toString() {
        return toJson();
    }
}

/**
 * Classe représentant une vidéo
 */
class Video {
    private String videoId;
    private String title;
    private String category;
    private int duration;
    private String uploadDate;
    private int views;
    private int likes;

    // Getters et Setters
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

    public String getUploadDate() {
        return uploadDate;
    }

    public void setUploadDate(String uploadDate) {
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
}
