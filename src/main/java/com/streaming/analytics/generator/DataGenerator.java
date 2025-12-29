package com.streaming.analytics.generator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.streaming.analytics.model.Video;
import com.streaming.analytics.model.ViewEvent;
import com.streaming.analytics.tmdb.TmdbClient;
import com.streaming.analytics.tmdb.TmdbMovie;
import org.bson.Document;

import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Data Generator for the streaming analytics platform
 * Generates simulated viewing events and video catalog
 * 
 * Usage:
 * mvn exec:java
 * -Dexec.mainClass="com.streaming.analytics.generator.DataGenerator"
 * -Dexec.args="MODE [OPTIONS]"
 * 
 * Modes:
 * batch - Generate 100K events + 10K videos to JSON files (default)
 * mongo [eventCount] - Generate and load data directly into MongoDB (default:
 * 100000 events)
 * continuous [rate] [sec] - Real-time streaming to MongoDB (default: 10
 * events/sec for 60 sec)
 */
public class DataGenerator {

    private static final String[] ACTIONS = { "WATCH", "PAUSE", "STOP", "RESUME", "SEEK" };
    private static final String[] QUALITIES = { "360p", "480p", "720p", "1080p", "4K" };
    private static final String[] DEVICE_TYPES = { "mobile", "desktop", "tablet", "tv", "console" };

    // Real movie categories and titles
    private static final String[] CATEGORIES = {
            "Action", "Comedy", "Drama", "Documentary", "Sci-Fi",
            "Horror", "Romance", "Thriller", "Animation", "Adventure"
    };

    // Real movies/shows organized by category
    private static final String[][] MOVIES_BY_CATEGORY = {
            // Action
            { "The Dark Knight", "Mad Max: Fury Road", "John Wick", "Die Hard", "Gladiator", "Mission: Impossible" },
            // Comedy
            { "The Hangover", "Superbad", "Bridesmaids", "Step Brothers", "Anchorman", "The Office" },
            // Drama
            { "The Shawshank Redemption", "Forrest Gump", "The Godfather", "Schindler's List", "Breaking Bad",
                    "The Crown" },
            // Documentary
            { "Planet Earth", "Our Planet", "Making a Murderer", "The Social Dilemma", "Free Solo", "Tiger King" },
            // Sci-Fi
            { "Interstellar", "Inception", "The Matrix", "Blade Runner 2049", "Stranger Things", "Black Mirror" },
            // Horror
            { "The Conjuring", "Get Out", "A Quiet Place", "Hereditary", "The Shining", "IT" },
            // Romance
            { "The Notebook", "Titanic", "Pride and Prejudice", "La La Land", "When Harry Met Sally", "Bridgerton" },
            // Thriller
            { "Gone Girl", "Se7en", "Silence of the Lambs", "Shutter Island", "Zodiac", "Mindhunter" },
            // Animation
            { "Toy Story", "Finding Nemo", "The Lion King", "Spirited Away", "Frozen",
                    "Spider-Man: Into the Spider-Verse" },
            // Adventure
            { "Indiana Jones", "Jurassic Park", "Avatar", "Pirates of the Caribbean", "The Lord of the Rings", "Dune" }
    };

    private static final int NUM_USERS = 50000;
    private static final int NUM_VIDEOS = 60; // Use the 60 real movies (6 per category * 10 categories)

    // MongoDB connection settings (can be overridden via environment variables)
    private static final String MONGO_URI = System.getenv("MONGO_URI") != null
            ? System.getenv("MONGO_URI")
            : "mongodb://admin:admin123@localhost:27017";
    private static final String MONGO_DATABASE = System.getenv("MONGO_DATABASE") != null
            ? System.getenv("MONGO_DATABASE")
            : "streaming_analytics";

    // TMDB API Key (can be overridden via environment variable)
    private static final String TMDB_API_KEY = System.getenv("TMDB_API_KEY") != null
            ? System.getenv("TMDB_API_KEY")
            : "10ddca8c791b2c0538514d2b7999731a";

    private final ObjectMapper objectMapper;
    private final Random random;

    public DataGenerator() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        this.random = new Random();
    }

    /**
     * Generates a single random viewing event
     */
    public ViewEvent generateEvent() {
        ViewEvent event = new ViewEvent();

        event.setEventId("evt_" + UUID.randomUUID().toString().substring(0, 8));
        event.setUserId("user_" + ThreadLocalRandom.current().nextInt(1, NUM_USERS + 1));
        event.setVideoId("video_" + ThreadLocalRandom.current().nextInt(1, NUM_VIDEOS + 1));

        // Timestamp within last 24 hours
        Instant now = Instant.now();
        long randomMinutes = ThreadLocalRandom.current().nextLong(0, 24 * 60);
        event.setTimestamp(now.minus(randomMinutes, ChronoUnit.MINUTES));

        event.setAction(ACTIONS[random.nextInt(ACTIONS.length)]);

        // Duration depends on action type
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
     * Generates video metadata with real movie titles
     */
    public Video generateVideo(int videoId) {
        Video video = new Video();

        video.setVideoId("video_" + videoId);

        // Map videoId to a specific category and movie
        int categoryIndex = (videoId - 1) % CATEGORIES.length;
        int movieIndex = ((videoId - 1) / CATEGORIES.length) % MOVIES_BY_CATEGORY[categoryIndex].length;

        String category = CATEGORIES[categoryIndex];
        String title = MOVIES_BY_CATEGORY[categoryIndex][movieIndex];

        video.setTitle(title);
        video.setCategory(category);
        video.setDuration(ThreadLocalRandom.current().nextInt(5400, 10800)); // 1.5h to 3h for movies

        // Upload date within last 365 days
        Instant now = Instant.now();
        long randomDays = ThreadLocalRandom.current().nextLong(0, 365);
        video.setUploadDate(now.minus(randomDays, ChronoUnit.DAYS));

        video.setViews(ThreadLocalRandom.current().nextInt(100, 1000000));
        video.setLikes(ThreadLocalRandom.current().nextInt(10, 50000));

        return video;
    }

    /**
     * Generates N events and returns as a list
     */
    public List<ViewEvent> generateEvents(int count) {
        List<ViewEvent> events = new ArrayList<>();

        System.out.println("🎬 Generating " + count + " events...");

        for (int i = 0; i < count; i++) {
            events.add(generateEvent());

            if ((i + 1) % 10000 == 0) {
                System.out.println("  ✓ " + (i + 1) + " events generated");
            }
        }

        System.out.println("✅ " + count + " events generated successfully");
        return events;
    }

    /**
     * Generates events and writes to JSON file
     */
    public void generateEventsToFile(int count, String filename) throws IOException {
        List<ViewEvent> events = generateEvents(count);

        System.out.println("💾 Writing to file " + filename + "...");

        try (FileWriter writer = new FileWriter(filename)) {
            objectMapper.writeValue(writer, events);
        }

        System.out.println("✅ " + count + " events written to " + filename);
    }

    /**
     * Generates video catalog
     */
    public List<Video> generateVideoCatalog(int count) {
        List<Video> videos = new ArrayList<>();

        System.out.println("📹 Generating " + count + " videos...");

        for (int i = 1; i <= count; i++) {
            videos.add(generateVideo(i));

            if (i % 1000 == 0) {
                System.out.println("  ✓ " + i + " videos generated");
            }
        }

        System.out.println("✅ Video catalog with " + count + " videos generated");
        return videos;
    }

    /**
     * Generates video catalog and writes to JSON file
     */
    public void generateVideosCatalogToFile(String filename) throws IOException {
        List<Video> videos = generateVideoCatalog(NUM_VIDEOS);

        System.out.println("💾 Writing catalog to " + filename + "...");

        try (FileWriter writer = new FileWriter(filename)) {
            objectMapper.writeValue(writer, videos);
        }

        System.out.println("✅ Video catalog written to " + filename);
    }

    /**
     * Converts a ViewEvent to a MongoDB Document
     */
    private Document eventToDocument(ViewEvent event) {
        Document doc = new Document();
        doc.append("eventId", event.getEventId());
        doc.append("userId", event.getUserId());
        doc.append("videoId", event.getVideoId());
        doc.append("timestamp", Date.from(event.getTimestamp()));
        doc.append("action", event.getAction());
        doc.append("duration", event.getDuration());
        doc.append("quality", event.getQuality());
        doc.append("deviceType", event.getDeviceType());
        return doc;
    }

    /**
     * Converts a Video to a MongoDB Document
     */
    private Document videoToDocument(Video video) {
        Document doc = new Document();
        doc.append("videoId", video.getVideoId());
        doc.append("title", video.getTitle());
        doc.append("category", video.getCategory());
        doc.append("duration", video.getDuration());
        doc.append("uploadDate", Date.from(video.getUploadDate()));
        doc.append("views", video.getViews());
        doc.append("likes", video.getLikes());
        return doc;
    }

    /**
     * Generates and loads events directly into MongoDB (bulk mode)
     */
    public void generateAndLoadToMongo(int eventCount) {
        System.out.println("🔗 Connecting to MongoDB: " + MONGO_URI);
        System.out.println("📊 Database: " + MONGO_DATABASE);

        try (MongoClient mongoClient = MongoClients.create(MONGO_URI)) {
            MongoDatabase database = mongoClient.getDatabase(MONGO_DATABASE);

            // Load videos first
            MongoCollection<Document> videosCollection = database.getCollection("videos");
            System.out.println("\n📹 Generating and loading " + NUM_VIDEOS + " videos...");

            List<Document> videoDocs = new ArrayList<>();
            for (int i = 1; i <= NUM_VIDEOS; i++) {
                videoDocs.add(videoToDocument(generateVideo(i)));
                if (i % 1000 == 0) {
                    System.out.println("  ✓ " + i + " videos generated");
                }
            }

            // Clear existing videos and insert new ones
            videosCollection.drop();
            videosCollection.insertMany(videoDocs);
            System.out.println("✅ " + NUM_VIDEOS + " videos loaded into MongoDB");

            // Load events in batches
            MongoCollection<Document> eventsCollection = database.getCollection("events");
            System.out.println("\n🎬 Generating and loading " + eventCount + " events...");

            // Clear existing events
            eventsCollection.drop();

            int batchSize = 5000;
            List<Document> eventBatch = new ArrayList<>();

            for (int i = 0; i < eventCount; i++) {
                eventBatch.add(eventToDocument(generateEvent()));

                if (eventBatch.size() >= batchSize) {
                    eventsCollection.insertMany(eventBatch);
                    System.out.println("  ✓ " + (i + 1) + " events loaded");
                    eventBatch.clear();
                }
            }

            // Insert remaining events
            if (!eventBatch.isEmpty()) {
                eventsCollection.insertMany(eventBatch);
            }

            System.out.println("✅ " + eventCount + " events loaded into MongoDB");

            // Run aggregation to compute video_stats
            aggregateVideoStats(database);

            System.out.println("\n🎉 Data generation complete!");
            System.out.println("   Events: " + eventsCollection.countDocuments());
            System.out.println("   Videos: " + videosCollection.countDocuments());
            System.out.println("   Stats:  " + database.getCollection("video_stats").countDocuments());

        } catch (Exception e) {
            System.err.println("❌ MongoDB Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Continuous mode: streams events directly to MongoDB in real-time
     */
    public void streamToMongo(int eventsPerSecond, int durationSeconds) {
        System.out.println("🔗 Connecting to MongoDB: " + MONGO_URI);
        System.out.println("📊 Database: " + MONGO_DATABASE);
        System.out.println("⚡ Rate: " + eventsPerSecond + " events/second");
        System.out.println("⏱️  Duration: " + durationSeconds + " seconds");
        System.out.println("📝 Total events: " + (eventsPerSecond * durationSeconds));

        try (MongoClient mongoClient = MongoClients.create(MONGO_URI)) {
            MongoDatabase database = mongoClient.getDatabase(MONGO_DATABASE);
            MongoCollection<Document> eventsCollection = database.getCollection("events");
            MongoCollection<Document> statsCollection = database.getCollection("video_stats");

            int totalEvents = 0;
            long intervalMs = 1000 / eventsPerSecond;
            long startTime = System.currentTimeMillis();
            long endTime = startTime + (durationSeconds * 1000L);

            System.out.println("\n🚀 Starting real-time event stream...\n");

            while (System.currentTimeMillis() < endTime) {
                // Generate and insert event with current timestamp
                ViewEvent event = generateEvent();
                event.setTimestamp(Instant.now()); // Override with real-time timestamp

                Document eventDoc = eventToDocument(event);
                eventsCollection.insertOne(eventDoc);

                // Update video stats incrementally
                updateVideoStatsIncremental(statsCollection, event);

                totalEvents++;

                if (totalEvents % 100 == 0) {
                    long elapsed = (System.currentTimeMillis() - startTime) / 1000;
                    System.out.println("  📊 " + totalEvents + " events streamed (" + elapsed + "s elapsed)");
                }

                // Sleep to maintain rate
                Thread.sleep(intervalMs);
            }

            System.out.println("\n✅ Streaming complete!");
            System.out.println("   Total events streamed: " + totalEvents);
            System.out.println("   Total events in DB: " + eventsCollection.countDocuments());

        } catch (Exception e) {
            System.err.println("❌ MongoDB Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Updates video stats incrementally for a single event
     */
    private void updateVideoStatsIncremental(MongoCollection<Document> statsCollection, ViewEvent event) {
        Document filter = new Document("videoId", event.getVideoId());
        Document update = new Document("$inc", new Document("totalViews", 1))
                .append("$set", new Document("lastUpdated", new Date()));

        statsCollection.updateOne(filter, update, new com.mongodb.client.model.UpdateOptions().upsert(true));
    }

    /**
     * Aggregates video statistics from events collection
     */
    private void aggregateVideoStats(MongoDatabase database) {
        System.out.println("\n📊 Computing video statistics...");

        MongoCollection<Document> eventsCollection = database.getCollection("events");
        MongoCollection<Document> statsCollection = database.getCollection("video_stats");

        // Clear existing stats
        statsCollection.drop();

        // Aggregation pipeline
        List<Document> pipeline = Arrays.asList(
                new Document("$group", new Document("_id", "$videoId")
                        .append("totalViews", new Document("$sum", 1))
                        .append("avgDuration", new Document("$avg", "$duration"))
                        .append("uniqueViewers", new Document("$addToSet", "$userId"))),
                new Document("$project", new Document("videoId", "$_id")
                        .append("totalViews", 1)
                        .append("avgDuration", new Document("$round", Arrays.asList("$avgDuration", 2)))
                        .append("uniqueViewers", new Document("$size", "$uniqueViewers"))
                        .append("lastUpdated", new Date())),
                new Document("$out", "video_stats"));

        eventsCollection.aggregate(pipeline).toCollection();

        System.out.println("✅ Video statistics computed: " + statsCollection.countDocuments() + " videos");
    }

    /**
     * Generates and loads data using TMDB API for real movie data
     */
    public void generateWithTmdb(int eventCount, int moviesPerCategory) {
        System.out.println("🔗 Connecting to MongoDB: " + MONGO_URI);
        System.out.println("📊 Database: " + MONGO_DATABASE);
        System.out.println("🎬 TMDB API Key: " + TMDB_API_KEY.substring(0, 8) + "...");

        // Test TMDB connection
        TmdbClient tmdbClient = new TmdbClient(TMDB_API_KEY);
        if (!tmdbClient.testConnection()) {
            System.err.println("❌ Failed to connect to TMDB API. Falling back to mock data.");
            generateAndLoadToMongo(eventCount);
            return;
        }
        System.out.println("✅ TMDB API connection successful!\n");

        try (MongoClient mongoClient = MongoClients.create(MONGO_URI)) {
            MongoDatabase database = mongoClient.getDatabase(MONGO_DATABASE);

            // Fetch movies from TMDB by category
            Map<String, List<TmdbMovie>> moviesByCategory = tmdbClient.fetchMoviesByAllCategories(moviesPerCategory);

            // Convert TMDB movies to Video objects and load to MongoDB
            MongoCollection<Document> videosCollection = database.getCollection("videos");
            System.out.println("\n📹 Loading movies to MongoDB...");

            // Clear existing videos
            videosCollection.drop();

            List<Document> videoDocs = new ArrayList<>();
            int videoId = 1;
            int totalMovies = 0;

            for (Map.Entry<String, List<TmdbMovie>> entry : moviesByCategory.entrySet()) {
                String category = entry.getKey();
                List<TmdbMovie> movies = entry.getValue();

                for (TmdbMovie tmdbMovie : movies) {
                    Video video = tmdbMovieToVideo(tmdbMovie, videoId, category);
                    videoDocs.add(videoToDocumentWithTmdb(video));
                    videoId++;
                    totalMovies++;
                }
            }

            if (!videoDocs.isEmpty()) {
                videosCollection.insertMany(videoDocs);
            }
            System.out.println("✅ " + totalMovies + " real movies loaded from TMDB");

            // Update NUM_VIDEOS for event generation
            int numVideos = totalMovies;

            // Generate events for the TMDB movies
            MongoCollection<Document> eventsCollection = database.getCollection("events");
            System.out.println("\n🎬 Generating " + eventCount + " events for TMDB movies...");

            eventsCollection.drop();

            int batchSize = 5000;
            List<Document> eventBatch = new ArrayList<>();

            for (int i = 0; i < eventCount; i++) {
                ViewEvent event = generateEventForVideoCount(numVideos);
                eventBatch.add(eventToDocument(event));

                if (eventBatch.size() >= batchSize) {
                    eventsCollection.insertMany(eventBatch);
                    System.out.println("  ✓ " + (i + 1) + " events loaded");
                    eventBatch.clear();
                }
            }

            if (!eventBatch.isEmpty()) {
                eventsCollection.insertMany(eventBatch);
            }

            System.out.println("✅ " + eventCount + " events loaded into MongoDB");

            // Compute video stats
            aggregateVideoStats(database);

            System.out.println("\n🎉 TMDB Data generation complete!");
            System.out.println("   Events: " + eventsCollection.countDocuments());
            System.out.println("   Videos: " + videosCollection.countDocuments());
            System.out.println("   Stats:  " + database.getCollection("video_stats").countDocuments());

            // Print sample movies
            System.out.println("\n📽️ Sample movies loaded:");
            videosCollection.find().limit(5).forEach(doc -> System.out.println("   - " + doc.getString("title") + " ("
                    + doc.getString("category") + ") ⭐ " + doc.getDouble("rating")));

        } catch (Exception e) {
            System.err.println("❌ Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Converts a TMDB movie to a Video entity
     */
    private Video tmdbMovieToVideo(TmdbMovie tmdbMovie, int videoId, String category) {
        Video video = new Video();
        video.setVideoId("video_" + videoId);
        video.setTitle(tmdbMovie.getTitle());
        video.setCategory(category);
        video.setRating(tmdbMovie.getVoteAverage());
        video.setPosterUrl(tmdbMovie.getFullPosterUrl());
        video.setOverview(tmdbMovie.getOverview());

        // Parse release date to Instant
        try {
            if (tmdbMovie.getReleaseDate() != null && !tmdbMovie.getReleaseDate().isEmpty()) {
                LocalDate releaseDate = LocalDate.parse(tmdbMovie.getReleaseDate(), DateTimeFormatter.ISO_LOCAL_DATE);
                video.setUploadDate(releaseDate.atStartOfDay().toInstant(ZoneOffset.UTC));
            } else {
                video.setUploadDate(Instant.now().minus(ThreadLocalRandom.current().nextLong(0, 365), ChronoUnit.DAYS));
            }
        } catch (Exception e) {
            video.setUploadDate(Instant.now().minus(ThreadLocalRandom.current().nextLong(0, 365), ChronoUnit.DAYS));
        }

        // Estimate duration (typical movie is 90-150 minutes)
        video.setDuration(ThreadLocalRandom.current().nextInt(5400, 10800));

        // Initial views and likes based on popularity
        video.setViews((int) (tmdbMovie.getPopularity() * 1000));
        video.setLikes((int) (tmdbMovie.getPopularity() * 100));

        return video;
    }

    /**
     * Converts a Video to MongoDB Document (with TMDB fields)
     */
    private Document videoToDocumentWithTmdb(Video video) {
        Document doc = new Document();
        doc.append("videoId", video.getVideoId());
        doc.append("title", video.getTitle());
        doc.append("category", video.getCategory());
        doc.append("duration", video.getDuration());
        doc.append("uploadDate", Date.from(video.getUploadDate()));
        doc.append("views", video.getViews());
        doc.append("likes", video.getLikes());
        doc.append("rating", video.getRating());
        doc.append("posterUrl", video.getPosterUrl());
        doc.append("overview", video.getOverview());
        return doc;
    }

    /**
     * Generates an event for a specific video count (for TMDB mode)
     */
    private ViewEvent generateEventForVideoCount(int videoCount) {
        ViewEvent event = new ViewEvent();

        event.setEventId("evt_" + UUID.randomUUID().toString().substring(0, 8));
        event.setUserId("user_" + ThreadLocalRandom.current().nextInt(1, NUM_USERS + 1));
        event.setVideoId("video_" + ThreadLocalRandom.current().nextInt(1, videoCount + 1));

        // Timestamp within last 24 hours
        Instant now = Instant.now();
        long randomMinutes = ThreadLocalRandom.current().nextLong(0, 24 * 60);
        event.setTimestamp(now.minus(randomMinutes, ChronoUnit.MINUTES));

        event.setAction(ACTIONS[random.nextInt(ACTIONS.length)]);

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
     * Main method for standalone execution
     */
    public static void main(String[] args) {
        DataGenerator generator = new DataGenerator();

        String mode = args.length > 0 ? args[0].toLowerCase() : "batch";

        System.out.println("╔══════════════════════════════════════════════════════════╗");
        System.out.println("║     STREAMING ANALYTICS DATA GENERATOR                   ║");
        System.out.println("╚══════════════════════════════════════════════════════════╝\n");

        try {
            switch (mode) {
                case "mongo":
                    // MongoDB bulk load mode
                    int eventCount = args.length > 1 ? Integer.parseInt(args[1]) : 100000;
                    System.out.println("📦 MODE: MongoDB Bulk Load\n");
                    generator.generateAndLoadToMongo(eventCount);
                    break;

                case "continuous":
                    // Real-time streaming mode
                    int rate = args.length > 1 ? Integer.parseInt(args[1]) : 10;
                    int duration = args.length > 2 ? Integer.parseInt(args[2]) : 60;
                    System.out.println("⚡ MODE: Continuous Real-time Streaming\n");
                    generator.streamToMongo(rate, duration);
                    break;

                case "tmdb":
                    // TMDB API mode - fetch real movies
                    int tmdbEventCount = args.length > 1 ? Integer.parseInt(args[1]) : 100000;
                    int moviesPerCategory = args.length > 2 ? Integer.parseInt(args[2]) : 10;
                    System.out.println("🎬 MODE: TMDB API Integration\n");
                    generator.generateWithTmdb(tmdbEventCount, moviesPerCategory);
                    break;

                case "batch":
                default:
                    // JSON file generation mode (original behavior)
                    System.out.println("📁 MODE: Batch JSON File Generation\n");
                    generator.generateEventsToFile(100000, "events_100k.json");
                    System.out.println();
                    generator.generateVideosCatalogToFile("videos_catalog.json");
                    System.out.println("\n📋 Files created:");
                    System.out.println("   - events_100k.json (100,000 viewing events)");
                    System.out.println("   - videos_catalog.json (10,000 videos)");
                    break;
            }

            System.out.println("\n╔══════════════════════════════════════════════════════════╗");
            System.out.println("║                    GENERATION COMPLETE                   ║");
            System.out.println("╚══════════════════════════════════════════════════════════╝");

        } catch (Exception e) {
            System.err.println("❌ Error during generation: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
