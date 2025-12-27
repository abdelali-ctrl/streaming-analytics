package com.streaming.analytics.config;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * CDI Producer for MongoDB client
 * Provides injectable MongoClient and MongoDatabase instances
 */
@ApplicationScoped
public class MongoClientProducer {

    private static final Logger logger = LoggerFactory.getLogger(MongoClientProducer.class);

    private MongoClient mongoClient;

    // MongoDB connection settings - can be overridden by environment variables
    private static final String MONGODB_HOST = System.getenv("MONGODB_HOST") != null
            ? System.getenv("MONGODB_HOST")
            : "localhost";
    private static final String MONGODB_PORT = System.getenv("MONGODB_PORT") != null
            ? System.getenv("MONGODB_PORT")
            : "27017";
    private static final String MONGODB_DATABASE = System.getenv("MONGODB_DATABASE") != null
            ? System.getenv("MONGODB_DATABASE")
            : "streaming_analytics";
    private static final String MONGODB_USERNAME = System.getenv("MONGODB_USERNAME") != null
            ? System.getenv("MONGODB_USERNAME")
            : "admin";
    private static final String MONGODB_PASSWORD = System.getenv("MONGODB_PASSWORD") != null
            ? System.getenv("MONGODB_PASSWORD")
            : "admin123";

    @PostConstruct
    public void init() {
        String connectionString = String.format(
                "mongodb://%s:%s@%s:%s/?authSource=admin",
                MONGODB_USERNAME, MONGODB_PASSWORD, MONGODB_HOST, MONGODB_PORT);

        logger.info("Connecting to MongoDB at {}:{}", MONGODB_HOST, MONGODB_PORT);
        mongoClient = MongoClients.create(connectionString);
        logger.info("MongoDB connection established");
    }

    @PreDestroy
    public void cleanup() {
        if (mongoClient != null) {
            logger.info("Closing MongoDB connection");
            mongoClient.close();
        }
    }

    @Produces
    @ApplicationScoped
    public MongoClient getMongoClient() {
        return mongoClient;
    }

    @Produces
    @ApplicationScoped
    public MongoDatabase getDatabase() {
        return mongoClient.getDatabase(MONGODB_DATABASE);
    }
}
