package com.streaming.analytics.repository;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.ReturnDocument;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.model.Updates;
import com.streaming.analytics.model.UserProfile;
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
import java.util.Map;

import static com.mongodb.MongoClientSettings.getDefaultCodecRegistry;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

/**
 * CDI Repository for UserProfile entities
 * Provides data access operations for the 'user_profiles' collection
 */
@ApplicationScoped
public class UserProfileRepository {

    private static final Logger logger = LoggerFactory.getLogger(UserProfileRepository.class);
    private static final String COLLECTION_NAME = "user_profiles";

    @Inject
    private MongoDatabase database;

    private MongoCollection<UserProfile> collection;

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
                .getCollection(COLLECTION_NAME, UserProfile.class);

        logger.info("UserProfileRepository initialized with collection: {}", COLLECTION_NAME);
    }

    /**
     * Gets a user profile by userId
     */
    public UserProfile findByUserId(String userId) {
        return collection.find(Filters.eq("userId", userId)).first();
    }

    /**
     * Gets or creates a user profile
     */
    public UserProfile getOrCreate(String userId) {
        UserProfile profile = findByUserId(userId);
        if (profile == null) {
            profile = new UserProfile(userId);
            collection.insertOne(profile);
            logger.debug("Created new user profile for: {}", userId);
        }
        return profile;
    }

    /**
     * Updates user profile with a new watch event
     */
    public void updateWithWatch(String userId, String videoId, String category, int watchDuration) {
        FindOneAndUpdateOptions options = new FindOneAndUpdateOptions()
                .upsert(true)
                .returnDocument(ReturnDocument.AFTER);

        collection.findOneAndUpdate(
                Filters.eq("userId", userId),
                Updates.combine(
                        Updates.push("watchHistory", videoId),
                        Updates.inc("preferences." + category, 1),
                        Updates.inc("totalWatchTime", watchDuration),
                        Updates.set("lastActive", Instant.now())),
                options);
    }

    /**
     * Updates recommended videos for a user
     */
    public void updateRecommendations(String userId, List<String> recommendedVideoIds) {
        collection.updateOne(
                Filters.eq("userId", userId),
                Updates.set("recommendedVideos", recommendedVideoIds));
    }

    /**
     * Gets the most active users
     */
    public List<UserProfile> getMostActiveUsers(int limit) {
        return collection.find()
                .sort(Sorts.descending("totalWatchTime"))
                .limit(limit)
                .into(new ArrayList<>());
    }

    /**
     * Saves or updates a user profile
     */
    public void save(UserProfile profile) {
        FindOneAndUpdateOptions options = new FindOneAndUpdateOptions()
                .upsert(true);

        collection.findOneAndUpdate(
                Filters.eq("userId", profile.getUserId()),
                Updates.combine(
                        Updates.set("watchHistory", profile.getWatchHistory()),
                        Updates.set("preferences", profile.getPreferences()),
                        Updates.set("recommendedVideos", profile.getRecommendedVideos()),
                        Updates.set("lastActive", profile.getLastActive()),
                        Updates.set("totalWatchTime", profile.getTotalWatchTime())),
                options);
    }
}
