package com.streaming.analytics.repository;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.model.Sorts;
import com.streaming.analytics.model.ViewEvent;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static com.mongodb.MongoClientSettings.getDefaultCodecRegistry;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

/**
 * CDI Repository for ViewEvent entities
 * Provides data access operations for the 'events' collection
 */
@ApplicationScoped
public class EventRepository {

    private static final Logger logger = LoggerFactory.getLogger(EventRepository.class);
    private static final String COLLECTION_NAME = "events";

    @Inject
    private MongoDatabase database;

    private MongoCollection<ViewEvent> collection;

    @PostConstruct
    public void init() {
        // Configure POJO codec for automatic mapping
        CodecProvider pojoCodecProvider = PojoCodecProvider.builder()
                .automatic(true)
                .build();
        CodecRegistry pojoCodecRegistry = fromRegistries(
                getDefaultCodecRegistry(),
                fromProviders(pojoCodecProvider));

        this.collection = database
                .withCodecRegistry(pojoCodecRegistry)
                .getCollection(COLLECTION_NAME, ViewEvent.class);

        // Ensure indexes exist
        ensureIndexes();
        logger.info("EventRepository initialized with collection: {}", COLLECTION_NAME);
    }

    private void ensureIndexes() {
        collection.createIndex(Indexes.ascending("userId"));
        collection.createIndex(Indexes.ascending("videoId"));
        collection.createIndex(Indexes.descending("timestamp"));
        collection.createIndex(Indexes.compoundIndex(
                Indexes.ascending("videoId"),
                Indexes.descending("timestamp")));
    }

    /**
     * Saves a single event to the database
     */
    public void save(ViewEvent event) {
        collection.insertOne(event);
        logger.debug("Saved event: {}", event.getEventId());
    }

    /**
     * Saves a batch of events to the database
     */
    public void saveBatch(List<ViewEvent> events) {
        if (events != null && !events.isEmpty()) {
            collection.insertMany(events);
            logger.info("Saved batch of {} events", events.size());
        }
    }

    /**
     * Finds all events for a specific user
     */
    public List<ViewEvent> findByUserId(String userId) {
        return collection.find(Filters.eq("userId", userId))
                .sort(Sorts.descending("timestamp"))
                .into(new ArrayList<>());
    }

    /**
     * Finds all events for a specific user with limit
     */
    public List<ViewEvent> findByUserId(String userId, int limit) {
        return collection.find(Filters.eq("userId", userId))
                .sort(Sorts.descending("timestamp"))
                .limit(limit)
                .into(new ArrayList<>());
    }

    /**
     * Finds all events for a specific video
     */
    public List<ViewEvent> findByVideoId(String videoId) {
        return collection.find(Filters.eq("videoId", videoId))
                .sort(Sorts.descending("timestamp"))
                .into(new ArrayList<>());
    }

    /**
     * Finds events within a time range
     */
    public List<ViewEvent> findByTimeRange(Instant start, Instant end) {
        return collection.find(
                Filters.and(
                        Filters.gte("timestamp", start),
                        Filters.lte("timestamp", end)))
                .sort(Sorts.descending("timestamp"))
                .into(new ArrayList<>());
    }

    /**
     * Counts total events in the collection
     */
    public long count() {
        return collection.countDocuments();
    }

    /**
     * Counts events for a specific video
     */
    public long countByVideoId(String videoId) {
        return collection.countDocuments(Filters.eq("videoId", videoId));
    }
}
